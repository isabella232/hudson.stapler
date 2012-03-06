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
 * Export flavor.
 *
 * @author Kohsuke Kawaguchi
 */
public enum Flavor {
    JSON("application/javascript;charset=UTF-8") {
        public DataWriter createDataWriter(Object bean, StaplerResponse rsp) throws IOException {
            return new JSONDataWriter(rsp);
        }
        public DataWriter createDataWriter(Object bean, Writer w) throws IOException {
            return new JSONDataWriter(w);
        }
    },
    PYTHON("text/x-python;charset=UTF-8") {
        public DataWriter createDataWriter(Object bean, StaplerResponse rsp) throws IOException {
            return new PythonDataWriter(rsp);
        }
        public DataWriter createDataWriter(Object bean, Writer w) throws IOException {
            return new PythonDataWriter(w);
        }
    },
    RUBY("text/x-ruby;charset=UTF-8") {
        public DataWriter createDataWriter(Object bean, StaplerResponse rsp) throws IOException {
            return new RubyDataWriter(rsp);
        }
        public DataWriter createDataWriter(Object bean, Writer w) throws IOException {
            return new RubyDataWriter(w);
        }
    },
    XML("application/xml;charset=UTF-8") {
        public DataWriter createDataWriter(Object bean, StaplerResponse rsp) throws IOException {
            return new XMLDataWriter(bean,rsp);
        }
        public DataWriter createDataWriter(Object bean, Writer w) throws IOException {
            return new XMLDataWriter(bean,w);
        }
    };

    /**
     * Content-type of this flavor, including charset "UTF-8".
     */
    public final String contentType;

    Flavor(String contentType) {
        this.contentType = contentType;
    }

    public abstract DataWriter createDataWriter(Object bean, StaplerResponse rsp) throws IOException;
    public abstract DataWriter createDataWriter(Object bean, Writer w) throws IOException;
}
