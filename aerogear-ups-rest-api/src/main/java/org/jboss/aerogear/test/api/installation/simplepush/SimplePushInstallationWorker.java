package org.jboss.aerogear.test.api.installation.simplepush;

import org.jboss.aerogear.test.Session;
import org.jboss.aerogear.test.api.installation.InstallationWorker;
import org.jboss.aerogear.unifiedpush.api.SimplePushVariant;

public class SimplePushInstallationWorker extends InstallationWorker<SimplePushInstallationBlueprint,
        SimplePushInstallationEditor, SimplePushVariant, SimplePushInstallationContext, SimplePushInstallationWorker> {

    private SimplePushInstallationWorker() {

    }

    @Override
    public SimplePushInstallationContext createContext(Session session, SimplePushVariant parent) {
        return new SimplePushInstallationContext(this, parent, session);
    }

    public static SimplePushInstallationWorker worker() {
        return new SimplePushInstallationWorker();
    }


}