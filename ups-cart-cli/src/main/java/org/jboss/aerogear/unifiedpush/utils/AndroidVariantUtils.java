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

import org.apache.http.HttpStatus;
import org.jboss.aerogear.unifiedpush.model.AndroidVariant;
import org.jboss.aerogear.unifiedpush.model.PushApplication;
import org.json.simple.JSONObject;

import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;

public final class AndroidVariantUtils {

    private static final int SINGLE = 1;

    private AndroidVariantUtils() {
    }

    public static AndroidVariant create(String name, String description, String googleKey, String projectNumber) {
        AndroidVariant androidVariant = new AndroidVariant();

        androidVariant.setName(name);
        androidVariant.setDescription(description);
        androidVariant.setGoogleKey(googleKey);
        androidVariant.setProjectNumber(projectNumber);

        return androidVariant;
    }

    public static AndroidVariant createAndRegister(String name, String description, String googleKey, String projectNumber,
        PushApplication pushApplication, Session session) {
        AndroidVariant androidVariant = create(name, description, googleKey, projectNumber);

        register(androidVariant, pushApplication, session);

        return androidVariant;
    }

    public static AndroidVariant generate() {
        return generate(SINGLE).iterator().next();
    }

    public static List<AndroidVariant> generate(int count) {
        List<AndroidVariant> androidVariants = new ArrayList<AndroidVariant>();

        for (int i = 0; i < count; i++) {
            String name = UUID.randomUUID().toString();
            String description = UUID.randomUUID().toString();
            String googleKey = UUID.randomUUID().toString();
            String projectNumber = UUID.randomUUID().toString();

            AndroidVariant androidVariant = create(name, description, googleKey, projectNumber);

            androidVariants.add(androidVariant);
        }

        return androidVariants;
    }

    public static AndroidVariant generateAndRegister(PushApplication pushApplication,
        Session session) {
        return generateAndRegister(SINGLE, pushApplication, session).iterator().next();
    }

    public static List<AndroidVariant> generateAndRegister(int count, PushApplication pushApplication,
        Session session) {
        List<AndroidVariant> androidVariants = generate(count);

        for (AndroidVariant androidVariant : androidVariants) {
            register(androidVariant, pushApplication, session);
        }

        return androidVariants;
    }

    public static void register(AndroidVariant androidVariant, PushApplication pushApplication,
        Session session) {
        register(androidVariant, pushApplication, session, ContentTypes.json());
    }

    public static void register(AndroidVariant androidVariant, PushApplication pushApplication,
        Session session, String contentType) throws NullPointerException, UnexpectedResponseException {

        Validate.notNull(session);

        Response response = session.given()
            .contentType(contentType)
            .header(Headers.acceptJson())
            .body(toJSONString(androidVariant))
            .post("/rest/applications/{pushApplicationID}/android", pushApplication.getPushApplicationID());

        UnexpectedResponseException.verifyResponse(response, HttpStatus.SC_CREATED);

        setFromJsonPath(response.jsonPath(), androidVariant);
    }

    public static List<AndroidVariant> listAll(PushApplication pushApplication, Session session) {
        Validate.notNull(session);

        Response response = session.given()
            .contentType(ContentTypes.json())
            .header(Headers.acceptJson())
            .get("/rest/applications/{pushApplicationID}/android", pushApplication.getPushApplicationID());

        UnexpectedResponseException.verifyResponse(response, HttpStatus.SC_OK);

        List<AndroidVariant> androidVariants = new ArrayList<AndroidVariant>();

        JsonPath jsonPath = response.jsonPath();

        List<Map<String, ?>> items = jsonPath.getList("");

        for (int i = 0; i < items.size(); i++) {
            jsonPath.setRoot("[" + i + "]");

            AndroidVariant androidVariant = fromJsonPath(jsonPath);

            androidVariants.add(androidVariant);
        }

        return androidVariants;
    }

