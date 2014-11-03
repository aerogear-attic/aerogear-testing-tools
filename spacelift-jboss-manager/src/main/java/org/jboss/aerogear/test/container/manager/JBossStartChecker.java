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

import org.arquillian.spacelift.execution.ExecutionCondition;
import org.arquillian.spacelift.execution.ExecutionException;
import org.arquillian.spacelift.execution.Task;
import org.arquillian.spacelift.execution.Tasks;
import org.arquillian.spacelift.process.ProcessResult;
import org.jboss.aerogear.test.container.spacelift.JBossCLI;

/**
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
class JBossStartChecker extends Task<Object, Boolean> {

    public static final ExecutionCondition<Boolean> jbossStartedCondition = new JBossStartChecker.JBossStartedCondition();

    @Override
    protected Boolean process(Object input) throws Exception {

        ProcessResult processResult = null;

        try {
            processResult = Tasks.prepare(JBossCLI.class)
                .connect()
                .cliCommand(":read-attribute(name=server-state)")
                .execute().await();
        } catch (Exception ex) {

        }

        if (processResult == null || processResult.exitValue() != 0) {
            return false;
        }

        boolean success = false;
        boolean running = false;

        for (String output : processResult.output()) {

            output = output.toLowerCase();

            if (output != null && output.contains("result") && output.contains("running")) {
                running = true;
                continue;
            }

            if (output != null && output.contains("outcome") && output.contains("success")) {
                success = true;
            }
        }

        return success && running;
    }

    private static final class JBossStartedCondition implements ExecutionCondition<Boolean> {

        @Override
        public boolean satisfiedBy(Boolean started) throws ExecutionException {
            return started;
        }

    }

}
