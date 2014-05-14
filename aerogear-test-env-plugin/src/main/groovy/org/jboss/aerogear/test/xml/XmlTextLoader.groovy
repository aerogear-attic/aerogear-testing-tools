package org.jboss.aerogear.test.xml

import org.arquillian.spacelift.execution.Task

class XmlTextLoader extends Task<String, Object>{

    @Override
    protected Object process(String input) throws Exception {
        new XmlParser(false, false).parseText(input)
    }
}
