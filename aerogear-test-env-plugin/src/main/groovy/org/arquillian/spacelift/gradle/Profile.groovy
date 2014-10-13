package org.arquillian.spacelift.gradle

import org.gradle.api.Project;

// this class represents a profile enumerating installations to be installed
class Profile {

    // this is required in order to use project container abstraction
    final String name

    // list of enabled installations
    def enabledInstallations

    // list of tests to execute
    def tests

    private Project project

    Profile(String profileName, Project project) {
        this.name = profileName
        this.project = project
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder()
        sb.append("Profile: ").append(name).append("\n")

        // installations
        sb.append("\tInstallations: ")
        enabledInstallations.each {
            sb.append(it).append(" ")
        }
        sb.append("\n")

        sb.append("\tTests: ")
        tests.each {
            sb.append(it).append(" ")
        }

        return sb.toString()
    }
}
