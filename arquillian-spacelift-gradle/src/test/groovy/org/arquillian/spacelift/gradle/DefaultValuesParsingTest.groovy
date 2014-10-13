package org.arquillian.spacelift.gradle

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.assertThat

/**
 * Ensures that setting default value reflect to values available in project
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 *
 */
class DefaultValuesParsingTest {

    @Test
    public void parseDefaultValues() {

        Project project = ProjectBuilder.builder().build()

        project.ext.set("defaultAndroidTargets", ["19", "18", "google-17"])

        project.apply plugin: 'spacelift'

        // find android targets from default value
        def androidTargets = project.androidTargets
        assertThat androidTargets, is(notNullValue())
        assertThat androidTargets[0], is("19")
    }

    @Test
    public void overrideDefaultValues() {

        Project project = ProjectBuilder.builder().build()

        project.ext.set("defaultAndroidTargets", ["19", "18", "google-17"])
        project.androidTargets = "18, 19"

        project.apply plugin: 'spacelift'

        // find android targets from default value
        def androidTargets = project.androidTargets
        assertThat androidTargets, is(notNullValue())
        assertThat androidTargets[0], is("18")
    }
}
