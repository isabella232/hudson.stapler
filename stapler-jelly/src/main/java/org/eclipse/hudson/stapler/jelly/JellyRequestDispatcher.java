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
import org.apache.commons.jelly.Script;
import org.eclipse.hudson.stapler.MetaClass;
import org.eclipse.hudson.stapler.StaplerRequest;
import org.eclipse.hudson.stapler.StaplerResponse;
import org.eclipse.hudson.stapler.WebApp;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

/**
 * @author Kohsuke Kawaguchi
 */
public final class JellyRequestDispatcher implements RequestDispatcher {
    private final Object it;
    private final Script script;
    private final JellyFacet facet;

    public JellyRequestDispatcher(Object it, Script script) {
        this.it = it;
        this.script = script;
        facet = WebApp.getCurrent().getFacet(JellyFacet.class);
    }

    public void forward(ServletRequest servletRequest, ServletResponse servletResponse) throws ServletException, IOException {
        try {
            facet.scriptInvoker.invokeScript(
                (StaplerRequest)servletRequest,
                (StaplerResponse)servletResponse,
                script, it);
        } catch (JellyTagException e) {
            throw new ServletException(e);
        }
    }

    public void include(ServletRequest servletRequest, ServletResponse servletResponse) throws ServletException, IOException {
        forward(servletRequest,servletResponse);
    }
}
