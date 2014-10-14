package org.arquillian.spacelift.gradle

import org.arquillian.spacelift.execution.Tasks
import org.arquillian.spacelift.process.ProcessResult
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.arquillian.spacelift.gradle.openshift.CreateOpenshiftCartridge;
import org.arquillian.spacelift.gradle.utils.KillJavas
import org.junit.Ignore;
import org.junit.Test

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.assertThat

/**
 * Asserts that installations can be specified without home
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 *
 */
public class ProfileParsingTest {

    @Test
    public void profilesWithoutTests() {
        Project project = ProjectBuilder.builder().build()

        project.apply plugin: 'spacelift'

        project.spacelift {
            tools { 
                rhc { command = "rhc" } 
            }
            profiles {
                foobar { enabledInstallations = ["eap"] }
            }
            installations { 
                eap {
                } 
            }
            tests {
            }
        }

        // initialize current project tools - this is effectively init-tools task
        GradleSpacelift.currentProject(project)

        project.spacelift.installations.each { installation ->
            assertThat installation.home, is(notNullValue())
            assertThat installation.home.exists(), is(true)
        }
    }
}
