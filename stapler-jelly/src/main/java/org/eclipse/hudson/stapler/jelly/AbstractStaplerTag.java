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

import org.apache.commons.jelly.TagSupport;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Kohsuke Kawaguchi
 */
abstract class AbstractStaplerTag extends TagSupport {

    protected HttpServletRequest getRequest() {
        return (HttpServletRequest)getContext().getVariable("request");
    }

    protected HttpServletResponse getResponse() {
        return (HttpServletResponse)getContext().getVariable("response");
    }

    protected ServletContext getServletContext() {
        return (ServletContext)getContext().getVariable("servletContext");
    }
}
