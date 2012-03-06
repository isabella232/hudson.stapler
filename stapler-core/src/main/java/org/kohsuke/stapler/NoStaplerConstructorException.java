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

package org.kohsuke.stapler;

/**
 * @author Kohsuke Kawaguchi
 */
public class NoStaplerConstructorException extends IllegalArgumentException {
    public NoStaplerConstructorException(String s) {
        super(s);
    }

    public NoStaplerConstructorException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoStaplerConstructorException(Throwable cause) {
        super(cause);
    }
}
