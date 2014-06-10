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

@Command(name = "cart-create", description = "Create OpenShift Cartridge based on latest commit in given organization, repository and branch. Requires rhc tools installed")
public class AppCartridgeCreateCommand extends OpenShiftCommand implements Runnable {

    private static final Logger log = Logger.getLogger(AppCartridgeCreateCommand.class.getName());

    @Option(name = { "-r", "--repository" }, title = "repository", description = "Repository to be used, default value: openshift-origin-cartridge-aerogear-push")
    public String repository = "openshift-origin-cartridge-aerogear-push";

    @Option(name = { "-o", "--organization" }, title = "organization", description = "Organization on GitHub, default value: aerogear")
    public String organization = "aerogear";

    @Option(name = { "-g", "--gear" }, title = "gear size", allowedValues = { "small", "medium" }, description = "Size of the gear to be used")
    public String gearSize = "small";

    @Option(name = { "-b", "--branch" }, title = "branch", description = "Branch to be used, default value: master")
    public String branch = "master";

    @Option(name = { "-f", "--force" }, title = "force", description = "Forces removal of the cartridge with the same name")
    public boolean force;

    @Arguments(title = "cartridges", description = "Additional Cartridges to be instantiated together with the one defined by commit. By default: mysql-5.5, myphpadmin-4")
    public List<String> additionalCartridges = new ArrayList<String>();

    @Override
    public void run() {

        GitHubRepository ghRepository = new GitHubRepository(organization, repository);

        String latestCommit = ghRepository.getLastestCommit(branch);

        log.log(Level.INFO, "Latest commit in {0}/{1} branch {2} was {3}", new Object[] {
            organization,
            repository,
            branch,
            latestCommit });

        if (force) {
            Tasks.prepare(CommandTool.class)
                .programName("rhc").parameters("app", "delete", "--confirm", appName)
                .shouldExitWith(0, 101)
                .interaction(new ProcessInteractionBuilder().outputPrefix("").when(".*").printToOut())
                .execute().await();
        }

        // add default additional cartridges
        if(additionalCartridges.isEmpty()) {
            additionalCartridges.addAll(Arrays.asList("mysql-5.5", "phpmyadmin-4"));
        }

        Tasks.prepare(CommandTool.class)
            .programName("rhc")
            .parameters("app", "create", "-g", gearSize, "--no-git", appName)
            .parameter("http://cartreflect-claytondev.rhcloud.com/reflect?github=" + organization + "/" + repository
                + "&commit=" + latestCommit)
            .parameters(additionalCartridges)
            .interaction(new ProcessInteractionBuilder().outputPrefix("").when(".*").printToOut())
            .execute().await();
    }
}