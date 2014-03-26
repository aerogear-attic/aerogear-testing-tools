package org.jboss.aerogear.test.cli;

import io.airlift.command.Option;

public abstract class OpenShiftCommand implements Runnable {

    @Option(name = { "-a", "--app-name" }, title = "app-name", required = true, description = "Name of the application on OpenShift")
    public String appName;

    @Option(name = { "-n", "--namespace" }, title = "namespace", description = "Namespace on OpenShift, default value: mobileqa")
    public String namespace = "mobileqa";
}
