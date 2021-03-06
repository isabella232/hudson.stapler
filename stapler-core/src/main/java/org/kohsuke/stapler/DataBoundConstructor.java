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

import net.sf.json.JSONObject;

import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * Designates the constructor to be created
 * from methods like
 * {@link StaplerRequest#bindJSON(Class, JSONObject)} and
 * {@link StaplerRequest#bindParameters(Class, String)}.
 *
 * <p>
 * Stapler will invoke the designated constructor by using arguments from the corresponding
 * {@link JSONObject} (in case of {@link StaplerRequest#bindJSON(Class, JSONObject)}) or request parameters
 * (in case of {@link StaplerRequest#bindParameters(Class, String)}).
 *
 * <p>
 * The matching is done by using the constructor parameter name. Since this information is not available
 * at the runtime, annotation processing runs during the compilation to capture them in separate "*.stapler" files.
 *
 *
 * <p>
 * This replaces "@stapler-constructor" annotation.
 *
 * @author Kohsuke Kawaguchi
 */
@Retention(RUNTIME)
@Target(CONSTRUCTOR)
@Documented
public @interface DataBoundConstructor {
}
