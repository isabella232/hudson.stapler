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

package org.kohsuke.stapler.bind;

import org.kohsuke.stapler.ClassDescriptor;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.MetaClass;
import org.kohsuke.stapler.WebApp;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;


/**
 * Handles to the object bound via {@link BoundObjectTable}.
 *
 * As {@link HttpResponse}, this object generates a redirect to the URL that it points to.
 *
 * @author Kohsuke Kawaguchi
 * @see MetaClass#buildDispatchers(ClassDescriptor)
 */
public abstract class Bound implements HttpResponse {
    /**
     * Explicitly unbind this object. The referenced object
     * won't be bound to URL anymore.
     */
    public abstract void release();

    /**
     * The URL where the object is bound to. This method
     * starts with '/' and thus always absolute within the current web server.
     */
    public abstract String getURL();

    /**
     * Gets the bound object.
     */
    public abstract Object getTarget();

    /**
     * Returns a JavaScript expression which evaluates to a JavaScript proxy that
     * talks back to the bound object that this handle represents.
     */
    public final String getProxyScript() {
        StringBuilder buf = new StringBuilder("makeStaplerProxy('").append(getURL()).append("','").append(
                WebApp.getCurrent().getCrumbIssuer().issueCrumb()
        ).append("',[");

        boolean first=true;
        for (Method m : getTarget().getClass().getMethods()) {
            Collection<String> names;
            if (m.getName().startsWith("js")) {
                names = Collections.singleton(camelize(m.getName().substring(2)));
            } else {
                JavaScriptMethod a = m.getAnnotation(JavaScriptMethod.class);
                if (a!=null) {
                    names = Arrays.asList(a.name());
                    if (names.isEmpty())
                        names = Collections.singleton(m.getName());
                } else
                    continue;
            }

            for (String n : names) {
                if (first)  first = false;
                else        buf.append(',');
                buf.append('\'').append(n).append('\'');
            }
        }
        buf.append("])");
        
        return buf.toString();
    }

    private static String camelize(String name) {
        return Character.toLowerCase(name.charAt(0))+name.substring(1);
    }
}
