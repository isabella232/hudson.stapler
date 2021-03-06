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

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.jvnet.tiger_types.Lister;
import org.kohsuke.stapler.bind.Bound;
import org.kohsuke.stapler.bind.BoundObjectTable;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * {@link StaplerRequest} implementation.
 * 
 * @author Kohsuke Kawaguchi
 */
public class RequestImpl extends HttpServletRequestWrapper implements StaplerRequest {
    /**
     * Tokenized URLs and consumed tokens.
     * This object is modified by {@link Stapler} as we parse through the URL.
     */
    public final TokenList tokens;
    /**
     * Ancesotr nodes traversed so far.
     * This object is modified by {@link Stapler} as we parse through the URL.
     */
    public final List<AncestorImpl> ancestors;

    private final List<Ancestor> ancestorsView;

    public final Stapler stapler;

    private final String originalRequestURI;

    /**
     * Cached result of {@link #getSubmittedForm()}
     */
    private JSONObject structuredForm;

    /**
     * If the request is "multipart/form-data", parsed result goes here.
     *
     * @see #parseMultipartFormData()
     */
    private Map<String, FileItem> parsedFormData;

    public RequestImpl(Stapler stapler, HttpServletRequest request, List<AncestorImpl> ancestors, TokenList tokens) {
        super(request);
        this.stapler = stapler;
        this.ancestors = ancestors;
        this.ancestorsView = Collections.<Ancestor>unmodifiableList(ancestors);
        this.tokens = tokens;
        this.originalRequestURI = request.getRequestURI();
    }

    public boolean isJavaScriptProxyCall() {
        String ct = getContentType();
        return ct!=null && ct.startsWith("application/x-stapler-method-invocation");
    }

    public BoundObjectTable getBoundObjectTable() {
        return stapler.getWebApp().boundObjectTable;
    }

    public String createJavaScriptProxy(Object toBeExported) {
        return getBoundObjectTable().bind(toBeExported).getProxyScript();
    }

    public Stapler getStapler() {
        return stapler;
    }

    public WebApp getWebApp() {
        return stapler.getWebApp();
    }

    public String getRestOfPath() {
        return tokens.assembleRestOfPath();
    }

    public String getOriginalRestOfPath() {
        return tokens.assembleOriginalRestOfPath();
    }

    public ServletContext getServletContext() {
        return stapler.getServletContext();
    }

    public RequestDispatcher getView(Object it,String viewName) throws IOException {
        return getView(it.getClass(),it,viewName);
    }

    public RequestDispatcher getView(Class clazz, String viewName) throws IOException {
        return getView(clazz,null,viewName);
    }

    public RequestDispatcher getView(Class clazz, Object it, String viewName) throws IOException {
        for( Facet f : stapler.getWebApp().facets ) {
            RequestDispatcher rd = f.createRequestDispatcher(this,clazz,it,viewName);
            if(rd!=null)
                return rd;
        }

        return null;
    }

    public String getRootPath() {
        StringBuffer buf = super.getRequestURL();
        int idx = 0;
        for( int i=0; i<3; i++ )
            idx += buf.substring(idx).indexOf("/")+1;
        buf.setLength(idx-1);
        buf.append(super.getContextPath());
        return buf.toString();
    }

    public String getReferer() {
        return getHeader("Referer");
    }

    public List<Ancestor> getAncestors() {
        return ancestorsView;
    }

    public Ancestor findAncestor(Class type) {
        for( int i = ancestors.size()-1; i>=0; i-- ) {
            AncestorImpl a = ancestors.get(i);
            Object o = a.getObject();
            if (type.isInstance(o))
                return a;
        }

        return null;
    }

    public <T> T findAncestorObject(Class<T> type) {
        Ancestor a = findAncestor(type);
        if(a==null) return null;
        return type.cast(a.getObject());
    }

    public Ancestor findAncestor(Object anc) {
        for( int i = ancestors.size()-1; i>=0; i-- ) {
            AncestorImpl a = ancestors.get(i);
            Object o = a.getObject();
            if (o==anc)
                return a;
        }

        return null;
    }

    public boolean hasParameter(String name) {
        return getParameter(name)!=null;
    }

    public String getOriginalRequestURI() {
        return originalRequestURI;
    }

    public boolean checkIfModified(long lastModified, StaplerResponse rsp) {
        return checkIfModified(lastModified,rsp,0);
    }

