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

import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.JellyException;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.Script;
import org.apache.commons.jelly.Tag;
import org.apache.commons.jelly.TagLibrary;
import org.apache.commons.jelly.impl.TagScript;
import org.eclipse.hudson.stapler.MetaClass;
import org.eclipse.hudson.stapler.WebApp;
import org.xml.sax.Attributes;

/**
 * Loads Jelly views associated with "it" as if it were taglibs.
 *
 * @author Kohsuke Kawaguchi
 */
public class ThisTagLibrary extends TagLibrary {
    private ThisTagLibrary() {}

    /**
     * IIUC, this method will never be invoked.
     */
    @Override
    public Tag createTag(final String name, Attributes attributes) throws JellyException {
        return null;
    }

    @Override
    public TagScript createTagScript(final String tagName, Attributes atts) throws JellyException {
        return new CallTagLibScript() {
            @Override
            protected Script resolveDefinition(JellyContext context) throws JellyTagException {
                Object it = context.getVariable("it");
                if (it==null)
                    throw new JellyTagException("'it' was not defined");
                try {
                    MetaClass c = WebApp.getCurrent().getMetaClass(it.getClass());
                    // prefer 'foo.jellytag' to avoid tags from showing up as views,
                    // but for backward compatibility, support the plain .jelly extention as well.
                    Script tag = c.loadTearOff(JellyClassTearOff.class).findScript(tagName+".jellytag");
                    if (tag==null)
                        tag = c.loadTearOff(JellyClassTearOff.class).findScript(tagName+".jelly");
                    return tag;
                } catch (JellyException e) {
                    throw new JellyTagException("Failed to load "+tagName+".jelly from "+it,e);
                }
            }
        };
    }

    public static final ThisTagLibrary INSTANCE = new ThisTagLibrary();
}
