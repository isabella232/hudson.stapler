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

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * Object that represents the HTTP response, which is defined as a capability to produce the response.
 *
 * <p>
 * <tt>doXyz(...)</tt> method could return an object of this type or throw an exception of this type, and if it does so,
 * the object is asked to produce HTTP response.
 *
 * <p>
 * This is useful to make <tt>doXyz</tt> look like a real function, and decouple it further from HTTP.
 *
 * @author Kohsuke Kawaguchi
 */
public interface HttpResponse {
    /**
     * @param node
     *      The object whose "doXyz" method created this object.
     */
    void generateResponse(StaplerRequest req, StaplerResponse rsp, Object node) throws IOException, ServletException;
}
