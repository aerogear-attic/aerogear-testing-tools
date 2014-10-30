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
package org.jboss.aerogear.test.api.installation;

import org.jboss.aerogear.unifiedpush.api.Category;
import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.api.Variant;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public abstract class InstallationExtension<
        BLUEPRINT extends InstallationBlueprint<BLUEPRINT, EDITOR, PARENT, WORKER, CONTEXT>,
        EDITOR extends InstallationEditor<BLUEPRINT, EDITOR, PARENT, WORKER, CONTEXT>,
        PARENT extends Variant,
        WORKER extends InstallationWorker<BLUEPRINT, EDITOR, PARENT, CONTEXT, WORKER>,
        CONTEXT extends InstallationContext<BLUEPRINT, EDITOR, PARENT, WORKER, CONTEXT>,
        EXTENSION extends InstallationExtension<BLUEPRINT, EDITOR, PARENT, WORKER, CONTEXT, EXTENSION>>

        extends Installation {

    private static final long serialVersionUID = 1L;

    protected final CONTEXT context;

    public InstallationExtension(CONTEXT context) {
        this.context = context;
    }

    public EXTENSION enabled(boolean enabled) {
        setEnabled(enabled);
        return castInstance();
    }

    public EXTENSION deviceToken(String deviceToken) {
        setDeviceToken(deviceToken);
        return castInstance();
    }

    public boolean isTokenInvalid() {
        return getDeviceToken().startsWith(Tokens.TOKEN_INVALIDATION_PREFIX);
    }

    public EXTENSION invalidateToken() {
        if (!isTokenInvalid()) {
            setDeviceToken(Tokens.TOKEN_INVALIDATION_PREFIX + getDeviceToken());
        }
        return castInstance();
    }

    public EXTENSION deviceType(String deviceType) {
        setDeviceType(deviceType);
        return castInstance();
    }

    public EXTENSION operatingSystem(String operatingSystem) {
        setOperatingSystem(operatingSystem);
        return castInstance();
    }

    public EXTENSION osVersion(String osVersion) {
        setOsVersion(osVersion);
        return castInstance();
    }

    public EXTENSION alias(String alias) {
        setAlias(alias);
        return castInstance();
    }

    public EXTENSION categories(String... categoryNames) {
        Category[] categories = new Category[categoryNames.length];
        for (int i = 0; i < categoryNames.length; i++) {
            categories[i] = new Category(categoryNames[i]);
        }
        return categories(categories);
    }

    public EXTENSION categories(Category... categories) {
        Set<Category> categorySet = new HashSet<Category>(Arrays.asList(categories));
        setCategories(categorySet);
        return castInstance();
    }

    public EXTENSION platform(String platform) {
        setPlatform(platform);
        return castInstance();
    }

    public EXTENSION generateMissingProperties() {

        return castInstance();
    }

    @SuppressWarnings("unchecked")
    private EXTENSION castInstance() {
        return (EXTENSION) this;
    }
}
