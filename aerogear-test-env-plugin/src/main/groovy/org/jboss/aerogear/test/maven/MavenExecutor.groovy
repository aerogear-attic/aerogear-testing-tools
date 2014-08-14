package org.jboss.aerogear.test.maven

import java.text.MessageFormat

import org.arquillian.spacelift.execution.Task
import org.arquillian.spacelift.process.Command
import org.arquillian.spacelift.process.CommandBuilder
import org.jboss.aerogear.test.GradleSpacelift
import org.jboss.aerogear.test.utils.EnvironmentUtils
import org.slf4j.LoggerFactory

class MavenExecutor extends Task<Object, Void>{

    def static final log = LoggerFactory.getLogger('MavenExecutor')

    def projectPom

    def workingDir

    def batchMode = true

    def nonRecursive = false

    def debug = false

    def settingsXml

    def goals = []

    def profiles = []

    def properties = []

    def env = [:]

    private def command = []

    MavenExecutor() {
        def project = GradleSpacelift.currentProject()
        if (new File("${project.aerogearTestEnv.workspace}/settings.xml").exists()) {
            this.settingsXml = "${project.aerogearTestEnv.workspace}/settings.xml"
            properties << "org.apache.maven.user-settings=${project.aerogearTestEnv.workspace}/settings.xml"
        }
    }

    @Override
    protected Void process(Object input) throws Exception {

        def command = GradleSpacelift.tools('mvn')

        if (batchMode) {
            command.parameter('-B')
        }

        if(nonRecursive) {
            command.parameter('-N')
        }

        if(debug) {
            command.parameter('-X')
        }

        command.parameters(getProfiles())

        if (projectPom) {
            command.parameter('-f')
            command.parameter(projectPom)
        }

        if (settingsXml) {
            command.parameter('-s')
            command.parameter(settingsXml)
        }

        command.addEnvironment(env)

        command.parameters(goals)
        command.parameters(getProperties())

        if(workingDir) {
            command.workingDir(workingDir)
        }

        command.interaction(GradleSpacelift.ECHO_OUTPUT).execute().await()

        return null;
    }

    def pom(projectPom) {
        this.projectPom = projectPom
        this
    }

    def withoutSubprojects() {
        this.nonRecursive = true
        this
    }

    def withoutBatchMode() {
        this.batchMode = false
        this
    }

    def workingDir(dir) {
        this.workingDir = (dir instanceof File) ? dir.getAbsolutePath() : dir;
        this
    }

    def settings(settingsXml) {
        this.settingsXml = settingsXml
        this
    }

    def goal(goal) {
        this.goals.add(goal)
        this
    }

    def goals(CharSequence...goals) {
        this.goals.addAll(goals)
        this
    }

    def property(property) {
        this.properties.add(property)
        this
    }

    def properties(CharSequence...properties) {
        this.properties.addAll(properties)
        this
    }

    def profile(profile) {
        this.profiles.add(profile)
        this
    }

    def profiles(CharSequence...profiles) {
        this.profiles.addAll(profiles)
        this
    }

    def androidHome(androidHome) {
        this.env << [ANDROID_HOME:androidHome.toString()]
        this
    }

    def androidSdkHome(androidSdkHome) {
        this.env << [ANDROID_SDK_HOME:androidSdkHome.toString()]
        this
    }

    def androidTarget(target) {
        // TODO identify all possible combinations for Android Target settings
        this.properties << "arq.group.containers.container.android.configuration.target=${target}"
        this
    }

    def jbossHome(jbossHome) {
        this.properties << "arq.container.main-server-group.configuration.jbossHome=${jbossHome}"
        this.properties << "arq.group.jboss.container.domain-controller.configuration.jbossHome=${jbossHome}"
        this
    }
    
    def surefireSuffix(suffix) {
        this.properties << "surefire.reportNameSuffix=${suffix}"
        this
    }

    def ignoreTestFailures() {
        this.properties << "maven.test.failure.ignore=true"
        this
    }

    private def getProfiles() {
        def profs = []
        this.profiles.each { p ->
            profs << "-P" + p
        }
        profs
    }

    private def getProperties() {
        def props = []
        this.properties.each { p -> props << "\"-D${p}\"" }
        props
    }
}