    public boolean checkIfModified(long lastModified, StaplerResponse rsp, long expiration) {
        if(lastModified<=0)
            return false;

        // send out Last-Modified, or check If-Modified-Since
        String since = getHeader("If-Modified-Since");
        Stapler.StaplerDateFormat format = Stapler.HTTP_DATE_FORMAT;
        if(since!=null) {
            try {
                long ims = format.parse(since).getTime();
                if(lastModified<ims+1000) {
                    // +1000 because date header is second-precision and Java has milli-second precision
                    rsp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                    return true;
                }
            } catch (NumberFormatException e) {
                // just ignore and serve the content
            } catch (ParseException e) {
                // just ignore and serve the content
            }
        }
        String tm = format.format(new Date(lastModified));
        rsp.setHeader("Last-Modified", tm);
        if(expiration==0) {
            // don't let browsers
            rsp.setHeader("Expires", tm);
        } else {
            // expire in "NOW+expiration" 
            rsp.setHeader("Expires",format.format(new Date(new Date().getTime()+expiration)));
        }
        return false;
    }

    public boolean checkIfModified(Date timestampOfResource, StaplerResponse rsp) {
        return checkIfModified(timestampOfResource.getTime(),rsp);
    }

    public boolean checkIfModified(Calendar timestampOfResource, StaplerResponse rsp) {
        return checkIfModified(timestampOfResource.getTimeInMillis(),rsp);
    }

    public void bindParameters(Object bean) {
        bindParameters(bean,"");
    }

    public void bindParameters(Object bean, String prefix) {
        Enumeration e = getParameterNames();
        while(e.hasMoreElements()) {
            String name = (String)e.nextElement();
            if(name.startsWith(prefix))
                fill(bean,name.substring(prefix.length()), getParameter(name) );
        }
    }

    public <T>
    List<T> bindParametersToList(Class<T> type, String prefix) {
        List<T> r = new ArrayList<T>();

        int len = Integer.MAX_VALUE;

        Enumeration e = getParameterNames();
        while(e.hasMoreElements()) {
            String name = (String)e.nextElement();
            if(name.startsWith(prefix))
                len = Math.min(len,getParameterValues(name).length);
        }

        if(len==Integer.MAX_VALUE)
            return r;   // nothing

        try {
            loadConstructorParamNames(type);
            // use the designated constructor for databinding
            for( int i=0; i<len; i++ )
                r.add(bindParameters(type,prefix,i));
        } catch (NoStaplerConstructorException _) {
            // no designated data binding constructor. use reflection
            try {
                for( int i=0; i<len; i++ ) {
                    T t = type.newInstance();
                    r.add(t);

                    e = getParameterNames();
                    while(e.hasMoreElements()) {
                        String name = (String)e.nextElement();
                        if(name.startsWith(prefix))
                            fill(t, name.substring(prefix.length()), getParameterValues(name)[i] );
                    }
                }
            } catch (InstantiationException x) {
                throw new InstantiationError(x.getMessage());
            } catch (IllegalAccessException x) {
                throw new IllegalAccessError(x.getMessage());
            }
        }

        return r;
    }

    public <T> T bindParameters(Class<T> type, String prefix) {
        return bindParameters(type,prefix,0);
    }

    public <T> T bindParameters(Class<T> type, String prefix, int index) {
        String[] names = loadConstructorParamNames(type);

        // the actual arguments to invoke the constructor with.
        Object[] args = new Object[names.length];

        // constructor
        Constructor<T> c = findConstructor(type, names.length);
        Class[] types = c.getParameterTypes();

        // convert parameters
        for( int i=0; i<names.length; i++ ) {
            String[] values = getParameterValues(prefix + names[i]);
            String param;
            if(values!=null)
                param = values[index];
            else
                param = null;

            Converter converter = Stapler.lookupConverter(types[i]);
            if (converter==null)
                throw new IllegalArgumentException("Unable to convert to "+types[i]);

            args[i] = converter.convert(types[i],param);
        }

        return invokeConstructor(c, args);
    }

