package org.jboss.aerogear.test.cli;

import io.airlift.command.Command;

import org.arquillian.spacelift.execution.Tasks;
import org.arquillian.spacelift.process.ProcessInteractionBuilder;
import org.arquillian.spacelift.process.impl.CommandTool;

@Command(name = "cart-delete", description = "Deletes Openshift application")
public class AppCartridgeDeleteCommand extends OpenShiftCommand implements Runnable {

    @Override
    public void run() {
        Tasks.prepare(CommandTool.class)
            .programName("rhc")
            .parameter("app")
            .parameter("delete")
            .parameter(appName)
            .shouldExitWith(0, 101)
            .interaction(new ProcessInteractionBuilder().outputPrefix("").when(".*").printToOut())
            .execute().await();
    }

}
