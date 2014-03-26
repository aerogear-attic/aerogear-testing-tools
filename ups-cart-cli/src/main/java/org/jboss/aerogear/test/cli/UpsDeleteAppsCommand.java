package org.jboss.aerogear.test.cli;

import io.airlift.command.Command;
import io.airlift.command.Option;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.aerogear.test.UnifiedPushServer;
import org.jboss.aerogear.unifiedpush.model.PushApplication;

@Command(name = "ups-delete", description = "Deletes data from an UPS instance")
public class UpsDeleteAppsCommand extends UnifiedPushServerCommand implements Runnable {

    private static final Logger log = Logger.getLogger(UpsDeleteAppsCommand.class.getName());

    @Option(name = { "--all" }, arity = 0, description = "Delete all Push applications")
    public boolean all;

    @Override
    public void run() {

        log.log(Level.INFO, "Connecting to UPS running at {0}", upsRootUrl());
        UnifiedPushServer server = new UnifiedPushServer(upsRootUrl());

        server.login(username, oldPassword, password);
        log.log(Level.INFO, "Logged as {0}", username);

        if (all) {
            List<PushApplication> apps = server.deletePushApplications();
            log.log(Level.INFO, "Deleted {0} PushApplications", apps.size());
        }
    }
}
