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

import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * This "hidden" annotation is injected by Groovy compiler to capture parameter names
 * in the class file. Groovyc doesn't let me generate additional files, so this is easier
 * to do than generating the same files that the annotation processor does.
 *
 * @author Kohsuke Kawaguchi
 */
@Retention(RUNTIME)
public @interface CapturedParameterNames {
    String[] value();
}
