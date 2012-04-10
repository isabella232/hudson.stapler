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

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.XMLOutput;
import org.jvnet.maven.jellydoc.annotation.NoContent;
import org.jvnet.maven.jellydoc.annotation.Required;
import org.kohsuke.stapler.WebApp;
import org.kohsuke.stapler.bind.Bound;
import org.xml.sax.SAXException;

/**
 * Binds a server-side object to client side so that JavaScript can call into server.
 * This tag evaluates to a &lt;script> tag.
 * 
 * @author Kohsuke Kawaguchi
 */
@NoContent
public class BindTag extends AbstractStaplerTag {
    private String varName;
    private Object javaObject;

    /**
     * JavaScript variable name to set the proxy to.
     * <p>
     * This name can be arbitrary left hand side expression,
     * such as "a[0]" or "a.b.c".
     *
     * If this value is unspecified, the tag generates a JavaScript expression to create a proxy.
     */
    public void setVar(String varName) {
        this.varName = varName;
    }

    @Required
    public void setValue(Object o) {
        this.javaObject = o;
    }

    public void doTag(XMLOutput out) throws JellyTagException {
        // make sure we get the supporting script in place
        AdjunctTag a = new AdjunctTag();
        a.setContext(getContext());
        a.setIncludes("org.kohsuke.stapler.bind");
        a.doTag(out);

        try {
            String expr;
            if (javaObject==null) {
                expr = "null";
            } else {
                Bound h = WebApp.getCurrent().boundObjectTable.bind(javaObject);
                expr = h.getProxyScript();
            }

            if (varName==null) {
                // this mode (of writing just the expression) needs to be used with caution because
                // the adjunct tag above might produce <script> tag.
                out.write(expr);
            } else {
                out.startElement("script");
                out.write(varName + "=" + expr + ";");
                out.endElement("script");
            }
        } catch (SAXException e) {
            throw new JellyTagException(e);
        }
    }
}
