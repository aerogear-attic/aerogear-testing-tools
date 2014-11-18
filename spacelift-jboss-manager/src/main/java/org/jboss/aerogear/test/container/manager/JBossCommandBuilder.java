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

    private static final String SERVER_BASE_PATH = "/standalone/";
    private static final String CONFIG_PATH = SERVER_BASE_PATH + "configuration/";
    private static final String LOG_PATH = SERVER_BASE_PATH + "log/";

    public Command build(ManagedContainerConfiguration configuration) throws Exception {

        validate(configuration);

        final String jbossHome = configuration.getJbossHome();

        final String modulesPath = configuration.getModulePath();

        File modulesDir = new File(modulesPath);
        if (!modulesDir.isDirectory()) {
            throw new IllegalStateException("Cannot find: " + modulesDir);
        }

        final String bundlesPath = modulesDir.getParent() + File.separator + "bundles";

        boolean withBundles = true;

        File bundlesDir = new File(bundlesPath);
        if (!bundlesDir.isDirectory()) {
            withBundles = false;
        }

        File modulesJar = new File(jbossHome + File.separatorChar + "jboss-modules.jar");
        if (!modulesJar.exists()) {
            throw new IllegalStateException("Cannot find: " + modulesJar);
        }

        final CommandBuilder cb = new CommandBuilder(configuration.getJavaBin());

        cb.splitToParameters(configuration.getJavaVmArguments());

        if (configuration.isEnableAssertions()) {
            cb.parameter("-ea");
        }

        cb.parameter("-Djboss.home.dir=" + jbossHome);
        cb.parameter("-Dorg.jboss.boot.log.file=" + jbossHome + LOG_PATH + "boot.log");
        cb.parameter("-Dlogging.configuration=file:" + jbossHome + CONFIG_PATH + "logging.properties");
        cb.parameter("-Djboss.modules.dir=" + modulesDir.getCanonicalPath());
        cb.parameter("-jar");
        cb.parameter(modulesJar.getAbsolutePath());
        cb.parameter("-mp");
        cb.parameter(modulesPath);
        cb.parameter("-jaxpmodule");
        cb.parameter("javax.xml.jaxp-provider");
        cb.parameter("org.jboss.as.standalone");
        cb.parameter("-server-config");
        cb.parameter(configuration.getServerConfig());

        if (withBundles) {
            cb.parameter("-Djboss.bundles.dir=" + bundlesDir.getCanonicalPath());
        }

        return cb.build();
    }

    /**
     *
     * @param configuration
     * @throws IllegalArgumentException iff {@code configuration} is null object.
     * @throws IllegalStateException iff {@link ManagedContainerConfiguration#getJbossHome()} returns null.
     */
    private void validate(ManagedContainerConfiguration configuration) throws RuntimeException {
        if (configuration == null) {
            throw new IllegalArgumentException("Configuration set to JBossCommandBuilder is null.");
        }

        if (configuration.getJbossHome() == null) {
            throw new IllegalStateException("JBOSS_HOME is set to null for JBossCommandBuilder");
        }
    }
}
