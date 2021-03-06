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

package org.kohsuke.stapler;

import org.apache.commons.discovery.ResourceNameIterator;
import org.apache.commons.discovery.resource.ClassLoaders;
import org.apache.commons.discovery.resource.names.DiscoverServiceNames;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Aspect of stapler that brings in an optional language binding.
 *
 * Put {@link MetaInfServices} on subtypes so that Stapler can discover them.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class Facet {
    /**
     * Adds {@link Dispatcher}s that look at one token and binds that
     * to the views associated with the 'it' object.
     */
    public abstract void buildViewDispatchers(MetaClass owner, List<Dispatcher> dispatchers);

    /**
     * Discovers all the facets in the classloader.
     */
    public static List<Facet> discover(ClassLoader cl) {
        return discoverExtensions(Facet.class, cl);
    }

    public static <T> List<T> discoverExtensions(Class<T> type, ClassLoader cl) {
        List<T> r = new ArrayList<T>();

        ClassLoaders classLoaders = new ClassLoaders();
        classLoaders.put(cl);
        DiscoverServiceNames dc = new DiscoverServiceNames(classLoaders);
        ResourceNameIterator itr = dc.findResourceNames(type.getName());
        while(itr.hasNext()) {
            String name = itr.nextResourceName();
            Class<?> c;
            try {
                c = cl.loadClass(name);
            } catch (ClassNotFoundException e) {
                LOGGER.log(Level.WARNING, "Failed to load "+name,e);
                continue;
            }
            try {
                r.add((T)c.newInstance());
            } catch (InstantiationException e) {
                LOGGER.log(Level.WARNING, "Failed to instanticate "+c,e);
            } catch (IllegalAccessException e) {
                LOGGER.log(Level.WARNING, "Failed to instanticate "+c,e);
            }
        }
        return r;
    }

    public static final Logger LOGGER = Logger.getLogger(Facet.class.getName());

    /**
     * Creates a {@link RequestDispatcher} that handles the given view, or
     * return null if no such view was found.
     *
     * @param type
     *      If "it" is non-null, {@code it.getClass()}. Otherwise the class
     *      from which the view is searched.
     */
    public abstract RequestDispatcher createRequestDispatcher(RequestImpl request, Class type, Object it, String viewName) throws IOException;

    /**
     * Attempts to route the HTTP request to the 'index' page of the 'it' object.
     *
     * @return
     *      true if the processing succeeds. Otherwise false.
     */
    public abstract boolean handleIndexRequest(RequestImpl req, ResponseImpl rsp, Object node, MetaClass nodeMetaClass) throws IOException, ServletException;
}
