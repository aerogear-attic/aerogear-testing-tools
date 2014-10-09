package org.jboss.aerogear.test.openshift

import org.arquillian.spacelift.execution.Tasks;
import org.arquillian.spacelift.process.ProcessInteractionBuilder;
import org.arquillian.spacelift.process.impl.CommandTool;
import org.arquillian.spacelift.tool.Tool
import org.jboss.aerogear.test.GradleSpacelift
import org.jboss.as.controller.parsing.Namespace;

class CreateOpenshiftCartridge extends Tool<Object, Void> {

    def name
    def namespace
    def server
    def gear
    def isScaling = false
    def noGit = true
    def force = false
    def ignoreIfExists = false
    def cartridges = []

    // credentials
    def password
    def login
    def token

    @Override
    protected Collection<String> aliases() {
        return "rhc-create-app"
    }

    @Override
    protected Void process(Object unused) throws Exception {

        // delete app if it exists
        if(force) {
            def command = GradleSpacelift.tools('rhc')
                    .parameters("app", "delete", "--confirm", name, "-n", namespace)
                    // if app is not present, it will fail with 101
                    .shouldExitWith(0, 101)
                    .interaction(GradleSpacelift.ECHO_OUTPUT)

            if(login) {
                command.parameters("-l", login)
            }
            if(password) {
                command.parameters("-p", password)
            }
            if(token) {
                command.parameters("--token", token)
            }

            command.execute().await();
        }

        def command = GradleSpacelift.tools('rhc')

        command.parameters("app", "create")

        command.parameters("-a", name)
        command.parameters("-n", namespace)

        if(gear) {
            command.parameters("-g", gear)
        }
        if(isScaling) {
            command.parameter("-s")
        }
        if(noGit) {
            command.parameter("--no-git")
        }
        if(server) {
            command.parameters("--server", server)
        }
        if(login) {
            command.parameters("-l", login)
        }
        if(password) {
            command.parameters("-p", password)
        }
        if(token) {
            command.parameters("--token", token)
        }
        if(ignoreIfExists) {
            command.shouldExitWith(0,1)
        }

        command.parameters(cartridges)

        command.interaction(GradleSpacelift.ECHO_OUTPUT).execute().await()

        return null
    }

    def ignoreIfExists() {
        this.ignoreIfExists = true
        this
    }

    def force() {
        this.force = true
        this
    }

    def named(name) {
        this.name = name;
        this;
    }

    def cartridges(CharSequence...cartridges) {
        this.cartridges.addAll(cartridges)
        this
    }

    def sized(gear) {
        this.gear = gear
        this
    }

    def at(namespace) {
        this.namespace = namespace
        this
    }

    def server(server) {
        this.server = server
        this
    }

    def scale() {
        this.isScaling = true
        this
    }

    def checkout() {
        this.noGit = false
        this
    }

    def username(username) {
        this.login = username
        this
    }

    def password(password) {
        this.password = password
        this
    }

    def token(token) {
        this.token = token
        this;
    }
}
