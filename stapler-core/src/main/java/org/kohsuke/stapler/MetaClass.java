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

import net.sf.json.JSONArray;
import org.apache.commons.io.IOUtils;

import javax.servlet.ServletException;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.kohsuke.stapler.bind.JavaScriptMethod;

/**
 * The stapler version of the {@link Class} object,
 * that retains some useful cache about a class and its view.
 *
 * @author Kohsuke Kawaguchi
 * @see WebApp#getMetaClass(Class)
 */
public class MetaClass extends TearOffSupport {
    /**
     * This meta class wraps this class
     */
    public final Class clazz;

    /**
     * {@link MetaClassLoader} that wraps {@code clazz.getClassLoader()}.
     * Null if the class is loaded by the bootstrap classloader.
     */
    public final MetaClassLoader classLoader;

    public final List<Dispatcher> dispatchers = new ArrayList<Dispatcher>();

    /**
     * Base metaclass.
     * Note that <tt>baseClass.clazz==clazz.getSuperClass()</tt>
     */
    public final MetaClass baseClass;

    /**
     * {@link WebApp} that owns this meta class.
     */
    public final WebApp webApp;

    /*package*/ MetaClass(WebApp webApp, Class clazz) {
        this.clazz = clazz;
        this.webApp = webApp;
        this.baseClass = webApp.getMetaClass(clazz.getSuperclass());
        this.classLoader = MetaClassLoader.get(clazz.getClassLoader());

        buildDispatchers(
            new ClassDescriptor(clazz,null/*support wrappers*/));
    }

