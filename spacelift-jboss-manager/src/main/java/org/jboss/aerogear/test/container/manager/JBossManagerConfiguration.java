/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
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
package org.jboss.aerogear.test.container.manager;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.jboss.aerogear.test.container.manager.configuration.ContainerType;
import org.jboss.aerogear.test.container.manager.configuration.ContainerJavaOptsConfiguration;

/**
 *
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @author <a href="mailto:aslak@redhat.com">Stefan Miklosovic</a>
 */
public class JBossManagerConfiguration {

    private static final String[] DEFAULT_DOMAIN_SERVERS = new String[] { "server-one", "server-two" };

    private File javaHome = null;

    private File jbossHome = null;

    private String javaOpts = null;

    private String processControllerJavaOpts = null;

    private String hostControllerJavaOpts = null;

    private String serverJavaOpts = "";

    private int startupTimeoutInSeconds = 120;

    private boolean outputToConsole = true;

    private String user;

    private String password;

    private ContainerType containerType = ContainerType.WILDFLY;

    private String domainConfig = "domain.xml";

    private String hostConfig = "host.xml";

    private String standaloneConfig = "standalone.xml";

    // domain specific

    private boolean domain;

    private String serverGroup = System.getProperty("server.group", "main-server-group");

    private long domainStartTimeout = 60; // seconds

    private List<String> domainServers = Arrays.asList(DEFAULT_DOMAIN_SERVERS);

    private String domainMasterHostName = "master";

    public JBossManagerConfiguration() {
        // get either from system property or env variable
        String javaHome = System.getProperty("java.home");
        if (javaHome == null || javaHome.length() == 0) {
            javaHome = System.getenv("JAVA_HOME");
        }
        if (javaHome != null) {
            this.javaHome = new File(javaHome);
        }

        // get either from system property or env variable
        String jbossHome = System.getProperty("jboss.home");
        if (jbossHome == null || jbossHome.length() == 0) {
            jbossHome = System.getenv("JBOSS_HOME");
        }

        if (jbossHome != null) {
            this.jbossHome = new File(jbossHome);
        }

    }

    /**
     *
     * @throws IllegalStateException if {@code jbossHome} or {@code javaHome} are not valid directories
     */
    public void validate() throws IllegalStateException {

        if (jbossHome != null) {
            if (!jbossHome.exists() || !jbossHome.isDirectory()) {
                throw new IllegalStateException("jbossHome '" + getJBossHome() + "' must exist!");
            }
        } else {
            throw new IllegalStateException("Could not determine the value of JBoss home directory.");
        }

        if (!new File(getJBossBaseDir()).exists()) {
            throw new IllegalStateException("Could not determine the value of JBoss base directory.");
        }

        if (!new File(getJBossConfigDir()).exists()) {
            throw new IllegalStateException("Could not determine the value of JBoss configuration directory.");
        }

        if (javaHome != null) {
            if (!javaHome.exists() || !javaHome.isDirectory()) {
                throw new IllegalStateException("javaHome '" + javaHome.getAbsolutePath() + "' must exist!");
            }
        } else {
            throw new IllegalStateException("Could not determine the value of Java home directory.");
        }
    }

    public String getJavaHome() {
        try {
            return javaHome.getCanonicalPath();
        }
        catch(IOException e) {
            throw new IllegalStateException("Unable to get canonical path of " + javaHome, e);
        }
    }

    public JBossManagerConfiguration setJavaHome(String javaHome) {
        if (javaHome != null && new File(javaHome).isDirectory()) {
            this.javaHome = new File(javaHome);
        }
        return this;
    }

    public String getJavaBin() {
        String javaExec = null;

        if (getJavaHome() != null) {
            return getJavaHome() + File.separatorChar + "bin" + File.separatorChar + "java";
        } else {
            return "java";
        }
    }

    public String getJBossHome() {
        try {
            return jbossHome.getCanonicalPath();
        }
        catch(IOException e) {
            throw new IllegalStateException("Unable to get canonical path of " + jbossHome, e);
        }
    }

    public JBossManagerConfiguration setJBossHome(String jbossHome) {
        if (jbossHome != null && new File(jbossHome).isDirectory()) {
            this.jbossHome = new File(jbossHome);
        }
        return this;
    }

    // helpers

    public String getJBossBaseDir() {
        if (isDomain()) {
            return getJBossHome() + "/domain";
        } else {
            return getJBossHome() + "/standalone";
        }
    }

    public String getJBossLogDir() {
        return getJBossBaseDir() + "/log";
    }

    public String getJBossConfigDir() {
        return getJBossBaseDir() + "/configuration";
    }

    public String getJBossModuleDir() {
        return getJBossHome() + "/modules";
    }

    public String getJavaOpts() {
        if (javaOpts == null) {
            return ContainerJavaOptsConfiguration.getContainerJavaOpts(this);
        } else {
            return javaOpts;
        }
    }

    public JBossManagerConfiguration setJavaOpts(String javaOpts) {
        if (javaOpts != null) {
            this.javaOpts = javaOpts;
        }
        return this;
    }

    public int getStartupTimeoutInSeconds() {
        return startupTimeoutInSeconds;
    }

