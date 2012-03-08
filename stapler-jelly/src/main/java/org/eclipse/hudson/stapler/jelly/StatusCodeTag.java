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

import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.jelly.JellyTagException;
import org.jvnet.maven.jellydoc.annotation.NoContent;
import org.jvnet.maven.jellydoc.annotation.Required;

/**
 * Sets HTTP status code.
 *
 * <p>
 * This is generally useful for programatically creating the error page.
 *
 * @author Kohsuke Kawaguchi
 */
@NoContent
public class StatusCodeTag extends AbstractStaplerTag {
    private int code;

    /**
     * HTTP status code to send back.
     */
    @Required
    public void setValue(int code) {
        this.code = code;
    }

    public void doTag(XMLOutput output) throws JellyTagException {
        getResponse().setStatus(code);
    }
}
