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

package org.kohsuke.stapler.export;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

/**
 * Indicates that the class has {@link Exported} annotations
 * on its properties to indicate which properties are written
 * as values to the remote XML/JSON API.
 *
 * <p>
 * This annotation inherits, so it only needs to be placed on the base class.
 *
 * @author Kohsuke Kawaguchi
 * @see Exported
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Target(ElementType.TYPE)
public @interface ExportedBean {
    /**
     * Controls the default visibility of all {@link Exported} properties
     * of this class (and its descendants.)
     *
     * <p>
     * A big default visibility value usually indicates that the bean
     * is always exposed as a descendant of another bean. In such case,
     * unless the default visibility is set no property will be exposed.
     */
    int defaultVisibility() default 1;
}
