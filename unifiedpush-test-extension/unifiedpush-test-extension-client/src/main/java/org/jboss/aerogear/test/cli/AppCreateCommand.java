package org.jboss.aerogear.test.cli;

import io.airlift.airline.Command;
import io.airlift.airline.Option;

import org.arquillian.spacelift.execution.Tasks;
import org.arquillian.spacelift.process.ProcessInteractionBuilder;
import org.arquillian.spacelift.process.impl.CommandTool;

@Command(name = "app-create", description = "Create OpenShift Cartridge. Requires rhc tools installed.")
public class AppCreateCommand extends AbstractCommand {

    @Option(name = { "-r", "--region" },
            title = "region",
            description = "Region where an application will be hosted")
    private String region;

    @Option(name = { "-g", "--gear-size" },
            title = "gear size",
            description = "Size of the gear to be used")
    private String gearSize;

    @Option(name = { "-s", "--scaling" },
            title = "scalable",
            description = "Created cartridge will be scalable")
    private boolean scaling;
    
    @Option(name = { "-f", "--force" },
            title = "force",
            description = "Forces removal of the cartridge")
    private boolean force;

    @Override
    public void run() {
        delete();
        create();
        stop();
        copyExtension();
        start();
    }

    private void delete() {
        if (force) {
            Tasks.prepare(CommandTool.class)
                .programName("rhc")
                .parameters("app", "delete", "-n", namespace, "--confirm", appName)
                .shouldExitWith(0, 101)
                .interaction(new ProcessInteractionBuilder().outputPrefix("").when(".*").printToOut())
                .execute().await();
        }
    }

    private void create() {
        CommandTool ct = Tasks.prepare(CommandTool.class)
            .programName("rhc")
            .parameters("app", "create", "--no-git", "-n", namespace, appName, "jboss-unified-push-1");
    
        if( gearSize != null ) {
            ct.parameters("--gear-size", gearSize);
        }
        if( region != null  ) {
            ct.parameters("--region", region);
        }
        if (scaling) {
            ct.parameter("--scaling");
        }
    
        ct.interaction(new ProcessInteractionBuilder().outputPrefix("").when(".*").printToOut());
        ct.execute().await();
    }

    private void copyExtension() {
        Tasks.prepare(CommandTool.class)
            .programName("rhc")
            .parameters("scp", appName, "-n", namespace, "upload") 
            .parameters("--local-path", "../../unifiedpush-test-extension/unifiedpush-test-extension-server/target/unifiedpush-test-extension-server.war")
            .parameters("--remote-path", "unified-push/standalone/deployments")
            .interaction(new ProcessInteractionBuilder().outputPrefix("").when(".*").printToOut())
            .execute().await();
    }

    private void start() {
        Tasks.prepare(CommandTool.class)
            .programName("rhc")
            .parameters("app", "start", appName, "-n", namespace)
            .interaction(new ProcessInteractionBuilder().outputPrefix("").when(".*").printToOut())
            .execute().await();
    }

    private void stop() {
        Tasks.prepare(CommandTool.class)
            .programName("rhc")
            .parameters("app", "stop", appName, "-n", namespace)
            .interaction(new ProcessInteractionBuilder().outputPrefix("").when(".*").printToOut())
            .execute().await();
    }

}