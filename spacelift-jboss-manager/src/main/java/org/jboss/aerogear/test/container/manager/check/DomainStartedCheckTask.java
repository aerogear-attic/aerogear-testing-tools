/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc. and/or its affiliates, and individual
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
package org.jboss.aerogear.test.container.manager.check;

import java.io.FileNotFoundException;
import java.util.concurrent.TimeUnit;

import org.arquillian.spacelift.execution.ExecutionCondition;
import org.arquillian.spacelift.execution.ExecutionException;
import org.arquillian.spacelift.execution.Task;
import org.arquillian.spacelift.execution.Tasks;
import org.arquillian.spacelift.process.ProcessResult;
import org.jboss.aerogear.test.container.manager.JBossManagerConfiguration;
import org.jboss.aerogear.test.container.spacelift.JBossCLI;
import org.jboss.aerogear.test.container.spacelift.JBossCLI.NotExecutableScriptException;

/**
 * Checks if domain has started, meaning all slave servers are being about to start.
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class DomainStartedCheckTask extends Task<Object, Boolean> {

    private JBossManagerConfiguration configuration = new JBossManagerConfiguration();

    public DomainStartedCheckTask configuration(JBossManagerConfiguration configuration) {
        if (configuration != null) {
            this.configuration = configuration;
        }
        return this;
    }

    @Override
    protected Boolean process(Object input) throws Exception {

        ProcessResult domainServersStartInitiatedResult = null;

        configuration.validate();

        try {
            domainServersStartInitiatedResult = Tasks.prepare(JBossCLI.class)
                .environment("JBOSS_HOME", configuration.getJBossHome())
                .user(configuration.getUser())
                .password(configuration.getPassword())
                .connect()
                .cliCommand("/host=" + configuration.getDomainMasterHostName() + ":read-children-names(child-type=server)")
                .execute()
                .reexecuteEvery(5, TimeUnit.SECONDS)
                .until(configuration.getDomainStartTimeout(), TimeUnit.SECONDS, new ExecutionCondition<ProcessResult>() {

                    @Override
                    public boolean satisfiedBy(ProcessResult result) throws ExecutionException {

                        int runningDomainServers = 0;

                        boolean success = false;

                        for (String line : result.output()) {
                            if (line.contains("outcome") && line.contains("success")) {
                                success = true;
                                break;
                            }
                        }

                        if (!success) {
                            return false;
                        }

                        for (String domainServer : configuration.getDomainServers()) {
                            for (String line : result.output()) {
                                if (line.contains(domainServer)) {
                                    ++runningDomainServers;
                                }
                            }
                        }

                        return runningDomainServers == configuration.getDomainServers().size();
                    }

                });
        } catch (Exception ex) {
            if (ex.getCause() instanceof FileNotFoundException) {
                throw ex;
            } else if (ex.getCause() instanceof NotExecutableScriptException) {
                throw ex;
            }
            domainServersStartInitiatedResult = null;
        }

        return domainServersStartInitiatedResult != null && domainServersStartInitiatedResult.exitValue() == 0;
    }

}
