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

package org.kohsuke.stapler.jelly;

/**
 * Factory for {@link ResourceBundle}s.
 *
 * @author Kohsuke Kawaguchi
 */
public class ResourceBundleFactory {
    public ResourceBundle create(String baseName) {
        return new ResourceBundle(baseName);
    }

    public static final ResourceBundleFactory INSTANCE = new ResourceBundleFactory();
}
