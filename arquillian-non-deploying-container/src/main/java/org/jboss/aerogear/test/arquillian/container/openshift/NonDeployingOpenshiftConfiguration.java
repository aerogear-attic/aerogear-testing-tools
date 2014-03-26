package org.jboss.aerogear.test.arquillian.container.openshift;

import java.net.MalformedURLException;
import java.net.URL;

import org.jboss.arquillian.container.spi.ConfigurationException;
import org.jboss.arquillian.container.spi.client.container.ContainerConfiguration;
import org.json.JSONException;
import org.json.JSONObject;

public class NonDeployingOpenshiftConfiguration implements ContainerConfiguration {

    private String contextRootRemap;

    private String app;

    private String namespace;

    private String protocol = "http";

    private String host;

    private int port = 80;

    @Override
    public void validate() throws ConfigurationException {

        if (namespace == null || namespace.length() == 0) {
            throw new ConfigurationException("Parameter \"namespace\" must not be null nor empty");
        }

        if (app == null || app.length() == 0) {
            throw new ConfigurationException("Parameter \"app\" must not be null nor empty");
        }

        if (host == null) {
            host = app + "-" + namespace + ".rhcloud.com";
        }

        if (contextRootRemap != null) {
            try {
                new JSONObject(contextRootRemap);
            } catch (JSONException e) {
                throw new ConfigurationException("Parameter \"contextRootRemap\" does not represent a valid JSON object", e);
            }
        }

        try {
            getAppUrl();
        } catch (IllegalStateException e) {
            throw new ConfigurationException(e.getMessage(), e.getCause().getCause());
        }
    }

    public URL getAppUrl() throws IllegalStateException {
        try {
            return new URL(protocol, host, port, "/");
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Combination of host, app, namespace, port and contextRoot does not form a valid URL",
                e);
        }
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getContextRootRemap() {
        return contextRootRemap;
    }

    public void setContextRootRemap(String contextRootRemap) {
        this.contextRootRemap = contextRootRemap;
    }

}
