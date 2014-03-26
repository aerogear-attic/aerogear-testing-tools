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
import org.jboss.aerogear.unifiedpush.model.PushApplication;
import org.jboss.aerogear.unifiedpush.model.SimplePushVariant;
import org.json.simple.JSONObject;

import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;

public final class SimplePushVariantUtils {

    private static final int SINGLE = 1;

    private SimplePushVariantUtils() {
    }

    public static SimplePushVariant create(String name, String description) {
        SimplePushVariant simplePushVariant = new SimplePushVariant();

        simplePushVariant.setName(name);
        simplePushVariant.setDescription(description);

        return simplePushVariant;
    }

    public static SimplePushVariant createAndRegister(String name, String description, PushApplication pushApplication,
        Session session) {
        SimplePushVariant simplePushVariant = create(name, description);

        register(simplePushVariant, pushApplication, session);

        return simplePushVariant;
    }

    public static SimplePushVariant generate() {
        return generate(SINGLE).iterator().next();
    }

    public static List<SimplePushVariant> generate(int count) {
        List<SimplePushVariant> simplePushVariants = new ArrayList<SimplePushVariant>();

        for (int i = 0; i < count; i++) {
            String name = UUID.randomUUID().toString();
            String description = UUID.randomUUID().toString();

            SimplePushVariant simplePushVariant = create(name, description);

            simplePushVariants.add(simplePushVariant);
        }

        return simplePushVariants;
    }

    public static SimplePushVariant generateAndRegister(PushApplication pushApplication,
        Session session) {
        return generateAndRegister(SINGLE, pushApplication, session).iterator().next();
    }

    public static List<SimplePushVariant> generateAndRegister(int count, PushApplication pushApplication,
        Session session) {
        List<SimplePushVariant> simplePushVariants = generate(count);

        for (SimplePushVariant simplePushVariant : simplePushVariants) {
            register(simplePushVariant, pushApplication, session);
        }

        return simplePushVariants;
    }

    public static void register(SimplePushVariant simplePushVariant, PushApplication pushApplication,
        Session session) {
        register(simplePushVariant, pushApplication, session, ContentTypes.json());
    }

    public static void register(SimplePushVariant simplePushVariant, PushApplication pushApplication,
        Session session, String contentType) {

        Response response = session.given()
            .contentType(contentType)
            .header(Headers.acceptJson())
            .body(toJSONString(simplePushVariant))
            .post("/rest/applications/{pushApplicationID}/simplePush",
                pushApplication.getPushApplicationID());

        UnexpectedResponseException.verifyResponse(response, HttpStatus.SC_CREATED);

        setFromJsonPath(response.jsonPath(), simplePushVariant);
    }

    public static List<SimplePushVariant> listAll(PushApplication pushApplication,
        Session session) {
        Response response = session.given()
            .contentType(ContentTypes.json())
            .header(Headers.acceptJson())
            .get("/rest/applications/{pushApplicationID}/simplePush",
                pushApplication.getPushApplicationID());

        UnexpectedResponseException.verifyResponse(response, HttpStatus.SC_OK);

        List<SimplePushVariant> simplePushVariants = new ArrayList<SimplePushVariant>();

        JsonPath jsonPath = response.jsonPath();

        List<Map<String, ?>> items = jsonPath.getList("");

        for (int i = 0; i < items.size(); i++) {
            jsonPath.setRoot("[" + i + "]");

            SimplePushVariant simplePushVariant = fromJsonPath(jsonPath);

            simplePushVariants.add(simplePushVariant);
        }

        return simplePushVariants;
    }

    public static SimplePushVariant findById(String variantID, PushApplication pushApplication,
        Session session) {
        Response response = session.given()
            .contentType(ContentTypes.json())
            .header(Headers.acceptJson())
            .get("/rest/applications/{pushApplicationID}/android/{variantID}",
                pushApplication.getPushApplicationID(), variantID);

        UnexpectedResponseException.verifyResponse(response, HttpStatus.SC_OK);

        return fromJsonPath(response.jsonPath());
    }

    public static void update(SimplePushVariant simplePushVariant, PushApplication pushApplication,
        Session session) {
        update(simplePushVariant, pushApplication, session, ContentTypes.json());
    }

