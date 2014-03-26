package org.jboss.aerogear.test.cli;

import io.airlift.command.Option;

import java.text.MessageFormat;

public abstract class UnifiedPushServerCommand extends OpenShiftCommand {

    @Option(name = { "--no-https" }, arity = 0, description = "Use HTTP protocol instead of HTTPS")
    public boolean noHttps;

    @Option(name = { "--port" }, title = "port", description = "Port to be used for REST calls, default value: 80 or 443, depending on --no-https")
    public int port = -1;

    @Option(name = { "-p", "--password" }, required = true, title = "password", description = "Password to be used for Unified Push Server login. If old-password matches, this one replaces the old one")
    public String password;

    @Option(name = { "--old-password" }, title = "old-password", description = "Previous password. Default value: 123")
    public String oldPassword = "123";

    @Option(name = { "-u", "--username" }, title = "username", description = "Username to be used for Unified Push Server login, default value: admin")
    public String username = "admin";

    protected String upsRootUrl() {
        String rootUrl = MessageFormat.format("{0}://{1}-{2}.rhcloud.com:{3}",
            (noHttps ? "http" : "https"),
            appName,
            namespace,
            (port == -1 ? (noHttps ? 80 : 443) : port));
        return rootUrl;
    }
}
