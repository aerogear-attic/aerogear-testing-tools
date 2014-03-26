/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.aerogear.test.arquillian.container.openshift;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.spi.client.container.DeploymentException;
import org.jboss.arquillian.container.spi.client.container.LifecycleException;
import org.jboss.arquillian.container.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.container.spi.client.protocol.metadata.HTTPContext;
import org.jboss.arquillian.container.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.arquillian.container.spi.client.protocol.metadata.Servlet;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;
import org.json.JSONObject;

/**
 * OpenShift fake container. With this container you can run your arquillian tests
 *
 * <p>
 * See {@link OpenShiftContainerConfiguration} for required configuration
 * </p>
 *
 * @author <a href="mailto:ecervena@redhat.com">Emil Cervenan</a>
 *
 */
public class NonDeployingOpenshiftContainer implements DeployableContainer<NonDeployingOpenshiftConfiguration> {

    private static final Logger log = Logger.getLogger(NonDeployingOpenshiftContainer.class.getName());

    private NonDeployingOpenshiftConfiguration configuration;

    @Inject
    private Instance<ServiceLoader> serviceLoader;

    @Override
    public ProtocolDescription getDefaultProtocol() {
        return new ProtocolDescription("Servlet 3.0");
    }

    @Override
    public Class<NonDeployingOpenshiftConfiguration> getConfigurationClass() {
        return NonDeployingOpenshiftConfiguration.class;
    }

    @Override
    public void setup(NonDeployingOpenshiftConfiguration configuration) {
        configuration.validate();
        this.configuration = configuration;
    }

    @Override
    public void start() throws LifecycleException {
        log.log(Level.INFO, "Assuming that OpenShift cartridge is ready at {0}", configuration.getAppUrl());
    }

    @Override
    public void stop() throws LifecycleException {
    }

    @Override
    public void deploy(Descriptor descriptor) throws DeploymentException {
    }

    @Override
    public void undeploy(Descriptor descriptor) throws DeploymentException {
    }

    @Override
    public ProtocolMetaData deploy(Archive<?> archive) throws DeploymentException {

        ProtocolMetaData metaData = new ProtocolMetaData();

        // get path based on archive name
        String contextPath = archive.getName();
        if (contextPath != null
            && (contextPath.endsWith(".war") || contextPath.endsWith(".jar") || contextPath.endsWith(".ear"))) {
            contextPath = contextPath.substring(0, contextPath.length() - 4);
        }

        // check for root
        if ("ROOT".equalsIgnoreCase(contextPath)) {
            contextPath = "/";
        }

        // try remapping contextPath from JSON map
        if (configuration.getContextRootRemap() != null) {
            JSONObject map = new JSONObject(configuration.getContextRootRemap());
            String remappedContextPath = map.optString(contextPath);
            if (remappedContextPath.length() != 0) {
                log.log(Level.INFO, "Applying contextPath remap from {0} to {1}", new Object[] {
                    contextPath,
                    remappedContextPath });
                contextPath = remappedContextPath;
            }
        }

        log.log(Level.INFO,
            "Pretending deployment of archive {0} to {1}", new Object[] {
                archive.getName(), buildUrl(configuration.getAppUrl(), contextPath) });

        HTTPContext context = new HTTPContext("openshift", configuration.getHost(), configuration.getPort());
        Servlet servlet = new Servlet("deployment", contextPath);
        context.add(servlet);
        metaData.addContext(context);

        return metaData;
    }

    @Override
    public void undeploy(Archive<?> archive) throws DeploymentException {
    }

    private URL buildUrl(URL url, String contextPath) throws DeploymentException {
        try {
            return new URL(url, contextPath);
        } catch (MalformedURLException e) {
            throw new DeploymentException("Unable to construct URL for deployment", e);
        }
    }

}