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
package org.jboss.aerogear.test.api.variant.simplepush;

import org.jboss.aerogear.test.Session;
import org.jboss.aerogear.test.api.variant.VariantContext;
import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.api.SimplePushVariant;

public class SimplePushVariantContext extends VariantContext<SimplePushVariant, String, SimplePushVariantBlueprint,
        SimplePushVariantEditor, PushApplication, SimplePushVariantWorker, SimplePushVariantContext> {

    public SimplePushVariantContext(SimplePushVariantWorker worker,
                                    PushApplication parent, Session session) {
        super(worker, parent, session);
    }

    @Override
    public SimplePushVariantBlueprint create() {
        return new SimplePushVariantBlueprint(this);
    }

    @Override
    public SimplePushVariantBlueprint generate() {
        return create()
                .name(randomString())
                .description(randomString());
    }

    @Override
    protected SimplePushVariantContext castInstance() {
        return this;
    }

    @Override
    public String getEntityID(SimplePushVariant simplePushVariant) {
        return simplePushVariant.getVariantID();
    }
}