    public static void update(SimplePushVariant simplePushVariant, PushApplication pushApplication,
        Session session, String contentType) {
        Validate.notNull(session);

        Response response = session.given()
            .contentType(contentType)
            .header(Headers.acceptJson())
            .body(toJSONString(simplePushVariant))
            .put("/rest/applications/{pushApplicationID}/simplePush/{variantID}",
                pushApplication.getPushApplicationID(), simplePushVariant.getVariantID());

        UnexpectedResponseException.verifyResponse(response, HttpStatus.SC_NO_CONTENT);
    }

    public static void delete(SimplePushVariant simplePushVariant, PushApplication pushApplication,
        Session session) {
        Validate.notNull(session);

        Response response = session.given()
            .contentType(ContentTypes.json())
            .header(Headers.acceptJson())
            .delete("/rest/applications/{pushApplicationID}/simplePush/{variantID}",
                pushApplication.getPushApplicationID(), simplePushVariant.getVariantID());

        UnexpectedResponseException.verifyResponse(response, HttpStatus.SC_NO_CONTENT);
    }

    public static JSONObject toJSONObject(SimplePushVariant simplePushVariant) {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("name", simplePushVariant.getName());
        jsonObject.put("description", simplePushVariant.getDescription());

        return jsonObject;
    }

    public static String toJSONString(SimplePushVariant simplePushVariant) {
        return toJSONObject(simplePushVariant).toJSONString();
    }

    public static SimplePushVariant fromJsonPath(JsonPath jsonPath) {
        SimplePushVariant simplePushVariant = new SimplePushVariant();

        setFromJsonPath(jsonPath, simplePushVariant);

        return simplePushVariant;
    }

    public static void setFromJsonPath(JsonPath jsonPath, SimplePushVariant simplePushVariant) {
        simplePushVariant.setId(jsonPath.getString("id"));
        simplePushVariant.setVariantID(jsonPath.getString("variantID"));
        simplePushVariant.setDeveloper(jsonPath.getString("developer"));
        simplePushVariant.setDescription(jsonPath.getString("description"));
        simplePushVariant.setName(jsonPath.getString("name"));
        simplePushVariant.setSecret(jsonPath.getString("secret"));
    }

    public static SimplePushVariant createSimplePushVariant(String name, String description, String variantID,
        String secret,
        String developer) {
        SimplePushVariant variant = new SimplePushVariant();
        variant.setName(name);
        variant.setDescription(description);
        variant.setVariantID(variantID);
        variant.setSecret(secret);
        variant.setDeveloper(developer);
        return variant;
    }

    /*
     * public static void checkEquality(SimplePushVariant expected, SimplePushVariant actual) {
     * assertEquals(expected.getName(), actual.getName());
     * assertEquals(expected.getDescription(), actual.getDescription());
     * assertEquals(expected.getVariantID(), actual.getVariantID());
     * assertEquals(expected.getSecret(), actual.getSecret());
     * assertEquals(expected.getDeveloper(), actual.getDeveloper());
     * }
     */

    @SuppressWarnings("unchecked")
    public static Response registerSimplePushVariant(String pushAppId, SimplePushVariant variant, Session session) {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", variant.getName());
        jsonObject.put("description", variant.getDescription());

        Response response = session.given().contentType("application/json").header("Accept", "application/json")
            .body(jsonObject.toString())
            .post("/rest/applications/{pushAppId}/simplePush", pushAppId);

        return response;
    }

    @SuppressWarnings("unchecked")
    public static Response updateSimplePushVariant(String pushAppId, SimplePushVariant variant, String variantId,
        Session session) {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", variant.getName());
        jsonObject.put("description", variant.getDescription());

        Response response = session.given().contentType("application/json").header("Accept", "application/json")
            .body(jsonObject.toString())
            .put("/rest/applications/{pushAppId}/simplePush/{variantId}", pushAppId, variantId);

        return response;
    }

    public static Response listAllSimplePushVariants(String pushAppId, Session session) {

        Response response = session.given()
            .contentType("application/json")
            .header("Accept", "application/json")
            .get("/rest/applications/{pushAppId}/simplePush", pushAppId);

        return response;
    }

    public static Response findSimplePushVariantById(String pushAppId, String variantId,
        Session session) {

        Response response = session.given()
            .contentType("application/json")
            .header("Accept", "application/json")
            .get("/rest/applications/{pushAppId}/simplePush/{variantId}", pushAppId, variantId);

        return response;
    }

    public static Response deleteSimplePushVariant(String pushAppId, String variantId, Session session) {

        Response response = session.given()
            .delete("/rest/applications/{pushAppId}/simplePush/{variantId}", pushAppId, variantId);

        return response;
    }
}
