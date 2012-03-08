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

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Indicates that concrete subtypes must have the views of the specified names.
 * For example, if your abstract class defines a mandatory view "foo.jelly", write
 * {@code @RequiresView("foo.jelly")}.
 *
 * <p>
 * TODO: write a checker that makes sure all the subtypes have required views.
 * I initially tried to do this in {@link AnnotationProcessorImpl}, but they don't see
 * resources, so the check needs to be done much later, probably by inspecting the jar file.
 *
 * @author Kohsuke Kawaguchi
 */
@Documented
@Retention(RUNTIME)
@Target(TYPE)
public @interface RequiresView {
    /**
     * Names of the view that's required.
     */
    String[] value();
}
