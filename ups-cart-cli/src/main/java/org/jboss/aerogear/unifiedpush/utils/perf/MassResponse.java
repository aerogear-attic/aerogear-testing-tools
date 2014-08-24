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
package org.jboss.aerogear.unifiedpush.utils.perf;

import java.io.Serializable;
import java.util.List;

/**
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class MassResponse implements Serializable {

    private static final long serialVersionUID = 4090661692812339499L;

    private List<String> appsIds;

    private List<String> varsIds;

    private List<String> installationIds;

    public List<String> getAppsIds() {
        return appsIds;
    }

    public void setAppsIds(List<String> appsIds) {
        this.appsIds = appsIds;
    }

    public List<String> getVarsIds() {
        return varsIds;
    }

    public void setVarsIds(List<String> varsIds) {
        this.varsIds = varsIds;
    }

    public List<String> getInstallationIds() {
        return installationIds;
    }

    public void setInstallationIds(List<String> installationIds) {
        this.installationIds = installationIds;
    }

}
