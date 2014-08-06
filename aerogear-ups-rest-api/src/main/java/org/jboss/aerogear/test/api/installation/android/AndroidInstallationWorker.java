package org.jboss.aerogear.test.api.installation.android;

import org.jboss.aerogear.test.Session;
import org.jboss.aerogear.test.api.installation.InstallationWorker;
import org.jboss.aerogear.unifiedpush.api.AndroidVariant;

public class AndroidInstallationWorker extends InstallationWorker<AndroidInstallationBlueprint,
        AndroidInstallationEditor, AndroidVariant, AndroidInstallationContext, AndroidInstallationWorker> {

    private AndroidInstallationWorker() {

    }

    @Override
    public AndroidInstallationContext createContext(Session session, AndroidVariant parent) {
        return new AndroidInstallationContext(this, parent, session);
    }

    public static AndroidInstallationWorker worker() {
        return new AndroidInstallationWorker();
    }


}