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

import org.jboss.aerogear.test.Session;
import org.jboss.aerogear.test.api.AbstractUPSContext;
import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.api.Variant;

import java.util.Collection;
import java.util.Collections;

public abstract class InstallationContext<
        BLUEPRINT extends InstallationBlueprint<BLUEPRINT, EDITOR, PARENT, WORKER, CONTEXT>,
        EDITOR extends InstallationEditor<BLUEPRINT, EDITOR, PARENT, WORKER, CONTEXT>,
        PARENT extends Variant,
        WORKER extends InstallationWorker<BLUEPRINT, EDITOR, PARENT, CONTEXT, WORKER>,
        CONTEXT extends InstallationContext<BLUEPRINT, EDITOR, PARENT, WORKER, CONTEXT>>


        extends AbstractUPSContext<Installation, String, BLUEPRINT, EDITOR, PARENT, WORKER, CONTEXT> {

    public InstallationContext(WORKER worker, PARENT parent, Session session) {
        super(worker, parent, session);
    }

    @Override
    public String getEntityID(Installation installation) {
        return installation.getId();
    }

    public CONTEXT unregisterAll() {
        return unregister(getEditors().values());
    }

    public CONTEXT unregisterById(String id) {
        return unregister(retrieve(id));
    }

    public CONTEXT unregister(Installation installation) {
        return unregister(Collections.singleton(installation));
    }

    public CONTEXT unregister(Collection<? extends Installation> installations) {
        getWorker().unregister(castInstance(), installations);
        for (Installation installation : installations) {
            localRemove(getEntityID(installation));
        }
        return castInstance();
    }

    protected abstract EDITOR createEditor();
}
