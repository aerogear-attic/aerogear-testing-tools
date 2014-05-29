package org.jboss.aerogear.test;

import static org.junit.Assert.*;

import org.junit.Test;
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder

class TestExecutionLifecycleTestCase {

    @Test
    public void dataProviderTest() {
        Project project = ProjectBuilder.builder().build()

        project.apply plugin: 'aerogear-test-env'

        project.aerogearTestEnv {
            tests {
                bar {
                    dataProvider {
                        ["first", "second"]
                    }
                    beforeSuite {
                        println "beforeSuite"
                    }
                    beforeTest {
                        println "beforeTest"
                    }
                    execute { value ->
                        println value
                    }
                    afterTest {
                        println "after test"
                    }
                    afterSuite {
                        println "after suite"
                    }
                }
            }
            tools {
            }
            profiles {
            }
            installations {
            }
        }
        
        GradleSpacelift.currentProject(project)
        
        project.aerogearTestEnv.tests.each { test ->
            test.executeTest()
        }
    }
}
