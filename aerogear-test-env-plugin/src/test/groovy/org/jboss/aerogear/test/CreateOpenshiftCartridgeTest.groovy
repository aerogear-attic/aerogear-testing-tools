package org.jboss.aerogear.test

import org.arquillian.spacelift.execution.Tasks
import org.arquillian.spacelift.process.ProcessResult
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.jboss.aerogear.test.openshift.CreateOpenshiftCartridge;
import org.jboss.aerogear.test.utils.KillJavas
import org.junit.Test

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.assertThat

/**
 * Creates OpenShift cartridge. Requires to have authorization token for rhc tools
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 *
 */
public class CreateOpenshiftCartridgeTest {

    @Test
    public void createCart() {
        Project project = ProjectBuilder.builder().build()

        project.apply plugin: 'aerogear-test-env'

        project.aerogearTestEnv {
            tools { rhc { command = "rhc" } }
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
        Tasks.prepare(CreateOpenshiftCartridge)
                .named('foobar')
                .at('mobileqa')
                .sized("small")
                .cartridges("https://cartreflect-claytondev.rhcloud.com/reflect?github=matzew/openshift-origin-cartridge-aerogear-push", "mysql-5.5")
                .force()
                .execute().await()
    }
}