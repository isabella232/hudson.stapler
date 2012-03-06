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

package org.kohsuke.stapler.export;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.io.IOException;

/**
 * {@link Property} based on {@link Field}.
 * @author Kohsuke Kawaguchi
 */
class FieldProperty extends Property {
    private final Field field;

    public FieldProperty(Model owner, Field field, Exported exported) {
        super(owner,field.getName(), exported);
        this.field = field;
    }

    public Type getGenericType() {
        return field.getGenericType();
    }

    public Class getType() {
        return field.getType();
    }

    public String getJavadoc() {
        return parent.getJavadoc().getProperty(field.getName());
    }

    protected Object getValue(Object object) throws IllegalAccessException {
        return field.get(object);
    }
}
