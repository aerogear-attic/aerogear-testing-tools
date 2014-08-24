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

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.HttpStatus;
import org.jboss.aerogear.test.ContentTypes;
import org.jboss.aerogear.test.Headers;
import org.jboss.aerogear.test.Session;
import org.jboss.aerogear.test.UnexpectedResponseException;
import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.utils.InstallationUtils;

import com.jayway.restassured.response.Response;

/**
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class BatchInstallationUtils extends InstallationUtils {

    private static final Logger logger = Logger.getLogger(BatchInstallationUtils.class.getName());

    private static final int OFFSET = 20000;

    public static void registerViaBatchEndpoint(Variant variant, List<Installation> installations, Session session) {

        MassInstallation massiveInstallation = new MassInstallation();
        massiveInstallation.setVariantId(variant.getVariantID());

        int count = installations.size();

        for (int i = 0; i <= count - OFFSET; i = i + OFFSET) {

            logger.log(Level.INFO, "Registering installations {0} - {1}.", new Object[] { i, i + OFFSET });

            List<Installation> sublist = installations.subList(i, i + OFFSET);

            massiveInstallation.setInstallations(sublist);

            Response response = session.givenAuthorized()
                .contentType(ContentTypes.json())
                .header(Headers.acceptJson())
                .body(massiveInstallation)
                .post("/rest/mass/installations");

            UnexpectedResponseException.verifyResponse(response, HttpStatus.SC_OK);
        }

    }
}
