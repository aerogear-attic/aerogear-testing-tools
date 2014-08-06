package org.jboss.aerogear.test.api.installation.chromepackagedapp;

import org.jboss.aerogear.test.Session;
import org.jboss.aerogear.test.api.installation.InstallationWorker;
import org.jboss.aerogear.unifiedpush.api.ChromePackagedAppVariant;

public class ChromePackagedAppInstallationWorker extends InstallationWorker<ChromePackagedAppInstallationBlueprint,
        ChromePackagedAppInstallationEditor, ChromePackagedAppVariant, ChromePackagedAppInstallationContext,
        ChromePackagedAppInstallationWorker> {

    private ChromePackagedAppInstallationWorker() {

    }

    @Override
    public ChromePackagedAppInstallationContext createContext(Session session, ChromePackagedAppVariant parent) {
        return new ChromePackagedAppInstallationContext(this, parent, session);
    }

    public static ChromePackagedAppInstallationWorker worker() {
        return new ChromePackagedAppInstallationWorker();
    }


}