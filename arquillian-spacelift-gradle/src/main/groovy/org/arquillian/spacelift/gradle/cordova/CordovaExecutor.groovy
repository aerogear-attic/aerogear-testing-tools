package org.arquillian.spacelift.gradle.cordova

import org.arquillian.spacelift.execution.Task
import org.arquillian.spacelift.execution.Tasks
import org.arquillian.spacelift.process.CommandBuilder
import org.arquillian.spacelift.process.impl.CommandTool
import org.arquillian.spacelift.gradle.GradleSpacelift

class CordovaExecutor extends Task<Object, Void> {

    def parameters
    def dir
    def env = [:]
    def sdkHome

    def setParameters(parameters) {
        this.parameters = parameters
        this
    }

    def setDir(dir) {
        this.dir = dir
        this
    }

    def androidHome(androidHome) {
        this.env << [ANDROID_HOME:androidHome.toString()]
        this
    }

    def androidSdkHome(androidSdkHome) {
        this.env << [ANDROID_SDK_HOME:androidSdkHome.toString()]
        this.sdkHome = androidSdkHome
        this
    }

    @Override
    protected Void process(Object input) throws Exception {
        def sep = System.getProperty("path.separator")

        Tasks.prepare(CommandTool)
                .command(new CommandBuilder("cordova").parameters(parameters.split(" ")))
                .workingDir(dir)
                .addEnvironment(env)
                .addEnvironment("PATH", "${sdkHome}/tools${sep}${sdkHome}/platform-tools${sep}${System.getenv("PATH")}")
                .interaction(GradleSpacelift.ECHO_OUTPUT)
                .shouldExitWith(0,1)
                .execute().await()

        return null
    }
}
