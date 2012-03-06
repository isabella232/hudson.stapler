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

import org.kohsuke.stapler.StaplerResponse;

import java.io.Writer;
import java.io.IOException;

/**
 * Writes out the format that can be <tt>eval</tt>-ed from Python.
 *
 * <p>
 * Python uses the same list and map literal syntax as JavaScript.
 * The only difference is <tt>null</tt> vs <tt>None</tt>.
 *
 * @author Kohsuke Kawaguchi
 */
final class PythonDataWriter extends JSONDataWriter {
    public PythonDataWriter(Writer out) throws IOException {
        super(out);
    }

    public PythonDataWriter(StaplerResponse rsp) throws IOException {
        super(rsp); 
    }

    @Override
    public void valueNull() throws IOException {
        data("None");
    }

    public void valuePrimitive(Object v) throws IOException {
        if(v instanceof Boolean) {
            if((Boolean)v)  data("True");
            else            data("False");
            return;
        }
        super.valuePrimitive(v);
    }
}