    /**
     * Build {@link #dispatchers}.
     *
     * <p>
     * This is the meat of URL dispatching. It looks at the class
     * via reflection and figures out what URLs are handled by who. 
     */
    private void buildDispatchers( ClassDescriptor node ) {
        // check action <obj>.do<token>(...)
        for( final Function f : node.methods.prefix("do") ) {
            WebMethod a = f.getAnnotation(WebMethod.class);
            
            String[] names;
            if(a!=null && a.name().length>0)   names=a.name();
            else    names=new String[]{camelize(f.getName().substring(2))}; // 'doFoo' -> 'foo'

            for (String name : names) {
                dispatchers.add(new NameBasedDispatcher(name,0) {
                    public boolean doDispatch(RequestImpl req, ResponseImpl rsp, Object node) throws IllegalAccessException, InvocationTargetException, ServletException, IOException {
                        if(traceable())
                            trace(req,rsp,"-> <%s>.%s(...)",node,f.getName());
                        f.bindAndInvokeAndServeResponse(node, req, rsp);
                        return true;
                    }
                    public String toString() {
                        return f.getQualifiedName()+"(...) for url=/"+name+"/...";
                    }
                });
            }
        }

        // JavaScript proxy method invocations for <obj>js<token>
        // reacts only to a specific content type
        for( final Function f : node.methods.prefix("js") ) {
            String name = camelize(f.getName().substring(2)); // jsXyz -> xyz

            dispatchers.add(new JavaScriptProxyMethodDispatcher(name, f));
        }

        // JavaScript proxy method with @JavaScriptMethod
        // reacts only to a specific content type
        for( final Function f : node.methods.annotated(JavaScriptMethod.class) ) {
            JavaScriptMethod a = f.getAnnotation(JavaScriptMethod.class);

            String[] names;
            if(a!=null && a.name().length>0)   names=a.name();
            else    names=new String[]{f.getName()};

            for (String name : names)
                dispatchers.add(new JavaScriptProxyMethodDispatcher(name,f));
        }

        for (Facet f : webApp.facets)
            f.buildViewDispatchers(this, dispatchers);

        // check action <obj>.doIndex(...)
        for( final Function f : node.methods.name("doIndex") ) {

            dispatchers.add(new Dispatcher() {
                public boolean dispatch(RequestImpl req, ResponseImpl rsp, Object node) throws IllegalAccessException, InvocationTargetException, ServletException, IOException {
                    if(req.tokens.hasMore())
                        return false;   // applicable only when there's no more token

                    if(traceable())
                        trace(req,rsp,"-> <%s>.doIndex(...)",node);

                    f.bindAndInvokeAndServeResponse(node,req,rsp);
                    return true;
                }
                public String toString() {
                    return f.getQualifiedName()+"(StaplerRequest,StaplerResponse) for url=/";
                }
            });
        }

        // check public properties of the form NODE.TOKEN
        for (final Field f : node.fields) {
            dispatchers.add(new NameBasedDispatcher(f.getName()) {
                final String role = getProtectedRole(f);
                public boolean doDispatch(RequestImpl req, ResponseImpl rsp, Object node) throws IOException, ServletException, IllegalAccessException {
                    if(role!=null && !req.isUserInRole(role))
                        throw new IllegalAccessException("Needs to be in role "+role);

                    if(traceable())
                        traceEval(req,rsp,node,f.getName());
                    req.getStapler().invoke(req, rsp, f.get(node));
                    return true;
                }
                public String toString() {
                    return String.format("%1$s.%2$s for url=/%2$s/...",f.getDeclaringClass().getName(),f.getName());
                }
            });
        }

        FunctionList getMethods = node.methods.prefix("get");

        // check public selector methods of the form NODE.getTOKEN()
        for( final Function f : getMethods.signature() ) {
            if(f.getName().length()<=3)
                continue;

            WebMethod a = f.getAnnotation(WebMethod.class);

            String[] names;
            if(a!=null && a.name().length>0)   names=a.name();
            else    names=new String[]{camelize(f.getName().substring(3))}; // 'getFoo' -> 'foo'


            for (String name : names) {
                dispatchers.add(new NameBasedDispatcher(name) {
                    public boolean doDispatch(RequestImpl req, ResponseImpl rsp, Object node) throws IOException, ServletException, IllegalAccessException, InvocationTargetException {
                        if(traceable())
                            traceEval(req,rsp,node,f.getName()+"()");
                        req.getStapler().invoke(req,rsp, f.invoke(req, node));
                        return true;
                    }
                    public String toString() {
                        return String.format("%1$s() for url=/%2$s/...",f.getQualifiedName(),name);
                    }
                });
            }
        }

        // check public selector methods of the form static NODE.getTOKEN(StaplerRequest)
        for( final Function f : getMethods.signature(StaplerRequest.class) ) {
            if(f.getName().length()<=3)
                continue;
            String name = camelize(f.getName().substring(3)); // 'getFoo' -> 'foo'
            dispatchers.add(new NameBasedDispatcher(name) {
                public boolean doDispatch(RequestImpl req, ResponseImpl rsp, Object node) throws IOException, ServletException, IllegalAccessException, InvocationTargetException {
                    if(traceable())
                        traceEval(req,rsp,node,f.getName()+"(...)");
                    req.getStapler().invoke(req,rsp, f.invoke(req, node, req));
                    return true;
                }
                public String toString() {
                    return String.format("%1$s(StaplerRequest) for url=/%2$s/...",f.getQualifiedName(),name);
                }
            });
        }

        // check public selector methods <obj>.get<Token>(String)
        for( final Function f : getMethods.signature(String.class) ) {
            if(f.getName().length()<=3)
                continue;
            String name = camelize(f.getName().substring(3)); // 'getFoo' -> 'foo'
            dispatchers.add(new NameBasedDispatcher(name,1) {
                public boolean doDispatch(RequestImpl req, ResponseImpl rsp, Object node) throws IOException, ServletException, IllegalAccessException, InvocationTargetException {
                    String token = req.tokens.next();
                    if(traceable())
                        traceEval(req,rsp,node,f.getName()+"(\""+token+"\")");
                    req.getStapler().invoke(req,rsp, f.invoke(req,node,token));
                    return true;
                }
                public String toString() {
                    return String.format("%1$s(String) for url=/%2$s/TOKEN/...",f.getQualifiedName(),name);
                }
            });
        }

        // check public selector methods <obj>.get<Token>(int)
        for( final Function f : getMethods.signature(int.class) ) {
            if(f.getName().length()<=3)
                continue;
            String name = camelize(f.getName().substring(3)); // 'getFoo' -> 'foo'
            dispatchers.add(new NameBasedDispatcher(name,1) {
                public boolean doDispatch(RequestImpl req, ResponseImpl rsp, Object node) throws IOException, ServletException, IllegalAccessException, InvocationTargetException {
                    int idx = req.tokens.nextAsInt();
                    if(traceable())
                        traceEval(req,rsp,node,f.getName()+"("+idx+")");
                    req.getStapler().invoke(req,rsp, f.invoke(req,node,idx));
                    return true;
                }
                public String toString() {
                    return String.format("%1$s(int) for url=/%2$s/N/...",f.getQualifiedName(),name);
                }
            });
        }

        if(node.clazz.isArray()) {
            dispatchers.add(new Dispatcher() {
                public boolean dispatch(RequestImpl req, ResponseImpl rsp, Object node) throws IOException, ServletException {
                    if(!req.tokens.hasMore())
                        return false;
                    try {
                        int index = req.tokens.nextAsInt();
                        if(traceable())
                            traceEval(req,rsp,node,"((Object[])",")["+index+"]");
                        req.getStapler().invoke(req,rsp, ((Object[]) node)[index]);
                        return true;
                    } catch (NumberFormatException e) {
                        return false; // try next
                    }
                }
                public String toString() {
                    return "Array look-up for url=/N/...";
                }
            });
        }

        if(List.class.isAssignableFrom(node.clazz)) {
            dispatchers.add(new Dispatcher() {
                public boolean dispatch(RequestImpl req, ResponseImpl rsp, Object node) throws IOException, ServletException {
                    if(!req.tokens.hasMore())
                        return false;
                    try {
                        int index = req.tokens.nextAsInt();
                        if(traceable())
                            traceEval(req,rsp,node,"((List)",").get("+index+")");
                        List list = (List) node;
                        if (0<=index && index<list.size())
                            req.getStapler().invoke(req,rsp, list.get(index));
                        else {
                            if(traceable())
                                trace(req,rsp,"-> IndexOutOfRange [0,%d)",list.size());
                            rsp.sendError(SC_NOT_FOUND);
                        }

                        return true;
                    } catch (NumberFormatException e) {
                        return false; // try next
                    }
                }
                public String toString() {
                    return "List.get(int) look-up for url=/N/...";
                }
            });
        }

        if(Map.class.isAssignableFrom(node.clazz)) {
            dispatchers.add(new Dispatcher() {
                public boolean dispatch(RequestImpl req, ResponseImpl rsp, Object node) throws IOException, ServletException {
                    if(!req.tokens.hasMore())
                        return false;
                    try {
                        String key = req.tokens.peek();
                        if(traceable())
                            traceEval(req,rsp,"((Map)",").get(\""+key+"\")");

                        Object item = ((Map)node).get(key);
                        if(item!=null) {
                            req.tokens.next();
                            req.getStapler().invoke(req,rsp,item);
                            return true;
                        } else {
                            // otherwise just fall through
                            if(traceable())
                                trace(req,rsp,"Map.get(\""+key+"\")==null. Back tracking.");
                            return false;
                        }
                    } catch (NumberFormatException e) {
                        return false; // try next
                    }
                }
                public String toString() {
                    return "Map.get(String) look-up for url=/TOKEN/...";
                }
            });
        }

        // TODO: check if we can route to static resources
        // which directory shall we look up a resource from?

        // check action <obj>.doDynamic(...)
        for( final Function f : node.methods.name("doDynamic") ) {

            dispatchers.add(new Dispatcher() {
                public boolean dispatch(RequestImpl req, ResponseImpl rsp, Object node) throws IllegalAccessException, InvocationTargetException, ServletException, IOException {
                    if(traceable())
                        trace(req,rsp,"-> <%s>.doDynamic(...)",node);
                    f.bindAndInvokeAndServeResponse(node,req,rsp);
                    return true;
                }
                public String toString() {
                    return String.format("%s(StaplerRequest,StaplerResponse) for any URL",f.getQualifiedName());
                }
            });
        }

        // check public selector methods <obj>.getDynamic(<token>,...)
        for( final Function f : getMethods.signatureStartsWith(String.class).name("getDynamic")) {
            dispatchers.add(new Dispatcher() {
                public boolean dispatch(RequestImpl req, ResponseImpl rsp, Object node) throws IllegalAccessException, InvocationTargetException, IOException, ServletException {
                    if(!req.tokens.hasMore())
                        return false;
                    String token = req.tokens.next();
                    if(traceable())
                        traceEval(req,rsp,node,"getDynamic(\""+token+"\",...)");

                    Object target = f.bindAndInvoke(node, req,rsp, token);
                    if(target!=null) {
                        req.getStapler().invoke(req,rsp, target);
                        return true;
                    } else {
                        if(traceable())
                            // indent:    "-> evaluate(
                            trace(req,rsp,"            %s.getDynamic(\"%s\",...)==null. Back tracking.",node,token);
                        req.tokens.prev(); // cancel the next effect
                        return false;
                    }
                }
                public String toString() {
                    return String.format("%s(String,StaplerRequest,StaplerResponse) for url=/TOKEN/...",f.getQualifiedName());
                }
            });
        }
    }

