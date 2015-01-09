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

import org.arquillian.spacelift.execution.Tasks;
import org.jboss.aerogear.test.container.manager.JBossManagerConfiguration;

/**
 * Resolves JAVA_OPTS for Java process which starts container according to the container type.
 *
 * Default values of JAVA_OPTS slightly differ from one container to another. To mimic the starting process as much as possible
 * with the propper options, we provide this helper class.
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class ContainerJavaOptsConfiguration {

    private static final String SPACE = " ";

    public static final String getContainerJavaOpts(JBossManagerConfiguration configuration) {

        CONTAINER_TYPE type = configuration.getContainerType();

        if (type == null) {
            throw new IllegalStateException("Type of container to get Java options for is a null object!");
        }

        switch (type) {
            case AS7:
                return getAS7JavaOpts(configuration);
            case EAP:
                return getEAPJavaOpts(configuration);
            case WILDFLY:
                return getWildFlyJavaOpts(configuration);
            default:
                throw new IllegalStateException("Unable to get Java options for container of type " + type.name());
        }
    }

    private static String getWildFlyJavaOpts(JBossManagerConfiguration configuration) {
        StringBuilder sb = new StringBuilder();

        if (!configuration.isDomain()) {
            sb.append("-server").append(SPACE);
        } else if (Tasks.chain(configuration, JavaServerOptionCapabilityCheck.class).execute().await()) {
            sb.append("-server").append(SPACE);
        }

        sb.append("-Xms64m").append(SPACE)
            .append("-Xmx512m").append(SPACE)
            .append("-XX:MaxPermSize=256m").append(SPACE)
            .append("-Djava.net.preferIPv4Stack=true").append(SPACE)
            .append("-Djboss.modules.system.pkgs=org.jboss.byteman").append(SPACE)
            .append("-Djava.awt.headless=true");

        if (configuration.isDomain()) {
            sb.append("-Djboss.domain.default.config=" + configuration.getDomainConfig()).append(SPACE)
                .append("-Djboss.host.default.config=" + configuration.getHostConfig());
        } else {
            sb.append("-Djboss.server.default.config=" + configuration.getStandaloneConfig());
        }

        return sb.toString();
    }

    private static String getEAPJavaOpts(JBossManagerConfiguration configuration) {
        StringBuilder sb = new StringBuilder();

        if (!configuration.isDomain()) {
            sb.append("-server").append(SPACE)
                .append("-XX:+UseCompressedOops").append(SPACE)
                .append("-verbose:gc").append(SPACE)
                .append("-Xloggc:\"" + configuration.getJBossLogDir() + "/gc.log\"").append(SPACE)
                .append("-XX:+PrintGCDetails").append(SPACE)
                .append("-XX:+PrintGCDateStamps").append(SPACE)
                .append("-XX:+UseGCLogFileRotation").append(SPACE)
                .append("-XX:NumberOfGCLogFiles=5").append(SPACE)
                .append("-XX:GCLogFileSize=3M").append(SPACE)
                .append("-XX:-TraceClassUnloading").append(SPACE)
                .append("-Xms1303m").append(SPACE)
                .append("-Xmx1303m").append(SPACE);
        } else {
            if (Tasks.chain(configuration, JavaServerOptionCapabilityCheck.class).execute().await()) {
                sb.append("-server").append(SPACE);
            }
            sb.append("-Xms64m").append(SPACE).append("-Xmx512m").append(SPACE);
        }

        sb.append("-XX:MaxPermSize=256m").append(SPACE)
            .append("-Djava.net.preferIPv4Stack=true").append(SPACE)
            .append("-Djboss.modules.system.pkgs=org.jboss.byteman").append(SPACE)
            .append("-Djava.awt.headless=true").append(SPACE)
            .append("-Djboss.modules.policy-permissions=true").append(SPACE);

        if (configuration.isDomain()) {
            sb.append("-Djboss.domain.default.config=" + configuration.getDomainConfig()).append(SPACE)
                .append("-Djboss.host.default.config=" + configuration.getHostConfig());
        } else {
            sb.append("-Djboss.server.default.config=" + configuration.getStandaloneConfig());
        }

        return sb.toString();
    }

    private static String getAS7JavaOpts(JBossManagerConfiguration configuration) {
        StringBuilder sb = new StringBuilder();

        if (!configuration.isDomain()) {
            sb.append("-server").append(SPACE)
                .append("-XX:+UseCompressedOops").append(SPACE)
                .append("-XX:+TieredCompilation").append(SPACE);
        } else if (Tasks.chain(configuration, JavaServerOptionCapabilityCheck.class).execute().await()) {
            sb.append("-server").append(SPACE);
        }

        sb.append("-Xms64m").append(SPACE)
            .append("-Xmx512m").append(SPACE)
            .append("-XX:MaxPermSize=256m").append(SPACE)
            .append("-Djava.net.preferIPv4Stack=true").append(SPACE)
            .append("-Dorg.jboss.resolver.warning=true").append(SPACE)
            .append("-Dsun.rmi.dgc.client.gcInterval=3600000").append(SPACE)
            .append("-Dsun.rmi.dgc.server.gcInterval=3600000").append(SPACE)
            .append("-Djboss.modules.system.pkgs=org.jboss.byteman").append(SPACE)
            .append("-Djava.awt.headless=true").append(SPACE);

        if (configuration.isDomain()) {
            sb.append("-Djboss.domain.default.config=" + configuration.getDomainConfig()).append(SPACE)
                .append("-Djboss.host.default.config=" + configuration.getHostConfig());
        } else {
            sb.append("-Djboss.server.default.config=" + configuration.getStandaloneConfig());
        }

        return sb.toString();
    }
}
