package org.jboss.aerogear.test

import org.apache.commons.lang3.SystemUtils
import org.gradle.api.Project
import org.slf4j.Logger
import org.slf4j.LoggerFactory


// this class represents anything installable
// values for installation can either be a single value or if anything requires multiple values per product
// you can use OS families:
// * windows
// * mac
// * linux
class Installation {
    static final Logger log = LoggerFactory.getLogger('Installation')

    // this is required in order to use project container abstraction
    final String name

    // version of the product installation belongs to
    def version

    // product name
    def product

    // these two variables allow to directly specify fs path or remote url of the installation bits
    def fsPath
    def remoteUrl

    // zip file name
    def fileName

    // represents directory where installation is extracted to
    def home

    // automatically extract archive
    def autoExtract = true

    // tools provided by this installation
    def tools = []

    // actions to be invoked after installation is done
    def postActions

    // internal access to project defined variables
    private Project project

    Installation(String productName, Project project) {
        this.name = this.product = productName
        this.project = project
    }

    // gets os specific value
    def getOsSpecificValue(mapClosureOrCollection) {

        // if this is a closure, execute it
        if(mapClosureOrCollection instanceof Closure) {
            mapClosureOrCollection.delegate = this
            return mapClosureOrCollection.doCall()
        }
        // if this is a single value, just return it
        else if(mapClosureOrCollection instanceof Map) {
            // expecting we have a map here

            // try to figure out value given the family
            if(SystemUtils.IS_OS_WINDOWS) {
                return getOsSpecificValue(mapClosureOrCollection['windows'])
            }
            else if(SystemUtils.IS_OS_MAC_OSX) {
                return getOsSpecificValue(mapClosureOrCollection['mac'])
            }
            else if(SystemUtils.IS_OS_LINUX) {
                return getOsSpecificValue(mapClosureOrCollection['linux'])
            }
            else if(SystemUtils.IS_OS_SOLARIS || SystemUtils.IS_OS_SUN_OS) {
                return getOsSpecificValue(mapClosureOrCollection['solaris'])
            }
            else {
                throw new IllegalStateException("Unknown system ${System.getProperty('os.name')}")
            }
        }
        // got a string value here
        return mapClosureOrCollection == null ? "" : mapClosureOrCollection;
    }

    def getFsPath() {
        fsPath ? new File(getOsSpecificValue(fsPath)) : new File("${project.aerogearTestEnv.installationsDir}/${product}/${getVersion()}/${getFileName()}")
    }

    def getRemoteUrl() {
        new URL(getOsSpecificValue(remoteUrl))
    }

    def getHome() {
        new File(project.aerogearTestEnv.workspace, getOsSpecificValue(home))
    }

    def getFileName() {
        getOsSpecificValue(fileName)
    }

    def getVersion() {
        getOsSpecificValue(version)
    }

    // get installation and perform steps defined in closure after it is extracted
    void install() {

        def ant = project.ant

        File targetFile = getFsPath()
        if(targetFile.exists()) {
            log.info("Grabbing ${getFileName()} from file system")
        }
        else {
            // ensure parent directory exists
            targetFile.getParentFile().mkdirs()

            // dowload bits if they do not exists
            log.info("Downloading ${getFileName()} from URL ${getRemoteUrl()} to ${getFsPath()}")
            ant.get(src: getRemoteUrl(), dest: getFsPath())
        }

        if(autoExtract) {
            if(getHome().exists()) {
                log.info("Reusing existing installation ${getHome()}")
            }
            else {
                log.info("Extracting installation to ${project.aerogearTestEnv.workspace}")

                // based on installation type, we might want to unzip/untar/something else
                switch(getFileName()) {
                    case ~/.*jar/:
                        ant.unzip(src:getFsPath(), dest: new File(project.aerogearTestEnv.workspace, getFileName()))
                        break
                    case ~/.*zip/:
                        ant.unzip(src: getFsPath(), dest: project.aerogearTestEnv.workspace)
                        break
                    case ~/.*tgz/:
                    case ~/.*tar\.gz/:
                        ant.untar(src: getFsPath(), dest: project.aerogearTestEnv.workspace, compression: 'gzip')
                        break
                    default:
                        throw new RuntimeException("Invalid file type for installation ${getFileName()}")
                }
            }
        }
        else {
            if(new File(getHome(), getFileName()).exists()) {
                log.info("Reusing existing installation ${new File(getHome(),getFileName())}")
            }
            else {
                log.info("Copying installation to ${project.aerogearTestEnv.workspace}")
                ant.copy(file: getFsPath(), tofile: new File(getHome(), getFileName()))
            }
        }

        // registerd installed tools
        tools.each { tool ->
            tool.registerInSpacelift(GradleSpacelift.toolRegistry())
        }
        // execute post actions
        if(postActions) {
            postActions.delegate = this
            postActions.doCall()
        }
    }

    // we keep post actions to be executed after installation is done
    def postActions(Closure closure) {
        this.postActions = closure;
    }

    def tool(Closure closure) {
        def tool = new GradleSpaceliftTool(project)

        // FIXME here we are dynamically adding home property to the tool instance
        // so we can reference it in the closure, this is Groovy magic and there might be a better way
        //tool.metaClass.home = getHome()
        closure.delegate = this
        project.configure(tool, closure)

        tools << tool
    }
}
