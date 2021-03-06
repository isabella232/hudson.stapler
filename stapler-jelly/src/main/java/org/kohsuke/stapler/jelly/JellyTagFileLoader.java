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

import org.kohsuke.stapler.Facet;
import org.apache.commons.jelly.Script;
import org.apache.commons.jelly.JellyException;

import java.util.List;

/**
 * Extension point that lets Jelly scripts written in other languages.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class JellyTagFileLoader {
    /**
     * Loads a tag file for the given tag library.
     *
     * @return null
     *      if this loader didn't find the script.
     */
    public abstract Script load(CustomTagLibrary taglib, String name, ClassLoader classLoader) throws JellyException;

    /**
     * Discovers all the facets in the classloader.
     */
    public static List<JellyTagFileLoader> discover(ClassLoader cl) {
        return Facet.discoverExtensions(JellyTagFileLoader.class,cl);
    }
}