    public static AndroidVariant findById(String variantID, PushApplication pushApplication,
        Session session) {
        Validate.notNull(session);

        Response response = session.given()
            .contentType(ContentTypes.json())
            .header(Headers.acceptJson())
            .get("/rest/applications/{pushApplicationID}/android/{variantID}",
                pushApplication.getPushApplicationID(), variantID);

        UnexpectedResponseException.verifyResponse(response, HttpStatus.SC_OK);

        return fromJsonPath(response.jsonPath());
    }

    public static void update(AndroidVariant androidVariant, PushApplication pushApplication,
        Session session) {
        update(androidVariant, pushApplication, session, ContentTypes.json());
    }

    public static void update(AndroidVariant androidVariant, PushApplication pushApplication,
        Session session, String contentType) {
        Validate.notNull(session);

        Response response = session.given()
            .contentType(contentType)
            .header(Headers.acceptJson())
            .body(toJSONString(androidVariant))
            .put("/rest/applications/{pushApplicationID}/android/{variantID}",
                pushApplication.getPushApplicationID(), androidVariant.getVariantID());

        UnexpectedResponseException.verifyResponse(response, HttpStatus.SC_NO_CONTENT);
    }

    public static void delete(AndroidVariant androidVariant, PushApplication pushApplication,
        Session session) {
        Validate.notNull(session);

        Response response = session.given()
            .contentType(ContentTypes.json())
            .header(Headers.acceptJson())
            .delete("/rest/applications/{pushApplicationID}/android/{variantID}",
                pushApplication.getPushApplicationID(), androidVariant.getVariantID());

        UnexpectedResponseException.verifyResponse(response, HttpStatus.SC_NO_CONTENT);
    }

    public static JSONObject toJSONObject(AndroidVariant androidVariant) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", androidVariant.getName());
        jsonObject.put("description", androidVariant.getDescription());
        jsonObject.put("googleKey", androidVariant.getGoogleKey());
        jsonObject.put("projectNumber", androidVariant.getProjectNumber());
        return jsonObject;
    }

    public static String toJSONString(AndroidVariant androidVariant) {
        return toJSONObject(androidVariant).toJSONString();
    }

    public static AndroidVariant fromJsonPath(JsonPath jsonPath) {
        AndroidVariant androidVariant = new AndroidVariant();

        setFromJsonPath(jsonPath, androidVariant);

        return androidVariant;
    }

    public static void setFromJsonPath(JsonPath jsonPath, AndroidVariant androidVariant) {
        androidVariant.setGoogleKey(jsonPath.getString("googleKey"));
        androidVariant.setId(jsonPath.getString("id"));
        androidVariant.setVariantID(jsonPath.getString("variantID"));
        androidVariant.setDeveloper(jsonPath.getString("developer"));
        androidVariant.setDescription(jsonPath.getString("description"));
        androidVariant.setName(jsonPath.getString("name"));
        androidVariant.setSecret(jsonPath.getString("secret"));
        androidVariant.setProjectNumber(jsonPath.getString("projectNumber"));
    }

    /*
     * // TODO there should be "equals" method in the model!
     * public static void checkEquality(AndroidVariant expected, AndroidVariant actual) {
     * assertEquals("Name is not equal!", expected.getName(), actual.getName());
     * assertEquals("Description is not equal!", expected.getDescription(), actual.getDescription());
     * assertEquals("VariantId is not equal!", expected.getVariantID(), actual.getVariantID());
     * assertEquals("Secret is not equal!", expected.getSecret(), actual.getSecret());
     * assertEquals("Developer is not equal!", expected.getDeveloper(), actual.getDeveloper());
     *
     * // TODO we can't do this check as none of variants has the equals method implemented
     * // assertEquals(expected.getIOSVariants(), actual.getIOSVariants());
     * // assertEquals(expected.getAndroidVariants(), actual.getAndroidVariants());
     * // assertEquals(expected.getSimplePushVariants(), actual.getSimplePushVariants());
     * }
     */
}
