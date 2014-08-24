package org.jboss.aerogear.test.cli;

import io.airlift.command.Arguments;
import io.airlift.command.Command;
import io.airlift.command.Option;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.aerogear.test.UnifiedPushServer;

@Command(name = "ups-dump", description = "Dumps all data from UPS so they are usable by performance tests")
public class UpsDumpCommand extends UnifiedPushServerCommand {
    private static final Logger log = Logger.getLogger(UpsDumpCommand.class.getName());

    @Option(
        name = { "--ignore-redirects" },
        description = "Disable automatic redirects for performance tests")
    public boolean ignoreRedirects;

    @Option(
        name = { "--alias" },
        description = "Alias to be used for variants, default value: mobile-qa-list@redhat.com")
    public String alias = "mobile-qa-list@redhat.com";

    @Arguments(
        title = "location",
        required = true,
        description = "Directory where where to store dumped files")
    public String location;

    @Override
    public void run() {

        log.log(Level.INFO, "Connecting to UPS running at {0}", upsRootUrl());
        UnifiedPushServer server;
        try {
            server = new UnifiedPushServer(unifiedPushServerUrl(), authServerUrl());
        } catch (MalformedURLException ex) {
            throw new RuntimeException("Server URL was malformed: " + ex.getMessage());
        }

        server.login(username, password);
        log.log(Level.INFO, "Logged as {0}", username);

        try {
            File dir = new File(location);
            dir.mkdirs();
            server.dump(dir, ignoreRedirects, alias);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}
