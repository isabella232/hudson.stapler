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

package org.eclipse.hudson.stapler.jelly;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.MissingAttributeException;
import org.apache.commons.jelly.XMLOutput;
import org.jvnet.maven.jellydoc.annotation.Required;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Kohsuke Kawaguchi
 */
public class IsUserInRoleTag extends AbstractStaplerTag {
    private String role;

    /**
     * The name of the role against which the user is checked.
     */
    @Required
    public void setRole(String role) {
        this.role = role;
    }

    public void doTag(XMLOutput output) throws MissingAttributeException, JellyTagException {
        HttpServletRequest req = getRequest();

        // work around http://jira.codehaus.org/browse/JETTY-234
        req.getUserPrincipal();

        if(req.isUserInRole(role))
            getBody().run(getContext(),output);
    }
}
