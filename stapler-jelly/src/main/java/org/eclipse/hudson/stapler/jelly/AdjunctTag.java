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
import org.apache.commons.jelly.XMLOutput;
import org.eclipse.hudson.stapler.framework.adjunct.AdjunctManager;
import org.eclipse.hudson.stapler.framework.adjunct.AdjunctsInPage;
import org.xml.sax.SAXException;
import org.jvnet.maven.jellydoc.annotation.NoContent;
import org.jvnet.maven.jellydoc.annotation.Required;

import java.io.IOException;

/**
 * Writes out links to adjunct CSS and JavaScript, if not done so already.
 * 
 * @author Kohsuke Kawaguchi
 */
@NoContent
public class AdjunctTag extends AbstractStaplerTag {
    private String[] includes;

    /**
     * Comma-separated adjunct names.
     */
    @Required
    public void setIncludes(String _includes) {
        includes = _includes.split(",");
        for (int i = 0; i < includes.length; i++)
              includes[i] = includes[i].trim();
    }

    public void doTag(XMLOutput out) throws JellyTagException {
        AdjunctManager m = AdjunctManager.get(getServletContext());
        if(m==null)
            throw new IllegalStateException("AdjunctManager is not installed for this application");

        try {
            AdjunctsInPage.get().generate(out,includes);
        } catch (IOException e) {
            throw new JellyTagException(e);
        } catch (SAXException e) {
            throw new JellyTagException(e);
        }
    }
}
