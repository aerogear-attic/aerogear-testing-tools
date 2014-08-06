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
package org.jboss.aerogear.test.api.auth;

import java.io.IOException;
import java.net.URI;

import org.apache.http.HttpStatus;
import org.jboss.aerogear.test.Headers;
import org.jboss.aerogear.test.Session;
import org.jboss.aerogear.test.UnexpectedResponseException;
import org.keycloak.OAuth2Constants;
import org.keycloak.ServiceUrlConstants;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.util.JsonSerialization;
import org.keycloak.util.KeycloakUriBuilder;

import com.jayway.restassured.response.Response;

public class LoginRequest extends AbstractAuthRequest<LoginRequest> {

    private String username;
    private String password;

    public LoginRequest username(String username) {
        this.username = username;
        return this;
    }

    public LoginRequest password(String password) {
        this.password = password;
        return this;
    }

    public Session login() {

        URI authServerEndpointUri = KeycloakUriBuilder.fromUri(getAuthServerUrl().toExternalForm())
                .path(ServiceUrlConstants.TOKEN_SERVICE_DIRECT_GRANT_PATH).build("aerogear");

        Session session = Session.newSession(authServerEndpointUri.toString());
        // FIXME dont use Session here! Fire up our own RestAssured
        Response response = session.given()
                .header(Headers.acceptJson())
                .formParam("username", username)
                .formParam("password", password)
                .formParam(OAuth2Constants.CLIENT_ID, "integration-tests")
                .post();

        if(response.statusCode() == HttpStatus.SC_OK) {
            try {
                AccessTokenResponse tokenResponse =
                        JsonSerialization.readValue(response.asString(), AccessTokenResponse.class);

                return new Session(getUnifiedPushServerUrl(), tokenResponse);
                // FIXME handle the possible io exception!
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }

        } else {
            throw new UnexpectedResponseException(response, HttpStatus.SC_OK);
        }
    }

    public static LoginRequest request() {
        return new LoginRequest();
    }

}
