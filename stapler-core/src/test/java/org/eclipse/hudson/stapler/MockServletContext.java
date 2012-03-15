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
package org.eclipse.hudson.stapler;

import javax.servlet.ServletContext;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.util.Set;
import java.util.Enumeration;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.InputStream;

/**
 * @author Kohsuke Kawaguchi
 */
public class MockServletContext implements ServletContext {

    public ServletContext getContext(String uripath) {
        return null;
    }

    public int getMajorVersion() {
        return 0;
    }

    public int getMinorVersion() {
        return 0;
    }

    public String getMimeType(String file) {
        return null;
    }

    public Set getResourcePaths(String s) {
        return null;
    }

    public URL getResource(String path) throws MalformedURLException {
        return null;
    }

    public InputStream getResourceAsStream(String path) {
        return null;
    }

    public RequestDispatcher getRequestDispatcher(String path) {
        return null;
    }

    public RequestDispatcher getNamedDispatcher(String name) {
        return null;
    }

    public Servlet getServlet(String name) throws ServletException {
        return null;
    }

    public Enumeration getServlets() {
        return null;
    }

    public Enumeration getServletNames() {
        return null;
    }

    public void log(String msg) {
    }

    public void log(Exception exception, String msg) {
    }

    public void log(String message, Throwable throwable) {
    }

    public String getRealPath(String path) {
        return null;
    }

    public String getServerInfo() {
        return null;
    }

    public String getInitParameter(String name) {
        return null;
    }

    public Enumeration getInitParameterNames() {
        return null;
    }

    public Object getAttribute(String name) {
        return null;
    }

    public Enumeration getAttributeNames() {
        return null;
    }

    public void setAttribute(String name, Object object) {
    }

    public void removeAttribute(String name) {
    }

    public String getServletContextName() {
        return null;
    }

    public String getContextPath() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
