package org.jboss.aerogear.test.cli;

import io.airlift.airline.Command;
import io.airlift.airline.Option;

import org.arquillian.spacelift.execution.Tasks;
import org.arquillian.spacelift.process.ProcessInteractionBuilder;
import org.arquillian.spacelift.process.impl.CommandTool;

import com.jayway.restassured.RestAssured;

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
    
    @Option(name = { "-c", "--cartridge"},
            title = "cartridge",
            description = "Cartridge to use, defaults to jboss-mobile/jboss-unified-push-openshift-cartridge")
    private String cartridge = "https://raw.githubusercontent.com/jboss-mobile/jboss-unified-push-openshift-cartridge/master/metadata/manifest.yml";

    @Option(name = { "-t", "--community"},
            title = "community",
            description = "Flag saying that cartridge is of community version. When not set, product version is taken into consideration."
        )
    private boolean community = false;
    
    @Override
    public void run() {
        delete();
        create();
        stop();
        copyExtension();
        start();
        configureKeycloak();
        restart();
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
            .parameters("app", "create", appName, cartridge, "--no-git", "-n", namespace);

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
        CommandTool ct = Tasks.prepare(CommandTool.class)
            .programName("rhc")
            .parameters("scp", appName, "-n", namespace, "upload")
            .parameters("--local-path", "../../unifiedpush-test-extension-server/target/unifiedpush-test-extension-server.war");

        if (community) {
            ct.parameters("--remote-path", "aerogear-push/standalone/deployments");
        } else {
            ct.parameters("--remote-path", "unified-push/standalone/deployments");
        }
        
        ct.interaction(new ProcessInteractionBuilder().outputPrefix("").when(".*").printToOut()).execute().await();
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
    
    private void restart() {
        Tasks.prepare(CommandTool.class)
            .programName("rhc")
            .parameters("app", "restart", appName, "-n", namespace)
            .interaction(new ProcessInteractionBuilder().outputPrefix("").when(".*").printToOut())
            .execute().await();
    }

    private void configureKeycloak() {
        RestAssured.given().baseUri(getUnifiedpushTestExtensionUri()).get("/keycloak");
    }

}