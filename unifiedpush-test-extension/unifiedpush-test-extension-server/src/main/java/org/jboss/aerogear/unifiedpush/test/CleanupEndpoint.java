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

import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.dao.PageResult;
import org.jboss.aerogear.unifiedpush.dao.PushApplicationDao;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

@Stateless
@Path("/cleanup")
public class CleanupEndpoint {

    @Inject
    PushApplicationDao pushApplicationDao;

    @GET
    @Path("/applications")
    @Produces(MediaType.APPLICATION_JSON)
    public Response cleanupApplications() {

        long start = System.currentTimeMillis();
        /* Retyping the count to int so it can be used in the findAll. It's basically impossible to have more than
         * 2^31 push applications and if so, we should fix this endpoint to allow long count. */
        int count = (int) pushApplicationDao.getNumberOfPushApplicationsForDeveloper();

        PageResult<PushApplication> pushApplicationPageResult = pushApplicationDao.findAll(0, count);

        for (PushApplication pushApplication : pushApplicationPageResult.getResultList()) {
            pushApplicationDao.delete(pushApplication);
        }

        long stop = System.currentTimeMillis();

        Map<String, Object> response = new HashMap<String, Object>();
        response.put("cleaned", count);
        response.put("duration", stop - start);
        return Response.ok(response).build();
    }

}
