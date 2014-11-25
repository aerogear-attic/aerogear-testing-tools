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

import org.keycloak.models.ClaimMask;
import org.keycloak.models.jpa.entities.OAuthClientEntity;
import org.keycloak.models.jpa.entities.RealmEntity;
import org.keycloak.models.jpa.entities.RoleEntity;
import org.keycloak.models.jpa.entities.ScopeMappingEntity;
import org.keycloak.models.jpa.entities.UserEntity;
import org.keycloak.models.jpa.entities.UserRequiredActionEntity;
import org.keycloak.models.utils.KeycloakModelUtils;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class which handles configuration of the keycloak so its testable.
 */
@Stateless
public class KeycloakConfigurator {
    private static final Logger LOGGER = Logger.getLogger(KeycloakConfigurator.class.getName());

    @PersistenceContext(unitName = "keycloak-default")
    EntityManager entityManager;

    /**
     * Changes keycloak configuration to allow rest login and removes all required actions for all users in the
     * `aerogear` realm/
     */
    public KeycloakConfigurationResult configureForIntegrationTests() {
        KeycloakConfigurationResult result = new KeycloakConfigurationResult();

        TypedQuery<RealmEntity> realmQuery = entityManager.createNamedQuery("getRealmByName", RealmEntity.class);
        realmQuery.setParameter("name", "aerogear");

        for (RealmEntity realm : realmQuery.getResultList()) {
            LOGGER.warning("Editing realm: " + realm.getName());
            result.foundRealms.add(realm.getName() + ":" + realm.getId());
            TypedQuery<UserEntity> userQuery = entityManager.createNamedQuery("getAllUsersByRealm", UserEntity.class);
            userQuery.setParameter("realmId", realm.getId());

            // Enable Direct Grant API
            realm.setPasswordCredentialGrantAllowed(true);

            // Make sure the session won't expire even when the testing runs very slow
            realm.setAccessTokenLifespan(3600);
            realm.setAccessCodeLifespan(3600);
            realm.setAccessCodeLifespanUserAction(3600);
            realm.setSsoSessionIdleTimeout(3600);

            // Any required action would prevent us to login
            for (UserEntity user : userQuery.getResultList()) {
                LOGGER.log(Level.INFO, "Editing user: {0}", user.getUsername());
                result.foundUsers.add(user.getUsername());
                for (UserRequiredActionEntity userRequiredAction : user.getRequiredActions()) {
                    LOGGER.log(Level.INFO, "Removing required action: {0}", userRequiredAction.getAction().name());
                    String current = result.removedRequiredActions.get(user.getUsername());
                    result.removedRequiredActions.put(user.getUsername(), current + userRequiredAction.getAction().name() + ", ");
                    entityManager.remove(userRequiredAction);
                }
                user.getRequiredActions().clear();
            }

            TypedQuery<OAuthClientEntity> existingEntityQuery =
                    entityManager.createNamedQuery("findOAuthClientByName", OAuthClientEntity.class);
            existingEntityQuery.setParameter("name", "integration-tests");
            existingEntityQuery.setParameter("realm", realm);

            // TODO should we instead remove all the oauthClients and create a new one?
            if(existingEntityQuery.getResultList().isEmpty()) {
                OAuthClientEntity oAuthClient = new OAuthClientEntity();
                oAuthClient.setId(KeycloakModelUtils.generateId());
                oAuthClient.setName("integration-tests");
                oAuthClient.setEnabled(true);
                oAuthClient.setPublicClient(true);
                oAuthClient.setDirectGrantsOnly(true);
                oAuthClient.setAllowedClaimsMask(ClaimMask.USERNAME);
                oAuthClient.setRealm(realm);
                entityManager.persist(oAuthClient);

                for (RoleEntity roleEntity : realm.getRoles()) {
                    result.roles.add(roleEntity.getName());
                    ScopeMappingEntity scopemapping = new ScopeMappingEntity();
                    scopemapping.setClient(oAuthClient);
                    scopemapping.setRole(roleEntity);
                    entityManager.persist(scopemapping);
                }
            }
        }

        return result;
    }

    public static class KeycloakConfigurationResult {
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
}
