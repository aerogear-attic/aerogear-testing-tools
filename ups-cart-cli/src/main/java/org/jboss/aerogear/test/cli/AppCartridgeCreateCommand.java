package org.jboss.aerogear.test.cli;

import io.airlift.command.Arguments;
import io.airlift.command.Command;
import io.airlift.command.Option;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.arquillian.spacelift.execution.Tasks;
import org.arquillian.spacelift.process.ProcessInteractionBuilder;
import org.arquillian.spacelift.process.impl.CommandTool;
import org.jboss.aerogear.test.GitHubRepository;
import org.jboss.aerogear.unifiedpush.utils.AWS_REGION;
import org.jboss.aerogear.unifiedpush.utils.GEAR_SIZE;

@Command(name = "cart-create", description = "Create OpenShift Cartridge based on latest commit in given organization, repository and branch. Requires rhc tools installed")
public class AppCartridgeCreateCommand extends AbstractOpenShiftCommand implements Runnable {

    private static final Logger log = Logger.getLogger(AppCartridgeCreateCommand.class.getName());

    @Option(
        name = { "-r", "--repository" },
        title = "repository",
        description = "Repository to be used, default value: openshift-origin-cartridge-aerogear-push")
    public String repository = "openshift-origin-cartridge-aerogear-push";

    @Option(
        name = { "-o", "--organization" },
        title = "organization",
        description = "Organization on GitHub, default value: aerogear")
    public String organization = "aerogear";

    @Option(
        name = { "--region" },
        title = "region",
        description = "Region where an application will be hosted, look at 'rhc region list' for options, defaults to 'aws-eu-west-1'")
    public String region = AWS_REGION.AWS_US_EAST_1.toString();

    @Option(
        name = { "-g", "--gear" },
        title = "gear size",
        allowedValues = { "small", "medium", "large" },
        description = "Size of the gear to be used, defauls to 'small'")
    public String gearSize = GEAR_SIZE.SMALL.toString();

    @Option(
        name = { "-b", "--branch" },
        title = "branch",
        description = "Branch to be used, default value: master")
    public String branch = "master";

    @Option(
        name = { "-f", "--force" },
        title = "force",
        description = "Forces removal of the cartridge with the same name")
    public boolean force;

    @Option(
        name = { "-s", "--scalable" },
        title = "scalable",
        description = "Created cartridge will be scalable")
    public boolean scalable;

    @Arguments(
        title = "cartridges",
        description = "Additional Cartridges to be instantiated together with the one defined by commit. "
            + "By default: mysql-5.5, myphpadmin-4")
    public List<String> additionalCartridges = new ArrayList<String>();

    @Override
    public void run() {

        validate();

        GitHubRepository ghRepository = new GitHubRepository(organization, repository);

        String latestCommit = null;

        latestCommit = ghRepository.getLastestCommit(branch);

        log.log(Level.INFO, "Latest commit in {0}/{1} branch {2} was {3}", new Object[] {
            organization,
            repository,
            branch,
            latestCommit });

        if (force) {
            Tasks.prepare(CommandTool.class)
                .programName("rhc").parameters("app", "delete", "-n", namespace, "--confirm", appName)
                .shouldExitWith(0, 101)
                .interaction(new ProcessInteractionBuilder().outputPrefix("").when(".*").printToOut())
                .execute().await();
        }

        // add default additional cartridges
        if (additionalCartridges.isEmpty()) {
            additionalCartridges.addAll(Arrays.asList("mysql-5.5", "phpmyadmin-4"));
        }

        if (scalable) { // scalable cartridge does not support phpmyadmin
            additionalCartridges.remove("phpmyadmin-4");
        }

        CommandTool ct = Tasks.prepare(CommandTool.class)
            .programName("rhc")
            .parameters("app", "create", "-n", namespace, "-g", gearSize, "--region", region, "--no-git", appName)
            .parameter("https://cartreflect-claytondev.rhcloud.com/github/" + organization + "/" + repository + "?commit=" + latestCommit)
            .parameters(additionalCartridges);

        if (scalable) {
            ct.parameter("-s");
        }

        ct.interaction(new ProcessInteractionBuilder().outputPrefix("").when(".*").printToOut()).execute().await();
    }

    private void validate() {

        AWS_REGION region = null;

        GEAR_SIZE gearSize = null;

        for (AWS_REGION r : AWS_REGION.values()) {
            if (r.toString().equals(this.region)) {
                region = r;
                break;
            }
        }

        for (GEAR_SIZE g : GEAR_SIZE.values()) {
            if (g.toString().equals(this.gearSize)) {
                gearSize = g;
                break;
            }
        }

        if (region == null) {
            throw new IllegalArgumentException(String.format("You have specified region which does not exist: %s. All regions: %s", this.region, getAllRegions()));
        }

        if (gearSize == null) {
            throw new IllegalArgumentException(String.format("You have specified gear size which does not exist: %s. All gear sizes: %s", this.gearSize, getAllGearSizes()));
        }

        if (region == AWS_REGION.AWS_EU_WEST_1 && gearSize == GEAR_SIZE.SMALL) {
            throw new IllegalStateException("You can not place '" + GEAR_SIZE.SMALL + "' gear to '" + AWS_REGION.AWS_EU_WEST_1 + "' region.");
        }
    }

    private Object getAllGearSizes() {

        StringBuilder sb = new StringBuilder();

        for (GEAR_SIZE gearSize : GEAR_SIZE.values()) {
            sb.append(gearSize.toString());
            sb.append(" ");
        }

        return sb.toString().trim();
    }

    private String getAllRegions() {

        StringBuilder sb = new StringBuilder();

        for (AWS_REGION region : AWS_REGION.values()) {
            sb.append(region.toString());
            sb.append(" ");
        }

        return sb.toString().trim();
    }
}