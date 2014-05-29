package org.jboss.aerogear.test.container.db.module

import org.jboss.aerogear.test.container.db.DatabaseModule
import org.jboss.aerogear.test.container.manager.ManagedContainerConfiguration
import org.jboss.aerogear.test.container.spacelift.JBossCLI
import org.jboss.aerogear.test.container.spacelift.JBossStarter;
import org.jboss.aerogear.test.container.spacelift.JBossStopper;
import org.jboss.aerogear.test.maven.*
import org.arquillian.spacelift.execution.Tasks


class PostgreSQLDatabaseModule extends DatabaseModule {

    //private static final String POSTGRESQL_VERSION = "9.2-1004"

    PostgreSQLDatabaseModule(String name, String jbossHome, String destination) {
        super(name, jbossHome, destination)
    }

    PostgreSQLDatabaseModule(String name, File jbossHome, File destination) {
        super(name, jbossHome.getAbsolutePath(), destination.getAbsolutePath())
    }

    @Override
    def install() {

        if (!new File("${destination}/postgresql-${version}-jdbc41.jar").exists()) {
            Tasks.prepare(MavenExecutor)
                    .goal("dependency:copy")
                    .property("artifact=org.postgresql:postgresql:${version}-jdbc41")
                    .property("outputDirectory=${destination}")
                    .execute().await()
        }

        if (! new File(jbossHome + "/modules/org/postgresql").exists()) {
            
            startContainer()

            Tasks.prepare(JBossCLI)
                    .environment("JBOSS_HOME", jbossHome)
                    .connect()
                    .cliCommand("module add --name=org.postgresql --resources=${destination}/postgresql-${version}-jdbc41.jar --dependencies=javax.api,javax.transaction.api")
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
