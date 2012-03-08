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
 * Signals an error that the class didn't have {@link ExportedBean}.
 *
 * @author Kohsuke Kawaguchi
 * @see Model
 */
public class NotExportableException extends IllegalArgumentException {
    private final Class type;

    public NotExportableException(Class type) {
        this(type+" doesn't have @"+ ExportedBean.class.getSimpleName(),type);
    }

    public NotExportableException(String s, Class type) {
        super(s);
        this.type = type;
    }

    public NotExportableException(String message, Throwable cause, Class type) {
        super(message, cause);
        this.type = type;
    }

    public NotExportableException(Throwable cause, Class type) {
        super(cause);
        this.type = type;
    }

    /**
     * Gets the type that didn't have {@link ExportedBean}
     */
    public Class getType() {
        return type;
    }
}
