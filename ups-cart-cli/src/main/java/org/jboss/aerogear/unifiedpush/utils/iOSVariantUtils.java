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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.jboss.aerogear.unifiedpush.model.PushApplication;
import org.jboss.aerogear.unifiedpush.model.iOSVariant;
import org.json.simple.JSONObject;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;

public final class iOSVariantUtils {

    private static final int SINGLE = 1;

    private iOSVariantUtils() {
    }

    public static iOSVariant create(String name, String description, String certificatePath, String passphrase,
        boolean production) {
        return create(name, description, new File(certificatePath), passphrase, production);
    }

    public static iOSVariant create(String name, String description, File certificateFile, String passphrase,
        boolean production) {
        byte[] certificate = toByteArray(certificateFile);

        return create(name, description, certificate, passphrase, production);
    }

    public static iOSVariant create(String name, String description, byte[] certificate, String passphrase,
        boolean production) {
        iOSVariant iOSVariant = new iOSVariant();

        iOSVariant.setName(name);
        iOSVariant.setDescription(description);
        iOSVariant.setCertificate(certificate);
        iOSVariant.setPassphrase(passphrase);
        iOSVariant.setProduction(production);

        return iOSVariant;
    }

    public static iOSVariant generate(String certificatePath, String passphrase, boolean production) {
        return generate(new File(certificatePath), passphrase, production);
    }

    public static iOSVariant generate(File certificateFile, String passphrase, boolean production) {
        return generate(SINGLE, certificateFile, passphrase, production).iterator().next();
    }

    public static List<iOSVariant> generate(int count, String certificatePath, String passphrase, boolean production) {
        return generate(count, new File(certificatePath), passphrase, production);
    }

    public static List<iOSVariant> generate(int count, File certificateFile, String passphrase, boolean production) {
        byte[] certificate = toByteArray(certificateFile);
        List<iOSVariant> iOSVariants = new ArrayList<iOSVariant>();

        for (int i = 0; i < count; i++) {
            String name = UUID.randomUUID().toString();
            String description = UUID.randomUUID().toString();

            iOSVariant variant = create(name, description, certificate, passphrase, production);

            iOSVariants.add(variant);
        }

        return iOSVariants;
    }

    public static iOSVariant generateAndRegister(String certificatePath, String passphrase, boolean production,
        PushApplication pushApplication, Session session) {
        return generateAndRegister(SINGLE, certificatePath, passphrase, production, pushApplication, session)
            .iterator().next();
    }

    public static List<iOSVariant> generateAndRegister(int count, String certificatePath, String passphrase,
        boolean production, PushApplication pushApplication,
        Session session) {
        return generateAndRegister(count, new File(certificatePath), passphrase, production, pushApplication, session);
    }

    public static List<iOSVariant> generateAndRegister(int count, File certificateFile, String passphrase,
        boolean production, PushApplication pushApplication,
        Session session) {
        List<iOSVariant> iOSVariants = generate(count, certificateFile, passphrase, production);

        for (iOSVariant iOSVariant : iOSVariants) {
            register(iOSVariant, pushApplication, session);
        }

        return iOSVariants;
    }

    public static void register(iOSVariant iOSVariant, PushApplication pushApplication,
        Session session) {
        register(iOSVariant, pushApplication, session, ContentTypes.multipartFormData());
    }

    public static void register(iOSVariant iOSVariant, PushApplication pushApplication,
        Session session, String contentType)
        throws UnexpectedResponseException {
        Validate.notNull(session);

        Response response = session.given()
            .contentType(contentType)
            .header(Headers.acceptJson())
            .multiPart("certificate", "certificate.p12", iOSVariant.getCertificate(), ContentTypes.octetStream())
            .multiPart("production", String.valueOf(iOSVariant.isProduction()))
            .multiPart("passphrase", iOSVariant.getPassphrase())
            .multiPart("name", iOSVariant.getName())
            .multiPart("description", iOSVariant.getDescription())
            .post("/rest/applications/{pushApplicationID}/iOS",
                pushApplication.getPushApplicationID());

        UnexpectedResponseException.verifyResponse(response, HttpStatus.SC_CREATED);

        setFromJsonPath(response.jsonPath(), iOSVariant);
    }

    public static List<iOSVariant> listAll(PushApplication pushApplication, Session session) {
        Validate.notNull(session);

        Response response = session.given()
            .contentType(ContentTypes.json())
            .header(Headers.acceptJson())
            .get("/rest/applications/{pushApplicationID}/iOS",
                pushApplication.getPushApplicationID());

        UnexpectedResponseException.verifyResponse(response, HttpStatus.SC_OK);

        List<iOSVariant> iOSVariants = new ArrayList<iOSVariant>();

        JsonPath jsonPath = response.jsonPath();

        List<Map<String, ?>> items = jsonPath.getList("");

        for (int i = 0; i < items.size(); i++) {
            jsonPath.setRoot("[" + i + "]");

            iOSVariant iOSVariant = fromJsonPath(jsonPath);

            iOSVariants.add(iOSVariant);
        }

        return iOSVariants;
    }

