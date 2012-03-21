/*******************************************************************************
 *
 * Copyright (c) 2012 Oracle Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *
 *    Winston Prakash
 *     
 *******************************************************************************/

package org.kohsuke.stapler;

import java.util.List;
import javax.servlet.http.HttpServletRequest;

/**
 * @see org.eclipse.hudson.stapler.RequestImpl
 * @deprecated  As of release 3.0.0, replaced by {@link org.eclipse.hudson.stapler.RequestImpl} 
 */
@Deprecated
public class RequestImpl extends org.eclipse.hudson.stapler.RequestImpl implements StaplerRequest{
    public RequestImpl(Stapler stapler, HttpServletRequest request, List<org.eclipse.hudson.stapler.AncestorImpl> ancestors, org.eclipse.hudson.stapler.TokenList tokens) {
        super((org.eclipse.hudson.stapler.Stapler)stapler, request, ancestors, tokens);
    }
}
