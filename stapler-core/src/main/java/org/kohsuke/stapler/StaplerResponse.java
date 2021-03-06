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

import org.kohsuke.stapler.export.Flavor;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;
import org.kohsuke.stapler.export.Model;
import org.kohsuke.stapler.export.NamedPathPruner;

/**
 * Defines additional operations made available by Stapler.
 *
 * @see Stapler#getCurrentResponse()
 * @author Kohsuke Kawaguchi
 */
public interface StaplerResponse extends HttpServletResponse {
    /**
     * Evaluates the url against the given object and
     * forwards the request to the result.
     *
     * <p>
     * This can be used for example inside an action method
     * to forward a request to a JSP.
     *
     * @param it
     *      the URL is evaluated against this object. Must not be null.
     * @param url
     *      the relative URL (e.g., "foo" or "foo/bar") to resolve
     *      against the "it" object.
     * @param request
     *      Request to be forwarded.
     */
    void forward(Object it, String url, StaplerRequest request) throws ServletException, IOException;
    /**
     * Redirects the browser to where it came from (the referer.)
     */
    void forwardToPreviousPage(StaplerRequest request) throws ServletException, IOException;

    /**
     * Works like {@link #sendRedirect(String)} except that this method
     * escapes the URL.
     */
    void sendRedirect2(String url) throws IOException;

    /**
     * Serves a static resource.
     *
     * <p>
     * This method sets content type, HTTP status code, sends the complete data
     * and closes the response. This method also handles cache-control HTTP headers
     * like "If-Modified-Since" and others.
     */
    void serveFile(StaplerRequest request, URL res) throws ServletException, IOException;

    void serveFile(StaplerRequest request, URL res, long expiration) throws ServletException, IOException;

    /**
     * Works like {@link #serveFile(StaplerRequest, URL)} but chooses the locale specific
     * version of the resource if it's available. The convention of "locale specific version"
     * is the same as that of property files.
     * So Japanese resource for <tt>foo.html</tt> would be named <tt>foo_ja.html</tt>.
     */
    void serveLocalizedFile(StaplerRequest request, URL res) throws ServletException, IOException;

    /**
     * Works like {@link #serveFile(StaplerRequest, URL, long)} but chooses the locale
     * specific version of the resource if it's available.
     *
     * See {@link #serveLocalizedFile(StaplerRequest, URL)} for more details.
     */
    void serveLocalizedFile(StaplerRequest request, URL res, long expiration) throws ServletException, IOException;

    /**
     * Serves a static resource.
     *
     * <p>
     * This method works like {@link #serveFile(StaplerRequest, URL)} but this version
     * allows the caller to fetch data from anywhere.
     *
     * @param data
     *      {@link InputStream} that contains the data of the static resource.
     * @param lastModified
     *      The timestamp when the resource was last modified. See {@link URLConnection#getLastModified()}
     *      for the meaning of the value. 0 if unknown, in which case "If-Modified-Since" handling
     *      will not be performed.
     * @param expiration
     *      The number of milliseconds until the resource will "expire".
     *      Until it expires the browser will be allowed to cache it
     *      and serve it without checking back with the server.
     *      After it expires, the client will send conditional GET to
     *      check if the resource is actually modified or not.
     *      If 0, it will immediately expire.
     * @param contentLength
     *      if the length of the input stream is known in advance, specify that value
     *      so that HTTP keep-alive works. Otherwise specify -1 to indicate that the length is unknown.
     * @param fileName
     *      file name of this resource. Used to determine the MIME type.
     *      Since the only important portion is the file extension, this could be just a file name,
     *      or a full path name, or even a pseudo file name that doesn't actually exist.
     *      It supports both '/' and '\\' as the path separator.
     *
     *      If this string starts with "mime-type:", like "mime-type:foo/bar", then "foo/bar" will
     *      be used as a MIME type without consulting the servlet container.
     */
    void serveFile(StaplerRequest req, InputStream data, long lastModified, long expiration, long contentLength, String fileName) throws ServletException, IOException;

    /**
     * @Deprecated use form with long contentLength
     */
    void serveFile(StaplerRequest req, InputStream data, long lastModified, long expiration, int contentLength, String fileName) throws ServletException, IOException;

    /**
     * Serves a static resource.
     *
     * Expiration date is set to the value that forces browser to do conditional GET
     * for all resources.
     *
     * @see #serveFile(StaplerRequest, InputStream, long, long, int, String)
     */
    void serveFile(StaplerRequest req, InputStream data, long lastModified, long contentLength, String fileName) throws ServletException, IOException;

    /**
     * @Deprecated use form with long contentLength
     */
    void serveFile(StaplerRequest req, InputStream data, long lastModified, int contentLength, String fileName) throws ServletException, IOException;

    /**
     * Serves the exposed bean in the specified flavor.
     *
     * <p>
     * This method performs the complete output from the header to the response body.
     * If the flavor is JSON, this method also supports JSONP via the {@code jsonp} query parameter.
     * 
     * <p>The {@code depth} parameter may be used to specify a recursion depth
     * as in {@link Model#writeTo(Object,int,DataWriter)}.
     * 
     * <p>As of 1.146, the {@code tree} parameter may be used to control the output
     * in detail; see {@link NamedPathPruner#NamedPathPruner(String)} for details.
     */
    void serveExposedBean(StaplerRequest req, Object exposedBean, Flavor flavor) throws ServletException,IOException;

    /**
     * Works like {@link #getOutputStream()} but tries to send the response
     * with gzip compression if the client supports it.
     *
     * <p>
     * This method is useful for sending out a large text content.
     *
     * @param req
     *      Used to determine whether the client supports compression
     */
    OutputStream getCompressedOutputStream(HttpServletRequest req) throws IOException;

    /**
     * Works like {@link #getCompressedOutputStream(HttpServletRequest)} but this
     * method is for {@link #getWriter()}.
     */
    Writer getCompressedWriter(HttpServletRequest req) throws IOException;

    /**
     * Performs the reverse proxy to the given URL.
     *
     * @return
     *      The status code of the response.
     */
    int reverseProxyTo(URL url, StaplerRequest req) throws IOException;
}
