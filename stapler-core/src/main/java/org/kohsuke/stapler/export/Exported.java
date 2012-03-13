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

package org.kohsuke.stapler.export;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Documented;
import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;

/**
 * @see org.eclipse.hudson.stapler.export.Exported
 * @deprecated  As of release 3.0.0, replaced by {@link org.eclipse.hudson.stapler.export.Exported} 
 */
@Deprecated
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target({FIELD, METHOD})
public @interface Exported {
    int visibility() default 0;
    String name() default "";
    boolean inline() default false;
}
