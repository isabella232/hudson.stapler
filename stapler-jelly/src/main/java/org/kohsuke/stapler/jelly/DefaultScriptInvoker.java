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

import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.apache.commons.jelly.Script;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.jelly.XMLOutputFactory;
import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.impl.TagScript;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.io.OutputStream;
import java.io.BufferedOutputStream;
import java.io.FilterOutputStream;
import java.io.Writer;
import java.util.Enumeration;

/**
 * Standard implementation of {@link ScriptInvoker}.
 * 
 * @author Kohsuke Kawaguchi
 */
public class DefaultScriptInvoker implements ScriptInvoker, XMLOutputFactory {
    public void invokeScript(StaplerRequest req, StaplerResponse rsp, Script script, Object it) throws IOException, JellyTagException {
        XMLOutput xmlOutput = createXMLOutput(req, rsp, script, it);

        invokeScript(req,rsp,script,it,xmlOutput);
        
        xmlOutput.flush();
        xmlOutput.close();
    }

    public void invokeScript(StaplerRequest req, StaplerResponse rsp, Script script, Object it, XMLOutput out) throws IOException, JellyTagException {
        JellyContext context = createContext(req,rsp,script,it);
        exportVariables(req, rsp, script, it, context);

        script.run(context,out);
    }

    protected XMLOutput createXMLOutput(StaplerRequest req, StaplerResponse rsp, Script script, Object it) throws IOException {
        // TODO: make XMLOutput auto-close OutputStream to avoid leak
        HTMLWriterOutput hwo = HTMLWriterOutput.create(createOutputStream(req, rsp, script, it));
        String ct = rsp.getContentType();
        if (ct != null && !ct.startsWith("text/html"))
            hwo.useHTML(false);
        return hwo;
    }

    protected OutputStream createOutputStream(StaplerRequest req, StaplerResponse rsp, Script script, Object it) throws IOException {
        // do we want to do compression?
        OutputStream output=null;
        if (script instanceof TagScript) {
            TagScript ts = (TagScript) script;
            if(ts.getLocalName().equals("compress"))
                output = rsp.getCompressedOutputStream(req);
        }
        if(output==null)    // nope
            output = new BufferedOutputStream(rsp.getOutputStream());

        output = new FilterOutputStream(output) {
            public void flush() {
                // flushing ServletOutputStream causes Tomcat to
                // send out headers, making it impossible to set contentType from the script.
                // so don't let Jelly flush.
            }
            public void write(byte b[], int off, int len) throws IOException {
                out.write(b, off, len);
            }
        };
        return output;
    }

    protected void exportVariables(StaplerRequest req, StaplerResponse rsp, Script script, Object it, JellyContext context) {
        Enumeration en = req.getAttributeNames();
        // expose request attributes, just like JSP
        while (en.hasMoreElements()) {
            String name = (String) en.nextElement();
            context.setVariable(name,req.getAttribute(name));
        }

        context.setVariable("request",req);
        context.setVariable("response",rsp);
        context.setVariable("it",it);
        ServletContext servletContext = req.getServletContext();
        context.setVariable("servletContext",servletContext);
        context.setVariable("app",servletContext.getAttribute("app"));
        // property bag to store request scope variables
        context.setVariable("requestScope",context.getVariables());
        // this variable is needed to make "jelly:fmt" taglib work correctly
        context.setVariable("org.apache.commons.jelly.tags.fmt.locale",req.getLocale());
    }

    protected JellyContext createContext(final StaplerRequest req, StaplerResponse rsp, Script script, Object it) {
        CustomJellyContext context = new CustomJellyContext();
        // let Jelly see the whole classes
        context.setClassLoader(req.getStapler().getWebApp().getClassLoader());
        // so TagScript.getBodyText() will use HTMLWriterOutput
        context.setVariable(XMLOutputFactory.class.getName(), this);
        return context;
    }

    public XMLOutput createXMLOutput(Writer writer, boolean escapeText) {
        StaplerResponse rsp = Stapler.getCurrentResponse();
        String ct = rsp!=null ? rsp.getContentType() : "?";
        if (ct != null && !ct.startsWith("text/html"))
            return XMLOutput.createXMLOutput(writer, escapeText);
        return HTMLWriterOutput.create(writer, escapeText);
    }
}