    public JBossManagerConfiguration setStartupTimeoutInSeconds(int startupTimeoutInSeconds) {
        if (startupTimeoutInSeconds > 0) {
            this.startupTimeoutInSeconds = startupTimeoutInSeconds;
        }
        return this;
    }

    public JBossManagerConfiguration setOutputToConsole(boolean outputToConsole) {
        this.outputToConsole = outputToConsole;
        return this;
    }

    public boolean isOutputToConsole() {
        return outputToConsole;
    }

    public String getProcessControllerJavaOpts() {
        if (processControllerJavaOpts == null) {
            return ContainerJavaOptsConfiguration.getContainerJavaOpts(this);
        }
        return processControllerJavaOpts;
    }

    public JBossManagerConfiguration setProcessControllerJavaOpts(String processControllerJavaOpts) {
        if (processControllerJavaOpts != null) {
            this.processControllerJavaOpts = processControllerJavaOpts;
        }
        return this;
    }

    public String getHostControllerJavaOpts() {
        if (hostControllerJavaOpts == null) {
            return ContainerJavaOptsConfiguration.getContainerJavaOpts(this);
        }
        return hostControllerJavaOpts;
    }

    public JBossManagerConfiguration setHostControllerJavaOpts(String hostControllerJavaOpts) {
        if (hostControllerJavaOpts != null) {
            this.hostControllerJavaOpts = hostControllerJavaOpts;
        }
        return this;
    }

    public String getServerJavaOpts() {
        return serverJavaOpts;
    }

    public JBossManagerConfiguration setServerJavaOpts(String serverJavaOpts) {
        if (serverJavaOpts != null) {
            this.serverJavaOpts = serverJavaOpts;
        }
        return this;
    }

    public JBossManagerConfiguration setUser(String user) {
        if (user != null) {
            this.user = user;
        }
        return this;
    }

    public String getUser() {
        return user;
    }

    public JBossManagerConfiguration setPassword(String password) {
        if (password != null) {
            this.password = password;
        }
        return this;
    }

    public String getPassword() {
        return password;
    }

    public ContainerType getContainerType() {
        return containerType;
    }

    public JBossManagerConfiguration setContainerType(ContainerType containerType) {
        if (containerType != null) {
            this.containerType = containerType;
        }
        return this;
    }

    // configs

    public String getDomainConfig() {
        return domainConfig;
    }

    public JBossManagerConfiguration setDomainConfig(String domainConfig) {
        if (domainConfig != null && domainConfig.length() != 0) {
            this.domainConfig = domainConfig;
        }
        return this;
    }

    public String getHostConfig() {
        return hostConfig;
    }

    public JBossManagerConfiguration setHostConfig(String hostConfig) {
        if (hostConfig != null && hostConfig.length() != 0) {
            this.hostConfig = hostConfig;
        }
        return this;
    }

    public String getStandaloneConfig() {
        return standaloneConfig;
    }

    public JBossManagerConfiguration setStandaloneConfig(String standaloneConfig) {
        if (standaloneConfig != null && standaloneConfig.length() != 0) {
            this.standaloneConfig = standaloneConfig;
        }
        return this;
    }

    // domain specific

    public boolean isDomain() {
        return domain;
    }

    public JBossManagerConfiguration domain() {
        domain = true;
        return this;
    }

    public String getServerGroup() {
        return serverGroup;
    }

    public JBossManagerConfiguration setServerGroup(String serverGroup) {
        if (serverGroup != null && serverGroup.length() != 0) {
            this.serverGroup = serverGroup;
        }

        return this;
    }

    public JBossManagerConfiguration setDomainStartTimeout(long domainStartTimeout) {
        if (domainStartTimeout > 0) {
            this.domainStartTimeout = domainStartTimeout;
        }
        return this;
    }

    public long getDomainStartTimeout() {
        return domainStartTimeout;
    }

    public JBossManagerConfiguration setDomainServers(String domainServer, String... domainServers) {
        if (domainServer != null && domainServer.length() != 0) {
            this.domainServers.clear();
            this.domainServers.add(domainServer);
        }

        for (String domainServerArg : domainServers) {
            if (domainServer != null && domainServer.length() != 0) {
                this.domainServers.add(domainServerArg);
            }
        }

        return this;
    }

    public JBossManagerConfiguration setDomainServers(List<String> domainServers) {
        if (domainServers != null) {
            this.domainServers.clear();
            for (String domainServer : domainServers) {
                if (domainServer != null && domainServer.length() != 0) {
                    this.domainServers.add(domainServer);
                }
            }
        }
        return this;
    }

    public List<String> getDomainServers() {
        return domainServers;
    }

    public JBossManagerConfiguration setDomainMasterHostName(String domainMasterHostName) {
        if (domainMasterHostName != null) {
            this.domainMasterHostName = domainMasterHostName;
        }
        return this;
    }

    public String getDomainMasterHostName() {
        return domainMasterHostName;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("jboss dir\t").append(getJBossHome()).append("\n")
            .append("base dir\t").append(getJBossBaseDir()).append("\n")
            .append("config dir\t").append(getJBossConfigDir()).append("\n")
            .append("log dir\t\t").append(getJBossLogDir()).append("\n")
            .append("module dir\t").append(getJBossModuleDir()).append("\n");

        return sb.toString();
    }
}
