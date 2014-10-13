package org.arquillian.spacelift.gradle.container.db.module

import org.arquillian.spacelift.gradle.container.db.DatabaseModule
import org.jboss.aerogear.test.container.spacelift.JBossCLI
import org.arquillian.spacelift.gradle.maven.*
import org.arquillian.spacelift.execution.Tasks


class PostgreSQLDatabaseModule extends DatabaseModule {

    private static final def POSTGRESQL_VERSION = "9.3-1100"

    PostgreSQLDatabaseModule(String name, String jbossHome, String destination) {
        super(name, jbossHome, destination)
    }

    PostgreSQLDatabaseModule(String name, File jbossHome, File destination) {
        super(name, jbossHome.getAbsolutePath(), destination.getAbsolutePath())
    }

    @Override
    def install() {

        def resolvedVersion = version
        
        if (!resolvedVersion) {
            resolvedVersion = POSTGRESQL_VERSION
        }
        
        if (!new File("${destination}/postgresql-${resolvedVersion}-jdbc41.jar").exists()) {
            Tasks.prepare(MavenExecutor)
                    .goal("dependency:copy")
                    .property("artifact=org.postgresql:postgresql:${resolvedVersion}-jdbc41")
                    .property("outputDirectory=${destination}")
                    .execute().await()
        }

        if (! new File(jbossHome + "/modules/org/postgresql").exists()) {
            
            startContainer()
            
            Tasks.prepare(JBossCLI)
                    .environment("JBOSS_HOME", jbossHome)
                    .connect()
                    .cliCommand("module add --name=org.postgresql --resources=${destination}/postgresql-${resolvedVersion}-jdbc41.jar --dependencies=javax.api,javax.transaction.api")
                    .execute().await()

            stopContainer()
        }
    }

    @Override
    def uninstall() {
        startContainer()

        Tasks.prepare(JBossCLI).environment("JBOSS_HOME", jbossHome).connect().cliCommand("module remove --name=org.postgresql").execute().await()

        stopContainer()
    }
}
