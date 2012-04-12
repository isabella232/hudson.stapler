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

import org.apache.commons.jelly.expression.Expression;
import org.apache.commons.jelly.expression.ExpressionSupport;
import org.apache.commons.jelly.JellyException;
import org.apache.commons.jelly.JellyContext;
import org.jvnet.localizer.LocaleProvider;
import org.kohsuke.stapler.Stapler;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Arrays;

/**
 * Expression of the form "%messageName(arg1,arg2,...)" that represents
 * internationalized text.
 *
 * <p>
 * The "(arg1,...)" portion is optional and can be ommitted. Each argument
 * is assumed to be a parenthesis-balanced expression and passed to
 * {@link JellyClassLoaderTearOff#EXPRESSION_FACTORY} to be parsed.
 *
 * <p>
 * The message resource is loaded from files like "xyz.properties" and
 * "xyz_ja.properties" when the expression is placed in "xyz.jelly". 
 *
 *
 * @author Kohsuke Kawaguchi
 */
public class InternationalizedStringExpression extends ExpressionSupport {
    public final ResourceBundle resourceBundle;
    private final Expression[] arguments;
    public final String key;
    public final String expressionText;

    public InternationalizedStringExpression(ResourceBundle resourceBundle, String text) throws JellyException {
        this.resourceBundle = resourceBundle;
        this.expressionText = text;
        if(!text.startsWith("%"))
            throw new JellyException(text+" doesn't start with %");
        text = text.substring(1);

        int idx = text.indexOf('(');
        if(idx<0) {
            // no arguments
            key = text;
            arguments = EMPTY_ARGUMENTS;
            return;
        }

        List<Expression> args = new ArrayList<Expression>();
        key = text.substring(0,idx);
        text = text.substring(idx+1);   // at this point text="arg,arg)"
        while(text.length()>0) {
            String token = tokenize(text);
            args.add(JellyClassLoaderTearOff.EXPRESSION_FACTORY.createExpression(token));
            text = text.substring(token.length()+1);
        }

        this.arguments = args.toArray(new Expression[args.size()]);
    }
    
    public List<Expression> getArguments() {
        return Collections.unmodifiableList(Arrays.asList(arguments));
    }

    /**
     * Takes a string like "arg)" or "arg,arg,...)", then
     * find "arg" and returns it.
     *
     * Note: this code is also copied into idea-stapler-plugin,
     * so don't forget to update that when this code changes.
     */
    private String tokenize(String text) throws JellyException {
        int parenthesis=0;
        for(int idx=0;idx<text.length();idx++) {
            char ch = text.charAt(idx);
            switch (ch) {
            case ',':
                if(parenthesis==0)
                    return text.substring(0,idx);
                break;
            case '(':
            case '{':
            case '[':
                parenthesis++;
                break;
            case ')':
                if(parenthesis==0)
                    return text.substring(0,idx);
                // fall through
            case '}':
            case ']':
                parenthesis--;
                break;
            case '"':
            case '\'':
                // skip strings
                idx = text.indexOf(ch,idx+1);
                break;
            }
        }
        throw new JellyException(expressionText+" is missing ')' at the end");
    }

    public String getExpressionText() {
        return expressionText;
    }

    public Object evaluate(JellyContext jellyContext) {
        return format(evaluateArguments(jellyContext));
    }

    Object[] evaluateArguments(JellyContext jellyContext) {
        Object[] args = new Object[arguments.length];
        for (int i = 0; i < args.length; i++)
            args[i] = arguments[i].evaluate(jellyContext);
        return args;
    }

    String format(Object[] args) {
        // notify the listener if set
        InternationalizedStringExpressionListener listener = (InternationalizedStringExpressionListener)Stapler.getCurrentRequest().getAttribute(LISTENER_NAME);
        if(listener!=null)
            listener.onUsed(this,args);

        return resourceBundle.format(LocaleProvider.getLocale(),key,args);
    }

    /**
     * Wraps value to indicate it contains raw HTML that should not be escaped.
     */
    public Object rawHtml(Object value) {
        return value != null ? new RawHtml(value) : null;
    }

    static final class RawHtml {
        final Object value;
 
        RawHtml(Object value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }
    
    Expression escape() {
        return new ExpressionSupport() {
            public String getExpressionText() {
                return expressionText;
            }
    
            public Object evaluate(JellyContext context) {
                Object[] args = evaluateArguments(context);
                for (int i = 0; i < args.length; i++) {
                    args[i] = escapeArgument(args[i]);
                }
                return format(args);
            }
        };
    }

    static Object escapeArgument(Object arg) {
        if (arg instanceof RawHtml) {
            return ((RawHtml)arg).value; // no escaping wanted
        }
        if ( arg == null || arg instanceof Number || arg instanceof Date || arg instanceof Calendar ) {
            return arg; // no escaping required
        }
        final String text = arg.toString();
        StringBuilder buf = null; // create on-demand
        for (int i = 0, len = text.length(); i < len; i++) {
            final char c = text.charAt(i);
            String replacement = null;
            if (c == '&') {
                replacement = "&amp;";
            } else if (c == '<') {
                replacement = "&lt;";
            } else if (buf != null) {
                buf.append(c); // maintain buffer
            }
            if (replacement != null) {
                if (buf == null) {
                    // only create translation buffer when we actually need it
                    buf = new StringBuilder(len+8).append(text.substring(0, i));
                }
                buf.append(replacement);
            }
        }
        return buf != null ? buf : text;
    }

    private static final Expression[] EMPTY_ARGUMENTS = new Expression[0];
    private static final String LISTENER_NAME = InternationalizedStringExpressionListener.class.getName();
}
