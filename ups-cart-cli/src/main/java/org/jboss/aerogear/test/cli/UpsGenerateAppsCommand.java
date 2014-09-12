package org.jboss.aerogear.test.cli;

import io.airlift.command.Command;
import io.airlift.command.Option;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.aerogear.test.UnifiedPushServer;
import org.jboss.aerogear.unifiedpush.api.AndroidVariant;
import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.api.SimplePushVariant;
import org.jboss.aerogear.unifiedpush.api.iOSVariant;
import org.jboss.aerogear.unifiedpush.utils.installation.InstallationUtils;

/**
 * Meant to be used with 'as-is' UPS installation (no custom batch endpoints added to it)
 *
 */
@Command(name = "ups-generate", description = "Generates data for an UPS instance")
public class UpsGenerateAppsCommand extends UnifiedPushServerCommand implements Runnable {

    private static final Logger log = Logger.getLogger(UpsGenerateAppsCommand.class.getName());

    @Option(
        name = { "--push-app-name" },
        title = "push-app-name",
        description = "Push application name",
        required = true)
    public String pushAppName;

    @Option(
        name = { "--google-key" },
        title = "google-key",
        description = "Google API key for Android application variant. "
            + "If set, --project-no is required and Android variant is created.")
    public String googleKey;

    @Option(
        name = { "--project-no" },
        title = "project-number",
        description = "Google Project Number for Android application variant. "
            + "If present, Android variant is created.")
    public String projectNumber;

    @Option(
        name = { "--simple-push" },
        arity = 0,
        description = "If set, SimplePush application variant is generated")
    public boolean simplePush;

    @Option(
        name = { "--cert-path" },
        title = "certificate-path",
        description = "Path to iOS certificate. "
            + "If set, --cert-pass is required and iOS variant is created.")
    public String certificatePath;

    @Option(
        name = { "--cert-pass" },
        title = "certificate-passphrase",
        description = "Certificate passphrase")
    public String certificatePass;

    @Option(
        name = { "--production" },
        arity = 0,
        title = "production",
        description = "If set, certificate is marked as production one")
    public boolean production;

    @Option(
        name = { "--installations" },
        title = "installations",
        description = "Number of installations being installed along variant registration.")
    public String numberOfInstallations = "-1";

    private UnifiedPushServer server = null;

    @Override
    public void run() {

        int installationsCount = parseNumber(numberOfInstallations);

        log.log(Level.INFO, "Connecting to UPS running at {0}", upsRootUrl());

        server = login(username, password);

        log.log(Level.INFO, "Logged as {0}", username);

        PushApplication app = server.addPushApplication(pushAppName);
        log.log(Level.INFO, "Added PushApplication named {0}", app.getName());

        if (googleKey != null && projectNumber != null) {
            AndroidVariant av = server.addAndroidVariant(app, googleKey, projectNumber);
            log.log(Level.INFO, "Added Android Variant named {0}", av.getName());

            List<Installation> installations = InstallationUtils.generateAndroid(installationsCount);
            server.registerInstallations(installations, av);
            log.log(Level.INFO, "Added {0} Android installations", installationsCount);
        }

        if (simplePush) {
            SimplePushVariant spv = server.addSimplePushVariant(app);
            log.log(Level.INFO, "Added SimplePush Variant named {0}", spv.getName());

            List<Installation> installations = InstallationUtils.generateSimplePush(installationsCount);
            server.registerInstallations(installations, spv);
            log.log(Level.INFO, "Added {0} SimplePush installations.", installationsCount);
        }

        if (certificatePath != null && certificatePass != null) {
            iOSVariant iv = server.addiOSVariant(app, certificatePath, certificatePass, production);
            log.log(Level.INFO, "Added iOS Variant named {0}", iv.getName());

            List<Installation> installations = InstallationUtils.generateIos(installationsCount);
            server.registerInstallations(installations, iv);
            log.log(Level.INFO, "Added {0} iOS installations.", installationsCount);
        }

    }

}