    public <T> T bindJSON(Class<T> type, JSONObject src) {
        try {
            if(src.has("stapler-class")) {
                // sub-type is specified in JSON.
                // note that this can come from malicious clients, so we need to make sure we don't have security issues.

                ClassLoader cl = stapler.getWebApp().getClassLoader();
                String className = src.getString("stapler-class");
                try {
                    Class<?> subType = cl.loadClass(className);
                    if(!type.isAssignableFrom(subType))
                        throw new IllegalArgumentException("Specified type "+subType+" is not assignable to the expected "+type);
                    type = (Class)subType; // I'm being lazy here
                } catch (ClassNotFoundException e) {
                    throw new IllegalArgumentException("Class "+className+" is specified in JSON, but no such class found in "+cl,e);
                }
            }

            if (type==JSONObject.class || type==JSON.class) return type.cast(src);

            String[] names = loadConstructorParamNames(type);

            // the actual arguments to invoke the constructor with.
            Object[] args = new Object[names.length];

            // constructor
            Constructor<T> c = findConstructor(type, names.length);
            Class[] types = c.getParameterTypes();
            Type[] genTypes = c.getGenericParameterTypes();

            // convert parameters
            for( int i=0; i<names.length; i++ ) {
                try {
                    args[i] = bindJSON(genTypes[i],types[i],src.get(names[i]));
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Failed to convert the "+names[i]+" parameter of the constructor "+c,e);
                }
            }

            return invokeConstructor(c, args);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Failed to instantiate "+type+" from "+src,e);
        }
    }

    public Object bindJSON(Type type, Class erasure, Object json) {
        return new TypePair(type,erasure).convertJSON(json);
    }

    public void bindJSON(Object bean, JSONObject src) {
        try {
            for( String key : (Set<String>)src.keySet() ) {
                TypePair type = getPropertyType(bean, key);
                if(type==null)
                    continue;

                fill(bean,key, type.convertJSON(src.get(key)));
            }
        } catch (IllegalAccessException e) {
            IllegalAccessError x = new IllegalAccessError(e.getMessage());
            x.initCause(e);
            throw x;
        } catch (InvocationTargetException x) {
            Throwable e = x.getTargetException();
            if(e instanceof RuntimeException)
                throw (RuntimeException)e;
            if(e instanceof Error)
                throw (Error)e;
            throw new RuntimeException(x);
        }
    }

    public <T> List<T> bindJSONToList(Class<T> type, Object src) {
        ArrayList<T> r = new ArrayList<T>();
        if (src instanceof JSONObject) {
            JSONObject j = (JSONObject) src;
            r.add(bindJSON(type,j));
        }
        if (src instanceof JSONArray) {
            JSONArray a = (JSONArray) src;
            for (Object o : a) {
                if (o instanceof JSONObject) {
                    JSONObject j = (JSONObject) o;
                    r.add(bindJSON(type,j));
                }
            }
        }
        return r;
    }


    private <T> T invokeConstructor(Constructor<T> c, Object[] args) {
        try {
            return c.newInstance(args);
        } catch (InstantiationException e) {
            InstantiationError x = new InstantiationError(e.getMessage());
            x.initCause(e);
            throw x;
        } catch (IllegalAccessException e) {
            IllegalAccessError x = new IllegalAccessError(e.getMessage());
            x.initCause(e);
            throw x;
        } catch (InvocationTargetException e) {
            Throwable x = e.getTargetException();
            if(x instanceof Error)
                throw (Error)x;
            if(x instanceof RuntimeException)
                throw (RuntimeException)x;
            throw new IllegalArgumentException(x);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Failed to invoke "+c+" with "+ Arrays.asList(args),e);
        }
    }

    private <T> Constructor<T> findConstructor(Class<T> type, int length) {
        Constructor<?>[] ctrs = type.getConstructors();
        // one with DataBoundConstructor is the most reliable
        for (Constructor c : ctrs) {
            if(c.getAnnotation(DataBoundConstructor.class)!=null) {
                if(c.getParameterTypes().length!=length)
                    throw new IllegalArgumentException(c+" has @DataBoundConstructor but it doesn't match with your .stapler file. Try clean rebuild");
                return c;
            }
        }
        // if not, maybe this was from @stapler-constructor,
        // so look for the constructor with the expected argument length.
        // this is not very reliable.
        for (Constructor c : ctrs) {
            if(c.getParameterTypes().length==length)
                return c;
        }
        throw new IllegalArgumentException(type+" does not have a constructor with "+length+" arguments");
    }

