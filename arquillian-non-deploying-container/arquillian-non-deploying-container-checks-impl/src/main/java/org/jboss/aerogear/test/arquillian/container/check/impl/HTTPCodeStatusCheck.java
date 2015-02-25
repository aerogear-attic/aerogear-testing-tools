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
package org.jboss.aerogear.test.arquillian.container.check.impl;

import java.net.URI;
import java.util.logging.Logger;

import org.apache.http.HttpStatus;
import org.jboss.aerogear.test.arquillian.container.check.StatusCheck;
import org.jboss.aerogear.test.arquillian.container.check.StatusCheckException;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;

/**
 * Checks target URI for HTTP status code of 200 when doing HTTP GET request.
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class HTTPCodeStatusCheck implements StatusCheck {

    private static final Logger logger = Logger.getLogger(HTTPCodeStatusCheck.class.getName());

    private URI uri;

    @Override
    public void target(URI uri) {
        if (uri == null) {
            throw new IllegalArgumentException("target URI to check status of can not be a null object");
        }
        this.uri = uri;
    }

    @Override
    public boolean execute() {

        if (uri == null) {
            throw new StatusCheckException("URI of target to check was not set.");
        }

        Response response = RestAssured.get(uri);

        int statusCode = response.getStatusCode();

        logger.fine(String.format("Status code: %s", statusCode));

        return statusCode == HttpStatus.SC_OK;
    }

}
