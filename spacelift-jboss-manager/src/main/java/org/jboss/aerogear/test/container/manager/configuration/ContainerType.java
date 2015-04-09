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
package org.jboss.aerogear.test.container.manager.configuration;

import org.arquillian.spacelift.Spacelift;
import org.jboss.aerogear.test.container.manager.JBossManagerConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public enum ContainerType {

    AS7 {
        @Override
        public List<String> javaOptions(JBossManagerConfiguration configuration) {

            List<String> opts = new ArrayList<String>();

            if (!configuration.isDomain()) {
                opts.add("-server");
                opts.add("-XX:+UseCompressedOops");
                opts.add("-XX:+TieredCompilation");
            } else if (Spacelift.task(configuration, JavaServerOptionCapabilityCheck.class).execute().await()) {
                opts.add("-server");
            }

            opts.add("-Xms64m");
            opts.add("-Xmx512m");
            opts.add("-XX:MaxPermSize=256m");
            opts.add("-Djava.net.preferIPv4Stack=true");
            opts.add("-Dorg.jboss.resolver.warning=true");
            opts.add("-Dsun.rmi.dgc.client.gcInterval=3600000");
            opts.add("-Dsun.rmi.dgc.server.gcInterval=3600000");
            opts.add("-Djboss.modules.system.pkgs=org.jboss.byteman");
            opts.add("-Djava.awt.headless=true");

            if (configuration.isDomain()) {
                opts.add("-Djboss.domain.default.config=" + configuration.getDomainConfig());
                opts.add("-Djboss.host.default.config=" + configuration.getHostConfig());
            } else {
                opts.add("-Djboss.server.default.config=" + configuration.getStandaloneConfig());
            }

            return opts;
        }
    },
    EAP {
        @Override
        public List<String> javaOptions(JBossManagerConfiguration configuration) {
            List<String> opts = new ArrayList<String>();
            if (!configuration.isDomain()) {
                opts.add("-server");
                opts.add("-XX:+UseCompressedOops");
                opts.add("-verbose:gc");
                opts.add("-Xloggc:" + configuration.getJBossLogDir() + "/gc.log");
                opts.add("-XX:+PrintGCDetails");
                opts.add("-XX:+PrintGCDateStamps");
                opts.add("-XX:+UseGCLogFileRotation");
                opts.add("-XX:NumberOfGCLogFiles=5");
                opts.add("-XX:GCLogFileSize=3M");
                opts.add("-XX:-TraceClassUnloading");
                opts.add("-Xms1303m");
                opts.add("-Xmx1303m");
            } else {
                if (Spacelift.task(configuration, JavaServerOptionCapabilityCheck.class).execute().await()) {
                    opts.add("-server");
                }
                opts.add("-Xms64m");
                opts.add("-Xmx512m");
            }

            opts.add("-XX:MaxPermSize=256m");
            opts.add("-Djava.net.preferIPv4Stack=true");
            opts.add("-Djboss.modules.system.pkgs=org.jboss.byteman");
            opts.add("-Djava.awt.headless=true");
            opts.add("-Djboss.modules.policy-permissions=true");

            if (configuration.isDomain()) {
                opts.add("-Djboss.domain.default.config=" + configuration.getDomainConfig());
                opts.add("-Djboss.host.default.config=" + configuration.getHostConfig());
            } else {
                opts.add("-Djboss.server.default.config=" + configuration.getStandaloneConfig());
            }

            return opts;
        }
    },
    WILDFLY {
        @Override
        public List<String> javaOptions(JBossManagerConfiguration configuration) {
            List<String> opts = new ArrayList<String>();
            if (!configuration.isDomain()) {
                opts.add("-server");
            } else if (Spacelift.task(configuration, JavaServerOptionCapabilityCheck.class).execute().await()) {
                opts.add("-server");
            }

            opts.add("-Xms64m");
            opts.add("-Xmx512m");
            opts.add("-XX:MaxPermSize=256m");
            opts.add("-Djava.net.preferIPv4Stack=true");
            opts.add("-Djboss.modules.system.pkgs=org.jboss.byteman");
            opts.add("-Djava.awt.headless=true");

            if (configuration.isDomain()) {
                opts.add("-Djboss.domain.default.config=" + configuration.getDomainConfig());
                opts.add("-Djboss.host.default.config=" + configuration.getHostConfig());
            } else {
                opts.add("-Djboss.server.default.config=" + configuration.getStandaloneConfig());
            }

            return opts;
        }
    };

    public abstract List<String> javaOptions(JBossManagerConfiguration managerConfiguration);
}
