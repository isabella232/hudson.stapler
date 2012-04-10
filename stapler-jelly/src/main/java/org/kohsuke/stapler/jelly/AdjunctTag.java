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

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.XMLOutput;
import org.kohsuke.stapler.framework.adjunct.AdjunctManager;
import org.kohsuke.stapler.framework.adjunct.AdjunctsInPage;
import org.xml.sax.SAXException;
import org.jvnet.maven.jellydoc.annotation.NoContent;
import org.jvnet.maven.jellydoc.annotation.Required;

import java.io.IOException;
import java.util.List;

/**
 * Writes out links to adjunct CSS and JavaScript, if not done so already.
 * 
 * @author Kohsuke Kawaguchi
 */
@NoContent
public class AdjunctTag extends AbstractStaplerTag {
    private String[] includes;
    private String[] assumes;

    /**
     * Comma-separated adjunct names.
     */
    public void setIncludes(String _includes) {
        includes = parse(_includes);
    }

    /**
     * Comma-separated adjunct names that are externally included in the page
     * and should be suppressed.
     */
    public void setAssumes(String _assumes) {
        assumes = parse(_assumes);
    }

    private String[] parse(String s) {
        String[] r = s.split(",");
        for (int i = 0; i < r.length; i++)
              r[i] = r[i].trim();
        return r;
    }

    public void doTag(XMLOutput out) throws JellyTagException {
        AdjunctManager m = AdjunctManager.get(getServletContext());
        if(m==null)
            throw new IllegalStateException("AdjunctManager is not installed for this application");

        try {
            AdjunctsInPage a = AdjunctsInPage.get();
            if (assumes!=null)
                a.assumeIncluded(assumes);
            if (includes!=null)
                a.generate(out, includes);
        } catch (IOException e) {
            throw new JellyTagException(e);
        } catch (SAXException e) {
            throw new JellyTagException(e);
        }
    }
}
