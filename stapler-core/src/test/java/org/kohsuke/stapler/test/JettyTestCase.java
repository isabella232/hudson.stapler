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

package org.kohsuke.stapler.test;

import junit.framework.TestCase;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.WebApp;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.jetty.webapp.WebAppContext;

import javax.servlet.ServletContext;
import java.net.URL;

/**
 * Base test case for embedded Jetty.
 * 
 * @author Kohsuke Kawaguchi
 */
public abstract class JettyTestCase extends TestCase {
    protected Server server;
    /**
     * The top URL of this test web application.
     */
    protected URL url;
    protected Stapler stapler;
    protected ServletContext servletContext;
    protected WebApp webApp;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        server = new Server();

        server.setHandler(new WebAppContext("/noroot", ""));

        final Context context = new Context(server, "", Context.SESSIONS);
        context.addServlet(new ServletHolder(new Stapler()), "/*");
        server.setHandler(context);

        SocketConnector connector = new SocketConnector();
        server.addConnector(connector);
        server.start();

        url = new URL("http://localhost:"+connector.getLocalPort()+"/");

        servletContext = context.getServletContext();
        webApp = WebApp.get(servletContext);

        // export the test object as the root as a reasonable default.
        webApp.setApp(this);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        server.stop();
    }
}
