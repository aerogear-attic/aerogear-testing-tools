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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.jboss.aerogear.unifiedpush.model.PushApplication;
import org.json.simple.JSONObject;

import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;

public final class PushApplicationUtils {

    private static final int SINGLE = 1;

    private PushApplicationUtils() {
    }

    public static PushApplication create(String name, String description) {
        PushApplication pushApplication = new PushApplication();

        pushApplication.setName(name);
        pushApplication.setDescription(description);

        return pushApplication;
    }

    public static PushApplication createAndRegister(String name, String description,
        Session session) {
        PushApplication pushApplication = create(name, description);

        register(pushApplication, session);

        return pushApplication;
    }

    public static PushApplication generate() {
        return generate(SINGLE).iterator().next();
    }

    public static List<PushApplication> generate(int count) {
        List<PushApplication> pushApplications = new ArrayList<PushApplication>();

        for (int i = 0; i < count; i++) {
            String name = UUID.randomUUID().toString();
            String description = UUID.randomUUID().toString();

            PushApplication pushApplication = create(name, description);

            pushApplications.add(pushApplication);
        }

        return pushApplications;
    }

    public static PushApplication generateAndRegister(Session session) throws NullPointerException,
        UnexpectedResponseException {
        return generateAndRegister(SINGLE, session).iterator().next();
    }

    public static List<PushApplication> generateAndRegister(int count, Session session)
        throws NullPointerException, UnexpectedResponseException {
        List<PushApplication> pushApplications = generate(count);

        for (PushApplication pushApplication : pushApplications) {
            register(pushApplication, session);
        }

        return pushApplications;
    }

    public static Response register(PushApplication pushApplication, Session session)
        throws NullPointerException, UnexpectedResponseException {
        return register(pushApplication, session, ContentTypes.json());
    }

    public static Response register(PushApplication pushApplication, Session session,
        String contentType) throws NullPointerException, UnexpectedResponseException {
        Validate.notNull(session);

        Response response = session.given()
            .contentType(contentType)
            .header(Headers.acceptJson())
            .body(toJSONString(pushApplication))
            .post("/rest/applications");

        UnexpectedResponseException.verifyResponse(response, HttpStatus.SC_CREATED);

        setFromJsonPath(response.jsonPath(), pushApplication);

        return response;
    }

    public static List<PushApplication> listAll(Session session) {
        Validate.notNull(session);

        Response response = session.given()
            .contentType(ContentTypes.json())
            .header(Headers.acceptJson())
            .get("/rest/applications");

        UnexpectedResponseException.verifyResponse(response, HttpStatus.SC_OK);

        List<PushApplication> pushApplications = new ArrayList<PushApplication>();

        JsonPath jsonPath = response.jsonPath();

        List<Map<String, ?>> items = jsonPath.getList("");

        for (int i = 0; i < items.size(); i++) {
            jsonPath.setRoot("[" + i + "]");

            PushApplication pushApplication = fromJsonPath(jsonPath);

            pushApplications.add(pushApplication);
        }

        return pushApplications;
    }

    public static PushApplication findById(String pushApplicationId, Session session) {
        Validate.notNull(session);

        Response response = session.given()
            .contentType(ContentTypes.json())
            .header(Headers.acceptJson())
            .get("/rest/applications/{pushApplicationId}", pushApplicationId);

        UnexpectedResponseException.verifyResponse(response, HttpStatus.SC_OK);

        return fromJsonPath(response.jsonPath());
    }

    public static Response update(PushApplication pushApplication, Session session) {
        return update(pushApplication, session, ContentTypes.json());
    }

    public static Response update(PushApplication pushApplication, Session session,
        String contentType) {
        Validate.notNull(session);

        Response response = session.given()
            .contentType(contentType)
            .header(Headers.acceptJson())
            .body(toJSONString(pushApplication))
            .put("/rest/applications/{pushApplicationID}",
                pushApplication.getPushApplicationID());

        UnexpectedResponseException.verifyResponse(response, HttpStatus.SC_NO_CONTENT);

        return response;
    }

    public static Response delete(PushApplication pushApplication, Session session) {
        Validate.notNull(session);

        Response response = session.given()
            .header(Headers.acceptJson())
            .delete("/rest/applications/{pushApplicationId}",
                pushApplication.getPushApplicationID());

        UnexpectedResponseException.verifyResponse(response, HttpStatus.SC_NO_CONTENT);

        return response;
    }

    public static JSONObject toJSONObject(PushApplication pushApplication) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", pushApplication.getName());
        jsonObject.put("description", pushApplication.getDescription());
        return jsonObject;
    }

    public static String toJSONString(PushApplication pushApplication) {
        return toJSONObject(pushApplication).toJSONString();
    }

    public static PushApplication fromJsonPath(JsonPath jsonPath) {
        PushApplication pushApplication = new PushApplication();

        setFromJsonPath(jsonPath, pushApplication);

        return pushApplication;
    }

    public static void setFromJsonPath(JsonPath jsonPath, PushApplication pushApplication) {
        pushApplication.setName(jsonPath.getString("name"));
        pushApplication.setDescription(jsonPath.getString("description"));
        pushApplication.setPushApplicationID(jsonPath.getString("pushApplicationID"));
        pushApplication.setMasterSecret(jsonPath.getString("masterSecret"));
        pushApplication.setDeveloper(jsonPath.getString("developer"));
    }

    /*
     * // TODO there should be "equals" method in the model!
     * public static void checkEquality(PushApplication expected, PushApplication actual) {
     * assertEquals("Name is not equal!", expected.getName(), actual.getName());
     * assertEquals("Description is not equal!", expected.getDescription(), actual.getDescription());
     * assertEquals("PushApplicationId is not equal!", expected.getPushApplicationID(), actual.getPushApplicationID());
     * assertEquals("MasterSecret is not equal!", expected.getMasterSecret(), actual.getMasterSecret());
     * assertEquals("Developer is not equal!", expected.getDeveloper(), actual.getDeveloper());
     *
     * // TODO we can't do this check as none of variants has the equals method implemented
     * // assertEquals(expected.getIOSVariants(), actual.getIOSVariants());
     * // assertEquals(expected.getAndroidVariants(), actual.getAndroidVariants());
     * // assertEquals(expected.getSimplePushVariants(), actual.getSimplePushVariants());
     * }
     */

    public static boolean appIdExistsInList(String pushAppId, List<PushApplication> pushAppsList) {
        if (!StringUtils.isEmpty(pushAppId) && pushAppsList != null) {
            for (PushApplication pushApp : pushAppsList) {
                if (pushApp != null && pushAppId.equals(pushApp.getPushApplicationID())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean nameExistsInList(String pushAppName, List<PushApplication> pushAppsList) {
        if (!StringUtils.isEmpty(pushAppName) && pushAppsList != null) {
            for (PushApplication pushApp : pushAppsList) {
                if (pushApp != null && pushAppName.equals(pushApp.getName())) {
                    return true;
                }
            }
        }
        return false;
    }

}
