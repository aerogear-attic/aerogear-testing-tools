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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.jboss.aerogear.test.ContentTypes;
import org.jboss.aerogear.test.Headers;
import org.jboss.aerogear.test.Session;
import org.jboss.aerogear.test.UnexpectedResponseException;
import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.utils.installation.InstallationUtils;
import org.jboss.aerogear.unifiedpush.utils.perf.model.MassInstallation;
import org.jboss.aerogear.unifiedpush.utils.perf.model.MassPushApplication;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.config.ObjectMapperConfig;
import com.jayway.restassured.config.RestAssuredConfig;
import com.jayway.restassured.internal.mapper.ObjectMapperType;
import com.jayway.restassured.response.Response;

/**
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class BatchUtils extends InstallationUtils {

    private static final int INSTALLATION_PAGING = 1000;

    private static final int APPLICATION_PAGING = 1000;

    static {
        RestAssured.config = RestAssuredConfig.config().objectMapperConfig(new ObjectMapperConfig(ObjectMapperType.JACKSON_1));
    }

    private static void send(Session session, MassInstallation mass) {
        Response response = session.givenAuthorized()
            .contentType(ContentTypes.json())
            .header(Headers.acceptJson())
            .body(mass)
            .post("/rest/mass/installations");

        UnexpectedResponseException.verifyResponse(response, HttpStatus.SC_OK);
    }

    public static void registerInstallationsViaBatchEndpoint(MassInstallation massInstallation, Session session) {

        int installations = 0;

        MassInstallation mass = new MassInstallation();

        for (Map.Entry<String, List<Installation>> entry : massInstallation.map.entrySet()) {

            installations += entry.getValue().size();

            if (entry.getValue().size() > 0) {
                mass.map.put(entry.getKey(), entry.getValue());
            }

            if (installations > INSTALLATION_PAGING) {
                send(session, mass);
                installations = 0;
                mass.map.clear();
            }

        }

        if (installations != 0) {
            send(session, mass);
        }
    }

    public static void registerApplicationsViaBatchEndpoint(MassPushApplication massive, Session session) {

        List<PushApplication> pushApplications = new ArrayList<PushApplication>(massive.getApplications());

        while (!pushApplications.isEmpty()) {
            int toIndex = APPLICATION_PAGING;

            if (pushApplications.size() < APPLICATION_PAGING) {
                toIndex = pushApplications.size();
            }

            List<PushApplication> sublist = pushApplications.subList(0, toIndex);

            MassPushApplication mass = new MassPushApplication();
            mass.setApplications(sublist);

            Response response = session.givenAuthorized()
                .contentType(ContentTypes.json())
                .header(Headers.acceptJson())
                .body(mass)
                .post("/rest/mass/applications/generated");

            UnexpectedResponseException.verifyResponse(response, HttpStatus.SC_OK);

            pushApplications.removeAll(sublist);
        }

    }

    public static MassPushApplication registerApplicationsViaBatchEndpoint(List<PushApplication> applications, Session session) {

        MassPushApplication massive = new MassPushApplication();
        massive.setApplications(applications);

        registerApplicationsViaBatchEndpoint(massive, session);

        return massive;
    }

}
