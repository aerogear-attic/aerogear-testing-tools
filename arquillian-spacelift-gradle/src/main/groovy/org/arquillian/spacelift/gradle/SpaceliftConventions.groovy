package org.arquillian.spacelift.gradle

import org.gradle.api.Project

/**
 * Defines a default configuration for Aerogear Test Environment Plugin
 *
 */
class SpaceliftConventions {

    // workspace configuration
    File workspace

    // base path to get local binaries
    File installationsDir

    // flag to kill already running servers
    boolean killServers

    // local Maven repository
    File localRepository

    // keystore and truststore files
    File keystoreFile
    File truststoreFile

    // staging JBoss repository
    boolean enableStaging

    // snapshots JBoss repository
    boolean enableSnapshots

    SpaceliftConventions(Project project) {
        this.workspace = project.rootDir
        this.installationsDir = new File(workspace, "installations")

        this.localRepository = new File(workspace, ".repository")
        this.keystoreFile = new File(project.rootDir, "patches/certs/aerogear.keystore")
        this.truststoreFile = new File(project.rootDir, "patches/certs/aerogear.truststore")
        this.enableStaging = false
        this.enableSnapshots = false
    }

    def setWorkspace(workspace) {
        // update also dependant repositories when workspace is updated
        if(localRepository.parentFile == this.workspace) {
            this.localRepository = new File(workspace, ".repository")
        }
        // update also dependant repositories when workspace is updated
        if(installationsDir.parentFile == this.workspace) {
            this.installationsDir = new File(workspace, "installations")
        }

        this.workspace = workspace
    }
}
