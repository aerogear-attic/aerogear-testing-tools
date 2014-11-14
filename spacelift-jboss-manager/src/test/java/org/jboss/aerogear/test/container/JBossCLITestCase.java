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
import org.jboss.aerogear.test.container.manager.JBossManager;
import org.jboss.aerogear.test.container.manager.ManagedContainerConfiguration;
import org.jboss.aerogear.test.container.spacelift.JBossCLI;
import org.jboss.aerogear.test.container.spacelift.JBossStarter;
import org.jboss.aerogear.test.container.spacelift.JBossStopper;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Starts container, connects and quits from CLI, stops container.
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
@RunWith(JUnit4.class)
public class JBossCLITestCase {

    private static final String JBOSS_HOME = TestUtils.getJBossHome();

    private JBossManager manager;

    @BeforeClass
    public static void beforeClass() {
        Tasks.setDefaultExecutionServiceFactory(new DefaultExecutionServiceFactory());
    }

    @Before
    public void setup() {
        manager = Tasks.prepare(JBossStarter.class)
            .configuration(new ManagedContainerConfiguration().setJbossHome(JBOSS_HOME))
            .execute()
            .await();
    }

    @After
    public void tearDown() {
        Tasks.chain(manager, JBossStopper.class).execute().await();
    }

    @Test
    public void executeCliCommand() {
        Tasks.prepare(JBossCLI.class)
            .environment("JBOSS_HOME", JBOSS_HOME)
            .connect()
            .cliCommand("quit")
            .execute()
            .await();
    }

}
