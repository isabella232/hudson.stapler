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

package org.kohsuke.stapler.framework.io;

import java.io.IOException;

/**
 * {@link IOException} with missing constructors.
 * @author Kohsuke Kawaguchi
 */
public class IOException2 extends IOException {
    public IOException2() {
    }

    public IOException2(String message) {
        super(message);
    }

    public IOException2(String message, Throwable cause) {
        super(message);
        initCause(cause);
    }

    public IOException2(Throwable cause) {
        this(cause.toString(),cause);
    }
}
