package org.arquillian.spacelift.gradle.java

import org.arquillian.spacelift.execution.Task
import org.arquillian.spacelift.gradle.GradleSpacelift

class JavaExecutor extends Task<Object, Void> {

    def workingDir

    def parameters = []

    def jarFile

    JavaExecutor parameter(CharSequence parameter) {
        parameters << parameter
        this
    }

    JavaExecutor parameters(CharSequence...parameters) {
        this.parameters.addAll(parameters)
        this
    }

    JavaExecutor jarFile(String file) {
        this.jarFile = file
        this
    }

    JavaExecutor workingDir(String dir) {
        this.workingDir = dir
        this
    }

    @Override
    protected Void process(Object input) throws Exception {

        def command = GradleSpacelift.tools('java')

        if(workingDir) {
            command.workingDir(workingDir)
        }

        if (jarFile) {
            command.parameters('-jar', jarFile)
        }

        command.parameters(parameters)
        command.interaction(GradleSpacelift.ECHO_OUTPUT).execute().await()

        return null;
    }
}
