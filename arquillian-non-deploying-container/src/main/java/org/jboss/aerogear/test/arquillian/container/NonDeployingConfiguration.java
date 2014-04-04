/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.aerogear.test.arquillian.container;

import java.net.URI;
import java.net.URISyntaxException;

import org.jboss.arquillian.container.spi.ConfigurationException;
import org.jboss.arquillian.container.spi.client.container.ContainerConfiguration;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Configuration of a non deploying container.
 *
 * @author <a href="kpiwko@redhat.com">Karel Piwko</a>
 *
 */
public class NonDeployingConfiguration implements ContainerConfiguration {

    private String baseURI;

    private String contextRootRemap;

    private URI _baseURI;

    private JSONObject _contextRootRemap;

    @Override
    public void validate() throws ConfigurationException {

        if (baseURI == null) {
            throw new ConfigurationException("Parameter \"baseURI\" must not be null nor empty");
        }
        else {
            try {
                this._baseURI = new URI(baseURI);
            } catch (URISyntaxException e) {
                throw new ConfigurationException("Parameter \"baseURI\" does not represent a valid URI", e);
            }
        }

        if (contextRootRemap != null) {
            try {
                this._contextRootRemap = new JSONObject(contextRootRemap);
            } catch (JSONException e) {
                throw new ConfigurationException("Parameter \"contextRootRemap\" does not represent a valid JSON object", e);
            }
        }
    }

    public URI getAppUri() {
        return _baseURI;
    }

    public JSONObject getContextRootRemap() {
        return _contextRootRemap;
    }
}
