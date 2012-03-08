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

package org.eclipse.hudson.stapler;

import java.util.Collection;

/**
 * A web-bound object can implement this interface to allow designated objects to selectively override
 * URL mappings.
 *
 * <p>
 * If the designated override objects fail to handle the request, the host object (the one that's implementing
 * {@link StaplerOverridable}) will handle the request.
 *
 * @author Kohsuke Kawaguchi
 */
public interface StaplerOverridable {
    /**
     * Returns a list of overridables.
     * Override objects are tried in their iteration order.
     *
     * @return can be null, which is treated as the same thing as returning an empty collection.
     */
    Collection<?> getOverrides();
}
