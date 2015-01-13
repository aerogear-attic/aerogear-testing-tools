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

import org.arquillian.spacelift.execution.ExecutionException;
import org.arquillian.spacelift.execution.Task;
import org.arquillian.spacelift.execution.Tasks;
import org.arquillian.spacelift.process.ProcessResult;
import org.jboss.aerogear.test.container.manager.JBossManagerConfiguration;
import org.jboss.aerogear.test.container.spacelift.JBossCLI;
import org.jboss.aerogear.test.container.spacelift.JBossCLI.NotExecutableScriptException;

/**
 * Checks STARTED status of slave server in a domain.
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class ServerInDomainStartCheckTask extends Task<String, Boolean> {

    private JBossManagerConfiguration configuration = new JBossManagerConfiguration();

    public ServerInDomainStartCheckTask configuration(JBossManagerConfiguration configuration) {
        if (configuration != null) {
            this.configuration = configuration;
        }
        return this;
    }

    @Override
    protected Boolean process(String domainServer) throws Exception {

        ProcessResult result = null;

        configuration.validate();

        String command = "/host=" + configuration.getDomainMasterHostName()
            + "/server-config=" + domainServer
            + ":read-resource(include-runtime=true)";

        try {
            result = Tasks.prepare(JBossCLI.class)
                .environment("JBOSS_HOME", configuration.getJBossHome())
                .user(configuration.getUser())
                .password(configuration.getPassword())
                .connect()
                .cliCommand(command)
                .execute()
                .await();

            for (String line : result.output()) {
                if (line.contains("status") && line.contains("STARTED")) {
                    return true;
                }
            }

        } catch (ExecutionException ex) {
            if (ex.getCause() instanceof FileNotFoundException) {
                throw ex;
            } else if (ex.getCause() instanceof NotExecutableScriptException) {
                throw ex;
            }
        }

        return false;
    }

}
