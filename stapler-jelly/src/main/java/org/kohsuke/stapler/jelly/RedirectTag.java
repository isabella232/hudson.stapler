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
import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerResponse;
import org.jvnet.maven.jellydoc.annotation.NoContent;
import org.jvnet.maven.jellydoc.annotation.Required;

import java.io.IOException;

/**
 * Sends HTTP redirect.
 * 
 * @author Kohsuke Kawaguchi
 */
@NoContent
public class RedirectTag extends TagSupport {
    private String url;

    /**
     * Sets the target URL to redirect to. This just gets passed
     * to {@link StaplerResponse#sendRedirect2(String)}.
     */
    @Required
    public void setUrl(String url) {
        this.url = url;
    }

    public void doTag(XMLOutput output) throws JellyTagException {
        try {
            Stapler.getCurrentResponse().sendRedirect2(url);
        } catch (IOException e) {
            throw new JellyTagException("Failed to redirect to "+url,e);
        }
    }
}

