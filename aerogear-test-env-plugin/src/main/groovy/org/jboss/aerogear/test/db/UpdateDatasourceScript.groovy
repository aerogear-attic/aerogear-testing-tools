package org.jboss.aerogear.test.db

import java.io.File;

class UpdateDatasourceScript {

    DBAllocation dbAllocation

    String script

    String jbossHome = System.getenv("JBOSS_HOME")

    def withScript(String script) {
        this.script = script
        this
    }

    def withScript(File script) {
        this.script = script.getAbsoluteFile()
        this
    }

    def withJBossHome(String jbossHome) {
        this.jbossHome = jbossHome
        this
    }

    def withJBossHome(File jbossHome) {
        this.jbossHome = jbossHome.getAbsoluteFile()
        this
    }

    def withDBAllocation(DBAllocation dbAllocation) {
        this.dbAllocation = dbAllocation
        this
    }

    def update() {
        if (!dbAllocation || !jbossHome || !script) {
            throw new IllegalStateException("One of datasource script, dbAllocation or jbossHome is null")
        }

        def script = new File(script)

        def targetScript = []

        if (script.exists() && script.canRead()) {
            script.eachLine { line ->
                targetScript.add(replaceLine(line, dbAllocation))
            }
        }

        linesToFile(targetScript)
    }

    private def String replaceLine(String line, DBAllocation allocation) {

        def properties = allocation.getProperties()

        properties.keys().each { key ->

            def toBeReplaced = '${' + key + '}'

            if (line.contains(toBeReplaced)) {
                line = line.replace(toBeReplaced, properties.get(key))
            }
        }

        line
    }

    private def File linesToFile(lines) {
        File file = File.createTempFile(dbAllocation.getProperty("uuid") +"-" + dbAllocation.getProperty("db.primary_label")    , ".cli.tmp")

        def lineSeparator = System.getProperty("line.separator")

        lines.each { line ->
            file.append(line + lineSeparator)
        }

        file
    }
}
