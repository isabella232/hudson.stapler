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

import org.apache.commons.jelly.XMLOutput;
import org.eclipse.hudson.stapler.StaplerRequest;
import org.eclipse.hudson.stapler.StaplerResponse;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.Script;

import java.io.IOException;

/**
 * Pluggability point for controlling how scripts get executed.
 *
 * @author Kohsuke Kawaguchi
 * @see JellyFacet#scriptInvoker
 */
public interface ScriptInvoker {
    /**
     * Invokes the script and generates output to {@link StaplerResponse#getOutputStream()}.
     */
    void invokeScript(StaplerRequest req, StaplerResponse rsp, Script script, Object it) throws IOException, JellyTagException;

    /**
     * Invokes the script and generates output to the specified output
     */
    void invokeScript(StaplerRequest req, StaplerResponse rsp, Script script, Object it, XMLOutput out) throws IOException, JellyTagException;
}
