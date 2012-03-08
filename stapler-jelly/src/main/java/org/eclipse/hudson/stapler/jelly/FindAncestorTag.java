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

package org.eclipse.hudson.stapler.jelly;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.NamespaceAwareTag;
import org.apache.commons.jelly.Tag;
import org.apache.commons.jelly.XMLOutput;
import org.jvnet.maven.jellydoc.annotation.NoContent;

import java.util.Map;

/**
 * Finds the nearest tag (in the call stack) that has the given tag name,
 * and sets that as a variable.
 *
 * @author Kohsuke Kawaguchi
 */
@NoContent
public class FindAncestorTag extends AbstractStaplerTag implements NamespaceAwareTag {
    private String tag;

    private String var;

    private Map nsMap;

    /**
     * Variable name to set the discovered {@link Tag} object.
     */
    public void setVar(String var) {
        this.var = var;
    }

    /**
     * QName of the tag to look for.
     */
    public void setTag(String tag) {
        this.tag = tag;
    }

    public void doTag(XMLOutput output) throws JellyTagException {
        // I don't think anyone is using this, but if we need to resurrect this,
        // we need to tweak CustomTagLibrary class and build up the stack of elements being processed.
        throw new UnsupportedOperationException();

//        int idx = tag.indexOf(':');
//        String prefix = tag.substring(0,idx);
//        String localName = tag.substring(idx+1);
//
//        String uri = (String) nsMap.get(prefix);
//
//        Tag tag = this;
//        while((tag=findAncestorWithClass(tag,StaplerDynamicTag.class))!=null) {
//            StaplerDynamicTag t = (StaplerDynamicTag)tag;
//            if(t.getLocalName().equals(localName) && t.getNsUri().equals(uri))
//                break;
//        }
//        getContext().setVariable(var,tag);
    }

    public void setNamespaceContext(Map prefixToUriMap) {
        this.nsMap = prefixToUriMap;
    }
}
