/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.unifiedpush.model;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.jboss.aerogear.unifiedpush.jpa.PersistentObject;

public class PushApplication extends PersistentObject implements org.jboss.aerogear.unifiedpush.api.PushApplication {
    private static final long serialVersionUID = 6507691362454032282L;

    private String name;

    private String description;

    private String pushApplicationID = UUID.randomUUID().toString();
    private String masterSecret = UUID.randomUUID().toString();

    private String developer;

    private Set<iOSVariant> iOSVariants = new HashSet<iOSVariant>();

    private Set<AndroidVariant> androidVariants = new HashSet<AndroidVariant>();

    private Set<SimplePushVariant> simplePushVariants = new HashSet<SimplePushVariant>();

    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public Set<iOSVariant> getIOSVariants() {
        return this.iOSVariants;
    }

    public void setIOSVariants(final Set<iOSVariant> iOSVariants) {
        this.iOSVariants = iOSVariants;
    }

    public Set<AndroidVariant> getAndroidVariants() {
        return this.androidVariants;
    }

    public void setAndroidVariants(final Set<AndroidVariant> androidVariants) {
        this.androidVariants = androidVariants;
    }

    public Set<SimplePushVariant> getSimplePushVariants() {
        return simplePushVariants;
    }

    public void setSimplePushVariants(final Set<SimplePushVariant> simplePushVariants) {
        this.simplePushVariants = simplePushVariants;
    }

    public String getPushApplicationID() {
        return pushApplicationID;
    }

    public void setPushApplicationID(String pushApplicationID) {
        this.pushApplicationID = pushApplicationID;
    }

    public void setMasterSecret(String secret) {
        this.masterSecret = secret;
    }

    @Override
    public String getMasterSecret() {
        return masterSecret;
    }

    public String getDeveloper() {
        return developer;
    }

    public void setDeveloper(String developer) {
        this.developer = developer;
    }
}