    /**
     * Determines the constructor parameter names.
     *
     * <p>
     * First, try to load names from the debug information. Otherwise
     * if there's the .stapler file, load it as a property file and determines the constructor parameter names.
     * Otherwise, look for {@link CapturedParameterNames} annotation.
     */
    private String[] loadConstructorParamNames(Class<?> type) {
        Constructor<?>[] ctrs = type.getConstructors();
        // which constructor was data bound?
        Constructor<?> dbc = null;
        for (Constructor<?> c : ctrs) {
            if (c.getAnnotation(DataBoundConstructor.class) != null) {
                dbc = c;
                break;
            }
        }

        if (dbc==null)
            throw new NoStaplerConstructorException("There's no @DataBoundConstructor on any constructor of " + type);

        String[] names = ClassDescriptor.loadParameterNames(dbc);
        if (names.length==dbc.getParameterTypes().length)
            return names;

        String resourceName = type.getName().replace('.', '/').replace('$','/') + ".stapler";
        ClassLoader cl = type.getClassLoader();
        if(cl==null)
            throw new NoStaplerConstructorException(type+" is a built-in type");
        InputStream s = cl.getResourceAsStream(resourceName);
        if (s != null) {// load the property file and figure out parameter names
            try {
                Properties p = new Properties();
                p.load(s);
                s.close();

                String v = p.getProperty("constructor");
                if (v.length() == 0) return new String[0];
                return v.split(",");
            } catch (IOException e) {
                throw new IllegalArgumentException("Unable to load " + resourceName, e);
            }
        }

        // no debug info and no stapler file
        throw new NoStaplerConstructorException(
                "Unable to find " + resourceName + ". " +
                        "Run 'mvn clean compile' once to run the annotation processor.");
    }

    private static void fill(Object bean, String key, Object value) {
        StringTokenizer tokens = new StringTokenizer(key);
        while(tokens.hasMoreTokens()) {
            String token = tokens.nextToken();
            boolean last = !tokens.hasMoreTokens();  // is this the last token?

            try {
                if(last) {
                    copyProperty(bean,token,value);
                } else {
                    bean = BeanUtils.getProperty(bean,token);
                }
            } catch (IllegalAccessException x) {
                throw new IllegalAccessError(x.getMessage());
            } catch (InvocationTargetException x) {
                Throwable e = x.getTargetException();
                if(e instanceof RuntimeException)
                    throw (RuntimeException)e;
                if(e instanceof Error)
                    throw (Error)e;
                throw new RuntimeException(x);
            } catch (NoSuchMethodException e) {
                // ignore if there's no such property
            }
        }
    }

    /**
     * Information about the type.
     */
    private final class TypePair {
        final Type genericType;
        final Class type;

        TypePair(Type genericType, Class type) {
            this.genericType = genericType;
            this.type = type;
        }

        TypePair(Field f) {
            this(f.getGenericType(),f.getType());
        }

        /**
         * Converts the given JSON object (either {@link JSONObject}, {@link JSONArray}, or other primitive types
         * in JSON, to the type represented by the 'this' object.
         */
        public Object convertJSON(Object o) {
            if(o==null) {
                // this method returns null if the type is not primitive, which works.
                return ReflectionUtils.getVmDefaultValueFor(type);
            }

            if (type==JSONArray.class) {
                if (o instanceof JSONArray) return o;

                JSONArray a = new JSONArray();
                a.add(o);
                return a;
            }

            Lister l = Lister.create(type,genericType);

            if (o instanceof JSONObject) {
                JSONObject j = (JSONObject) o;

                if (j.isNullObject())   // another flavor of null. json-lib sucks.
                    return ReflectionUtils.getVmDefaultValueFor(type);

                if(l==null) {
                    // single value conversion
                    return bindJSON(type,j);
                } else {
                    if(j.has("stapler-class-bag")) {
                        // this object is a hash from class names to their parameters
                        // build them into a collection via Lister

                        ClassLoader cl = stapler.getWebApp().getClassLoader();
                        for (Map.Entry<String,Object> e : (Set<Map.Entry<String,Object>>)j.entrySet()) {
                            Object v = e.getValue();

                            String className = e.getKey().replace('-','.'); // decode JSON-safe class name escaping
                            try {
                                Class<?> itemType = cl.loadClass(className);
                                if (v instanceof JSONObject) {
                                    l.add(bindJSON(itemType, (JSONObject) v));
                                }
                                if (v instanceof JSONArray) {
                                    for(Object i : bindJSONToList(itemType, (JSONArray) v))
                                        l.add(i);
                                }
                            } catch (ClassNotFoundException e1) {
                                // ignore unrecognized element
                            }
                        }
                    } else if (Enum.class.isAssignableFrom(l.itemType)) {
                        // this is a hash of element names as enum constant names
                        for (Map.Entry<String,Object> e : (Set<Map.Entry<String,Object>>)j.entrySet()) {
                            Object v = e.getValue();
                            if (v==null || (v instanceof Boolean && !(Boolean)v))
                                continue;       // skip if the value is null or false

                            l.add(Enum.valueOf(l.itemType,e.getKey()));
                        }
                    } else {
                        // only one value given to the collection
                        l.add(new TypePair(l.itemGenericType,l.itemType).convertJSON(j));
                    }
                    return l.toCollection();
                }
            }
            if (o instanceof JSONArray) {
                JSONArray a = (JSONArray) o;
                TypePair itemType = new TypePair(l.itemGenericType,l.itemType);
                for (Object item : a)
                    l.add(itemType.convertJSON(item));
                return l.toCollection();
            }

            if(Enum.class.isAssignableFrom(type))
                return Enum.valueOf(type,o.toString());

            if (l==null) {// single value conversion
                Converter converter = Stapler.lookupConverter(type);
                if (converter==null)
                    throw new IllegalArgumentException("Unable to convert to "+type);

                return converter.convert(type,o);
            } else {// single value in a collection
                Converter converter = Stapler.lookupConverter(l.itemType);
                if (converter==null)
                    throw new IllegalArgumentException("Unable to convert to "+type);

                l.add(converter.convert(type,o));
                return l.toCollection();
            }
        }
    }

