package org.jboss.aerogear.test

import org.arquillian.spacelift.execution.Tasks
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.internal.reflect.Instantiator
import org.jboss.aerogear.test.maven.SettingsXmlUpdater
import org.jboss.aerogear.test.utils.KillJavas
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class AerogearTestEnvPlugin implements Plugin<Project> {

    static final Logger log = LoggerFactory.getLogger('AerogearTestEnvPlugin')

    // this plugin prepares aerogear environment
    void apply(Project project) {
        project.extensions.create("aerogearTestEnv", AerogearTestEnvConventions, project)

        // add tools definitions
        project.aerogearTestEnv.extensions.tools = project.container(GradleSpaceliftTool) { toolAlias ->
            project.gradle.services.get(Instantiator).newInstance(GradleSpaceliftTool, toolAlias, project)
        }

        // add profile definitions
        project.aerogearTestEnv.extensions.profiles = project.container(Profile) { profileName ->
            project.gradle.services.get(Instantiator).newInstance(Profile, profileName, project)
        }

        // add installation definitions
        project.aerogearTestEnv.extensions.installations = project.container(Installation) { productName ->
            def installation = project.gradle.services.get(Instantiator).newInstance(Installation, productName, project)
        }

        // add test definitions
        project.aerogearTestEnv.extensions.tests = project.container(Test) { testName ->
            project.gradle.services.get(Instantiator).newInstance(Test, testName, project)
        }

        // set default values if not specified from command line
        setDefaultDataProviders(project);


        // set current project and initialize tools
        project.task('init-tools') << { 
            logger.lifecycle(":init-tools:defaultValues")
            // set default values if not specified from command line in case plugin is applied prior ext {} block
            setDefaultDataProviders(project);
            GradleSpacelift.currentProject(project) 
        }


        // this task shows configuration of the plugin
        project.task('show-configuration') << {
            println "WORKSPACE:          ${project.aerogearTestEnv.workspace}"
            println "HUDSON_STATIC_ENV:  ${project.aerogearTestEnv.installationsDir}"
            println "AVAILABLE PROFILES: "

            project.aerogearTestEnv.profiles.each { println it }
        }

        // this task prepares test environment by
        // 1. Identifying activated profile
        // 2. Installing all installations
        project.task('prepare-env') << {
            // task closure has access to "logger" object

            // check for -Pprofile, fallback to default if not defined
            def profileName = 'default'
            if(project.hasProperty('profile')){
                profileName = project.profile
            }
            else {
                logger.warn(":prepare-env: Please select profile by -Pprofile=name, now using default")
            }

            // find profile that enumerates installations
            def profile = project.aerogearTestEnv.profiles.find { profile -> profile.name == profileName }
            if(profile==null) {
                def availableProfiles = project.aerogearTestEnv.profiles.collect { it.name }.join(', ')
                throw new GradleException("Unable to find ${profileName} profile in build.gradle file, available profiles were: ${availableProfiles}")
            }

            // make it global selected profile
            project.ext.set("selectedProfile", profile)

            logger.lifecycle(":prepare-env:profile-${profileName}")

            if(project.aerogearTestEnv.killServers) {
                Tasks.prepare(KillJavas).execute().await()
            }

            // create settings.xml with local repository
            Tasks.prepare(SettingsXmlUpdater).execute().await()

            if(project.aerogearTestEnv.enableStaging) {
                // here it is named logger, because it is a part of Plugin<Project> implementation
                logger.lifecycle(":prepare-env:enableJBossStagingRepository")
                Tasks.prepare(SettingsXmlUpdater).repository("jboss-staging-repository-group", new URI("https://repository.jboss.org/nexus/content/groups/staging"), true).execute().await()

            }

            if(project.aerogearTestEnv.enableSnapshots) {
                logger.lifecycle(":prepare-env:enableJBossSnapshotsRepository")
                Tasks.prepare(SettingsXmlUpdater).repository("jboss-snapshots-repository", new URI("https://repository.jboss.org/nexus/content/repositories/snapshots"), true).execute().await()
            }

            // execute only installations that were enabled in profile
            project.aerogearTestEnv.installations.each { installation ->
                profile.enabledInstallations.each { installationName ->
                    if(installationName == '*' || installationName == installation.name ) {
                        logger.lifecycle(":prepare-env:install ${installation.name}")
                        installation.install()
                    }
                }
            }

        }

        // this task executes tests
        // it used prepared environment created by previous task
        project.task('executeTests') << {
            project.aerogearTestEnv.tests.each { test ->
                project.selectedProfile.tests.each { testInProfile ->
                    if (testInProfile == test.name) {
                        test.executeTest()
                    }
                }
            }
        }

        project.tasks.getByName("prepare-env").dependsOn(project.tasks.getByName("init-tools"))

        project.tasks.getByName("executeTests").dependsOn(project.tasks.getByName("prepare-env"))

        // test task alias, you can use tests instead of executeTests
        project.task('test') << {
        }

        project.tasks.getByName("test").dependsOn(project.tasks.getByName("executeTests"))
    }

    private void setDefaultDataProviders(Project project) {
        // parse both properties defined deprecated way and project.ext way. find the ones starting with default
        def defaultValues = project.getProperties()
                .findAll {key, value -> return (key.startsWith("default") && !key.startsWith("defaultTask")) } << project.ext.properties
                .findAll {key, value -> return (key.startsWith("default"))}

        defaultValues.each { key, value ->
            def overrideKey = key.substring("default".length(), key.length())
            overrideKey = overrideKey[0].toLowerCase() + overrideKey.substring(1)
            if(project.hasProperty(overrideKey)) {
                // get and parse new value to always return a collection
                def newValue = project.property(overrideKey)
                newValue = (newValue instanceof Object[] || newValue instanceof Collection) ? newValue : newValue.toString().split(",")
                //project.setProperty(overrideKey, newValue)
                project.ext.set(overrideKey, newValue)
                log.info("Set ${overrideKey} from command line property -P${overrideKey}=${newValue}")
            }
            else {
                //project.setProperty(overrideKey, value)
                project.ext.set(overrideKey, value)
                log.info("Set ${overrideKey} from default value ${key}=${value}")
            }
        }
    }
}
