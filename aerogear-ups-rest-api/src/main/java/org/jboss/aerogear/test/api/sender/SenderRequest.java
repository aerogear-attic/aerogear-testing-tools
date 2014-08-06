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
package org.jboss.aerogear.test.api.sender;

import org.apache.http.HttpStatus;
import org.jboss.aerogear.test.UnexpectedResponseException;
import org.jboss.aerogear.test.api.AbstractSessionRequest;
import org.jboss.aerogear.unifiedpush.SenderClient;
import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.message.MessageResponseCallback;
import org.jboss.aerogear.unifiedpush.message.UnifiedMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class SenderRequest extends AbstractSessionRequest<SenderRequest> {

    public UnifiedMessageBlueprint message() {
        return new UnifiedMessageBlueprint();
    }

    /* FIXME what should we generate?
    public UnifiedMessageBlueprint generate() {
        return message();
    }*/

    public SenderRequest send(UnifiedMessage message) {
        SenderClient senderClient = new SenderClient.Builder(getSession().getBaseUrl().toExternalForm())
                .customTrustStore("setup/aerogear.truststore", null, "aerogear")
                .build();

        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicInteger statusCode = new AtomicInteger(-1);

        MessageResponseCallback callback = new MessageResponseCallback() {
            @Override
            public void onComplete(int status) {
                statusCode.set(status);
                latch.countDown();
            }

            @Override
            public void onError(Throwable throwable) {
                throw new RuntimeException(throwable);
            }
        };

        senderClient.send(message, callback);

        try {
            latch.await(5000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        UnexpectedResponseException.verifyStatusCode(statusCode.get(), HttpStatus.SC_OK);

        return this;
    }

    public static SenderRequest request() {
        return new SenderRequest();
    }

    public class UnifiedMessageBlueprint {

        private final UnifiedMessage.Builder builder;

        public UnifiedMessageBlueprint() {
            builder = new UnifiedMessage.Builder();
        }

        public UnifiedMessageBlueprint pushApplication(PushApplication pushApplication) {
            return pushApplicationId(pushApplication.getPushApplicationID())
                    .masterSecret(pushApplication.getMasterSecret());
        }

        public UnifiedMessageBlueprint pushApplicationId(String pushApplicationId) {
            builder.pushApplicationId(pushApplicationId);
            return this;
        }

        public UnifiedMessageBlueprint masterSecret(String masterSecret) {
            builder.masterSecret(masterSecret);
            return this;
        }

        public UnifiedMessageBlueprint aliases(String... aliases) {
            return aliases(Arrays.asList(aliases));
        }

        public UnifiedMessageBlueprint aliases(List<String> aliases) {
            builder.aliases(aliases);
            return this;
        }

        public UnifiedMessageBlueprint aliasesOf(Installation... installations) {
            return aliasesOf(Arrays.asList(installations));
        }

        public UnifiedMessageBlueprint aliasesOf(List<? extends Installation> installations) {
            List<String> aliases = new ArrayList<String>();
            for (Installation installation : installations) {
                aliases.add(installation.getAlias());
            }
            return aliases(aliases);
        }

        public UnifiedMessageBlueprint deviceTypes(String... deviceTypes) {
            return deviceTypes(Arrays.asList(deviceTypes));
        }

        public UnifiedMessageBlueprint deviceTypes(List<String> deviceTypes) {
            builder.deviceType(deviceTypes);
            return this;
        }

        public UnifiedMessageBlueprint deviceTypesOf(Installation... installations) {
            return deviceTypesOf(Arrays.asList(installations));
        }

        public UnifiedMessageBlueprint deviceTypesOf(List<? extends Installation> installations) {
            List<String> deviceTypes = new ArrayList<String>();
            for (Installation installation : installations) {
                deviceTypes.add(installation.getDeviceType());
            }
            return deviceTypes(deviceTypes);
        }

        public UnifiedMessageBlueprint categories(String... categories) {
            builder.categories(categories);
            return this;
        }

        public UnifiedMessageBlueprint categories(Set<String> categories) {
            builder.categories(categories);
            return this;
        }

        public UnifiedMessageBlueprint categoriesOf(Installation... installations) {
            return categoriesOf(Arrays.asList(installations));
        }

        public UnifiedMessageBlueprint categoriesOf(List<? extends Installation> installations) {
            Set<String> categories = new HashSet<String>();
            for (Installation installation : installations) {
                categories.addAll(installation.getCategories());
            }
            return categories(categories);
        }

        public UnifiedMessageBlueprint variantIDs(String... variants) {
            return variantIDs(Arrays.asList(variants));
        }

        public UnifiedMessageBlueprint variantIDs(List<String> variants) {
            builder.variants(variants);
            return this;
        }

        public UnifiedMessageBlueprint variants(Variant... variants) {
            return variants(Arrays.asList(variants));
        }

        public UnifiedMessageBlueprint variants(List<? extends Variant> variants) {
            List<String> variantIDs = new ArrayList<String>();
            for (Variant variant : variants) {
                variantIDs.add(variant.getVariantID());
            }
            return variantIDs(variantIDs);
        }

        public UnifiedMessageBlueprint attribute(String key, String value) {
            builder.attribute(key, value);
            return this;
        }

        public UnifiedMessageBlueprint alert(String message) {
            builder.alert(message);
            return this;
        }

        public UnifiedMessageBlueprint sound(String sound) {
            builder.sound(sound);
            return this;
        }

        public UnifiedMessageBlueprint badge(String badge) {
            builder.badge(badge);
            return this;
        }

        public UnifiedMessageBlueprint contentAvailable() {
            builder.contentAvailable();
            return this;
        }

        public UnifiedMessageBlueprint simplePush(String version) {
            builder.simplePush(version);
            return this;
        }

        public UnifiedMessageBlueprint timeToLive(int seconds) {
            builder.timeToLive(seconds);
            return this;
        }

        public SenderRequest send() {
            SenderRequest.this.send(builder.build());
            return SenderRequest.this;
        }

    }

}
