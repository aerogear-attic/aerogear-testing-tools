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

import org.jboss.aerogear.test.Session;

import java.util.Collection;
import java.util.List;

public interface UPSContext<
        ENTITY,
        ENTITY_ID,
        BLUEPRINT extends ENTITY,
        EDITOR extends ENTITY,
        PARENT,
        WORKER extends UPSWorker<ENTITY, ENTITY_ID, BLUEPRINT, EDITOR, PARENT, CONTEXT, WORKER>,
        CONTEXT extends UPSContext<ENTITY, ENTITY_ID, BLUEPRINT, EDITOR, PARENT, WORKER, CONTEXT>> {

    BLUEPRINT create();

    BLUEPRINT generate();

    BlueprintList<ENTITY, ENTITY_ID, BLUEPRINT, EDITOR, PARENT, WORKER, CONTEXT> generate(int count);

    CONTEXT findAll();

    CONTEXT find(ENTITY_ID id);

    EDITOR edit(ENTITY_ID id);

    WORKER getWorker();

    PARENT getParent();

    CONTEXT persist(BLUEPRINT blueprint);

    CONTEXT persist(Collection<? extends BLUEPRINT> blueprints);

    CONTEXT merge(ENTITY entity);

    CONTEXT merge(Collection<? extends ENTITY> entities);

    CONTEXT remove(ENTITY entity);

    CONTEXT remove(Collection<? extends ENTITY> entities);

    CONTEXT removeById(ENTITY_ID id);

    CONTEXT removeAll();

    Session getSession();

    List<ENTITY> detachEntities();

    ENTITY detachEntity() throws IllegalStateException;

    ENTITY detachEntity(ENTITY_ID id);

    ENTITY_ID getEntityID(ENTITY entity);
}