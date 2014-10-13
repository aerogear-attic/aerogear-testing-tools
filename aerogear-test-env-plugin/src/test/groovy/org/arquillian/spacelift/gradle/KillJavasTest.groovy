package org.arquillian.spacelift.gradle;

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.assertThat

import org.arquillian.spacelift.execution.Tasks
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.arquillian.spacelift.gradle.utils.KillJavas
import org.junit.Test

public class KillJavasTest {

    @Test
    public void killJavas() {
        Project project = ProjectBuilder.builder().build()

        project.apply plugin: 'spacelift'

        project.spacelift {
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
        Tasks.prepare(KillJavas).execute().await()

    }
}
