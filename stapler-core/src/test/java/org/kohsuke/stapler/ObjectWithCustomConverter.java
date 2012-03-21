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

import org.apache.commons.beanutils.Converter;

/**
 * @author Kohsuke Kawaguchi
 */
public class ObjectWithCustomConverter {
    public final int x,y;

    public ObjectWithCustomConverter(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public static class StaplerConverterImpl implements Converter {
        public Object convert(Class type, Object value) {
            String[] tokens = value.toString().split(",");
            return new ObjectWithCustomConverter(
                    Integer.parseInt(tokens[0]),
                    Integer.parseInt(tokens[1]));
        }
    }
}
