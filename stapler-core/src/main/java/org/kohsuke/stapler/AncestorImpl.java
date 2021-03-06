/*******************************************************************************
 *
 * Copyright (c) 2004-2010 Oracle Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *
 *    Kohsuke Kawaguchi
 *     
 *******************************************************************************/ 

package org.kohsuke.stapler;

import java.util.List;

/**
 * @author Kohsuke Kawaguchi
 */
public class AncestorImpl implements Ancestor {
    private final List<AncestorImpl> owner;
    private final int listIndex;

    private Object object;
    private String[] tokens;
    private int index;
    private String contextPath;

    public AncestorImpl(List<AncestorImpl> owner) {
        this.owner = owner;
        listIndex = owner.size();
        owner.add(this);
    }

    public void set(Object object, RequestImpl req ) {
        this.object = object;
        this.tokens = req.tokens.rawTokens;
        this.index = req.tokens.idx;
        this.contextPath = req.getContextPath();
    }

    public Object getObject() {
        return object;
    }

    public String getUrl() {
        StringBuilder buf = new StringBuilder(contextPath);
        for( int i=0; i<index; i++ ) {
            buf.append('/');
            buf.append(tokens[i]);
        }
        
        return buf.toString();
    }

    public String getRestOfUrl() {
        StringBuilder buf = new StringBuilder(contextPath);
        for( int i=index; i<tokens.length; i++ ) {
            if (buf.length()>0) buf.append('/');
            buf.append(tokens[i]);
        }

        return buf.toString();
    }

    public String getFullUrl() {
        StringBuilder buf = new StringBuilder();
        StaplerRequest req = Stapler.getCurrentRequest();
        buf.append(req.getScheme());
        buf.append("://");
        buf.append(req.getServerName());
        if(req.getServerPort()!=80)
            buf.append(':').append(req.getServerPort());
        buf.append(getUrl());

        return buf.toString();
    }

    public String getRelativePath() {
        StringBuilder buf = new StringBuilder();
        for( int i=index; i<tokens.length; i++ ) {
            if(buf.length()>0)  buf.append('/');
            buf.append("..");
        }
        if(buf.length()==0) buf.append('.');
        return buf.toString();
    }

    public String getNextToken(int n) {
        return tokens[index+n];
    }

    public Ancestor getPrev() {
        if(listIndex==0)
            return null;
        else
            return owner.get(listIndex-1);
    }

    public Ancestor getNext() {
        if(listIndex==owner.size()-1)
            return null;
        else
            return owner.get(listIndex+1);
    }

    @Override
    public String toString() {
        return object.toString();
    }
}
