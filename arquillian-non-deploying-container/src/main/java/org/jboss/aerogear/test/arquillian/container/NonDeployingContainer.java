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

import java.net.MalformedURLException;
import java.net.URI;
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

/**
 * Arquillian non-deploying container. With this container you can run your Arquillian tests
 * against already deployed application.
 *
 * <p>
 * See {@link NonDeployingConfiguration} for required configuration
 * </p>
 *
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 * @author <a href="mailto:ecervena@redhat.com">Emil Cervenan</a>
 *
 */
public class NonDeployingContainer implements DeployableContainer<NonDeployingConfiguration> {

    private static final Logger log = Logger.getLogger(NonDeployingContainer.class.getName());

    private NonDeployingConfiguration configuration;

    @Inject
    private Instance<ServiceLoader> serviceLoader;

    @Override
    public ProtocolDescription getDefaultProtocol() {
        return new ProtocolDescription("Servlet 3.0");
    }

    @Override
    public Class<NonDeployingConfiguration> getConfigurationClass() {
        return NonDeployingConfiguration.class;
    }

    @Override
    public void setup(NonDeployingConfiguration configuration) {
        configuration.validate();
        this.configuration = configuration;
    }

    @Override
    public void start() throws LifecycleException {
        log.log(Level.INFO, "Assuming that remote deployment is ready at {0}", configuration.getAppUri());
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
            String remappedContextPath = configuration.getContextRootRemap().optString(contextPath);
            if (remappedContextPath.length() != 0) {
                log.log(Level.INFO, "Applying contextPath remap from {0} to {1}", new Object[] {
                    contextPath,
                    remappedContextPath });
                contextPath = remappedContextPath;
            }
        }

        log.log(Level.INFO,
            "Pretending deployment of archive {0} to {1}", new Object[] {
                archive.getName(), buildUrl(configuration.getAppUri(), contextPath) });

        HTTPContext context = new HTTPContext("openshift", configuration.getAppUri().getHost(), configuration.getAppUri()
            .getPort());
        Servlet servlet = new Servlet("deployment", contextPath);
        context.add(servlet);
        metaData.addContext(context);

        return metaData;
    }

    @Override
    public void undeploy(Archive<?> archive) throws DeploymentException {
    }

    private URL buildUrl(URI uri, String contextPath) throws DeploymentException {
        try {
            return new URL(uri.toURL(), contextPath);
        } catch (MalformedURLException e) {
            throw new DeploymentException("Unable to construct URL for deployment", e);
        }
    }

}