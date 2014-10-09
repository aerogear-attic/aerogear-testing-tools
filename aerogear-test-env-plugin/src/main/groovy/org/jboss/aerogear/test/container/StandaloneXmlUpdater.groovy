package org.jboss.aerogear.test.container

import java.io.File;
import java.text.MessageFormat

import org.arquillian.spacelift.execution.Task
import org.arquillian.spacelift.execution.Tasks
import org.jboss.aerogear.test.xml.XmlFileLoader;
import org.jboss.aerogear.test.xml.XmlTextLoader;
import org.jboss.aerogear.test.xml.XmlUpdater;

class StandaloneXmlUpdater extends Task<Object, File> {
    
    def standaloneXmlFile

    // https://access.redhat.com/site/documentation/en-US/JBoss_Enterprise_Application_Platform/6.2/html/Security_Guide/SSL_Connector_Reference1.html
    def SSL_CONNECTOR_TEMPLATE = '''
            <connector name="https" protocol="HTTP/1.1" scheme="https" socket-binding="https" secure="true">
                <ssl name="aerogear-ssl" key-alias="aerogear" password="{0}" certificate-key-file="{1}" ca-certificate-file="{2}" protocol="{3}" />
            </connector>
    '''
    
    private def keystoreFile
    private def keystorePass
    private def truststoreFile
    private def protocol

    def file(xmlFile) {
        this.standaloneXmlFile = xmlFile
        this
    }

    def keystore(keystoreFile, keystorePass, truststoreFile, protocol) {
        this.keystoreFile = keystoreFile
        this.keystorePass = keystorePass
        this.truststoreFile = truststoreFile
        this.protocol = protocol
        this
    }

    @Override
    protected File process(Object input) throws Exception {

        def server = Tasks.chain(standaloneXmlFile, XmlFileLoader).execute().await()
        def sslConnectorElement = Tasks.chain(MessageFormat.format(SSL_CONNECTOR_TEMPLATE, keystorePass, keystoreFile.getAbsolutePath(), truststoreFile.getAbsolutePath(), protocol),
                XmlTextLoader)
                .execute().await()      
                
        server.profile.subsystem.find { s ->
            s.@xmlns.contains('jboss:domain:web:')
        }.connector.findAll{ c ->
            c.@name == "https"
        }.each { it.replaceNode { } }

        server.profile.subsystem.findAll { s ->
            s.@xmlns.contains('jboss:domain:web:')
        }.each {
            // add https connector right after http connector
            it.children().add(1, sslConnectorElement)
        }
        
        // define JVM parameters after <extensions>
        
        // if there is no system-properties section, create one
        if (server."system-properties".isEmpty()) {
            server.children().add(1, new Node(null, "system-properties"))
        }
        
        // remove truststore properties
        server."system-properties".property.findAll { s ->
            s.@name == "javax.net.ssl.trustStore" || s.@name == "javax.net.ssl.trustStorePassword"
        }.each {
            it.replaceNode { }
        }
        
        // add new truststore properties
        def systemProperties = server."system-properties"[0]
        systemProperties.appendNode("property", [name: "javax.net.ssl.trustStore", value: truststoreFile.getAbsolutePath()])
        systemProperties.appendNode("property", [name: "javax.net.ssl.trustStorePassword", value: keystorePass])
        
        Tasks.chain(server, XmlUpdater).file(standaloneXmlFile).execute().await()

        return standaloneXmlFile
    }

}