    public static iOSVariant findById(String variantID, PushApplication pushApplication,
        Session session) {
        return findById(variantID, pushApplication.getPushApplicationID(), session);
    }

    public static iOSVariant findById(String variantID, String pushApplicationID, Session session) {
        Validate.notNull(session);

        Response response = session.given()
            .contentType(ContentTypes.json())
            .header(Headers.acceptJson())
            .get("/rest/applications/{pushApplicationID}/iOS/{variantID}",
                pushApplicationID, variantID);

        UnexpectedResponseException.verifyResponse(response, HttpStatus.SC_OK);

        return fromJsonPath(response.jsonPath());
    }

    public static void update(iOSVariant iOSVariant, PushApplication pushApplication,
        Session session) {
        update(iOSVariant, pushApplication, session, ContentTypes.multipartFormData());
    }

    public static void update(iOSVariant iOSVariant, PushApplication pushApplication,
        Session session,
        String contentType) {
        Validate.notNull(session);

        Response response = session.given()
            .contentType(contentType)
            .header(Headers.acceptJson())
            .multiPart("certificate", "certificate.p12", iOSVariant.getCertificate(), ContentTypes.octetStream())
            .multiPart("production", String.valueOf(iOSVariant.isProduction()))
            .multiPart("passphrase", iOSVariant.getPassphrase())
            .multiPart("name", iOSVariant.getName())
            .multiPart("description", iOSVariant.getDescription())
            .put("/rest/applications/{pushApplicationID}/iOS/{variantID}",
                pushApplication.getPushApplicationID(), iOSVariant.getVariantID());

        UnexpectedResponseException.verifyResponse(response, HttpStatus.SC_NO_CONTENT);
    }

    public static void updatePatch(iOSVariant iOSVariant, PushApplication pushApplication,
        Session session) {
        updatePatch(iOSVariant, pushApplication, session, ContentTypes.json());
    }

    public static void updatePatch(iOSVariant iOSVariant, PushApplication pushApplication,
        Session session, String contentType) {
        Validate.notNull(session);

        Response response = session.given()
            .contentType(contentType)
            .header(Headers.acceptJson())
            .body(toJSONString(iOSVariant))
            .patch("/rest/applications/{pushApplicationID}/iOS/{variantID}",
                pushApplication.getPushApplicationID(), iOSVariant.getVariantID());

        UnexpectedResponseException.verifyResponse(response, HttpStatus.SC_NO_CONTENT);
    }

    public static void delete(iOSVariant iOSVariant, PushApplication pushApplication,
        Session session) {
        Validate.notNull(session);

        Response response = session.given()
            .contentType(ContentTypes.json())
            .header(Headers.acceptJson())
            .delete("/rest/applications/{pushApplicationID}/iOS/{variantID}",
                pushApplication.getPushApplicationID(), iOSVariant.getVariantID());

        UnexpectedResponseException.verifyResponse(response, HttpStatus.SC_NO_CONTENT);
    }

    public static JSONObject toJSONObject(iOSVariant iOSVariant) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("production", iOSVariant.isProduction());
        jsonObject.put("name", iOSVariant.getName());
        jsonObject.put("description", iOSVariant.getDescription());
        return jsonObject;
    }

    public static String toJSONString(iOSVariant iOSVariant) {
        return toJSONObject(iOSVariant).toJSONString();
    }

    public static iOSVariant fromJsonPath(JsonPath jsonPath) {
        iOSVariant iOSVariant = new iOSVariant();

        setFromJsonPath(jsonPath, iOSVariant);

        return iOSVariant;
    }

    public static void setFromJsonPath(JsonPath jsonPath, iOSVariant iOSVariant) {
        iOSVariant.setId(jsonPath.getString("id"));
        iOSVariant.setVariantID(jsonPath.getString("variantID"));
        iOSVariant.setPassphrase(jsonPath.getString("passphrase"));
        iOSVariant.setDeveloper(jsonPath.getString("developer"));
        iOSVariant.setDescription(jsonPath.getString("description"));
        iOSVariant.setName(jsonPath.getString("name"));
        iOSVariant.setSecret(jsonPath.getString("secret"));
        iOSVariant.setProduction(jsonPath.getBoolean("production"));
    }

    /*
     * public static void checkEquality(iOSVariant expected, iOSVariant actual) {
     * assertEquals(expected.getName(), actual.getName());
     * assertEquals(expected.getDescription(), actual.getDescription());
     * assertEquals(expected.getVariantID(), actual.getVariantID());
     * assertEquals(expected.getSecret(), actual.getSecret());
     * assertEquals(expected.getDeveloper(), actual.getDeveloper());
     * }
     */

    private static byte[] toByteArray(File file) {
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);

            return IOUtils.toByteArray(inputStream);
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("File doesn't exist!", e);
        } catch (IOException e) {
            throw new IllegalStateException("Couldn't read file!", e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

}
