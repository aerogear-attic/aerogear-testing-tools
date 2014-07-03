package org.jboss.aerogear.test

import org.arquillian.spacelift.execution.Tasks
import org.arquillian.spacelift.process.ProcessResult
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.jboss.aerogear.test.openshift.CreateOpenshiftCartridge;
import org.jboss.aerogear.test.utils.KillJavas
import org.junit.Ignore;
import org.junit.Test

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.assertThat

/**
 * Asserts that installations can be specified without home
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 *
 */
@Ignore
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
}