    /**
     * Gets the type of the field/property designate by the given name.
     */
    private TypePair getPropertyType(Object bean, String name) throws IllegalAccessException, InvocationTargetException {
        try {
            PropertyDescriptor propDescriptor = PropertyUtils.getPropertyDescriptor(bean, name);
            if(propDescriptor!=null) {
                Method m = propDescriptor.getWriteMethod();
                if(m!=null)
                    return new TypePair(m.getGenericParameterTypes()[0], m.getParameterTypes()[0]);
            }
        } catch (NoSuchMethodException e) {
            // no such property
        }

        // try a field
        try {
            return new TypePair(bean.getClass().getField(name));
        } catch (NoSuchFieldException e) {
            // no such field
        }

        return null;
    }

    /**
     * Sets the property/field value of the given name, by performing a value type conversion if necessary.
     */
    private static void copyProperty(Object bean, String name, Object value) throws IllegalAccessException, InvocationTargetException {
        PropertyDescriptor propDescriptor;
        try {
            propDescriptor =
                PropertyUtils.getPropertyDescriptor(bean, name);
        } catch (NoSuchMethodException e) {
            propDescriptor = null;
        }
        if ((propDescriptor != null) &&
            (propDescriptor.getWriteMethod() == null)) {
            propDescriptor = null;
        }
        if (propDescriptor != null) {
            Converter converter = Stapler.lookupConverter(propDescriptor.getPropertyType());
            if (converter != null)
                value = converter.convert(propDescriptor.getPropertyType(), value);
            try {
                PropertyUtils.setSimpleProperty(bean, name, value);
            } catch (NoSuchMethodException e) {
                throw new NoSuchMethodError(e.getMessage());
            }
            return;
        }

        // try a field
        try {
            Field field = bean.getClass().getField(name);
            Converter converter = ConvertUtils.lookup(field.getType());
            if (converter != null)
                value = converter.convert(field.getType(), value);
            field.set(bean,value);
        } catch (NoSuchFieldException e) {
            // no such field
        }
    }

    private void parseMultipartFormData() throws ServletException {
        if(parsedFormData!=null)    return;

        parsedFormData = new HashMap<String,FileItem>();
        ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
        try {
            for( FileItem fi : (List<FileItem>)upload.parseRequest(this) )
                parsedFormData.put(fi.getFieldName(),fi);
        } catch (FileUploadException e) {
            throw new ServletException(e);
        }
    }

    public JSONObject getSubmittedForm() throws ServletException {
        if(structuredForm==null) {
            String ct = getContentType();
            String p = null;
            boolean isSubmission; // for error diagnosis, if something is submitted, set to true

            if(ct!=null && ct.startsWith("multipart/")) {
                isSubmission=true;
                parseMultipartFormData();
                FileItem item = parsedFormData.get("json");
                if(item!=null)
                    p = item.getString();
            } else {
                p = getParameter("json");
                isSubmission = !getParameterMap().isEmpty();
            }
            
            if(p==null) {
                // no data submitted
                try {
                    StaplerResponse rsp = Stapler.getCurrentResponse();
                    if(isSubmission)
                        rsp.sendError(SC_BAD_REQUEST,"This page expects a form submission");
                    else
                        rsp.sendError(SC_BAD_REQUEST,"Nothing is submitted");
                    rsp.getWriter().close();
                    throw new Error("This page expects a form submission");
                } catch (IOException e) {
                    throw new Error(e);
                }
            }
            structuredForm = JSONObject.fromObject(p);
        }
        return structuredForm;
    }

    public FileItem getFileItem(String name) throws ServletException, IOException {
        parseMultipartFormData();
        if(parsedFormData==null)    return null;
        FileItem item = parsedFormData.get(name);
        if(item==null || item.isFormField())    return null;
        return item;
    }
}
