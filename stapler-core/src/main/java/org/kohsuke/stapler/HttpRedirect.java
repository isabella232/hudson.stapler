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

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * {@link HttpResponse} that dose HTTP 302 redirect.
 * Extends from {@link RuntimeException} so that you can throw it.
 *
 * @author Kohsuke Kawaguchi
 */
public final class HttpRedirect extends RuntimeException implements HttpResponse {
    private final String url;

    public HttpRedirect(String url) {
        this.url = url;
    }

    public void generateResponse(StaplerRequest req, StaplerResponse rsp, Object node) throws IOException, ServletException {
        rsp.sendRedirect2(url);
    }

    /**
     * @param relative
     *      The path relative to the context path. The context path + this value
     *      is sent to the user.
     * @deprecated
     *      Use {@link HttpResponses#redirectViaContextPath(String)}.
     */
    public static HttpResponse fromContextPath(final String relative) {
        return HttpResponses.redirectViaContextPath(relative);
    }

    /**
     * Redirect to "."
     */
    public static HttpRedirect DOT = new HttpRedirect(".");

    /**
     * Redirect to the context root
     */
    public static HttpResponse CONTEXT_ROOT = fromContextPath("");
}
