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

package org.kohsuke.stapler.jelly;

import org.apache.commons.jelly.XMLOutput;
import org.dom4j.io.HTMLWriter;
import org.dom4j.io.OutputFormat;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

/**
 * Wrapper for XMLOutput using HTMLWriter that can turn off its HTML handling
 * (if the Content-Type gets set to something other than text/html).
 * 
 * @author Alan.Harder@Sun.Com
 */
public class HTMLWriterOutput extends XMLOutput {
    private HTMLWriter htmlWriter;
    private OutputFormat format;

    public static HTMLWriterOutput create(OutputStream out) throws UnsupportedEncodingException {
        OutputFormat format = createFormat();
        return new HTMLWriterOutput(new HTMLWriter(out, format), format, false);
    }

    public static HTMLWriterOutput create(Writer out, boolean escapeText) {
        OutputFormat format = createFormat();
        return new HTMLWriterOutput(new HTMLWriter(out, format), format, escapeText);
    }

    private static OutputFormat createFormat() {
        OutputFormat format = new OutputFormat();
        format.setXHTML(true);
        // Only use short close for tags identified by HTMLWriter:
        format.setExpandEmptyElements(true);
        return format;
    }

    private HTMLWriterOutput(HTMLWriter hw, OutputFormat fmt, boolean escapeText) {
        super(hw);
        hw.setEscapeText(escapeText);
        this.htmlWriter = hw;
        this.format = fmt;
    }

    @Override public void close() throws IOException {
        htmlWriter.close();
    }

    /**
     * False to turn off HTML handling and reenable "/>" for any empty XML element.
     * True to switch back to default mode with HTML handling.
     */
    public void useHTML(boolean enabled) {
        htmlWriter.setEnabled(enabled);
        format.setExpandEmptyElements(enabled);
    }
}
