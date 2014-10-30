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
package org.jboss.aerogear.test.arquillian.container.check;

import org.arquillian.spacelift.execution.ExecutionCondition;
import org.arquillian.spacelift.execution.ExecutionException;
import org.arquillian.spacelift.execution.Task;

/**
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 * 
 */
public class StatusCheckTask extends Task<Object, Boolean> {

    public static final ExecutionCondition<Boolean> statusCheckCondition = new StatusCheckTask.StatusCheckCondition();

    private StatusCheck check;

    public StatusCheckTask check(StatusCheck check) {
        this.check = check;
        return this;
    }

    @Override
    protected Boolean process(Object input) throws Exception {
        return check.execute();
    }

    private static class StatusCheckCondition implements ExecutionCondition<Boolean> {

        @Override
        public boolean satisfiedBy(Boolean ok) throws ExecutionException {
            return ok;
        }

    }

}
