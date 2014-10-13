package org.arquillian.spacelift.gradle.db

import org.jboss.aerogear.test.container.manager.ManagedContainerConfiguration
import org.jboss.aerogear.test.container.spacelift.JBossStarter;
import org.jboss.aerogear.test.container.spacelift.JBossStopper;
import org.jboss.aerogear.test.container.spacelift.JBossCLI;
import org.arquillian.spacelift.execution.Tasks

class UpdateJBossDatasource {

    def jbossHome = System.getenv("JBOSS_HOME")

    def script

    def shouldStartContainer = false

    def withScript(String script) {
        this.script = script
        this
    }

    def withScript(File script) {
        withScript(script.getAbsolutePath())
    }

    def withJBossHome(File jbossHome) {
        withJBossHome(jbossHome.getAbsoluteFile())
    }

    def withJBossHome(String jbossHome) {
        this.jbossHome = jbossHome
        this
    }

    def shouldStartContainer() {
        shouldStartContainer = true
        this
    }

    def update() {

        def manager

        if (shouldStartContainer) {
            manager = Tasks.prepare(JBossStarter)
                    .configuration(new ManagedContainerConfiguration().setJbossHome(jbossHome))
                    .execute()
                    .await()
        }

        Tasks.prepare(JBossCLI)
                .environment("JBOSS_HOME", jbossHome)
                .file(script)
                .execute()
                .await()

        if (shouldStartContainer) {
            Tasks.chain(manager, JBossStopper).execute().await()
        }

        this
    }
}
