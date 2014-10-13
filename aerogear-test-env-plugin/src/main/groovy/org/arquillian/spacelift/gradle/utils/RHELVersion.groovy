package org.arquillian.spacelift.gradle.utils

import org.arquillian.spacelift.execution.Task
import org.arquillian.spacelift.execution.Tasks
import org.arquillian.spacelift.process.CommandBuilder
import org.arquillian.spacelift.process.impl.CommandTool

class RHELVersion extends Task<Object, String> {

    @Override
    protected String process(Object input) throws Exception {
        
        def version = "no RHEL"
        
        if (!EnvironmentUtils.runsOnLinux()) {
            return version
        }
        
        def rhelVersion = Tasks.prepare(CommandTool).command(new CommandBuilder('cat'))
                .parameters('/etc/redhat-release')
                .shouldExitWith(0,1)
                .execute()
                .await()
                .output()

        if (!rhelVersion.isEmpty()) {
            if (rhelVersion.get(0).contains("Red Hat")) {
                if (rhelVersion.get(0).contains(" 6")) {
                    version = "6"
                } else if (rhelVersion.get(0).contains(" 7")) {
                    version = "7"
                }
            }
        }

        version
    }
}
