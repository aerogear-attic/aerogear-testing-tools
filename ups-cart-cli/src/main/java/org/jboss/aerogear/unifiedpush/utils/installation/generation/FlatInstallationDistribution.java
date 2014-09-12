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
package org.jboss.aerogear.unifiedpush.utils.installation.generation;

import java.util.List;

import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.utils.installation.InstallationUtils;
import org.jboss.aerogear.unifiedpush.utils.perf.model.MassInstallation;

/**
 * For every variant belonging to some application, there will be generated same number of installations for given variant type.
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class FlatInstallationDistribution implements InstallationDistributionStrategy {

    @Override
    public MassInstallation generate(List<PushApplication> registeredApplications, int count) {

        MassInstallation massInstallation = new MassInstallation();

        for (PushApplication pushApp : registeredApplications) {
            for (Variant variant : pushApp.getVariants()) {

                String variantId = variant.getVariantID();

                switch (variant.getType()) {
                    case ANDROID:
                        massInstallation.map.put(variantId, InstallationUtils.generateAndroid(count));
                        break;
                    case IOS:
                        massInstallation.map.put(variantId, InstallationUtils.generateIos(count));
                        break;
                    case SIMPLE_PUSH:
                        massInstallation.map.put(variantId, InstallationUtils.generateSimplePush(count));
                        break;
                    case CHROME_PACKAGED_APP:
                        throw new UnsupportedOperationException();
                    default:
                        break;
                }
            }
        }

        return massInstallation;
    }

}
