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
import org.apache.commons.jelly.MissingAttributeException;
import org.apache.commons.jelly.XMLOutput;
import org.jvnet.maven.jellydoc.annotation.Required;
import org.jvnet.maven.jellydoc.annotation.NoContent;

/**
 * Set the HTTP Content-Type header of the page.
 *
 * @author Kohsuke Kawaguchi
 */
@NoContent
public class ContentTypeTag extends AbstractStaplerTag {
    private String contentType;

    /**
     * The content-type value, such as "text/html".
     */
    @Required
    public void setValue(String contentType) {
        this.contentType = contentType;
    }

    public void doTag(XMLOutput output) throws MissingAttributeException, JellyTagException {
        getResponse().setContentType(contentType);
        if (output instanceof HTMLWriterOutput)
            ((HTMLWriterOutput)output).useHTML(contentType.startsWith("text/html"));
    }
}
