package org.arquillian.spacelift.gradle.db

import java.util.Properties

class DBAllocation {

    private def Properties properties

    DBAllocation(Properties properties) {
        this.properties = properties
    }

    def String getProperty(String property) {
        this.properties.getProperty(property)
    }

    def Properties getProperties() {
        properties
    }
}
