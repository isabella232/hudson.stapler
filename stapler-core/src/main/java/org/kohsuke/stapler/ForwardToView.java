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
import javax.servlet.RequestDispatcher;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * {@link HttpResponse} that forwards to a {@link RequestDispatcher}, such as a view.
 * @author Kohsuke Kawaguchi
 */
public class ForwardToView implements HttpResponse {
    private final DispatcherFactory factory;
    
    private boolean optional;
    private final Map<String,Object> attributes = new HashMap<String, Object>();
    private interface DispatcherFactory {
        RequestDispatcher get(StaplerRequest req) throws IOException;
    }

    public ForwardToView(final RequestDispatcher dispatcher) {
        this.factory = new DispatcherFactory() {
            public RequestDispatcher get(StaplerRequest req) {
                return dispatcher;
            }
        };
    }

    public ForwardToView(final Object it, final String view) {
        this.factory = new DispatcherFactory() {
            public RequestDispatcher get(StaplerRequest req) throws IOException {
                return req.getView(it,view);
            }
        };
    }

    public ForwardToView(final Class c, final String view) {
        this.factory = new DispatcherFactory() {
            public RequestDispatcher get(StaplerRequest req) throws IOException {
                return req.getView(c,view);
            }
        };
    }

    /**
     * Forwards to the view with specified attributes exposed as a variable binding.
     */
    public ForwardToView with(String varName, Object value) {
        attributes.put(varName,value);
        return this;
    }

    public ForwardToView with(Map<String,?> attributes) {
        this.attributes.putAll(attributes);
        return this;
    }

    /**
     * Make this forwarding optional. Render nothing if a view doesn't exist.
     */
    public ForwardToView optional() {
        optional = true;
        return this;
    }

    public void generateResponse(StaplerRequest req, StaplerResponse rsp, Object node) throws IOException, ServletException {
        for (Entry<String, Object> e : attributes.entrySet())
            req.setAttribute(e.getKey(),e.getValue());
        RequestDispatcher rd = factory.get(req);
        if (rd==null && optional)
            return;
        rd.forward(req, rsp);
    }
}
