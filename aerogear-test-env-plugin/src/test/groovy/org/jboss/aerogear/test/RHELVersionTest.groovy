package org.jboss.aerogear.test;

import static org.junit.Assert.*;

import org.arquillian.spacelift.execution.Tasks;
import org.jboss.aerogear.test.utils.RHELVersion;
import org.junit.Test;

class RHELVersionTest {

    @Test
    public void testRHELVersion() {

        def rhelVersionFile = new File("/etc/redhat-release")

        def version = Tasks.prepare(RHELVersion).execute().await()

        if (version.equals("no RHEL")) {
            assertTrue (!rhelVersionFile.exists() || !contains("Red Hat", rhelVersionFile))
        } else {
            assertTrue contains("Red Hat", rhelVersionFile)
        }
    }

    def boolean contains(release, file) {

        file.eachLine {
            if (it.contains("Red Hat")) {
                return true
            }
        }

        false
    }
}
