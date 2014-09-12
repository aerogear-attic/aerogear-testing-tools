package org.jboss.aerogear.test

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.assertThat

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.jboss.as.controller.operations.common.SystemPropertyAddHandler;
import org.junit.Test

/**
 * Asserts that installations can be specified without home
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 *
 */
public class InstallationParsingTest {

    @Test
    public void noHomeInstallation() {
        Project project = ProjectBuilder.builder().build()

        project.apply plugin: 'aerogear-test-env'

        project.aerogearTestEnv {
            tools { rhc { command = "rhc"
                } }
            profiles {
            }
            installations { eap { } }
            tests {
            }
        }

        // initialize current project tools - this is effectively init-tools task
        GradleSpacelift.currentProject(project)

        project.aerogearTestEnv.installations.each { installation ->
            assertThat installation.home, is(notNullValue())
            assertThat installation.home.exists(), is(true)
        }
    }
    
    @Test
    public void preconditionTest() {
        
        Project project = ProjectBuilder.builder().build()
        
        project.apply plugin: 'aerogear-test-env'

        project.aerogearTestEnv {
            tools {
            }
            profiles {
            }
            tests {
            }
            installations {
                didNotMeetPreconditionInstallation {
                    preconditions {
                        false
                    }
                    postActions {
                        new File(System.getProperty("java.io.tmpdir"), "preconditionTestFile.tmp").createNewFile()
                    }
                }
            }
        }
        
        GradleSpacelift.currentProject(project)
        
        project.aerogearTestEnv.installations.each { installation -> 
            installation.install()
        }
        
        assertThat new File(System.getProperty("java.io.tmpdir"), "preconditionTestFile.tmp").exists(), is(false)
    }
}