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

import java.util.HashMap;
import java.util.Map;

/**
 * @author Kohsuke Kawaguchi
 */
public class ReflectionUtils {
    /**
     * Given the primitive type, returns the VM default value for that type in a boxed form.
     * For reference types, return null.
     */
    public static Object getVmDefaultValueFor(Class<?> type) {
        return defaultPrimitiveValue.get(type);
    }

    private static final Map<Class,Object> defaultPrimitiveValue = new HashMap<Class, Object>();
    static {
        defaultPrimitiveValue.put(boolean.class,false);
        defaultPrimitiveValue.put(int.class,0);
        defaultPrimitiveValue.put(long.class,0L);
    }
}
