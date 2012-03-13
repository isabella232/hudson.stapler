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

package org.eclipse.hudson.stapler.bind;

import org.eclipse.hudson.stapler.bind.Bound;
import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.WebClient;
import org.eclipse.hudson.stapler.HttpResponse;
import org.eclipse.hudson.stapler.StaplerResponse;
import org.eclipse.hudson.stapler.test.JettyTestCase;

import java.io.IOException;
import java.net.URL;

/**
 * @author Kohsuke Kawaguchi
 */
public class BoundObjectTableTest extends JettyTestCase {
    /**
     * Exports an object and see if it can be reached.
     */
    public void testExport() throws Exception {
        TextPage page = new WebClient().getPage(new URL(url, "/bind"));
        assertEquals("hello world",page.getContent());
    }

    public HttpResponse doBind() throws IOException {
        Bound h = webApp.boundObjectTable.bind(new HelloWorld("hello world"));
        System.out.println(h.getURL());
        return h;
    }

    public static class HelloWorld {
        private final String message;

        public HelloWorld(String message) {
            this.message = message;
        }

        public void doIndex(StaplerResponse rsp) throws IOException {
            rsp.setContentType("text/plain;charset=UTF-8");
            rsp.getWriter().write(message);
        }
    }
}