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
package org.jboss.aerogear.test.api;

import java.util.ArrayList;

public class BlueprintList<
        ENTITY,
        ENTITY_ID,
        BLUEPRINT extends ENTITY,
        EDITOR extends ENTITY,
        PARENT,
        WORKER extends UPSWorker<ENTITY, ENTITY_ID, BLUEPRINT, EDITOR, PARENT, CONTEXT, WORKER>,
        CONTEXT extends UPSContext<ENTITY, ENTITY_ID, BLUEPRINT, EDITOR, PARENT, WORKER, CONTEXT>>

        extends ArrayList<BLUEPRINT> {

    private static final long serialVersionUID = 1L;

    private final CONTEXT context;

    public BlueprintList(CONTEXT context) {
        this.context = context;
    }

    public CONTEXT persist() {
        return context.persist(this);
    }
}