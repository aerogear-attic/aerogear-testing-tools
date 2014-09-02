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
        // parses default profile, installations and tests
        // this task can use following properties specified on command line
        // * -Pprofile=string
        // * -Pinstallations=comma-separated-strings
        // * -Ptests=comma-separated-strings
        // this task make following properties available
        // * project.selectedProfile
        // * project.selectedInstallations
        // * project.selectedTests
        project.task('init') << {
            logger.lifecycle(":init:defaultValues")
            // set default values if not specified from command line in case plugin is applied prior ext {} block
            setDefaultDataProviders(project);
            GradleSpacelift.currentProject(project)

            // find default profile and propagate enabled installations and tests
            // check for -Pprofile, fallback to default if not defined
            def profileName = 'default'
            if(project.hasProperty('profile')){
                profileName = project.profile
            }
            else {
                // task closure has access to "logger" object
                logger.warn(":init: Please select profile by -Pprofile=name, now using default")
            }

            // find if such profile is available
            def profile = project.aerogearTestEnv.profiles.find { profile -> profile.name == profileName }
            if(profile==null) {
                def availableProfiles = project.aerogearTestEnv.profiles.collect { it.name }.join(', ')
                throw new GradleException("Unable to find ${profileName} profile in build.gradle file, available profiles were: ${availableProfiles}")
            }

            // make selected profile global
            project.ext.set("selectedProfile", profile)

            // find installations that were specified by profile or enabled manually from command line
            def installations = []
            def installationNames = []
            if(project.hasProperty('installations')) {
                installationNames = project.installations.split(',').findAll { return !it.isEmpty() }
            }
            else if(profile.enabledInstallations==null) {
                installationNames = []
            }
            else if(profile.enabledInstallations.contains('*')) {
                installationNames = project.aerogearTestEnv.installations.collect(new ArrayList()) {installation -> installation.name}
            }
            else {
                installationNames = profile.enabledInstallations
            }
            installations = installationNames.inject(new ArrayList()) { list, installationName ->
                def installation = project.aerogearTestEnv.installations[installationName]
                if(installation) {
                    logger.info("init: Installation ${installationName} will be installed.")
                    list << installation
                    return list
                }
                logger.warn("init: Selected installation ${installationName} does not exist and will be ignored")
                return list
            }

            // make selected installations global
            project.ext.set("selectedInstallations", installations)

            // find tests that were specified by profile or enabled manually from command line
            def tests = []
            def testNames = []
            if(project.hasProperty('tests')) {
                testNames = project.tests.split(',').findAll { return !it.isEmpty() }
            }
            else if(profile.tests==null) {
                testNames = []
            }
            else if(profile.tests.contains('*')) {
                testNames = project.aerogearTestEnv.tests.collect(new ArrayList()) {test -> test.name}
            }
            else {
                testNames = profile.tests
            }
            tests = testNames.inject(new ArrayList()) { list, testName ->
                def test = project.aerogearTestEnv.tests[testName]
                if(test) {
                    logger.info("init: Selected test ${testName} will be tested (if task 'test' is run).")
                    list << test
                    return list
                }
                logger.warn("init: Selected tests ${testName} does not exist and will be ignored")
                return list
            }

            // make selected installations global
            project.ext.set("selectedTests", tests)
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

            logger.lifecycle(":prepare-env:profile-${project.selectedProfile.name}")

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

            project.selectedInstallations.each { installation ->
                logger.lifecycle(":prepare-env:install ${installation.name}")
                installation.install()
            }
        }

        // this task executes tests
        // it used prepared environment created by previous task
        project.task('test') << {
            project.selectedTests.each { test ->
                test.executeTest()
            }
        }

        project.tasks.getByName("prepare-env").dependsOn(project.tasks.getByName("init"))

        project.tasks.getByName("test").dependsOn(project.tasks.getByName("prepare-env"))

        // test task alias, you can use tests instead of executeTests
        project.task('executeTests') << {
        }

        project.task('testreport') << {
            logger.lifecycle(":testreport:generating JUnit report for all tests in ${project.aerogearTestEnv.workspace}")

            //ant.taskdef(name: 'junitreport', classname: 'org.apache.tools.ant.taskdefs.optional.junit.XMLResultAggregator', classpath: project.configurations.junitreport.asPath)
            ant.mkdir(dir: "${project.aerogearTestEnv.workspace}/test-reports") 
            ant.mkdir(dir: "${project.aerogearTestEnv.workspace}/test-reports/html") 
            ant.junitreport(todir: "${project.aerogearTestEnv.workspace}/test-reports") {
                fileset(dir: "${project.aerogearTestEnv.workspace}") {
                    include(name: "**/TEST*.xml")
                    exclude(name: "test-reports/*.xml")
                    exclude(name: "test-reports/html/*")
                }
                report(format: "noframes", todir: "${project.aerogearTestEnv.workspace}/test-reports/html")
            }
            logger.lifecycle(":testreport:test report available in ${project.aerogearTestEnv.workspace}/test-reports/html/junit-noframes.html")
        }

        project.tasks.getByName("executeTests").dependsOn(project.tasks.getByName("test"))
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
