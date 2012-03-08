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

package org.eclipse.hudson.stapler.export;

/**
 * Interface that an exposed bean can implement, to do the equivalent
 * of <tt>writeReplace</tt> in Java serialization.
 * @author Kohsuke Kawaguchi
 */
public interface CustomExportedBean {
    /**
     * The returned object will be introspected and written as JSON/XML.
     */
    Object toExportedObject();
}
