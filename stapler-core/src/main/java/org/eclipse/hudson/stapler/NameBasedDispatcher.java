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

package org.eclipse.hudson.stapler;

import javax.servlet.ServletException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * @author Kohsuke Kawaguchi
 */
abstract class NameBasedDispatcher extends Dispatcher {
    protected final String name;
    private final int argCount;

    protected NameBasedDispatcher(String name, int argCount) {
        this.name = name;
        this.argCount = argCount;
    }

    protected NameBasedDispatcher(String name) {
        this(name,0);
    }

    public final boolean dispatch(RequestImpl req, ResponseImpl rsp, Object node)
        throws IOException, ServletException, IllegalAccessException, InvocationTargetException {
        if(!req.tokens.hasMore() || !req.tokens.peek().equals(name))
            return false;
        if(req.tokens.countRemainingTokens()<=argCount)
            return false;
        req.tokens.next();
        return doDispatch(req,rsp,node);
    }

    protected abstract boolean doDispatch(RequestImpl req, ResponseImpl rsp, Object node)
        throws IOException, ServletException, IllegalAccessException, InvocationTargetException;
}
