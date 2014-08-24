/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.aerogear.unifiedpush.utils.perf;

import io.airlift.command.Command;
import java.net.MalformedURLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.aerogear.test.cli.UpsGenerateAppsCommand;
import org.jboss.aerogear.unifiedpush.api.AndroidVariant;
import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.api.SimplePushVariant;
import org.jboss.aerogear.unifiedpush.api.iOSVariant;
import org.jboss.aerogear.unifiedpush.utils.InstallationUtils;

/**
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
@Command(name = "ups-generate-batch", description = "Generates arbitrary number of installations via custom batching endpoint")
public class UpsBatchGenerateCommand extends UpsGenerateAppsCommand {

    private static final Logger log = Logger.getLogger(UpsBatchGenerateCommand.class.getName());

    private BatchUnifiedPushServer server;

    private int[] registrationCounts = new int[3];

    @Override
    public void run() {

        registrationCounts[0] = 1;
        registrationCounts[1] = 1;
        registrationCounts[2] = parseCountOfInstallations(numberOfInstallations);

        log.log(Level.INFO, "Connecting to UPS running at {0}", upsRootUrl());

        try {
            server = new BatchUnifiedPushServer(unifiedPushServerUrl(), authServerUrl());
        } catch (MalformedURLException ex) {
            throw new RuntimeException("Server URL was malformed: " + ex.getMessage());
        }

        server.login(username, password);
        log.log(Level.INFO, "Logged as {0}", username);

        int installationsCount = parseCountOfInstallations(numberOfInstallations);

        PushApplication app = server.addPushApplication(pushAppName);
        log.log(Level.INFO, "Added PushApplication named {0}", app.getName());

        if (googleKey != null && projectNumber != null) {
            AndroidVariant av = server.addAndroidVariant(app, googleKey, projectNumber);
            log.log(Level.INFO, "Added Android Variant named {0}", av.getName());

            List<Installation> installations = InstallationUtils.generateAndroid(installationsCount);
            server.registerInstallationsViaEndpoint(installations, av);
            log.log(Level.INFO, "Added {0} Android installations", installationsCount);
        }

        if (simplePush) {
            SimplePushVariant spv = server.addSimplePushVariant(app);
            log.log(Level.INFO, "Added SimplePush Variant named {0}", spv.getName());

            List<Installation> installations = InstallationUtils.generateSimplePush(installationsCount);
            server.registerInstallationsViaEndpoint(installations, spv);
            log.log(Level.INFO, "Added {0} SimplePush installations", installationsCount);
        }

        if (certificatePath != null && certificatePass != null) {
            iOSVariant iv = server.addiOSVariant(app, certificatePath, certificatePass, production);
            log.log(Level.INFO, "Added iOS Variant named {0}", iv.getName());

            List<Installation> installations = InstallationUtils.generateIos(installationsCount);
            server.registerInstallationsViaEndpoint(installations, iv);
            log.log(Level.INFO, "Added {0} iOS installations", installationsCount);
        }
    }

}
