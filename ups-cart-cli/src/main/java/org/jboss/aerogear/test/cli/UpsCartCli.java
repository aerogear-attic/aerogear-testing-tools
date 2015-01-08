package org.jboss.aerogear.test.cli;

import io.airlift.command.Cli;
import io.airlift.command.Cli.CliBuilder;
import io.airlift.command.Help;

import org.arquillian.spacelift.execution.Tasks;
import org.arquillian.spacelift.execution.impl.DefaultExecutionServiceFactory;
import org.jboss.aerogear.unifiedpush.utils.perf.UpsBatchGenerateCommand;

public class UpsCartCli {

    public static void main(String[] args) {

        @SuppressWarnings("unchecked")
        CliBuilder<Runnable> builder = Cli.<Runnable> builder("ups-cart-cli")
            .withDefaultCommand(Help.class)
            .withCommands(Help.class,
                AppCartridgeCreateCommand.class,
                AppCartridgeDeleteCommand.class,
                DataGeneratorCommand.class,
                UpsGenerateAppsCommand.class,
                UpsDeleteAppsCommand.class,
                UpsDumpCommand.class,
                UpsBatchGenerateCommand.class);

        // register tasks execution factory
        Tasks.setDefaultExecutionServiceFactory(new DefaultExecutionServiceFactory());

        builder.build().parse(args).run();
    }
}
