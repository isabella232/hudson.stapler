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

import org.kohsuke.stapler.AbstractTearOff;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.bind.BoundObjectTable;

import javax.servlet.ServletContext;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.WeakHashMap;
import java.util.Hashtable;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Object scoped to the entire webapp. Mostly used for configuring behavior of Stapler.
 *
 * <p>
 * In contrast, {@link Stapler} is a servlet, so there can be multiple instances per webapp.
 *
 * @author Kohsuke Kawaguchi
 * @see WebApp#get(ServletContext)
 * @see WebApp#getCurrent()  
 * @see Stapler#getWebApp()
 */
public class WebApp {
    /**
     * Obtains the {@link WebApp} associated with the given {@link ServletContext}.
     */
    public static WebApp get(ServletContext context) {
        Object o = context.getAttribute(WebApp.class.getName());
        if(o==null) {
            synchronized (WebApp.class) {
                o = context.getAttribute(WebApp.class.getName());
                if(o==null) {
                    o = new WebApp(context);
                    context.setAttribute(WebApp.class.getName(),o);
                }
            }
        }
        return (WebApp)o;
    }

    /**
     * {@link ServletContext} for this webapp.
     */
    public final ServletContext context;

    /**
     * Duck-type wrappers for the given class.
     */
    public final Map<Class,Class[]> wrappers = new HashMap<Class,Class[]>();

    /**
     * MIME type -> encoding map that determines how static contents in the war file is served.
     */
    public final Map<String,String> defaultEncodingForStaticResources = new HashMap<String,String>();

    /**
     * Activated facets.
     *
     * TODO: is this really mutable?
     */
    public final List<Facet> facets = new Vector<Facet>();

    /**
     * MIME type mapping from extensions (like "txt" or "jpg") to MIME types ("foo/bar").
     *
     * This overrides whatever mappings given in the servlet as far as stapler is concerned.
     * This is case insensitive, and should be normalized to lower case.
     */
    public final Map<String,String> mimeTypes = new Hashtable<String,String>();

    private volatile ClassLoader classLoader;

    /**
     * All {@link MetaClass}es.
     *
     * Avoids class leaks by {@link WeakHashMap}.
     */
    private final Map<Class,MetaClass> classMap = new WeakHashMap<Class,MetaClass>();

    /**
     * Handles objects that are exported.
     */
    public final BoundObjectTable boundObjectTable = new BoundObjectTable();

    private final CopyOnWriteArrayList<HttpResponseRenderer> responseRenderers = new CopyOnWriteArrayList<HttpResponseRenderer>();

    private CrumbIssuer crumbIssuer = CrumbIssuer.DEFAULT;

    public WebApp(ServletContext context) {
        this.context = context;
        // TODO: allow classloader to be given?
        facets.addAll(Facet.discover(Thread.currentThread().getContextClassLoader()));
        responseRenderers.add(new HttpResponseRenderer.Default());
    }

    /**
     * Returns the 'app' object, which is the user-specified object that
     * sits at the root of the URL hierarchy and handles the request to '/'.
     */
    public Object getApp() {
        return context.getAttribute("app");
    }

    public void setApp(Object app) {
        context.setAttribute("app",app);
    }

    public CrumbIssuer getCrumbIssuer() {
        return crumbIssuer;
    }

    public void setCrumbIssuer(CrumbIssuer crumbIssuer) {
        this.crumbIssuer = crumbIssuer;
    }

    public CopyOnWriteArrayList<HttpResponseRenderer> getResponseRenderers() {
        return responseRenderers;
    }

    public ClassLoader getClassLoader() {
        ClassLoader cl = classLoader;
        if(cl==null)
            cl = Thread.currentThread().getContextClassLoader();
        if(cl==null)
            cl = Stapler.class.getClassLoader();
        return cl;
    }

    /**
     * If the facet of the given type exists, return it. Otherwise null.
     */
    public <T extends Facet> T getFacet(Class<T> type) {
        for (Facet f : facets)
            if(type==f.getClass())
                return type.cast(f);
        return null;
    }

    /**
     * Sets the classloader used by {@link StaplerRequest#bindJSON(Class, JSONObject)} and its sibling methods.
     */
    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public MetaClass getMetaClass(Class c) {
        if(c==null)     return null;
        synchronized(classMap) {
            MetaClass mc = classMap.get(c);
            if(mc==null) {
                mc = new MetaClass(this,c);
                classMap.put(c,mc);
            }
            return mc;
        }
    }

    /**
     * Convenience maintenance method to clear all the cached scripts for the given tearoff type.
     *
     * <p>
     * This is useful when you want to have the scripts reloaded into the live system without
     * the performance penalty of {@link MetaClass#NO_CACHE}.
     *
     * @see MetaClass#NO_CACHE
     */
    public void clearScripts(Class<? extends AbstractTearOff> clazz) {
        synchronized (classMap) {
            for (MetaClass v : classMap.values()) {
                AbstractTearOff t = v.getTearOff(clazz);
                if (t!=null)
                    t.clearScripts();
            }
        }
    }

    /**
     * Gets the current {@link WebApp} that the calling thread is associated with.
     */
    public static WebApp getCurrent() {
        return Stapler.getCurrent().getWebApp();
    }
}
