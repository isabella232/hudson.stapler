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

import javax.servlet.http.HttpSession;
import java.util.UUID;

/**
 * Generates a nonce value that allows us to protect against cross-site request forgery (CSRF) attacks.
 *
 * <p>
 * We send this with each JavaScript proxy and verify them when we receive a request.
 *
 * @author Kohsuke Kawaguchi
 * @see WebApp#getCrumbIssuer()
 * @see WebApp#setCrumbIssuer(CrumbIssuer)  
 */
public abstract class CrumbIssuer {
    /**
     * Issues a crumb for the given request.
     */
    public abstract String issueCrumb(StaplerRequest request);

    public final String issueCrumb() {
        return issueCrumb(Stapler.getCurrentRequest());
    }

    /**
     * Validates a crumb that was submitted along with the request.
     *
     * @param request
     *      The request that submitted the crumb
     * @param submittedCrumb
     *      The submitted crumb value to be validated.
     *
     * @throws Exception
     *      If the crumb doesn't match and the request processing should abort.
     */
    public void validateCrumb(StaplerRequest request, String submittedCrumb) {
        if (!issueCrumb(request).equals(submittedCrumb)) {
            throw new SecurityException("Request failed to pass the crumb test");
        }
    }

    /**
     * Default crumb issuer.
     */
    public static final CrumbIssuer DEFAULT = new CrumbIssuer() {
        @Override
        public String issueCrumb(StaplerRequest request) {
            HttpSession s = request.getSession();
            String v = (String)s.getAttribute(ATTRIBUTE_NAME);
            if (v!=null)    return v;
            v = UUID.randomUUID().toString();
            s.setAttribute(ATTRIBUTE_NAME,v);
            return v;
        }
    };
    private static final String ATTRIBUTE_NAME = CrumbIssuer.class.getName();
}
