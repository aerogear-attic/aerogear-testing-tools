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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.arquillian.spacelift.execution.CountDownWatch;
import org.arquillian.spacelift.execution.Tasks;
import org.arquillian.spacelift.process.Command;
import org.jboss.aerogear.test.container.manager.api.ContainerManager;
import org.jboss.aerogear.test.container.manager.api.ContainerManagerException;

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

    private static final Logger logger = Logger.getLogger(JBossManager.class.getName());

    private Thread shutdownThread;
    private Process process;
    private final JBossManagerConfiguration configuration;

    public JBossManager() {
        this(new JBossManagerConfiguration());
    }

    public JBossManager(JBossManagerConfiguration configuration) {
        if (configuration == null) {
            throw new IllegalArgumentException("Provided configuration to JBossManager is a null object!");
        }
        this.configuration = configuration;
    }

    @Override
    public void start() throws ContainerManagerException {

        try {
            Command command = new JBossCommandBuilder().build(configuration);

            logger.info("Starting container with: " + command.toString());

            ProcessBuilder processBuilder = new ProcessBuilder(command.getFullCommand());
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

            Tasks.chain(configuration, JBossStartChecker.class)
                .execute()
                .until(new CountDownWatch(configuration.getStartupTimeoutInSeconds(), TimeUnit.SECONDS), JBossStartChecker.jbossStartedCondition);

        } catch (Exception e) {
            throw new ContainerManagerException("Could not start container", e);
        }
    }

    @Override
    public void stop() throws ContainerManagerException {
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

    // helpers

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

}
