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
package org.jboss.aerogear.test.container.manager;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.sasl.RealmCallback;

import org.jboss.aerogear.test.container.manager.api.ContainerManager;
import org.jboss.aerogear.test.container.manager.api.ContainerManagerException;
import org.jboss.as.controller.client.ModelControllerClient;

/**
 * Code taken and refactored from
 *
 * https://github.com/wildfly/wildfly/blob/master/arquillian/container-managed/src/main/java/org/jboss/as/arquillian/container/
 * managed/ManagedDeployableContainer.java
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class JBossManager implements ContainerManager {

    private static final Logger log = Logger.getLogger(JBossManager.class.getName());

    static final String TEMP_CONTAINER_DIRECTORY = "arquillian-temp-container";

    private static final String CONFIG_PATH = "/standalone/configuration/";

    static final String CONFIG_DIR = "configuration";
    static final String SERVER_BASE_DIR = "standalone";
    static final String LOG_DIR = "log";
    static final String DATA_DIR = "data";

    private Thread shutdownThread;
    private Process process;
    private ManagementClient client;
    private final ManagedContainerConfiguration configuration;

    public JBossManager() {
        this(new ManagedContainerConfiguration());
    }

    public JBossManager(ManagedContainerConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void start() throws ContainerManagerException {
        if (configuration.getUsername() != null) {
            Authentication.username = configuration.getUsername();
            Authentication.password = configuration.getPassword();
        }

        ModelControllerClient modelControllerClient = null;
        try {
            modelControllerClient = ModelControllerClient.Factory.create(
                configuration.getManagementAddress(),
                configuration.getManagementPort(),
                Authentication.getCallbackHandler());
        } catch (UnknownHostException e) {
            throw new ContainerManagerException(e);
        }

        client = new ManagementClient(modelControllerClient, configuration.getManagementAddress(), configuration.getManagementPort());

        try {
            startInternal();
        } catch (RuntimeException e) {
            safeCloseClient();
            throw new ContainerManagerException(e);
        }
    }

    @Override
    public void stop() throws ContainerManagerException {
        try {
            stopInternal();
        } finally {
            safeCloseClient();
        }
    }

    private void startInternal() {

        if (isServerRunning()) {
            if (configuration.isAllowConnectingToRunningServer()) {
                return;
            } else {
                failDueToRunning();
            }
        }

        try {
            final String jbossHomeDir = configuration.getJbossHome();
            String modulesPath = configuration.getModulePath();
            if (modulesPath == null || modulesPath.isEmpty()) {
                modulesPath = jbossHomeDir + File.separatorChar + "modules";
            }
            File modulesDir = new File(modulesPath);
            if (modulesDir.isDirectory() == false)
                throw new IllegalStateException("Cannot find: " + modulesDir);

            String bundlesPath = modulesDir.getParent() + File.separator + "bundles";
            File bundlesDir = new File(bundlesPath);
            if (bundlesDir.isDirectory() == false)
                throw new IllegalStateException("Cannot find: " + bundlesDir);

            final String additionalJavaOpts = configuration.getJavaVmArguments();

            File modulesJar = new File(jbossHomeDir + File.separatorChar + "jboss-modules.jar");
            if (!modulesJar.exists())
                throw new IllegalStateException("Cannot find: " + modulesJar);

            List<String> cmd = new ArrayList<String>();
            String javaExec = configuration.getJavaHome() + File.separatorChar + "bin" + File.separatorChar + "java";
            if (configuration.getJavaHome().contains(" ")) {
                javaExec = "\"" + javaExec + "\"";
            }
            cmd.add(javaExec);
            if (additionalJavaOpts != null) {
                for (String opt : additionalJavaOpts.split("\\s+")) {
                    cmd.add(opt);
                }
            }

            if (configuration.isEnableAssertions()) {
                cmd.add("-ea");
            }

            cmd.add("-Djboss.home.dir=" + jbossHomeDir);
            cmd.add("-Dorg.jboss.boot.log.file=" + jbossHomeDir + "/standalone/log/boot.log");
            cmd.add("-Dlogging.configuration=file:" + jbossHomeDir + CONFIG_PATH + "logging.properties");
            cmd.add("-Djboss.modules.dir=" + modulesDir.getCanonicalPath());
            cmd.add("-Djboss.bundles.dir=" + bundlesDir.getCanonicalPath());
            cmd.add("-jar");
            cmd.add(modulesJar.getAbsolutePath());
            cmd.add("-mp");
            cmd.add(modulesPath);
            cmd.add("-jaxpmodule");
            cmd.add("javax.xml.jaxp-provider");
            cmd.add("org.jboss.as.standalone");
            cmd.add("-server-config");
            cmd.add(configuration.getServerConfig());

            log.info("Starting container with: " + cmd.toString());
            ProcessBuilder processBuilder = new ProcessBuilder(cmd);
            processBuilder.redirectErrorStream(true);
            process = processBuilder.start();
            new Thread(new ConsoleConsumer()).start();
            final Process proc = process;
            shutdownThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    if (proc != null) {
                        proc.destroy();
                        try {
                            proc.waitFor();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            });
            Runtime.getRuntime().addShutdownHook(shutdownThread);

            long startupTimeout = configuration.getStartupTimeoutInSeconds();
            long timeout = startupTimeout * 1000;
            boolean serverAvailable = false;
            long sleep = 1000;
            while (timeout > 0 && serverAvailable == false) {
                serverAvailable = getManagementClient().isServerInRunningState();
                if (!serverAvailable) {
                    if (processHasDied(proc))
                        break;
                    Thread.sleep(sleep);
                    timeout -= sleep;
                    sleep = Math.max(sleep / 2, 100);
                }
            }
            if (!serverAvailable) {
                destroyProcess();
                throw new TimeoutException(String.format("Managed server was not started within [%d] s", configuration.getStartupTimeoutInSeconds()));
            }

        } catch (Exception e) {
            throw new RuntimeException("Could not start container", e);
        }
    }

    // helpers

    private ManagementClient getManagementClient() {
        return this.client;
    }

    private void stopInternal() throws ContainerManagerException {
        if (shutdownThread != null) {
            Runtime.getRuntime().removeShutdownHook(shutdownThread);
            shutdownThread = null;
        }
        try {
            if (process != null) {
                process.destroy();
                process.waitFor();
                process = null;
            }
        } catch (Exception e) {
            throw new ContainerManagerException("Could not stop container", e);
        }
    }

    private static boolean processHasDied(final Process process) {
        try {
            process.exitValue();
            return true;
        } catch (IllegalThreadStateException e) {
            // good
            return false;
        }
    }

    private void safeCloseClient() {
        try {
            client.close();
        } catch (Exception e) {
            log.log(Level.WARNING, "Caught exception closing ModelControllerClient", e);
        }
    }

    private boolean isServerRunning() {
        Socket socket = null;
        try {
            socket = new Socket(
                configuration.getManagementAddress(),
                configuration.getManagementPort());
        } catch (Exception ignored) { // nothing is running on defined ports
            return false;
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (Exception e) {
                    throw new RuntimeException("Could not close isServerStarted socket", e);
                }
            }
        }
        return true;
    }

    private void failDueToRunning() {
        throw new RuntimeException(
            "The server is already running! " +
                "Managed containers does not support connecting to running server instances due to the " +
                "possible harmful effect of connecting to the wrong server. Please stop server before running or " +
                "change to another type of container.\n");
    }

    private int destroyProcess() {
        if (process == null)
            return 0;
        process.destroy();
        try {
            return process.waitFor();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Runnable that consumes the output of the process. If nothing consumes the output the AS will hang on some platforms
     *
     * @author Stuart Douglas
     */
    private class ConsoleConsumer implements Runnable {

        @Override
        public void run() {
            final InputStream stream = process.getInputStream();
            final BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            final boolean writeOutput = configuration.isOutputToConsole();

            String line = null;
            try {
                while ((line = reader.readLine()) != null) {
                    if (writeOutput) {
                        System.out.println(line);
                    }
                }
            } catch (IOException e) {
            }
        }

    }

    private static class Authentication {

        public static String username = "";
        public static String password = "";

        public static CallbackHandler getCallbackHandler() {
            return new CallbackHandler();
        }

        public static class CallbackHandler implements javax.security.auth.callback.CallbackHandler {

            public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
                for (Callback current : callbacks) {
                    if (current instanceof NameCallback) {
                        NameCallback ncb = (NameCallback) current;
                        ncb.setName(username);
                    } else if (current instanceof PasswordCallback) {
                        PasswordCallback pcb = (PasswordCallback) current;
                        pcb.setPassword(password.toCharArray());
                    } else if (current instanceof RealmCallback) {
                        RealmCallback rcb = (RealmCallback) current;
                        rcb.setText(rcb.getDefaultText());
                    } else {
                        throw new UnsupportedCallbackException(current);
                    }
                }
            }
        }
    }

}
