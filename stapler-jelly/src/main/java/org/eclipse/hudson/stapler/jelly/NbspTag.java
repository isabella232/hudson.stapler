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
import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;
import org.xml.sax.SAXException;
import org.jvnet.maven.jellydoc.annotation.NoContent;

/**
 * Writes out '&amp;nbsp;'.
 *
 * @author Kohsuke Kawaguchi
 */
@NoContent
public class NbspTag extends TagSupport {
    public void doTag(XMLOutput output) throws MissingAttributeException, JellyTagException {
        try {
            output.write("\u00A0"); // nbsp
        } catch (SAXException e) {
            throw new JellyTagException(e);
        }
    }
}
