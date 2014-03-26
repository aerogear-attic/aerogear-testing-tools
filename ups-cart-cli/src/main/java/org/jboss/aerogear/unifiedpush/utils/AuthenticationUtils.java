/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.unifiedpush.utils;

import org.apache.http.HttpStatus;
import org.json.simple.JSONObject;

import com.jayway.restassured.response.Response;

public final class AuthenticationUtils {

    private static final String ADMIN_LOGIN_NAME = "admin";
    private static final String ADMIN_OLD_PASSWORD = "123";
    private static final String ADMIN_NEW_PASSWORD = "opensource2013";

    private AuthenticationUtils() {
    }

    public static Session login(String loginName, String password, String root) throws NullPointerException,
        UnexpectedResponseException {
        Validate.notNull(root);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("loginName", loginName);
        jsonObject.put("password", password);

        Response response = Session.newSession(root).given()
            .contentType(ContentTypes.json())
            .header(Headers.acceptJson())
            .body(jsonObject.toJSONString())
            .post("/rest/auth/login");

        // TODO should we throw or return invalid session?
        if (response.statusCode() == HttpStatus.SC_OK) {
            return new Session(root, loginName, password, response.cookies());
        } else if (response.statusCode() == HttpStatus.SC_FORBIDDEN) {
            throw new ExpiredPasswordException(response);
        } else if (response.statusCode() == HttpStatus.SC_UNAUTHORIZED) {
            throw new InvalidPasswordException(response);
        } else {
            // This should never happen
            throw new UnexpectedResponseException(response);
        }
    }

    public static boolean changePassword(String loginName, String oldPassword, String newPassword, String root) {
        Validate.notNull(root);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("loginName", loginName);
        jsonObject.put("password", oldPassword);
        jsonObject.put("newPassword", newPassword);

        // FIXME should not this be using already existing session?
        Response response = Session.newSession(root)
            .given()
            .contentType(ContentTypes.json())
            .header(Headers.acceptJson())
            .body(jsonObject.toJSONString())
            .put("/rest/auth/update");

        if (response.statusCode() == HttpStatus.SC_OK) {
            return true;
        } else if (response.statusCode() == HttpStatus.SC_UNAUTHORIZED) {
            throw new InvalidPasswordException(response);
        } else {
            throw new UnexpectedResponseException(response);
        }
    }

    public static void logout(Session session) {
        Validate.notNull(session);
        if (session.isValid() == false) {
            throw new IllegalStateException("Session has to be valid!");
        }

        Response response = session.given()
            .header(Headers.acceptJson())
            .post("/rest/auth/logout");

        if (response.statusCode() == HttpStatus.SC_OK) {
            session.invalidate();
        } else if (response.statusCode() == HttpStatus.SC_UNAUTHORIZED) {
            throw new IllegalStateException("Session was marked as valid, but the logout was unsuccessful!");
        } else {
            throw new UnexpectedResponseException(response);
        }

        session.invalidate();
    }

    public static Session completeLogin(String loginName, String oldPassword, String newPassword, String root) {
        try {
            return login(loginName, oldPassword, root);
        } catch (ExpiredPasswordException e) {
            changePassword(loginName, oldPassword, newPassword, root);

            return login(loginName, newPassword, root);
        } catch (InvalidPasswordException e) {
            return login(loginName, newPassword, root);
        }
    }

    public static Session completeDefaultLogin(String root) {
        return completeLogin(ADMIN_LOGIN_NAME, ADMIN_OLD_PASSWORD, ADMIN_NEW_PASSWORD, root);
    }

    public static class ExpiredPasswordException extends RuntimeException {

        private Response response;

        public ExpiredPasswordException(Response response) {
            this.response = response;
        }

        public Response getResponse() {
            return response;
        }
    }

    public static class InvalidPasswordException extends RuntimeException {

        private Response response;

        public InvalidPasswordException(Response response) {
            this.response = response;
        }

        public Response getResponse() {
            return response;
        }

    }
}
