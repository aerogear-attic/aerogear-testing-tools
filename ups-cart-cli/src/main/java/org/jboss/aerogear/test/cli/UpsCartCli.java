package org.jboss.aerogear.test.cli;

import org.arquillian.spacelift.execution.Tasks;
import org.arquillian.spacelift.execution.impl.DefaultExecutionServiceFactory;

import io.airlift.command.Cli;
import io.airlift.command.Cli.CliBuilder;
import io.airlift.command.Help;

public class UpsCartCli {

    public static void main(String[] args) {

        @SuppressWarnings("unchecked")
        CliBuilder<Runnable> builder = Cli.<Runnable> builder("perf-test-env")
            .withDefaultCommand(Help.class)
            .withCommands(Help.class,
                AppCartridgeCreateCommand.class,
                UpsGenerateAppsCommand.class,
                UpsDeleteAppsCommand.class,
                UpsDumpCommand.class);

        // register tasks execution factory
        Tasks.setDefaultExecutionServiceFactory(new DefaultExecutionServiceFactory());

        builder.build().parse(args).run();
    }
}
