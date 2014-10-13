package org.arquillian.spacelift.gradle.xml

import groovy.util.Node;

import java.io.File;

import org.arquillian.spacelift.execution.Task

class XmlFileLoader extends Task<File, Object> {

    def storeXmlToFile(File file, xml) {
        // backup previous configuration
        ant.copy(file: "${file}", tofile: "${file}.backup${++backupCounter}")

        file.withPrintWriter("UTF-8") { writer ->
            def printer = new XmlNodePrinter(writer, '    ');
            printer.setPreserveWhitespace(true)
            printer.print(xml)
        }
    }

    @Override
    protected Object process(File input) throws Exception {
        new XmlParser(false, false).parse(input)
    }


}
