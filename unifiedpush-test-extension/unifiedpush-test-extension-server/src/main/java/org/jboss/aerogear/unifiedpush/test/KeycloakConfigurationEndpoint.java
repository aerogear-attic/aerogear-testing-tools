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

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Stateless
@Path("/keycloak")
public class KeycloakConfigurationEndpoint {

    @Inject
    KeycloakConfigurator configurator;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response configureKeycloak() {
        KeycloakConfigurationResult result = configurator.configureForIntegrationTests();
        return Response.ok(result).build();
    }

    @GET
    @Path("/realms")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRealms() {
        List<String> result = configurator.getRealms();
        return Response.ok(result).build();
    }

}