    private String getProtectedRole(Field f) {
        try {
            LimitedTo a = f.getAnnotation(LimitedTo.class);
            return (a!=null)?a.value():null;
        } catch (LinkageError e) {
            return null;    // running in JDK 1.4
        }
    }

    private static String camelize(String name) {
        return Character.toLowerCase(name.charAt(0))+name.substring(1);
    }

    private static class JavaScriptProxyMethodDispatcher extends NameBasedDispatcher {
        private final Function f;

        public JavaScriptProxyMethodDispatcher(String name, Function f) {
            super(name, 0);
            this.f = f;
        }

        public boolean doDispatch(RequestImpl req, ResponseImpl rsp, Object node) throws IllegalAccessException, InvocationTargetException, ServletException, IOException {
            if (!req.isJavaScriptProxyCall())
                return false;

            req.stapler.getWebApp().getCrumbIssuer().validateCrumb(req,req.getHeader("Crumb"));

            if(traceable())
                trace(req,rsp,"-> <%s>.%s(...)",node, f.getName());

            JSONArray jsargs = JSONArray.fromObject(IOUtils.toString(req.getReader()));
            Object[] args = new Object[jsargs.size()];
            Class[] types = f.getParameterTypes();
            Type[] genericTypes = f.getParameterTypes();

            for (int i=0; i<args.length; i++)
                args[i] = req.bindJSON(genericTypes[i],types[i],jsargs.get(i));

            f.bindAndInvokeAndServeResponse(node,req,rsp,args);
            return true;
        }

        public String toString() {
            return f.getQualifiedName()+"(...) for url=/"+name+"/...";
        }
    }

    /**
     * Don't cache anything in memory, so that any change
     * will take effect instantly.
     */
    public static boolean NO_CACHE = false;

    static {
        try {
            NO_CACHE = Boolean.getBoolean("stapler.jelly.noCache");
        } catch (SecurityException e) {
            // ignore.
        }
    }
}
