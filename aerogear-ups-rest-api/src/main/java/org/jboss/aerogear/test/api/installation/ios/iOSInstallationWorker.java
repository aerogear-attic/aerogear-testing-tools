package org.jboss.aerogear.test.api.installation.ios;

import org.jboss.aerogear.test.Session;
import org.jboss.aerogear.test.api.installation.InstallationWorker;
import org.jboss.aerogear.unifiedpush.api.iOSVariant;

public class iOSInstallationWorker extends InstallationWorker<iOSInstallationBlueprint,
        iOSInstallationEditor, iOSVariant, iOSInstallationContext, iOSInstallationWorker> {

    private iOSInstallationWorker() {

    }

    @Override
    public iOSInstallationContext createContext(Session session, iOSVariant parent) {
        return new iOSInstallationContext(this, parent, session);
    }

    public static iOSInstallationWorker worker() {
        return new iOSInstallationWorker();
    }


}