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

import java.io.IOException;

/**
 * Receives the event callback on the model data to be exposed.
 *
 * <p>
 * The call sequence is:
 *
 * <pre>
 * EVENTS := startObject PROPERTY* endObject
 * PROPERTY := name VALUE
 * VALUE := valuePrimitive
 *        | value
 *        | valueNull
 *        | startArray VALUE* endArray
 *        | EVENTS
 * </pre>
 * 
 * @author Kohsuke Kawaguchi
 */
public interface DataWriter {
    void name(String name) throws IOException;

    void valuePrimitive(Object v) throws IOException;
    void value(String v) throws IOException;
    void valueNull() throws IOException;

    void startArray() throws IOException;
    void endArray() throws IOException;

    void startObject() throws IOException;
    void endObject() throws IOException;
}
