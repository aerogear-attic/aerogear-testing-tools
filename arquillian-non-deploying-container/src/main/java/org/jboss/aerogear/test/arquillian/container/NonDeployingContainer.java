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
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.arquillian.spacelift.execution.CountDownWatch;
import org.arquillian.spacelift.execution.ExecutionException;
import org.arquillian.spacelift.execution.Tasks;
import org.jboss.aerogear.test.arquillian.container.check.StatusCheck;
import org.jboss.aerogear.test.arquillian.container.check.StatusCheckTask;
import org.jboss.arquillian.container.spi.ConfigurationException;
import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.spi.client.container.DeploymentException;
import org.jboss.arquillian.container.spi.client.container.LifecycleException;
import org.jboss.arquillian.container.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.container.spi.client.protocol.metadata.HTTPContext;
import org.jboss.arquillian.container.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.arquillian.container.spi.client.protocol.metadata.Servlet;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;
import org.json.JSONObject;

/**
 * Arquillian non-deploying container. With this container you can run your Arquillian tests against already deployed
 * application.
 * 
 * <p>
 * See {@link NonDeployingConfiguration} for required configuration.
 * </p>
 * 
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 * @author <a href="mailto:ecervena@redhat.com">Emil Cervenan</a>
 * 
 */
public class NonDeployingContainer implements DeployableContainer<NonDeployingConfiguration> {

    private static final Logger log = Logger.getLogger(NonDeployingContainer.class.getName());

    @Inject
    @ApplicationScoped
    private InstanceProducer<NonDeployingConfiguration> configurationProducer;

    @Inject
    private Instance<ServiceLoader> serviceLoader;

    @Inject
    private Instance<NonDeployingConfiguration> configuration;

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
        this.configurationProducer.set(configuration);
    }

    @Override
    public void start() throws LifecycleException {
        log.log(Level.INFO, "Assuming that remote deployment is ready at {0}", getAppURI());
    }

    @Override
    public void stop() throws LifecycleException {
        log.log(Level.INFO, "NonDeploying container stops effectively nothing.");
    }

    @Override
    public ProtocolMetaData deploy(Archive<?> archive) throws DeploymentException {

        // get path based on archive name
        String contextPath = getContextPathFromArchive(archive);

        // check for root
        if ("ROOT".equalsIgnoreCase(contextPath)) {
            contextPath = "/";
        }

        // try remapping contextPath from JSON map

        JSONObject contextRootRemap = getContextRootRemapAsJSON();

        if (contextRootRemap != null) {
            String remappedContextPath = contextRootRemap.optString(contextPath);
            if (remappedContextPath.length() != 0) {
                log.log(Level.INFO, "Applying contextPath remap from {0} to {1}", new Object[] {
                    contextPath,
                    remappedContextPath
                });
                contextPath = remappedContextPath;
            }
        }

        log.log(Level.INFO,
            "Pretending deployment of archive {0} to {1}", new Object[] {
                archive.getName(),
                buildUrl(getAppURI(), contextPath)
            });

        URI contextURI = getAppURI();

        HTTPContext context = new HTTPContext("openshift", contextURI.getHost(), getPort(contextURI));
        Servlet servlet = new Servlet("deployment", contextPath);
        context.add(servlet);

        ProtocolMetaData metaData = new ProtocolMetaData();
        metaData.addContext(context);

        log.info("Performing status check");

        StatusCheck statusCheck = getStatusCheck();
        statusCheck.target(contextURI);

        CountDownWatch countDownWatch = new CountDownWatch(configuration.get().getCheckTimeout(), TimeUnit.SECONDS);

        try {
            Tasks.prepare(StatusCheckTask.class)
                .check(statusCheck)
                .execute().until(countDownWatch, StatusCheckTask.statusCheckCondition);
        } catch (ExecutionException ex) {
            throw new RuntimeException(String.format("Unable to satisfy status of '%s' until '%s' seconds.",
                contextURI.toString(), configuration.get().getCheckTimeout()), ex.getCause());
        }

        return metaData;
    }

    private StatusCheck getStatusCheck() {
        String checkClassName = configuration.get().getCheck();

        StatusCheck statusCheck = SecurityActions.newInstance(checkClassName, new Class<?>[] {}, new Object[] {}, StatusCheck.class);

        return statusCheck;
    }

    @Override
    public void undeploy(Archive<?> archive) throws DeploymentException {
        log.log(Level.INFO, "NonDeploying container undeploys effectively nothing.");
    }

    @Override
    public void deploy(Descriptor descriptor) throws DeploymentException {
    }

    @Override
    public void undeploy(Descriptor descriptor) throws DeploymentException {
    }

    private URL buildUrl(URI uri, String contextPath) throws DeploymentException {
        try {
            return new URL(uri.toURL(), contextPath);
        } catch (MalformedURLException e) {
            throw new DeploymentException("Unable to construct URL for deployment", e);
        }
    }

    /**
     * 
     * @param archive archive to get context path from
     * @return context path of archive
     */
    private String getContextPathFromArchive(Archive<?> archive) {
        String contextPath = archive.getName();

        String[] types = { ".war", ".jar", ".ear" };

        if (contextPath != null) {
            for (String type : types) {
                if (contextPath.endsWith(type)) {
                    contextPath = contextPath.substring(0, contextPath.length() - type.length());
                    break;
                }
            }
        }

        return contextPath;
    }

    /**
     * 
     * @return context root remap from configuration as a JSON object
     */
    private JSONObject getContextRootRemapAsJSON() {
        if (configurationProducer.get().getContextRootRemap() == null) {
            return null;
        }
        return new JSONObject(configurationProducer.get().getContextRootRemap());
    }

    /**
     * 
     * @return URI of application form configuration
     */
    private URI getAppURI() {
        URI uri = null;
        try {
            uri = new URI(configurationProducer.get().getBaseURI());
        } catch (URISyntaxException e) {
            throw new ConfigurationException("Parameter \"baseURI\" does not represent a valid URI", e);
        }

        return uri;
    }

    /**
     * When {@code contextURI} does not have a port, {@code defaultPort} will be used. When it is not specified, 80 is used.
     * 
     * @param contextURI uri to get port from
     * @param defaultPort other default port then 80
     * @return port from {@code contextURI}
     */
    private int getPort(URI contextURI, int... defaultPort) {

        final int DEFAULT_PORT = 80;

        int port = contextURI.getPort();

        if (port == -1) {
            if (defaultPort.length == 1) {
                port = defaultPort[0];
            } else {
                port = DEFAULT_PORT;
            }
        }

        return port;
    }

}
