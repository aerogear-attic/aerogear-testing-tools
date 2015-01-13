package org.jboss.aerogear.test.cli;

import io.airlift.airline.Option;

public abstract class AbstractCommand implements Runnable {

    @Option(name = { "-a", "--app-name" },
            title = "app-name",
            required = true,
            description = "Name of the application on OpenShift")
    public String appName;

    @Option(name = { "-n", "--namespace" },
            title = "namespace",
            description = "Namespace on OpenShift, default value: mobileqa")
    public String namespace = "mobileqa";

    protected final String getUnifiedpushTestExtensionUri() {
        return "https://"+appName+"-"+namespace+".rhcloud.com/unifiedpush-test-extension-server";
    }

}