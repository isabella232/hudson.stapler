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

import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.JellyException;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.Tag;
import org.apache.commons.jelly.TagLibrary;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.jelly.expression.ConstantExpression;
import org.apache.commons.jelly.expression.Expression;
import org.apache.commons.jelly.impl.ExpressionAttribute;
import org.apache.commons.jelly.impl.StaticTag;
import org.apache.commons.jelly.impl.StaticTagScript;
import org.apache.commons.jelly.impl.TagScript;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Jelly tag library for static tags.
 *
 * <p>
 * Unlike {@link StaticTagScript}, this doesn't even try to see if the tag name is available as a dynamic tag.
 * By not doing so, this implementation achieves a better performance both in speed and memory usage. 
 *
 * <p>
 * Jelly by default uses {@link StaticTagScript} instance to represent a tag that's parsed as a static tag,
 * and for each invocation, this code checks if the tag it represents is now defined as a dynamic tag.
 * Plus it got the code to cache {@link StaticTag} instances per thread, which consumes more space and time.
 *
 * @author Kohsuke Kawaguchi
 * @since 1.342
 */
public class ReallyStaticTagLibrary extends TagLibrary {
    /**
     * IIUC, this method will never be invoked.
     */
    @Override
    public Tag createTag(final String name, Attributes attributes) throws JellyException {
        return null;
    }

    @Override
    public TagScript createTagScript(String tagName, Attributes atts) throws JellyException {
        return createTagScript();
    }

    /**
     * Creates a new instance of {@link TagScript} that generates a literal element.
     */
    public static TagScript createTagScript() {
        return new TagScript() {
            /**
             * If all the attributes are constant, as is often the case with literal tags,
             * then we can skip the attribute expression evaluation altogether.
             */
            private boolean allAttributesAreConstant = true;

            @Override
            public void addAttribute(String name, Expression expression) {
                allAttributesAreConstant &= expression instanceof ConstantExpression;
                super.addAttribute(name, expression);
            }

            @Override
            public void addAttribute(String name, String prefix, String nsURI, Expression expression) {
                allAttributesAreConstant &= expression instanceof ConstantExpression;
                super.addAttribute(name, prefix, nsURI, expression);
            }

            public void run(JellyContext context, XMLOutput output) throws JellyTagException {
                Attributes actual = (allAttributesAreConstant && !EMIT_LOCATION) ? getSaxAttributes() : buildAttributes(context);

                try {
                    output.startElement(getNsUri(),getLocalName(),getElementName(),actual);
                    getTagBody().run(context,output);
                    output.endElement(getNsUri(),getLocalName(),getElementName());
                } catch (SAXException x) {
                    throw new JellyTagException(x);
                }
            }

            private AttributesImpl buildAttributes(JellyContext context) {
                AttributesImpl actual = new AttributesImpl();

                for (ExpressionAttribute att : attributes.values()) {
                    Expression expression = att.exp;
                    String v = expression.evaluateAsString(context);
                    if (v==null)    continue; // treat null as no attribute
                    actual.addAttribute(att.nsURI, att.name, att.qname(),"CDATA", v);
                }

                if (EMIT_LOCATION) {
                    actual.addAttribute("","file","file","CDATA",String.valueOf(getFileName()));
                    actual.addAttribute("","line","line","CDATA",String.valueOf(getLineNumber()));

                    // try to obtain the meaningful part of the script and put it in CSS with a
                    // class name like "jelly-foo-bar-xyz" given "file://path/to/src/tree/src/main/resources/foo/bar/xyz.jelly"
                    String form = getFileName().replace('\\','/');
                    for (String suffix : SUFFIX) {
                        int idx = form.lastIndexOf(suffix);
                        if (idx>0)  form=form.substring(idx+suffix.length());
                    }

                    int c = actual.getIndex("class");
                    if (c>=0)   actual.setValue(c, actual.getValue(c)+" "+form);
                    else        actual.addAttribute("","class","class","CDATA",form);
                }

                return actual;
            }
        };
    }
    /**
     * Reusable instance.
     */
    public static final TagLibrary INSTANCE = new ReallyStaticTagLibrary();

    /**
     * If true, emit the location information.
     */
    public static boolean EMIT_LOCATION = false;

    private static final String[] SUFFIX = {"src/main/resources/","src/test/resources/"};
}
