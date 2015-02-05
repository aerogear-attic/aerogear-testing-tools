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

import org.arquillian.spacelift.Spacelift;
import org.jboss.aerogear.test.container.manager.JBossManager;
import org.jboss.aerogear.test.container.manager.JBossManagerConfiguration;
import org.jboss.aerogear.test.container.manager.configuration.ContainerType;
import org.jboss.aerogear.test.container.spacelift.JBossCLI;
import org.jboss.aerogear.test.container.spacelift.JBossStarter;
import org.jboss.aerogear.test.container.spacelift.JBossStopper;
import org.junit.After;
import org.junit.Before;
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

    private ContainerType containerType = TestUtils.getContainerType();

    private JBossManager manager;

    @Before
    public void setup() {
        manager = Spacelift.task(JBossStarter.class)
            .configuration(new JBossManagerConfiguration().setJBossHome(JBOSS_HOME).setContainerType(containerType))
            .execute()
            .await();
    }

    @After
    public void tearDown() {
        Spacelift.task(manager, JBossStopper.class).execute().await();
    }

    @Test
    public void executeCliCommand() {
        Spacelift.task(JBossCLI.class)
            .environment("JBOSS_HOME", JBOSS_HOME)
            .connect()
            .cliCommand("quit")
            .execute()
            .await();
    }

}
