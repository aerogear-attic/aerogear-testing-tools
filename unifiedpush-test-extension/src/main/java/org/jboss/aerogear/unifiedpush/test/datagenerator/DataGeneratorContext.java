package org.jboss.aerogear.unifiedpush.test.datagenerator;

import java.util.ArrayList;
import java.util.List;

import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.api.Installation;

public class DataGeneratorContext {

    private final DataGeneratorConfig config;
    private final List<PushApplication> applications = new ArrayList<PushApplication>();
    private final List<Installation> installations = new ArrayList<Installation>();

    public DataGeneratorContext(DataGeneratorConfig config) {
        this.config = config;
    }

    public DataGeneratorConfig getConfig() {
        return config;
    }

    public List<PushApplication> getApplications() {
        return applications;
    }

    public List<Installation> getInstallations() {
        return installations;
    }

}