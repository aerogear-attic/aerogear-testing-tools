/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.arquillian.container.spi.client.protocol.metadata;

import java.net.URI;

import org.jboss.arquillian.container.spi.client.protocol.metadata.Servlet;

/**
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class HTTPSServlet extends Servlet {

    static final String HTTP_SECURE_SCHEME = "https://";

    static final String HTTP_SCHEME_PART = "http";

    public HTTPSServlet(String name, String contextRoot) {
        super(name, contextRoot);
    }

    public URI getBaseURI()
    {
        URI uri = null;

        URI baseURI = super.getBaseURI();

        if (baseURI.getScheme().equals(HTTP_SCHEME_PART)) {
            try {
                uri = new URI(baseURI.toString().replace(HTTP_SCHEME, HTTP_SECURE_SCHEME));
            } catch (Exception ex) {
                throw new IllegalStateException("Unable to convert URI address to address with HTTPS scheme: " + baseURI.toString());
            }
        } else {
            uri = baseURI;
        }

        return uri;
    }

}
