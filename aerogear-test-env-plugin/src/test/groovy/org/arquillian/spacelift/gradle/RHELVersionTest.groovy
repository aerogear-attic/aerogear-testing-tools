package org.arquillian.spacelift.gradle;

import static org.junit.Assert.*;

import org.arquillian.spacelift.execution.Tasks;
import org.arquillian.spacelift.execution.impl.DefaultExecutionServiceFactory
import org.arquillian.spacelift.gradle.utils.RHELVersion;
import org.junit.Test;

class RHELVersionTest {

    @Test
    public void testRHELVersion() {
		
		Tasks.setDefaultExecutionServiceFactory(new DefaultExecutionServiceFactory())
		

        def rhelVersionFile = new File("/etc/redhat-release")

        def version = Tasks.prepare(RHELVersion).execute().await()

        if (version.equals("no RHEL")) {
            assertTrue (!rhelVersionFile.exists() || !contains("Red Hat", rhelVersionFile))
        } else {
            assertTrue contains(version, rhelVersionFile)
        }
    }

    def boolean contains(release, file) {

        def found = false

        file.eachLine {
            if (it.contains(release)) {
                found = true
            }
        }

        found
    }
}
