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

package org.eclipse.hudson.stapler.bind;

/**
 * Marker interface for objects that have known URLs.
 *
 * If objects that implement this interface are exported, the well-known URLs
 * are used instead of assigning temporary URLs to objects. In this way, the application
 * can reduce memory consumption. This also enables objects to have "identities"
 * that outlive their GC life (for example, instance of the JPA-bound "User" class can
 * come and go, but they represent the single identity that's pointed by its primary key.)
 *
 * @author Kohsuke Kawaguchi
 */
public interface WithWellKnownURL {
    String getWellKnownUrl();
}
