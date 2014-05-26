package org.jboss.aerogear.test.container.manager;

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

import java.io.File;

/**
 * JBossAsManagedConfiguration
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ManagedContainerConfiguration {

    private String jbossHome = System.getenv("JBOSS_HOME");

    private String javaHome = System.getenv("JAVA_HOME");

    private String modulePath = System.getProperty("module.path");

    private String javaVmArguments = System.getProperty("jboss.options", "-Xmx512m -XX:MaxPermSize=128m");

    private String managementAddress;

    private int managementPort;

    private String username;

    private String password;

    private int startupTimeoutInSeconds = 60;

    private boolean outputToConsole = true;

    private String serverConfig = System.getProperty("jboss.server.config.file.name", "standalone.xml");

    private boolean allowConnectingToRunningServer = false;

    private boolean enableAssertions = true;

    public ManagedContainerConfiguration() {
        // if no javaHome is set use java.home of already running jvm
        if (javaHome == null || javaHome.isEmpty()) {
            javaHome = System.getProperty("java.home");
        }

        managementAddress = "127.0.0.1";
        managementPort = 9999;
    }

    /**
     * 
     * @throws IllegalStateException if {@code jbossHome} or {@code javaHome} are not valid directories or if {@code username}
     *         is not null and password is null
     */
    public void validate() throws IllegalStateException {

        File jbossHome = new File(this.jbossHome);

        if (!jbossHome.exists() || !jbossHome.isDirectory()) {
            throw new IllegalStateException("jbossHome '" + jbossHome.getAbsolutePath() + "' must exist!");
        }

        File javaHome = new File(this.javaHome);

        if (!javaHome.exists() || !jbossHome.isDirectory()) {
            throw new IllegalStateException("javaHome '" + javaHome.getAbsolutePath() + "' must exist!");
        }

        if (username != null && password == null) {
            throw new IllegalStateException("username has been set, but no password given");
        }
    }

    public String getManagementAddress() {
        return managementAddress;
    }

    public ManagedContainerConfiguration setManagementAddress(String host) {
        this.managementAddress = host;
        return this;
    }

    public int getManagementPort() {
        return managementPort;
    }

    public ManagedContainerConfiguration setManagementPort(int managementPort) {
        this.managementPort = managementPort;
        return this;
    }

    /**
     * @return the jbossHome
     */
    public String getJbossHome() {
        return jbossHome;
    }

    /**
     * @param jbossHome the jbossHome to set
     */
    public ManagedContainerConfiguration setJbossHome(String jbossHome) {
        this.jbossHome = jbossHome;
        return this;
    }

    /**
     * @return the javaHome
     */
    public String getJavaHome() {
        return javaHome;
    }

    /**
     * @param javaHome the javaHome to set
     */
    public ManagedContainerConfiguration setJavaHome(String javaHome) {
        this.javaHome = javaHome;
        return this;
    }

    /**
     * @return the javaVmArguments
     */
    public String getJavaVmArguments() {
        return javaVmArguments;
    }

    /**
     * @param javaVmArguments the javaVmArguments to set
     */
    public ManagedContainerConfiguration setJavaVmArguments(String javaVmArguments) {
        this.javaVmArguments = javaVmArguments;
        return this;
    }

    /**
     * @param startupTimeoutInSeconds the startupTimeoutInSeconds to set
     */
    public ManagedContainerConfiguration setStartupTimeoutInSeconds(int startupTimeoutInSeconds) {
        this.startupTimeoutInSeconds = startupTimeoutInSeconds;
        return this;
    }

    /**
     * @return the startupTimeoutInSeconds
     */
    public int getStartupTimeoutInSeconds() {
        return startupTimeoutInSeconds;
    }

    /**
     * @param outputToConsole the outputToConsole to set
     */
    public ManagedContainerConfiguration setOutputToConsole(boolean outputToConsole) {
        this.outputToConsole = outputToConsole;
        return this;
    }

    /**
     * @return the outputToConsole
     */
    public boolean isOutputToConsole() {
        return outputToConsole;
    }

    /**
     * Get the server configuration file name. Equivalent to [-server-config=...] on the command line.
     *
     * @return the server config
     */
    public String getServerConfig() {
        return serverConfig;
    }

    /**
     * Set the server configuration file name. Equivalent to [-server-config=...] on the command line.
     *
     * @param serverConfig the server config
     */
    public ManagedContainerConfiguration setServerConfig(String serverConfig) {
        this.serverConfig = serverConfig;
        return this;
    }

    public String getModulePath() {
        return modulePath;
    }

    public ManagedContainerConfiguration setModulePath(String modulePath) {
        this.modulePath = modulePath;
        return this;
    }

    public boolean isAllowConnectingToRunningServer() {
        return allowConnectingToRunningServer;
    }

    public ManagedContainerConfiguration setAllowConnectingToRunningServer(boolean allowConnectingToRunningServer) {
        this.allowConnectingToRunningServer = allowConnectingToRunningServer;
        return this;
    }

    public boolean isEnableAssertions() {
        return enableAssertions;
    }

    public ManagedContainerConfiguration setEnableAssertions(boolean enableAssertions) {
        this.enableAssertions = enableAssertions;
        return this;
    }

    public String getUsername() {
        return this.username;
    }

    /**
     * 
     * @param username can not be a null object or an empty string
     * @return
     */
    public ManagedContainerConfiguration setUsername(String username) {
        if (username != null && username.length() > 0) {
            this.username = username;
        }
        return this;
    }

    public String getPassword() {
        return this.password;
    }

    /**
     * 
     * @param password can not be a null object nor an empty string
     * @return
     */
    public ManagedContainerConfiguration setPassword(String password) {
        if (password != null && password.length() > 0) {
            this.password = password;
        }
        return this;
    }
}