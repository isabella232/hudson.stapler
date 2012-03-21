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

package org.kohsuke.stapler.framework.errors;

import java.io.File;

/**
 * Model object used to display the error top page if
 * we couldn't create the home directory.
 *
 * <p>
 * <tt>index.jelly</tt> would display a nice friendly error page.
 *
 * @author Kohsuke Kawaguchi
 */
public class NoHomeDirError extends ErrorObject {
    public final File home;

    public NoHomeDirError(File home) {
        this.home = home;
    }

    @Override
    public String getMessage() {
        return "Unable to create home directory: "+home;
    }
}
