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

import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * @see org.eclipse.hudson.stapler.DataBoundConstructor
 * @deprecated  As of release 3.0.0, replaced by {@link org.eclipse.hudson.stapler.DataBoundConstructor} 
 */
@Deprecated
@Retention(RUNTIME)
@Target(CONSTRUCTOR)
@Documented
public @interface DataBoundConstructor  {
    
}
