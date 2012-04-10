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

import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.METHOD;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * Indicates that the method is exposed to client-side JavaScript proxies
 * and is callable as a method from them.
 *
 * <p>
 * This annotation is assumed to be implicit on every public methods
 * that start with 'js', like 'jsFoo' or 'jsBar', but you can use this annotation
 * on those methods to assign different names.
 *
 * @author Kohsuke Kawaguchi
 */
@Retention(RUNTIME)
@Target(METHOD)
@Documented
public @interface JavaScriptMethod {
    /**
     * JavaScript method name assigned to this method.
     *
     * <p>
     * If unspecified, defaults to the method name.
     */
    String[] name() default {};
}
