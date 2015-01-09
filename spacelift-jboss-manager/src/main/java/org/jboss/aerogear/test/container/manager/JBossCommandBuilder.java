package org.jboss.aerogear.test.container.manager;

import java.io.File;

import org.arquillian.spacelift.process.Command;
import org.arquillian.spacelift.process.CommandBuilder;

/**
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class JBossCommandBuilder {

    public Command build(JBossManagerConfiguration configuration) throws Exception {

        validate(configuration);

        if (configuration.isDomain()) {
            return buildDomainCommand(configuration);
        } else {
            return buildStandaloneCommand(configuration);
        }
    }

    private Command buildStandaloneCommand(JBossManagerConfiguration configuration) {

        final CommandBuilder cb = new CommandBuilder(configuration.getJavaBin());

        cb.parameter("-D[Standalone]");
        cb.splitToParameters(configuration.getJavaOpts());
        cb.parameter("-Dorg.jboss.boot.log.file=" + configuration.getJBossLogDir() + "/server.log");
        cb.parameter("-Dlogging.configuration=file:" + configuration.getJBossConfigDir() + "/logging.properties");

        if (new File(configuration.getJBossModuleDir() + "/bundles").exists()) {
            cb.parameter("-Djboss.bundles.dir=" + configuration.getJBossModuleDir() + "/bundles");
        }

        cb.parameters("-jar", configuration.getJBossHome() + "/jboss-modules.jar");
        cb.parameters("-mp", configuration.getJBossModuleDir());
        cb.parameters("-jaxpmodule", "javax.xml.jaxp-provider");
        cb.parameters("org.jboss.as.standalone");
        cb.parameter("-Djboss.home.dir=" + configuration.getJBossHome());
        cb.parameter("-Djboss.server.base.dir=" + configuration.getJBossBaseDir());
        cb.splitToParameters(configuration.getServerJavaOpts());

        return cb.build();
    }

    private Command buildDomainCommand(JBossManagerConfiguration configuration) {

        final CommandBuilder cb = new CommandBuilder(configuration.getJavaBin());

        cb.parameters("-D[Process Controller]");
        cb.splitToParameters(configuration.getProcessControllerJavaOpts());
        cb.parameter("-Dorg.jboss.boot.log.file=" + configuration.getJBossLogDir() + "/process-controller.log");
        cb.parameter("-Dlogging.configuration=file:" + configuration.getJBossConfigDir() + "/logging.properties");

        if (new File(configuration.getJBossModuleDir() + "/bundles").exists()) {
            cb.parameter("-Djboss.bundles.dir=" + configuration.getJBossModuleDir() + "/bundles");
        }

        cb.parameters("-jar", configuration.getJBossHome() + "/jboss-modules.jar");
        cb.parameters("-mp", configuration.getJBossModuleDir());
        cb.parameter("org.jboss.as.process-controller");
        cb.parameters("-jboss-home", configuration.getJBossHome());
        cb.parameters("-jvm", configuration.getJavaBin());
        cb.parameters("-mp", configuration.getJBossModuleDir());
        cb.parameter("--");
        cb.parameter("-Dorg.jboss.boot.log.file=" + configuration.getJBossLogDir() + "/host-controller.log");
        cb.parameter("-Dlogging.configuration=file:" + configuration.getJBossConfigDir() + "/logging.properties");
        cb.splitToParameters(configuration.getHostControllerJavaOpts());
        cb.parameter("--");
        cb.parameters("-default-jvm", configuration.getJavaBin());

        return cb.build();
    }

    /**
     *
     * @param configuration
     * @throws IllegalArgumentException iff {@code configuration} is null object.
     * @throws IllegalStateException iff {@link JBossManagerConfiguration#getJbossHome()} returns null.
     */
    private void validate(JBossManagerConfiguration configuration) throws RuntimeException {
        if (configuration == null) {
            throw new IllegalArgumentException("Configuration set to JBossCommandBuilder is null.");
        }

        if (configuration.getJBossHome() == null) {
            throw new IllegalStateException("JBOSS_HOME is set to null for JBossCommandBuilder");
        }
    }
}
