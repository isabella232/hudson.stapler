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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Creates and maintains {@link Model}s, that are used to write out
 * the value representation of {@link ExportedBean exposed beans}.
 * @author Kohsuke Kawaguchi
 */
public class ModelBuilder {
    /**
     * Instanciated {@link Model}s.
     * Registration happens in {@link Model#Model(ModelBuilder,Class)} so that cyclic references
     * are handled correctly.
     */
    /*package*/ final Map<Class, Model> models = new ConcurrentHashMap<Class, Model>();

    public <T> Model<T> get(Class<T> type) {
        Model m = models.get(type);
        if(m==null) {
            synchronized(this) {
                m = models.get(type);
                if(m==null)
                    m = new Model<T>(this,type);
            }
        }
        return m;
    }
}
