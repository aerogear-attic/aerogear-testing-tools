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
package org.jboss.aerogear.unifiedpush.utils.variant.generation;

import org.jboss.aerogear.unifiedpush.api.AndroidVariant;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.api.VariantType;
import org.jboss.aerogear.unifiedpush.utils.variant.AndroidVariantUtils;
import org.jboss.aerogear.unifiedpush.utils.variant.SimplePushVariantUtils;
import org.jboss.aerogear.unifiedpush.utils.variant.VariantMetadata;
import org.jboss.aerogear.unifiedpush.utils.variant.iOSVariantUtils;

/**
 * Generates arbitrary variant.
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class VariantGeneratorUtil {

    /**
     * Generates arbitrary variant (Android, iOS or SimplePush)
     *
     * @param variantType type of variant to be generated
     * @param variantMetadata
     * @return generated variant of type {@code variantType}
     */
    public static Variant getVariant(VariantType variantType, VariantMetadata variantMetadata) {

        Variant variant = null;

        switch (variantType) {
            case ANDROID:
                AndroidVariant androidVariant = AndroidVariantUtils.generate();

                if (variantMetadata.getGoogleKey() != null) {
                    androidVariant.setGoogleKey(variantMetadata.getGoogleKey());
                }

                if (variantMetadata.getProjectNumber() != null) {
                    androidVariant.setProjectNumber(variantMetadata.getProjectNumber());
                }

                variant = androidVariant;

                break;
            case IOS:
                variant = iOSVariantUtils.generate(
                    variantMetadata.getCertificatePath(),
                    variantMetadata.getCertificatePass(),
                    variantMetadata.getProduction());

                break;
            case SIMPLE_PUSH:
                variant = SimplePushVariantUtils.generate();
                break;
            case CHROME_PACKAGED_APP:
                throw new UnsupportedOperationException("Generation of Chrome variant is not supported.");
            default:
                break;
        }

        return variant;
    }
}
