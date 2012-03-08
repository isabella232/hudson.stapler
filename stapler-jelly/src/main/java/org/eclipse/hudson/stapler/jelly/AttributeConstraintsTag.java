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

import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;
import org.jvnet.maven.jellydoc.annotation.Required;

/**
 * DTD-like expression that specifies the consraints on attribute appearances.
 *
 * <p>
 * This tag should be placed right inside {@link DocumentationTag}
 * to describe attributes of a tag.
 *
 * @author Kohsuke Kawaguchi
 */
public class AttributeConstraintsTag extends TagSupport {
    public void doTag(XMLOutput output) {
        // noop
    }

    /**
     * Constraint expression.
     */
    @Required
    public void setExpr(String v) {}
}

