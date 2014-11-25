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
package org.jboss.aerogear.unifiedpush.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KeycloakConfigurationResult {
    private List<String> foundRealms = new ArrayList<String>();
    private List<String> foundUsers = new ArrayList<String>();
    private Map<String, String> removedRequiredActions = new HashMap<String, String>();
    private List<String> roles = new ArrayList<String>();

    public List<String> getFoundRealms() {
        return foundRealms;
    }

    public void setFoundRealms(List<String> foundRealms) {
        this.foundRealms = foundRealms;
    }

    public List<String> getFoundUsers() {
        return foundUsers;
    }

    public void setFoundUsers(List<String> foundUsers) {
        this.foundUsers = foundUsers;
    }

    public Map<String, String> getRemovedRequiredActions() {
        return removedRequiredActions;
    }

    public void setRemovedRequiredActions(Map<String, String> removedRequiredActions) {
        this.removedRequiredActions = removedRequiredActions;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}
