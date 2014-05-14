package org.jboss.aerogear.test

import org.gradle.api.Project

class Test {

    // required by gradle to be defined
    final String name

    final String testName

    def execute

    private Project project

    Test(String testName, Project project) {
        this.name = this.testName = testName
        this.project = project
    }

    def executeTest(boolean onAndroid) {
        if (execute) {
            if (onAndroid) {
                project.androidTargets.each { androidTarget ->
                    println "execution on Android target: ${androidTarget}"
                    execute.delegate = this
                    execute.doCall(androidTarget)
                }
            } else {
                execute.delegate = this
                execute.doCall()
            }
        }
    }

    def execute(Closure closure) {
        this.execute = closure
    }
}
