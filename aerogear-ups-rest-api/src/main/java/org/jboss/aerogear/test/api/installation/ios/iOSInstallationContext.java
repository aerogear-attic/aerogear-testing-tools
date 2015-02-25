/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.test.api.installation.ios;

import org.jboss.aerogear.test.Session;
import org.jboss.aerogear.test.api.installation.InstallationContext;
import org.jboss.aerogear.unifiedpush.api.iOSVariant;

public class iOSInstallationContext extends InstallationContext<iOSInstallationBlueprint,
        iOSInstallationEditor, iOSVariant, iOSInstallationWorker, iOSInstallationContext> {

    public iOSInstallationContext(iOSInstallationWorker worker, iOSVariant parent, Session session) {
        super(worker, parent, session);
    }

    @Override
    protected iOSInstallationContext castInstance() {
        return this;
    }

    @Override
    protected iOSInstallationEditor createEditor() {
        return new iOSInstallationEditor(this);
    }

    @Override
    public iOSInstallationBlueprint create() {
        return new iOSInstallationBlueprint(this);
    }

    @Override
    public iOSInstallationBlueprint generate() {
        return create()
                .deviceToken(randomIOSDeviceToken())
                .alias(randomString());
    }

    public String randomIOSDeviceToken() {
        // We ask for 128 which should be more than enough to leave us with at least 64 characters after
        // all dashes are replaced.
        return randomStringOfLength(128).toLowerCase().replaceAll("-", "").substring(0, 64);
    }
}
