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

import javax.servlet.http.HttpServletRequest;

/**
 * Information about ancestor of the "it" node.
 *
 * @author Kohsuke Kawaguchi
 */
public interface Ancestor {
    /**
     * Gets the model object of the application.
     */
    Object getObject();

    /**
     * Gets the URL to this ancestor.
     *
     * <p>
     * The returned string represents the portion of the request URL
     * that matches this object. It starts with
     * {@link HttpServletRequest#getContextPath() context path},
     * and it ends without '/'. So, for example, if your web app
     * is deployed as "mywebapp" and this ancestor object is
     * obtained from the app root object by <tt>getFoo().getBar(3)</tt>,
     * then this string will be <tt>/mywebapp/foo/bar/3</tt>
     *
     * <p>
     * Any ASCII-unsafe characters are escaped.
     *
     * @return
     *      never null.
     */
    String getUrl();

    /**
     * Gets the remaining URL after this ancestor.
     *
     * <p>
     * The returned string represents the portion of the request URL
     * that follows this ancestor. It starts and ends without '/'.
     * So, for example, if the request URL is "foo/bar/3" and this ancestor object is
     * obtained from the app root object by <tt>getFoo()</tt>,
     * then this string will be <tt>bar/3</tt>
     */
    String getRestOfUrl();

    /**
     * Of the tokens that constitute {@link #getRestOfUrl()},
     * return the n-th token. So in the example described in {@link #getRestOfUrl()},
     * {@code getNextToken(0).equals("bar")} and
     * {@code getNextToken(1).equals("3")}
     */
    String getNextToken(int n);

    /**
     * Gets the complete URL to this ancestor.
     *
     * <p>
     * This method works like {@link #getUrl()} except it contains
     * the host name and the port number.
     */
    String getFullUrl();

    /**
     * Gets the relative path from the current object to this ancestor.
     *
     * <p>
     * The returned string looks like "../.." (ends without '/')
     *
     * @return
     *      never null.
     */
    String getRelativePath();

    /**
     * Gets the previous ancestor, or null if none (meaning
     * this is the root object.)
     */
    Ancestor getPrev();

    /**
     * Gets the next ancestor, or null if none (meaning
     * this is the 'it' object.
     */
    Ancestor getNext();
}
