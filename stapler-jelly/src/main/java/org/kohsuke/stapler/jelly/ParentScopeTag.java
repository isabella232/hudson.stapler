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

package org.kohsuke.stapler.jelly;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;

/**
 * Executes the body in the parent scope.
 * This is useful for creating a 'local' scope.
 *
 * @author Kohsuke Kawaguchi
 */
public class ParentScopeTag extends TagSupport {
    public void doTag(XMLOutput output) throws JellyTagException {
        getBody().run(context.getParent(), output);
    }
}
