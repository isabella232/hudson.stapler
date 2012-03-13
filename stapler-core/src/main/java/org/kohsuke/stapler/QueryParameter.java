/*******************************************************************************
 *
 * Copyright (c) 2012 Oracle Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *
 *    Winston Prakash
 *     
 *******************************************************************************/

package org.kohsuke.stapler;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @see org.eclipse.hudson.stapler.QueryParameter
 * @deprecated  As of release 3.0.0, replaced by {@link org.eclipse.hudson.stapler.QueryParameter} 
 */
@Deprecated
@Retention(RUNTIME)
@Target(PARAMETER)
@Documented
public @interface QueryParameter {
     
    String value() default "";

    boolean required() default false;

    boolean fixEmpty() default false;
}
