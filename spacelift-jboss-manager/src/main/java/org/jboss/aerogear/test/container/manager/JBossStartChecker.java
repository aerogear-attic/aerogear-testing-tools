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

import org.arquillian.spacelift.Spacelift;
import org.arquillian.spacelift.execution.ExecutionCondition;
import org.arquillian.spacelift.execution.ExecutionException;
import org.arquillian.spacelift.task.Task;
import org.jboss.aerogear.test.container.manager.check.DomainStartedCheckTask;
import org.jboss.aerogear.test.container.manager.check.ServerInDomainStartCheckTask;
import org.jboss.aerogear.test.container.manager.check.StandaloneStartedCheckTask;

/**
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
class JBossStartChecker extends Task<JBossManagerConfiguration, Boolean> {

    public static final ExecutionCondition<Boolean> jbossStartedCondition = new JBossStartChecker.JBossStartedCondition();

    @Override
    protected Boolean process(JBossManagerConfiguration configuration) throws Exception {

        if (configuration == null) {
            throw new IllegalStateException("configuration is null object!");
        }

        configuration.validate();

        if (configuration.isDomain()) {
            return startDomainCheck(configuration);
        } else {
            return startStandaloneCheck(configuration);
        }
    }

    private Boolean startStandaloneCheck(final JBossManagerConfiguration configuration) {
        return Spacelift.task(configuration, StandaloneStartedCheckTask.class).execute().await();
    }

    private Boolean startDomainCheck(JBossManagerConfiguration configuration) {

        boolean domainStarted = Spacelift.task(DomainStartedCheckTask.class).configuration(configuration).execute().await();

        if (!domainStarted) {
            return false;
        }

        // at this point we have all servers up but they are starting underneath
        // we have to check the status of every server until all are in STARTED status

        for (String domainServer : configuration.getDomainServers()) {

            boolean domainServerStarted = Spacelift.task(domainServer, ServerInDomainStartCheckTask.class)
                .configuration(configuration)
                .execute()
                .await();

            if (!domainServerStarted) {
                return false;
            }
        }

        return true;
    }

    private static final class JBossStartedCondition implements ExecutionCondition<Boolean> {

        @Override
        public boolean satisfiedBy(Boolean started) throws ExecutionException {
            return started;
        }

    }

}
