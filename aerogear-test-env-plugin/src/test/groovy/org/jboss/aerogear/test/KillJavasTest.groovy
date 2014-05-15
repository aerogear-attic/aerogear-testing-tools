package org.jboss.aerogear.test;

import org.arquillian.spacelift.execution.Tasks
import org.arquillian.spacelift.process.ProcessResult
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.jboss.aerogear.test.utils.KillJavas
import org.junit.Test

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.assertThat

public class KillJavasTest {

    @Test
    public void killJavas() {
        Project project = ProjectBuilder.builder().build()

        project.apply plugin: 'aerogear-test-env'

        project.aerogearTestEnv {
            tools {
            }
            profiles {
            }
            installations {
            }
            tests {
            }
        }

        // initialize current project tools - this is effectively init-tools task
        GradleSpacelift.currentProject(project)

        // kill servers
        ProcessResult result = Tasks.prepare(KillJavas).execute().await()
        //assertThat result.exitValue(), is(0)
    }
}
