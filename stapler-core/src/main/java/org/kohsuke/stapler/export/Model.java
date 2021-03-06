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

import org.kohsuke.stapler.export.TreePruner.ByDepth;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * Writes all the property of one {@link ExportedBean} to {@link DataWriter}.
 *
 * @author Kohsuke Kawaguchi
 */
public class Model<T> {
    /**
     * The class being modeled.
     */
    public final Class<T> type;

    /**
     * {@link Model} for the super class.
     */
    public final Model<? super T> superModel;

    private final Property[] properties;

    /*package*/ final ModelBuilder parent;
    /*package*/ final int defaultVisibility;

    /**
     * Lazily loaded "*.javadoc" file for this model. 
     */
    private volatile Properties javadoc;

    /*package*/ Model(ModelBuilder parent, Class<T> type) {
        this.parent = parent;
        this.type = type;
        ExportedBean eb = type.getAnnotation(ExportedBean.class);
        if(eb ==null)
            throw new NotExportableException(type);
        this.defaultVisibility = eb.defaultVisibility();
        
        Class<? super T> sc = type.getSuperclass();
        if(sc!=null && sc.getAnnotation(ExportedBean.class)!=null)
            superModel = parent.get(sc);
        else
            superModel = null;

        List<Property> properties = new ArrayList<Property>();

        // Use reflection to find out what properties are exposed.
        for( Field f : type.getFields() ) {
            if(f.getDeclaringClass()!=type) continue;
            Exported exported = f.getAnnotation(Exported.class);
            if(exported !=null)
                properties.add(new FieldProperty(this,f, exported));
        }

        for( Method m : type.getMethods() ) {
            if(m.getDeclaringClass()!=type) continue;
            Exported exported = m.getAnnotation(Exported.class);
            if(exported !=null)
                properties.add(new MethodProperty(this,m, exported));
        }

        this.properties = properties.toArray(new Property[properties.size()]);
        Arrays.sort(this.properties);

        parent.models.put(type,this);
    }

    /**
     * Gets all the exported properties.
     */
    public List<Property> getProperties() {
        return Collections.unmodifiableList(Arrays.asList(properties));
    }

    /**
     * Loads the javadoc list and returns it as {@link Properties}.
     *
     * @return always non-null.
     */
    /*package*/ Properties getJavadoc() {
        if(javadoc!=null)    return javadoc;
        synchronized(this) {
            if(javadoc!=null)    return javadoc;

            // load
            Properties p = new Properties();
            InputStream is = type.getClassLoader().getResourceAsStream(type.getName().replace('$', '/').replace('.', '/') + ".javadoc");
            if(is!=null) {
                try {
                    try {
                        p.load(is);
                    } finally {
                        is.close();
                    }
                } catch (IOException e) {
                    throw new RuntimeException("Unable to load javadoc for "+type,e);
                }
            }
            javadoc = p;
            return javadoc;
        }
    }

    /**
     * Writes the property values of the given object to the writer.
     */
    public void writeTo(T object, DataWriter writer) throws IOException {
        writeTo(object,0,writer);
    }

    /**
     * Writes the property values of the given object to the writer.
     *
     * @param pruner
     *      Controls which portion of the object graph will be sent to the writer.
     */
    public void writeTo(T object, TreePruner pruner, DataWriter writer) throws IOException {
        writer.startObject();
        writeNestedObjectTo(object,pruner,writer);
        writer.endObject();
    }

    /**
     * Writes the property values of the given object to the writer.
     *
     * @param baseVisibility
     *      This parameters controls how much data we'd be writing,
     *      by adding bias to the sub tree cutting.
     *      A property with {@link Exported#visibility() visibility} X will be written
     *      if the current depth Y and baseVisibility Z satisfies X+Z>Y.
     *
     *      0 is the normal value. Positive value means writing bigger tree,
     *      and negative value means writing smaller trees.
     *
     * @deprecated as of 1.139
     */
    public void writeTo(T object, int baseVisibility, DataWriter writer) throws IOException {
        writeTo(object,new ByDepth(1-baseVisibility),writer);
    }

    void writeNestedObjectTo(T object, TreePruner pruner, DataWriter writer) throws IOException {
        if(superModel !=null)
            superModel.writeNestedObjectTo(object,pruner,writer);

        for (Property p : properties)
            p.writeTo(object,pruner,writer);
    }
}
