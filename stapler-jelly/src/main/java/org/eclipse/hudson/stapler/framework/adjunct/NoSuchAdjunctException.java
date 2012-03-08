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

package org.eclipse.hudson.stapler.framework.adjunct;

import java.io.IOException;

/**
 * @author Kohsuke Kawaguchi
 */
public class NoSuchAdjunctException extends IOException {
    public NoSuchAdjunctException() {
    }

    public NoSuchAdjunctException(String message) {
        super(message);
    }

    public NoSuchAdjunctException(String message, Throwable cause) {
        super(message);
        initCause(cause);
    }

    public NoSuchAdjunctException(Throwable cause) {
        super();
        initCause(cause);
    }
}
