package org.arquillian.spacelift.gradle

import org.eclipse.jdt.core.dom.ThisExpression;
import org.gradle.api.Project

class Test {

    // required by gradle to be defined
    final String name

    final String testName

    def execute

    def dataProvider

    def beforeSuite

    def beforeTest

    def afterSuite

    def afterTest

    private Project project

    Test(String testName, Project project) {
        this.name = this.testName = testName
        this.project = project
    }

    def executeTest() {

        if (beforeSuite) {
            beforeSuite.delegate = this
            beforeSuite.doCall()
        }

        if (execute) {

            if (dataProvider) {
                dataProvider.delegate = this
                dataProvider.doCall().each { data ->

                    if (beforeTest) {
                        beforeTest.delegate = this
                        beforeTest.doCall(data)
                    }

                    execute.delegate = this
                    execute.doCall(data)

                    if (afterTest) {
                        afterTest.delegate = this
                        afterTest.doCall(data)
                    }
                }
            } else {

                if (beforeTest) {
                    beforeTest.delegate = this
                    beforeTest.doCall()
                }

                execute.delegate = this
                execute.doCall()

                if (afterTest) {
                    afterTest.delegate = this
                    afterTest.doCall()
                }
            }

        }

        if (afterSuite) {
            afterSuite.delegate = this
            afterSuite.doCall()
        }
    }

    def execute(Closure closure) {
        this.execute = closure
    }

    def dataProvider(Closure closure) {
        this.dataProvider = closure
    }

    def beforeSuite(Closure closure) {
        this.beforeSuite = closure
    }

    def beforeTest(Closure closure) {
        this.beforeTest = closure
    }

    def afterSuite(Closure closure) {
        this.afterSuite = closure
    }

    def afterTest(Closure closure) {
        this.afterTest = closure
    }
}
