package org.jboss.aerogear.test.cli;

import io.airlift.command.Arguments;
import io.airlift.command.Command;
import io.airlift.command.Option;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.arquillian.spacelift.process.CommandBuilder;
import org.arquillian.spacelift.process.ProcessExecutor;
import org.arquillian.spacelift.process.ProcessInteractionBuilder;
import org.arquillian.spacelift.process.impl.DefaultProcessExecutorFactory;
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

    @Arguments(title = "cartridges", description = "Additional Cartridges to be instantiated together with the one defined by commit. By default: mysql-5.1, myphpadmin-4")
    public List<String> additionalCartridges = Arrays.asList("mysql-5.1", "phpmyadmin-4");

    private ProcessExecutor executor = new DefaultProcessExecutorFactory().getProcessExecutorInstance();

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
            executor.execute(new ProcessInteractionBuilder().outputs(".*").build(),
                new CommandBuilder().add("rhc", "app", "delete", "--confirm", appName)
                    .build());
        }

        executor.execute(new ProcessInteractionBuilder().outputs(".*").build(),
            new CommandBuilder().add("rhc", "app", "create", "-g", gearSize, "--no-git", appName)
                .add("http://cartreflect-claytondev.rhcloud.com/reflect?github=" + organization + "/" + repository + "&commit="
                    + latestCommit)
                .add(additionalCartridges)
                .build());

    }
}