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

import org.apache.commons.jelly.Script;
import org.apache.commons.jelly.expression.ExpressionFactory;
import org.kohsuke.stapler.Dispatcher;
import org.kohsuke.stapler.Facet;
import org.kohsuke.stapler.MetaClass;
import org.kohsuke.stapler.RequestImpl;
import org.kohsuke.stapler.ResponseImpl;
import org.kohsuke.stapler.TearOffSupport;
import org.kohsuke.MetaInfServices;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletContextEvent;
import java.io.IOException;
import java.net.URL;
import java.util.List;

/**
 * {@link Facet} that adds Jelly as the view.
 * 
 * @author Kohsuke Kawaguchi
 */
@MetaInfServices
public class JellyFacet extends Facet {
    /**
     * Used to invoke Jelly script. Can be replaced to the custom object.
     */
    public volatile ScriptInvoker scriptInvoker = new DefaultScriptInvoker();

    /**
     * Used to load {@link ResourceBundle}s.
     */
    public volatile ResourceBundleFactory resourceBundleFactory = ResourceBundleFactory.INSTANCE;

    public void buildViewDispatchers(final MetaClass owner, List<Dispatcher> dispatchers) {
        dispatchers.add(new Dispatcher() {
            final JellyClassTearOff tearOff = owner.loadTearOff(JellyClassTearOff.class);

            public boolean dispatch(RequestImpl req, ResponseImpl rsp, Object node) throws IOException, ServletException {
                // check Jelly view
                String next = req.tokens.peek();
                if(next==null)  return false;

                // only match the end of the URL
                if (req.tokens.countRemainingTokens()>1)    return false;

                try {
                    Script script = tearOff.findScript(next+".jelly");

                    if(script==null)        return false;   // no Jelly script found

                    req.tokens.next();

                    if (traceable()) {
                        // Null not expected here
                        String src = next+".jelly";
                        if (script instanceof JellyViewScript) {
                            JellyViewScript jvs = (JellyViewScript) script;
                            src = jvs.getName();
                        }
                        trace(req,rsp,"-> %s on <%s>", src, node);
                    }

                    scriptInvoker.invokeScript(req, rsp, script, node);

                    return true;
                } catch (RuntimeException e) {
                    throw e;
                } catch (IOException e) {
                    throw e;
                } catch (Exception e) {
                    throw new ServletException(e);
                }
            }

            public String toString() {
                return "VIEW.jelly for url=/VIEW";
            }
        });
    }

    public RequestDispatcher createRequestDispatcher(RequestImpl request, Class type, Object it, String viewName) throws IOException {
        TearOffSupport mc = request.stapler.getWebApp().getMetaClass(type);
        return mc.loadTearOff(JellyClassTearOff.class).createDispatcher(it,viewName);
    }

    public boolean handleIndexRequest(RequestImpl req, ResponseImpl rsp, Object node, MetaClass nodeMetaClass) throws IOException, ServletException {
        return nodeMetaClass.loadTearOff(JellyClassTearOff.class).serveIndexJelly(req,rsp,node);
    }

    /**
     * Sets the Jelly {@link ExpressionFactory} to be used to parse views.
     *
     * <p>
     * This method should be invoked from your implementation of
     * {@link ServletContextListener#contextInitialized(ServletContextEvent)}.
     *
     * <p>
     * Once views are parsed, they won't be re-parsed just because you called
     * this method to override the expression factory.
     *
     * <p>
     * The primary use case of this feature is to customize the behavior
     * of JEXL evaluation.
     */
    public static void setExpressionFactory( ServletContextEvent event, ExpressionFactory factory ) {
        JellyClassLoaderTearOff.EXPRESSION_FACTORY = factory;
    }

    /**
     * This flag will activate the Jelly evaluation trace.
     * It generates extra comments into HTML, indicating where the fragment was rendered.
     */
    public static boolean TRACE = Boolean.getBoolean("stapler.jelly.trace");
}
