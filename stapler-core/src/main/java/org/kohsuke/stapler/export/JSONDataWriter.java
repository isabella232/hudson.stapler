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

import java.io.IOException;
import java.io.Writer;

/**
 * JSON writer.
 *
 * @author Kohsuke Kawaguchi
 */
class JSONDataWriter implements DataWriter {
    protected boolean needComma;
    protected final Writer out;

    JSONDataWriter(Writer out) throws IOException {
        this.out = out;
    }

    JSONDataWriter(StaplerResponse rsp) throws IOException {
        out = rsp.getWriter();
    }

    public void name(String name) throws IOException {
        comma();
        out.write('"'+name+"\":");
        needComma = false;
    }

    protected void data(String v) throws IOException {
        comma();
        out.write(v);
    }

    protected void comma() throws IOException {
        if(needComma) out.write(',');
        needComma = true;
    }

    public void valuePrimitive(Object v) throws IOException {
        data(v.toString());
    }

    public void value(String v) throws IOException {
        StringBuilder buf = new StringBuilder(v.length());
        buf.append('\"');
        for( int i=0; i<v.length(); i++ ) {
            char c = v.charAt(i);
            switch(c) {
            case '"':   buf.append("\\\"");break;
            case '\\':  buf.append("\\\\");break;
            case '\n':  buf.append("\\n");break;
            case '\r':  buf.append("\\r");break;
            case '\t':  buf.append("\\t");break;
            default:    buf.append(c);break;
            }
        }
        buf.append('\"');
        data(buf.toString());
    }

    public void valueNull() throws IOException {
        data("null");
    }

    public void startArray() throws IOException {
        comma();
        out.write('[');
        needComma = false;
    }

    public void endArray() throws IOException {
        out.write(']');
        needComma = true;
    }

    public void startObject() throws IOException {
        comma();
        out.write('{');
        needComma=false;
    }

    public void endObject() throws IOException {
        out.write('}');
        needComma=true;
    }
}