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
import java.util.logging.Logger;

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

    private static final Logger logger = Logger.getLogger(NonDeployingConfiguration.class.getName());

    private static final String DEFAULT_STATUS_CHECK_CLASS_NAME = "org.jboss.aerogear.test.arquillian.container.check.impl.HTTPCodeStatusCheck";

    private String baseURI;

    private String contextRootRemap;

    private String check = DEFAULT_STATUS_CHECK_CLASS_NAME;

    private String checkTimeout = "300";

    public String getBaseURI() {
        return baseURI;
    }

    public void setBaseURI(String baseURI) {
        this.baseURI = baseURI;
    }

    public void setContextRootRemap(String contextRootRemap) {
        this.contextRootRemap = contextRootRemap;
    }

    public String getContextRootRemap() {
        return contextRootRemap;
    }

    public void setCheck(String check) {
        this.check = check;
    }

    public String getCheck() {
        return check;
    }

    public int getCheckTimeout() {
        return Integer.parseInt(checkTimeout);
    }

    public void setCheckTimeout(String checkTimeout) {
        this.checkTimeout = checkTimeout;
    }

    @Override
    public void validate() throws ConfigurationException {

        if (getBaseURI() == null) {
            throw new ConfigurationException("Parameter \"baseURI\" must not be null nor empty");
        }
        else {
            try {
                new URI(getBaseURI());
            } catch (URISyntaxException e) {
                throw new ConfigurationException("Parameter \"baseURI\" does not represent a valid URI", e);
            }
        }

        if (getContextRootRemap() != null) {
            try {
                new JSONObject(getContextRootRemap());
            } catch (JSONException e) {
                throw new ConfigurationException("Parameter \"contextRootRemap\" does not represent a valid JSON object", e);
            }
        }

        if (getCheck() == null || getCheck().isEmpty()) {
            throw new ConfigurationException("Unable to use check which class name is null object or an empty string!");
        }

        try {
            int timeout = getCheckTimeout();
            if (timeout <= 0) {
                throw new ConfigurationException("Timeout check can not be lower then 0.");
            }
        } catch (NumberFormatException ex) {
            throw new ConfigurationException(String.format("Check timeout value you set is not a number: '%s'.", checkTimeout));
        }

        logger.info(this.toString());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("\nbaseURI:\t\t").append(getBaseURI())
            .append("\ncontextRootRemap:\t").append(getContextRootRemap())
            .append("\ncheck:\t\t\t").append(getCheck())
            .append("\ncheck timeout:\t\t").append(getCheckTimeout());

        return sb.toString();
    }

}
