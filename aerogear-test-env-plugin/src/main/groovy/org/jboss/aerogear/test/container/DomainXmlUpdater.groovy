package org.jboss.aerogear.test.container

import java.text.MessageFormat

import org.arquillian.spacelift.execution.Task
import org.arquillian.spacelift.execution.Tasks
import org.jboss.aerogear.test.xml.XmlFileLoader
import org.jboss.aerogear.test.xml.XmlTextLoader
import org.jboss.aerogear.test.xml.XmlUpdater

class DomainXmlUpdater extends Task<Object, File> {

    def domainXmlFile

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
    private def profile

    def file(xmlFile) {
        this.domainXmlFile = xmlFile
        this
    }

    def keystore(keystoreFile, keystorePass, truststoreFile, protocol, profile) {
        this.keystoreFile = keystoreFile
        this.keystorePass = keystorePass
        this.truststoreFile = truststoreFile
        this.protocol = protocol
        this.profile = profile
        this
    }

    @Override
    protected File process(Object input) throws Exception {

        def domain = Tasks.chain(domainXmlFile, XmlFileLoader).execute().await()
        def sslConnectorElement = Tasks.chain(MessageFormat.format(SSL_CONNECTOR_TEMPLATE, keystorePass, keystoreFile.getAbsolutePath(), truststoreFile.getAbsolutePath(), protocol),
                XmlTextLoader).execute().await()

        domain.profiles.profile.find {p -> p.@name == profile}.subsystem.find { s -> s.@xmlns.contains('jboss:domain:web:')}.connector.findAll{ c -> c.@name == "https" }.each { it.replaceNode { } }

        domain.profiles.profile.find {p -> p.@name == profile}.subsystem.findAll { s -> s.@xmlns.contains('jboss:domain:web:')}.each {
            // add https connector right after http connector
            it.children().add(1, sslConnectorElement)
        }

        Tasks.chain(domain, XmlUpdater).file(domainXmlFile).execute().await()

        return domainXmlFile
    }




}
