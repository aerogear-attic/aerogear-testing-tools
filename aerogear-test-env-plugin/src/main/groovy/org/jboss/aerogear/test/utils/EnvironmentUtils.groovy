package org.jboss.aerogear.test.utils

import org.apache.commons.lang3.SystemUtils

/**
 * Utility methods for determining user or OS these scripts runs at.
 *
 */
class EnvironmentUtils {

    static def runsOnWindows() {
        SystemUtils.IS_OS_WINDOWS
    }

    static def runsOnLinux() {
        SystemUtils.IS_OS_LINUX
    }
    
    static runsOnHudson() {
        System.getProperty("user.name") ==~ /hudson/
    }
}
