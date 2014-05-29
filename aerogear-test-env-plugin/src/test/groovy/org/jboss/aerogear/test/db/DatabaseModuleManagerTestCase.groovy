package org.jboss.aerogear.test.db;

import org.jboss.aerogear.test.GradleSpacelift
import org.arquillian.spacelift.process.impl.CommandTool
import org.arquillian.spacelift.execution.Tasks

import static org.junit.Assert.*;
import static org.junit.Assume.*;

import org.arquillian.spacelift.process.CommandBuilder
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.jboss.aerogear.test.container.db.DatabaseModulesManager;
import org.jboss.aerogear.test.container.db.module.MySQLDatabaseModule;
import org.jboss.aerogear.test.container.db.module.PostgreSQLDatabaseModule
import org.jboss.aerogear.test.container.manager.ManagedContainerConfiguration
import org.jboss.aerogear.test.container.spacelift.JBossStarter;
import org.jboss.aerogear.test.container.spacelift.JBossStopper;
import org.junit.Test;

class DatabaseModuleManagerTestCase {

    @Test
    public void moduleManagerTest() {

        def jbossHome = System.getenv("JBOSS_HOME")

        assumeNotNull(jbossHome)

        Project project = ProjectBuilder.builder().build()

        project.apply plugin: 'aerogear-test-env'

        project.aerogearTestEnv {
            tools {
                mvn { command = { Tasks.prepare(CommandTool).command(new CommandBuilder("mvn")) }}
            }
            profiles {
            }
            installations {
            }
            tests {
            }
        }

        GradleSpacelift.currentProject(project)

        DatabaseModulesManager moduleManager = new DatabaseModulesManager()

        def jbossManager = Tasks.prepare(JBossStarter).configuration(new ManagedContainerConfiguration().setJbossHome(jbossHome)).execute().await()

        moduleManager.add(new MySQLDatabaseModule("mysql51", jbossHome, jbossHome).version("5.1.18"))
        moduleManager.add(new PostgreSQLDatabaseModule("postgresql", jbossHome, jbossHome).version("9.2-1004"))

        moduleManager.installAll().uninstallAll()

        Tasks.chain(jbossManager, JBossStopper).execute().await()
    }
}
