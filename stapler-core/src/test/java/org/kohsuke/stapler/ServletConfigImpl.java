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

package org.kohsuke.stapler;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.util.Enumeration;

/**
 * @author Kohsuke Kawaguchi
 */
public class ServletConfigImpl implements ServletConfig {
    ServletContext context = new MockServletContext();

    public String getServletName() {
        return null;
    }

    public ServletContext getServletContext() {
        return context;
    }

    public String getInitParameter(String name) {
        return null;
    }

    public Enumeration getInitParameterNames() {
        return null;
    }
}
