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

package org.eclipse.hudson.stapler.framework.errors;

/**
 * Root class of the stapler error objects.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class ErrorObject {
    /**
     * Gets the error message.
     */
    public abstract String getMessage();
}
