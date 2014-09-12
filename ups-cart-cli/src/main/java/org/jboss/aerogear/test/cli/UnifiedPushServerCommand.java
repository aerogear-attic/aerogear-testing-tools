package org.jboss.aerogear.test.cli;

import io.airlift.command.Option;

import java.net.MalformedURLException;
import java.text.MessageFormat;

import org.jboss.aerogear.test.UnifiedPushServer;

public abstract class UnifiedPushServerCommand extends OpenShiftCommand {

    @Option(
        name = { "--no-https" },
        arity = 0,
        description = "Use HTTP protocol instead of HTTPS")
    public boolean noHttps;

    @Option(
        name = { "--port" },
        title = "port",
        description = "Port to be used for REST calls, default value: 80 or 443, depending on --no-https")
    public int port = -1;

    @Option(
        name = { "-p", "--password" },
        required = true, title = "password",
        description = "Password to be used for Unified Push Server login. "
            + "If old-password matches, this one replaces the old one")
    public String password;

    @Option(
        name = { "--old-password" },
        title = "old-password",
        description = "Previous password. Default value: 123")
    public String oldPassword = "123";

    @Option(
        name = { "-u", "--username" },
        title = "username",
        description = "Username to be used for Unified Push Server login, default value: admin")
    public String username = "admin";

    protected String upsRootUrl() {
        String rootUrl = MessageFormat.format("{0}://{1}-{2}.rhcloud.com:{3}",
            (noHttps ? "http" : "https"),
            appName,
            namespace,
            (port == -1 ? (noHttps ? 80 : 443) : port));
        return rootUrl;
    }

    protected String unifiedPushServerUrl() {
        return upsRootUrl() + "/ag-push";
    }

    protected String authServerUrl() {
        return upsRootUrl() + "/auth";
    }

    /**
     * Logs you into UPS with {@code username} and {@code password}.
     *
     * @param username
     * @param password
     * @return
     */
    protected UnifiedPushServer login(String username, String password) {
        UnifiedPushServer server = null;
        try {
            server = new UnifiedPushServer(unifiedPushServerUrl(), authServerUrl());
            server.login(username, password);
        } catch (MalformedURLException ex) {
            throw new RuntimeException("Server URL was malformed: " + ex.getMessage());
        }
        return server;
    }

    /**
     *
     * @param number number to be parsed
     * @throws NumberFormatException if {@code number <= 0} or {@code number} is not a number
     * @return 0 if {@code number == -1}, else {@code number} as integer.
     */
    protected int parseNumber(String number) {

        int parsedNumber;

        try {
            parsedNumber = Integer.parseInt(number);
            if (parsedNumber == -1) {
                return 0;
            }
            if (parsedNumber < 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ex) {
            throw new NumberFormatException(
                String.format("Parsed number '%s' is not a valid number or it is lower then 0", number));
        }

        return parsedNumber;
    }
}
