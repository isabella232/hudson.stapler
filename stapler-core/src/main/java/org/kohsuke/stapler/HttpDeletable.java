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

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * Marks the object that can handle HTTP DELETE.
 *
 * @author Kohsuke Kawaguchi
 */
public interface HttpDeletable {
    /**
     * Called when HTTP DELETE method is invoked.
     */
    void delete( StaplerRequest req, StaplerResponse rsp ) throws IOException, ServletException;
}
