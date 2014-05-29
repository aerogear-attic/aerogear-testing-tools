package org.jboss.aerogear.test.container.db

import org.jboss.aerogear.test.container.manager.ManagedContainerConfiguration
import org.jboss.aerogear.test.container.spacelift.JBossStarter;
import org.jboss.aerogear.test.container.spacelift.JBossStopper;
import org.arquillian.spacelift.execution.Tasks

abstract class DatabaseModule {

    def String jbossHome

    def String name

    def String destination

    def String version

    def startContainer = false

    private def manager

    /**
     * 
     * @param name just some identifier
     * @param jbossHome location of container you want to add this module to 
     * @param destination where to download / save resolved artifact
     */
    DatabaseModule(String name, String jbossHome, String destination) {
        this.name = name
        this.jbossHome = jbossHome
        this.destination = destination
    }

    def version(String version) {
        this.version = version
        this
    }

    def shouldStartContainer() {
        startContainer = true
        this
    }

    def startContainer() {
        if (startContainer) {
            manager = Tasks.prepare(JBossStarter).configuration(new ManagedContainerConfiguration().setJbossHome(jbossHome)).execute().await()
        }
    }

    def stopContainer() {
        if (startContainer && manager) {
            Tasks.chain(manager, JBossStopper).execute().await()
        }
    }

    abstract def install()

    abstract def uninstall()
}
