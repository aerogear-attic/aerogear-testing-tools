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

import org.jboss.aerogear.test.ContentTypes;

import java.util.Collection;

// FIXME no need for abstract UPS worker?
public abstract class AbstractUPSWorker<
        ENTITY,
        ENTITY_ID,
        BLUEPRINT extends ENTITY,
        EDITOR extends ENTITY,
        PARENT,
        CONTEXT extends UPSContext<ENTITY, ENTITY_ID, BLUEPRINT, EDITOR, PARENT, WORKER, CONTEXT>,
        WORKER extends UPSWorker<ENTITY, ENTITY_ID, BLUEPRINT, EDITOR, PARENT, CONTEXT, WORKER>>

        implements UPSWorker<ENTITY, ENTITY_ID, BLUEPRINT, EDITOR, PARENT, CONTEXT, WORKER> {

    private String contentType = ContentTypes.json();

    public String getContentType() {
        return contentType;
    }

    @Override
    public void delete(CONTEXT context, Collection<? extends ENTITY> entities) {
        for (ENTITY entity : entities) {
            deleteById(context, context.getEntityID(entity));
        }
    }

    @SuppressWarnings("unchecked")
    public WORKER contentType(String contentType) {
        this.contentType = contentType;
        return (WORKER) this;
    }
}
