package org.arquillian.spacelift.gradle.maven

import org.arquillian.spacelift.execution.Task
import org.arquillian.spacelift.execution.Tasks
import org.arquillian.spacelift.gradle.GradleSpacelift
import org.arquillian.spacelift.gradle.xml.XmlFileLoader
import org.arquillian.spacelift.gradle.xml.XmlUpdater

class PomXmlUpdater extends Task<Object, Void> {

    def xmlFiles = []

    PomXmlUpdater dir(dir) {
        def project = GradleSpacelift.currentProject()
        this.xmlFiles = project.fileTree("${dir}") {
            include "**/pom.xml"
            exclude "${project.spacelift.localRepository}/**", "**/target/**"
        }
        this
    }

    @Override
    protected Void process(Object properties) throws Exception {

        xmlFiles.each { file ->
            def pom = Tasks.chain(file, XmlFileLoader).execute().await()

            properties.each { propertyKey, value ->
                pom.properties.each { p ->
                    p.value().each { v ->
                        if (v.name() == propertyKey) {
                            v.value = value
                        }
                    }
                }
            }

            Tasks.chain(pom, XmlUpdater).file(file).execute().await()
        }

        return null
    }
}
