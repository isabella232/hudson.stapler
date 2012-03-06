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

package org.kohsuke.stapler.export;

/**
 * Controls the portion of the object graph to be written to {@link DataWriter}.
 *
 * @author Kohsuke Kawaguchi
 * @see Model#writeTo(Object, TreePruner, DataWriter)
 */
public abstract class TreePruner {
    /**
     * Called before Hudson writes a new property.
     *
     * @return
     *      null if this property shouldn't be written. Otherwise the returned {@link TreePruner} object
     *      will be consulted to determine properties of the child object in turn.
     */
    public abstract TreePruner accept(Object node, Property prop);

    public static class ByDepth extends TreePruner {
        final int n;
        private ByDepth next;

        public ByDepth(int n) {
            this.n = n;
        }

        private ByDepth next() {
            if (next==null)
                next = new ByDepth(n+1);
            return next;
        }

        @Override
        public TreePruner accept(Object node, Property prop) {
            if (prop.visibility < n)    return null;    // not visible

            if (prop.inline)    return this;
            return next();
        }
    }
}
