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

package org.kohsuke.stapler.jelly;

import org.apache.commons.jelly.JellyException;
import org.apache.commons.jelly.Script;
import org.kohsuke.stapler.AbstractTearOff;
import static org.kohsuke.stapler.Dispatcher.trace;
import static org.kohsuke.stapler.Dispatcher.traceable;
import org.kohsuke.stapler.MetaClass;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.WebApp;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Logger;

/**
 * @author Kohsuke Kawaguchi
 */
public class JellyClassTearOff extends AbstractTearOff<JellyClassLoaderTearOff,Script,JellyException> {
    private JellyFacet facet;

    public JellyClassTearOff(MetaClass owner) {
        super(owner,JellyClassLoaderTearOff.class);
        facet = WebApp.getCurrent().getFacet(JellyFacet.class);
    }

    protected Script parseScript(URL res) throws JellyException {
        return new JellyViewScript(owner.clazz, res, classLoader.createContext().compileScript(res));
    }

    /**
     * Serves <tt>index.jelly</tt> if it's available, and returns true.
     */
    public boolean serveIndexJelly(StaplerRequest req, StaplerResponse rsp, Object node) throws ServletException, IOException {
        try {
            Script script = findScript("index.jelly");
            if(script!=null) {
                if(traceable()) {
                    String src = "index.jelly";
                    if (script instanceof JellyViewScript) {
                        JellyViewScript jvs = (JellyViewScript) script;
                        src = jvs.getName();
                    }
                    trace(req,rsp,"-> %s on <%s>",src,node);
                }
                facet.scriptInvoker.invokeScript(req, rsp, script, node);
                return true;
            }
            return false;
        } catch (JellyException e) {
            throw new ServletException(e);
        }
    }

    /**
     * Creates a {@link RequestDispatcher} that forwards to the jelly view, if available.
     */
    public RequestDispatcher createDispatcher(Object it, String viewName) throws IOException {
        try {
            // backward compatible behavior that expects full file name including ".jelly"
            Script script = findScript(viewName);
            if(script!=null)
                return new JellyRequestDispatcher(it,script);
            
            // this is what the look up was really supposed to be.
            script = findScript(viewName+".jelly");
            if(script!=null)
                return new JellyRequestDispatcher(it,script);
            return null;
        } catch (JellyException e) {
            IOException io = new IOException(e.getMessage());
            io.initCause(e);
            throw io;
        }
    }

    private static final Logger LOGGER = Logger.getLogger(JellyClassTearOff.class.getName());
}
