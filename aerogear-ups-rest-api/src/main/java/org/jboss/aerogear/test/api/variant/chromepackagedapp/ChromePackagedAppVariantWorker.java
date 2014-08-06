package org.jboss.aerogear.test.api.variant.chromepackagedapp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.jboss.aerogear.test.Headers;
import org.jboss.aerogear.test.Session;
import org.jboss.aerogear.test.UnexpectedResponseException;
import org.jboss.aerogear.test.api.variant.VariantWorker;
import org.jboss.aerogear.unifiedpush.api.ChromePackagedAppVariant;
import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.json.simple.JSONObject;

import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;

public class ChromePackagedAppVariantWorker extends VariantWorker<ChromePackagedAppVariant, String,
        ChromePackagedAppVariantBlueprint, ChromePackagedAppVariantEditor, PushApplication,
        ChromePackagedAppVariantContext, ChromePackagedAppVariantWorker> {

    private ChromePackagedAppVariantWorker() {

    }

    @Override
    public ChromePackagedAppVariantContext createContext(Session session, PushApplication parent) {
        if (parent == null) {
            throw new IllegalArgumentException("Parent cannot be null!");
        }
        return new ChromePackagedAppVariantContext(this, parent, session);
    }

    @Override
    public JSONObject marshall(ChromePackagedAppVariant entity) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", entity.getName());
        jsonObject.put("description", entity.getDescription());
        jsonObject.put("clientId", entity.getClientId());
        jsonObject.put("clientSecret", entity.getClientSecret());
        jsonObject.put("refreshToken", entity.getRefreshToken());
        return jsonObject;
    }

    @Override
    public ChromePackagedAppVariantEditor demarshall(ChromePackagedAppVariantContext context, JsonPath jsonPath) {
        ChromePackagedAppVariantEditor editor = new ChromePackagedAppVariantEditor(context);
        editor.setName(jsonPath.getString("name"));
        editor.setDescription(jsonPath.getString("description"));
        editor.setVariantID(jsonPath.getString("variantID"));
        editor.setSecret(jsonPath.getString("secret"));
        editor.setDeveloper(jsonPath.getString("developer"));
        editor.setId(jsonPath.getString("id"));
        editor.setClientId(jsonPath.getString("clientId"));
        editor.setClientSecret(jsonPath.getString("clientSecret"));
        editor.setRefreshToken(jsonPath.getString("refreshToken"));
        return editor;
    }

    @Override
    public List<ChromePackagedAppVariantEditor> create(ChromePackagedAppVariantContext context, Collection<? extends


            ChromePackagedAppVariantBlueprint> blueprints) {
        List<ChromePackagedAppVariantEditor> editors = new ArrayList<ChromePackagedAppVariantEditor>();
        for (ChromePackagedAppVariantBlueprint blueprint : blueprints) {
            Response response = context.getSession().givenAuthorized()
                    .contentType(getContentType())
                    .header(Headers.acceptJson())
                    .body(marshall(blueprint))
                    .post("/rest/applications/{pushApplicationID}/chrome", context.getParent().getPushApplicationID());

            UnexpectedResponseException.verifyResponse(response, HttpStatus.SC_CREATED);

            editors.add(demarshall(context, response.jsonPath()));
        }
        return editors;
    }

    @Override
    public List<ChromePackagedAppVariantEditor> readAll(ChromePackagedAppVariantContext context) {
        Response response = context.getSession().givenAuthorized()
                .contentType(getContentType())
                .header(Headers.acceptJson())
                .get("/rest/applications/{pushApplicationID}/chrome", context.getParent().getPushApplicationID());

        UnexpectedResponseException.verifyResponse(response, HttpStatus.SC_OK);

        List<ChromePackagedAppVariantEditor> editors = new ArrayList<ChromePackagedAppVariantEditor>();

        JsonPath jsonPath = response.jsonPath();

        List<Map<String, ?>> items = jsonPath.getList("");

        for (int i = 0; i < items.size(); i++) {
            jsonPath.setRoot("[" + i + "]");

            ChromePackagedAppVariantEditor editor = demarshall(context, jsonPath);
            editors.add(editor);
        }

        return editors;
    }

    @Override
    public ChromePackagedAppVariantEditor read(ChromePackagedAppVariantContext context, String id) {
        Response response = context.getSession().givenAuthorized()
                .contentType(getContentType())
                .header(Headers.acceptJson())
                .get("/rest/applications/{pushApplicationID}/chrome/{variantID}",
                        context.getParent().getPushApplicationID(), id);

        UnexpectedResponseException.verifyResponse(response, HttpStatus.SC_OK);

        return demarshall(context, response.jsonPath());
    }

    @Override
    public void update(ChromePackagedAppVariantContext context,
                       Collection<? extends ChromePackagedAppVariant> entities) {
        for (ChromePackagedAppVariant entity : entities) {
            Response response = context.getSession().givenAuthorized()
                    .contentType(getContentType())
                    .header(Headers.acceptJson())
                    .body(marshall(entity))
                    .put("/rest/applications/{pushApplicationID}/chrome/{variantID}",
                            context.getParent().getPushApplicationID(), context.getEntityID(entity));

            UnexpectedResponseException.verifyResponse(response, HttpStatus.SC_NO_CONTENT);

            // FIXME do we need to demarshall?
        }
    }

    @Override
    public void deleteById(ChromePackagedAppVariantContext context, String id) {
        Response response = context.getSession().givenAuthorized()
                .contentType(getContentType())
                .header(Headers.acceptJson())
                .delete("/rest/applications/{pushApplicationID}/chrome/{variantID}",
                        context.getParent().getPushApplicationID(), id);

        UnexpectedResponseException.verifyResponse(response, HttpStatus.SC_NO_CONTENT);
    }

    @Override
    public void resetSecret(ChromePackagedAppVariantContext context, String id) {
        Response response = context.getSession().givenAuthorized()
                .contentType(getContentType())
                .header(Headers.acceptJson())
                .body("[]")
                .put("/rest/applications/{pushApplicationID}/chrome/{variantID}/reset",
                        context.getParent().getPushApplicationID(), id);

        UnexpectedResponseException.verifyResponse(response, HttpStatus.SC_OK);
    }

    public static ChromePackagedAppVariantWorker worker() {
        return new ChromePackagedAppVariantWorker();
    }


}