package org.jboss.aerogear.test.xml

import java.io.File;

import org.arquillian.spacelift.execution.Task
import org.gradle.api.AntBuilder;
import org.jboss.aerogear.test.GradleSpacelift;
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.util.XmlNodePrinter
import groovy.util.XmlParser

class XmlUpdater extends Task<Object, File>{

    protected static final Logger log = LoggerFactory.getLogger('Xml')

    def static backupCounter = 0

    private File file
    private AntBuilder ant

    XmlUpdater() {
        this.ant = GradleSpacelift.currentProject().ant
    }

    XmlUpdater file(File file) {
        this.file = file
        this
    }

    @Override
    protected File process(Object xml) throws Exception {
        // backup previous configuration
        ant.copy(file: "${file}", tofile: "${file}.backup${++backupCounter}")

        file.withPrintWriter("UTF-8") { writer ->
            def printer = new XmlNodePrinter(writer, '    ');
            printer.setPreserveWhitespace(true)
            printer.print(xml)
        }

        return file
    }
}