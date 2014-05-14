package org.jboss.aerogear.test.arquillian

import org.arquillian.spacelift.execution.Task
import org.arquillian.spacelift.execution.Tasks
import org.jboss.aerogear.test.GradleSpacelift
import org.jboss.aerogear.test.xml.XmlFileLoader
import org.jboss.aerogear.test.xml.XmlUpdater
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ArquillianXmlUpdater extends Task<Object, Void>{
    private static final Logger log = LoggerFactory.getLogger('ArquillianXml')

    def arquillianXmlFiles

    def container

    def extension

    ArquillianXmlUpdater dir(File dir) {
        def project = GradleSpacelift.currentProject()

        // get all arquillian.xml files in directory
        this.arquillianXmlFiles = project.fileTree("${dir}") {
            include "**/arquillian.xml"
            exclude "${project.aerogearTestEnv.localRepository}/**", "**/target/**"
        }

        log.debug("There are ${arquillianXmlFiles.getFiles().size()} arquillian.xml files to be modified in ${dir}")
        this
    }

    ArquillianXmlUpdater container(String container) {
        this.container = container
        this
    }

    ArquillianXmlUpdater extension(String extension) {
        this.extension = extension
        this
    }


    @Override
    protected Void process(Object properties) throws Exception {

        if(container) {
            configureContainer(container, properties);
        }

        if(extension) {
            configureExtension(extension, properties);
        }

        return null;
    }

    def configureContainer(String container, def properties) {

        arquillianXmlFiles.each { arquillianXml ->

            log.info("Modifying container \"*${container}*\" configuration(s) in ${arquillianXml}")

            def arquillian = Tasks.chain(arquillianXml, XmlFileLoader).execute().await()

            // replace standalone <container>s
            arquillian.container.findAll {c -> c.@qualifier.contains(container)}.configuration.each { configuration ->
                properties.each { key, value ->
                    // remove existing properties
                    configuration.property.findAll { p -> p.@name == "${key}"}.each { it.replaceNode {}}
                    // put new configuration properties
                    configuration*.append(new Node(null, 'property', [name: "${key}"], "${value}"))
                }
            }
            // replace containers in <group>
            arquillian.group.container.findAll {c -> c.@qualifier.contains(container)}.configuration.each { configuration ->
                properties.each { key, value ->
                    // remove existing properties
                    configuration.property.findAll { p -> p.@name == "${key}"}.each { it.replaceNode {} }
                    // put new configuration properties
                    configuration*.append(new Node(null, 'property', [name: "${key}"], "${value}"))
                }
            }

            Tasks.chain(arquillian, XmlUpdater).file(arquillianXml).execute().await()
        }
    }

    def configureExtension(String extensionQualifier, def properties) {
        arquillianXmlFiles.each { arquillianXml ->
            configureExtension(extensionQualifier, properties, arquillianXml)
        }
    }

    def configureExtension(String extensionQualifier, def properties, File arquillianXml) {
        log.info("Modifying Arquillian extension \"${extensionQualifier}\" configuration(s) at ${arquillianXml}")

        def arquillian = Tasks.chain(arquillianXml, XmlFileLoader).execute().await()
        arquillian.extension.findAll {e -> e.@qualifier == "${extensionQualifier}"}.each { extension ->
            properties.each { key, value ->
                // remove existing property
                extension.property.findAll { p -> p.@name == "${key}"}.each { it.replaceNode {} }
                // put new property
                extension.append(new Node(null, 'property', [name: "${key}"], "${value}"))
            }
        }
        Tasks.chain(arquillian, XmlUpdater).file(arquillianXml).execute().await()
    }
}
