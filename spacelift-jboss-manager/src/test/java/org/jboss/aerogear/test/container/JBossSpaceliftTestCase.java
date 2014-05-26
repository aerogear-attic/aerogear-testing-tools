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
package org.jboss.aerogear.test.container;

import org.arquillian.spacelift.execution.Tasks;
import org.arquillian.spacelift.execution.impl.DefaultExecutionServiceFactory;
import org.jboss.aerogear.test.container.manager.ManagedContainerConfiguration;
import org.jboss.aerogear.test.container.spacelift.JBossStarter;
import org.jboss.aerogear.test.container.spacelift.JBossStopper;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class JBossSpaceliftTestCase {

    private static final String JBOSS_HOME = "target/jboss-as-7.1.1.Final";

    @BeforeClass
    public static void setup() {
        Tasks.setDefaultExecutionServiceFactory(new DefaultExecutionServiceFactory());
    }

    @Test
    public void startAndStopJBossContainer() {

        Tasks.prepare(JBossStarter.class)
            .configuration(new ManagedContainerConfiguration().setJbossHome(JBOSS_HOME))
            .then(JBossStopper.class)
            .execute()
            .await();
    }
}
