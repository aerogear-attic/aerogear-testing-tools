/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc. and/or its affiliates, and individual
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
package org.jboss.aerogear.unifiedpush.test;

import org.jboss.aerogear.unifiedpush.test.sender.ProxySetup;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Starts and stops proxy on demand.
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
@Stateless
@Path("/proxy")
public class ProxyEndpoint {

    @Inject
    private ProxySetup proxySetup;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/activate")
    public Response activateProxy() {

        if (!proxySetup.isActive()) {
            proxySetup.startProxyServer();
        }

        return Response.ok().build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/deactivate")
    public Response deactivateProxy() {

        if (proxySetup.isActive()) {
            proxySetup.stopProxyServer();
        }

        return Response.ok().build();
    }
}
