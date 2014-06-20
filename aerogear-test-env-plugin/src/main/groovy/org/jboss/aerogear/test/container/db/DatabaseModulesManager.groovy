package org.jboss.aerogear.test.container.db


class DatabaseModulesManager {

    def databaseModules = []

    def add(DatabaseModule module) {
        databaseModules.add(module)
        this
    }

    def install(String name) {
        databaseModules.each { module ->
            if (module.name == name) {
                module.install()
            }
        }
        this
    }

    def install(names) {
        databaseModules.each { module ->
            if (names.contains(module.name)) {
                module.install()
            }
        }
    }
    
    def installAll() {
        databaseModules.each { module ->
            module.install()
        }
        this
    }

    def uninstall(String name) {
        databaseModules.each { module ->
            if (module.name == name) {
                module.uninstall()
            }
        }
        this
    }

    def uninstallAll() {
        databaseModules.each { module ->
            module.uninstall()
        }
        this
    }

    def size() {
        databaseModules.size()
    }
}
