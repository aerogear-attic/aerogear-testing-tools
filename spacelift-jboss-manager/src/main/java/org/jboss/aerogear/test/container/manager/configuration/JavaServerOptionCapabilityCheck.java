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

import java.util.List;

import org.apache.commons.lang3.SystemUtils;
import org.arquillian.spacelift.Spacelift;
import org.arquillian.spacelift.process.CommandBuilder;
import org.arquillian.spacelift.task.Task;
import org.arquillian.spacelift.task.os.CommandTool;
import org.jboss.aerogear.test.container.manager.JBossManagerConfiguration;

/**
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class JavaServerOptionCapabilityCheck extends Task<JBossManagerConfiguration, Boolean> {

    private static final String[] JAVA_VENDORS = { "HotSpot", "OpenJDK", "IBM J9" };

    @Override
    protected Boolean process(JBossManagerConfiguration configuration) throws Exception {

        List<String> javaVersionOutput = Spacelift.task(CommandTool.class)
            .command(new CommandBuilder(configuration.getJavaBin()).build())
            .parameter("-version")
            .execute()
            .await()
            .output();

        boolean isServerCapable = false;

        for (String javaVendor : JAVA_VENDORS) {
            isServerCapable = checkServerOptionCapability(javaVendor, javaVersionOutput);
            if (isServerCapable) {
                break;
            }
        }

        // according to domain.sh script, -server option is not supported for MacOS
        if (isServerCapable && !(SystemUtils.IS_OS_MAC || SystemUtils.IS_OS_MAC_OSX)) {
            return true;
        }

        return false;
    }

    private boolean checkServerOptionCapability(String javaVendor, List<String> javaVersionOutput) {

        for (String line : javaVersionOutput) {
            if (line != null && line.length() != 0) {
                if (line.toLowerCase().contains(javaVendor.toLowerCase())) {
                    return true;
                }
            }
        }

        return false;
    }

}
