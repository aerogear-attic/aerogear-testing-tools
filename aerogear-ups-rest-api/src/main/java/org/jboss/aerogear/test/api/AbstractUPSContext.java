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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.aerogear.test.Helper;
import org.jboss.aerogear.test.Session;

public abstract class AbstractUPSContext<
        ENTITY,
        ENTITY_ID,
        BLUEPRINT extends ENTITY,
        EDITOR extends ENTITY,
        PARENT,
        WORKER extends UPSWorker<ENTITY, ENTITY_ID, BLUEPRINT, EDITOR, PARENT, CONTEXT, WORKER>,
        CONTEXT extends UPSContext<ENTITY, ENTITY_ID, BLUEPRINT, EDITOR, PARENT, WORKER, CONTEXT>>

        implements UPSContext<ENTITY, ENTITY_ID, BLUEPRINT, EDITOR, PARENT, WORKER, CONTEXT> {

    public static final int DEFAULT_RANDOM_STRING_LENGTH = 32;

    private final WORKER worker;
    private final PARENT parent;
    private final Session session;
    private final Map<ENTITY_ID, EDITOR> editors;

    public AbstractUPSContext(WORKER worker, PARENT parent, Session session) {
        this.worker = worker;
        this.parent = parent;
        this.session = session;
        this.editors = new HashMap<ENTITY_ID, EDITOR>();
    }

    @Override
    public List<ENTITY> detachEntities() {
        List<ENTITY> entities = new ArrayList<ENTITY>();
        entities.addAll(editors.values());
        return entities;
    }

    @Override
    public ENTITY detachEntity() throws IllegalStateException {
        if (editors.size() != 1) {
            throw new IllegalStateException(
                    MessageFormat.format("There has to be exactly one entity in the context to detach single entity! " +
                            "There were {0}", editors.size())
            );
        }
        return editors.values().iterator().next();
    }

    @Override
    public ENTITY detachEntity(ENTITY_ID id) {
        return retrieveOrThrow(id);
    }

    @Override
    public CONTEXT find(ENTITY_ID id) {
        EDITOR editor = getWorker().read(castInstance(), id);
        store(editor);
        return castInstance();
    }

    @Override
    public CONTEXT findAll() {
        clear();
        List<EDITOR> editors = getWorker().readAll(castInstance());
        store(editors);
        return castInstance();
    }

    @Override
    public BlueprintList<ENTITY, ENTITY_ID, BLUEPRINT, EDITOR, PARENT, WORKER, CONTEXT> generate(int count) {
        BlueprintList<ENTITY, ENTITY_ID, BLUEPRINT, EDITOR, PARENT, WORKER, CONTEXT> list =
                new BlueprintList<ENTITY, ENTITY_ID, BLUEPRINT, EDITOR, PARENT, WORKER, CONTEXT>(castInstance());

        for (int i = 0; i < count; i++) {
            list.add(generate());
        }

        return list;
    }

    @Override
    public EDITOR edit(ENTITY_ID id) {
        if(!contains(id)) {
            find(id);
        }
        return retrieve(id);
    }

    @Override
    public CONTEXT persist(BLUEPRINT blueprint) {
        return persist(Collections.singletonList(blueprint));
    }

    @Override
    public CONTEXT persist(Collection<? extends BLUEPRINT> blueprints) {
        List<EDITOR> editors = getWorker().create(castInstance(), blueprints);
        store(editors);
        return castInstance();
    }

    @Override
    public CONTEXT merge(ENTITY entity) {
        return merge(Collections.singletonList(entity));
    }

    @Override
    public CONTEXT merge(Collection<? extends ENTITY> entities) {
        getWorker().update(castInstance(), entities);
        return castInstance();
    }

    @Override
    public CONTEXT removeAll() {
        getWorker().delete(castInstance(), editors.values());
        editors.clear();
        return castInstance();
    }

    @Override
    public CONTEXT removeById(ENTITY_ID id) {
        getWorker().deleteById(castInstance(), id);
        return castInstance();
    }

    @Override
    public CONTEXT remove(ENTITY entity) {
        return remove(Collections.singletonList(entity));
    }

    @Override
    public CONTEXT remove(Collection<? extends ENTITY> entities) {
        getWorker().delete(castInstance(), entities);
        for (ENTITY entity : entities) {
            localRemove(getEntityID(entity));
        }
        return castInstance();
    }

    @Override
    public PARENT getParent() {
        return parent;
    }

    @Override
    public WORKER getWorker() {
        return worker;
    }

    @Override
    public Session getSession() {
        return session;
    }

    protected void store(List<EDITOR> editors) {
        for (EDITOR editor : editors) {
            store(editor);
        }
    }

    protected void store(Map<ENTITY_ID, EDITOR> editors) {
        this.editors.putAll(editors);
    }

    protected void store(EDITOR editor) {
        store(getEntityID(editor), editor);
    }

    protected void store(ENTITY_ID id, EDITOR editor) {
        editors.put(id, editor);
    }

    protected void localRemove(ENTITY_ID id) {
        editors.remove(id);
    }

    protected EDITOR retrieveOrThrow(ENTITY_ID id) {
        if (!contains(id)) {
            throw new IllegalStateException(MessageFormat.format("Entity with id {0} has to be loaded before " +
                    "detaching!", id));
        }
        return editors.get(id);
    }

    protected EDITOR retrieve(ENTITY_ID id) {
        if (!contains(id)) {
            find(id);
        }
        return editors.get(id);
    }

    protected Map<ENTITY_ID, EDITOR> getEditors() {
        return editors;
    }

    protected boolean contains(ENTITY_ID id) {
        return editors.containsKey(id);
    }

    protected void clear() {
        editors.clear();
    }

    protected String randomString() {
        return randomStringOfLength(DEFAULT_RANDOM_STRING_LENGTH);
    }

    protected String randomStringOfLength(int length) {
        return Helper.randomStringOfLength(length);
    }

    // FIXME think of a better name
    protected abstract CONTEXT castInstance();
}
