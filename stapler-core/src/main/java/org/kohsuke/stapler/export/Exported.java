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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Documented;
import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;

/**
 * Mark the field or the getter method whose value gets exposed
 * to the remote API.
 *
 * @author Kohsuke Kawaguchi
 * @see ExportedBean
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target({FIELD, METHOD})
public @interface Exported {
    /**
     * Controls how visible this property is.
     *
     * <p>
     * If the value is 1, the property will be
     * visible only when the current model object is exposed as the
     * top-level object.
     * <p>
     * If the value is 2, in addition to above, the property will still
     * be visible if the current model object is exposed as the 2nd-level
     * object.
     * <p>
     * And the rest goes in the same way. If the value is N, the object
     * is exposed as the Nth level object.
     *
     * <p>
     * The default value of this property is determined by
     * {@link ExportedBean#defaultVisibility()}.
     *
     * <p>
     * So bigger the number, more important the property is.
     */
    int visibility() default 0;

    /**
     * Name of the exposed property.
     * <p>
     * This token is used as the XML element name or the JSON property name.
     * The default is to use the Java property name.
     */
    String name() default "";

    /**
     * Visibility adjustment for traversing this property.
     *
     * <p>
     * If true, visiting this property won't increase the depth count,
     * so the referenced object is exported as if it were a part of this
     * object.
     *
     * <p>
     * This flag can be used to selectively expand the subree to be
     * returned to the client.
     */
    boolean inline() default false;

    /**
     * If a string value "key/value" is given, produce a map in more verbose following form:
     * "[{key:KEY1, value:VALUE1}, {key:KEY2, value:VALUE2}, ...]
     * (whereas normally it produces more compact {KEY1:VALUE1, KEY2:VALUE2, ...}
     *
     * <p>
     * So for example, if you say "name/value", you might see something like
     * "[{name:"kohsuke", value:"abc"], ...}
     *
     * <p>
     * The verboose form is useful/necessary when you use complex data structure as a key,
     * or if the string representation of the key can contain letters that are unsafe in some flavours
     * (such as XML, which prohibits a number of characters to be used as tag names.)
     *
     */
    String verboseMap() default "";
}
