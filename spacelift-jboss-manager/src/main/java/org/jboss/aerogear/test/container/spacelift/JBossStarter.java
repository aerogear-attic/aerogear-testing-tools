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
package org.jboss.aerogear.test.container.spacelift;

import java.util.logging.Logger;

import org.arquillian.spacelift.execution.Task;
import org.jboss.aerogear.test.container.manager.JBossManager;
import org.jboss.aerogear.test.container.manager.ManagedContainerConfiguration;

/**
 * Starts JBoss instance.
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class JBossStarter extends Task<ManagedContainerConfiguration, JBossManager> {

    private static final Logger logger = Logger.getLogger(JBossStarter.class.getName());

    private ManagedContainerConfiguration configuration;

    public JBossStarter configuration(ManagedContainerConfiguration configuration) {
        setConfiguration(configuration);
        return this;
    }

    public JBossStarter() {
        configuration = new ManagedContainerConfiguration();
    }

    @Override
    protected JBossManager process(ManagedContainerConfiguration configuration) throws Exception {

        setConfiguration(configuration);

        JBossManager jbossManager = new JBossManager(this.configuration);

        jbossManager.start();

        return jbossManager;
    }

    private void setConfiguration(ManagedContainerConfiguration configuration) {
        if (configuration != null) {
            try {
                configuration.validate();
                this.configuration = configuration;
            } catch (IllegalStateException ex) {
                logger.info("Configuration passed to JBossStarted was not valid and it was not taken into account.");
            }
        }
    }
}
