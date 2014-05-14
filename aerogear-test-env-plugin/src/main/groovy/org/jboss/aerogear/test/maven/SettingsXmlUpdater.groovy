package org.jboss.aerogear.test.maven

import java.text.MessageFormat

import org.arquillian.spacelift.execution.Task
import org.arquillian.spacelift.execution.Tasks
import org.jboss.aerogear.test.GradleSpacelift
import org.jboss.aerogear.test.xml.XmlFileLoader
import org.jboss.aerogear.test.xml.XmlTextLoader;
import org.jboss.aerogear.test.xml.XmlUpdater;

class SettingsXmlUpdater extends Task<Object, Void> {

    def static final PROFILE_TEMPLATE = '''
        <profile>
            <id>{0}</id>

            <repositories>
                <repository>
                    <id>{0}</id>
                    <name>{0}</name>
                    <url>{1}</url>
                    <layout>default</layout>
                    <releases>
                        <enabled>true</enabled>
                        <updatePolicy>never</updatePolicy>
                    </releases>
                    <snapshots>
                        <enabled>{2}</enabled>
                        <updatePolicy>never</updatePolicy>
                    </snapshots>
                </repository>
            </repositories>
            <!-- plugin repositories are required to fetch dependencies for plugins (e.g. gwt-maven-plugin) -->
            <pluginRepositories>
                <pluginRepository>
                    <id>{0}</id>
                    <name>{0}</name>
                    <url>{1}</url>
                    <layout>default</layout>
                    <releases>
                        <enabled>true</enabled>
                        <updatePolicy>never</updatePolicy>
                    </releases>
                    <snapshots>
                        <enabled>{2}</enabled>
                        <updatePolicy>never</updatePolicy>
                    </snapshots>
                </pluginRepository>
            </pluginRepositories>
        </profile>
    '''

    def static final PROFILE_ACTIVATION_TEMPLATE = '''
        <activeProfile>{0}</activeProfile>
    '''

    def settingsXmlFile

    def localRepositoryDir

    def repositories = []

    SettingsXmlUpdater() {

        def project = GradleSpacelift.currentProject()
        def ant = project.ant

        this.settingsXmlFile = new File(project.aerogearTestEnv.workspace, "settings.xml")

        // ensure there is a settings.xml file
        if(!settingsXmlFile.exists()) {
            log.warn("No settings.xml file found in ${project.aerogearTestEnv.workspace}, trying to copy the one from default location")
            def defaultFile = new File(new File(System.getProperty("user.home")), '.m2/settings.xml')

            if(!defaultFile.exists()) {
                log.warn("No settings.xml file found in ${defaultFile.getAbsolutePath()}, using fallback template")
                defaultFile = new File(project.rootDir, "patches/settings.xml-template")
            }

            ant.copy(file: "${defaultFile}", tofile: "${settingsXmlFile}")
        }

        this.localRepositoryDir = project.aerogearTestEnv.localRepository
    }

    SettingsXmlUpdater repository(repositoryId, repositoryUri, snapshotsEnabled) {
        repositories.add(["repositoryId":repositoryId, "repositoryUri": repositoryUri, "snapshotsEnabled":snapshotsEnabled])
        this
    }

    @Override
    protected Void process(Object input) throws Exception {
        def settings = Tasks.chain(settingsXmlFile, XmlFileLoader).execute().await()

        // update with defined repositories
        repositories.each { r ->
            def profileElement = Tasks.chain(MessageFormat.format(PROFILE_TEMPLATE, r.repositoryId, r.repositoryUri, r.snapshotsEnabled), XmlTextLoader).execute().await()
            def profileActivationElement = Tasks.chain(MessageFormat.format(PROFILE_ACTIVATION_TEMPLATE, r.repositoryId), XmlTextLoader).execute().await()

            // remove previous profiles with the same id
            settings.profiles.profile.findAll { p -> p.id.text() == "${repositoryId}" }.each { it.replaceNode {} }
            // append profiles
            settings.profiles.each { it.append(profileElement) }

            // remove previous profile activations
            settings.activeProfiles.activeProfile.findAll { ap -> ap.text() == "${repositoryId}" }.each { it.replaceNode {} }

            // append profile activations
            settings.activeProfiles.each { it.append(profileActivationElement) }
        }

        // update with local repository
        // delete <localRepository> if present
        settings.localRepository.each { it.replaceNode {} }
        settings.children().add(0, new Node(null, 'localRepository', "${localRepositoryDir.getAbsolutePath()}"))

        Tasks.chain(settings, XmlUpdater).file(settingsXmlFile).execute().await()
        // TODO Auto-generated method stub
        return null;
    }

}
