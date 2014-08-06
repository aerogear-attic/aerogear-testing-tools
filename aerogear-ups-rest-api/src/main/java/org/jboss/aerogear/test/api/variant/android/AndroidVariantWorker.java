package org.jboss.aerogear.test.api.variant.android;

import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.jboss.aerogear.test.Headers;
import org.jboss.aerogear.test.Session;
import org.jboss.aerogear.test.UnexpectedResponseException;
import org.jboss.aerogear.test.api.variant.VariantWorker;
import org.jboss.aerogear.unifiedpush.api.AndroidVariant;
import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class AndroidVariantWorker extends VariantWorker<AndroidVariant, String, AndroidVariantBlueprint,
        AndroidVariantEditor, PushApplication, AndroidVariantContext, AndroidVariantWorker> {

    private AndroidVariantWorker() {

    }

    @Override
    public AndroidVariantContext createContext(Session session, PushApplication parent) {
        if (parent == null) {
            throw new IllegalArgumentException("Parent cannot be null!");
        }
        return new AndroidVariantContext(this, parent, session);
    }

    @Override
    public JSONObject marshall(AndroidVariant entity) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", entity.getName());
        jsonObject.put("description", entity.getDescription());
        jsonObject.put("googleKey", entity.getGoogleKey());
        jsonObject.put("projectNumber", entity.getProjectNumber());
        return jsonObject;
    }

    @Override
    public AndroidVariantEditor demarshall(AndroidVariantContext context, JsonPath jsonPath) {
        AndroidVariantEditor editor = new AndroidVariantEditor(context);
        editor.setName(jsonPath.getString("name"));
        editor.setDescription(jsonPath.getString("description"));
        editor.setVariantID(jsonPath.getString("variantID"));
        editor.setSecret(jsonPath.getString("secret"));
        editor.setDeveloper(jsonPath.getString("developer"));
        editor.setGoogleKey(jsonPath.getString("googleKey"));
        editor.setId(jsonPath.getString("id"));
        editor.setProjectNumber(jsonPath.getString("projectNumber"));
        return editor;
    }

    @Override
    public List<AndroidVariantEditor> create(AndroidVariantContext context, Collection<? extends
            AndroidVariantBlueprint> blueprints) {
        List<AndroidVariantEditor> editors = new ArrayList<AndroidVariantEditor>();
        for (AndroidVariantBlueprint blueprint : blueprints) {
            Response response = context.getSession().givenAuthorized()
                    .contentType(getContentType())
                    .header(Headers.acceptJson())
                    .body(marshall(blueprint))
                    .post("/rest/applications/{pushApplicationID}/android", context.getParent().getPushApplicationID());

            UnexpectedResponseException.verifyResponse(response, HttpStatus.SC_CREATED);

            editors.add(demarshall(context, response.jsonPath()));
        }
        return editors;
    }

    @Override
    public List<AndroidVariantEditor> readAll(AndroidVariantContext context) {
        Response response = context.getSession().givenAuthorized()
                .contentType(getContentType())
                .header(Headers.acceptJson())
                .get("/rest/applications/{pushApplicationID}/android", context.getParent().getPushApplicationID());

        UnexpectedResponseException.verifyResponse(response, HttpStatus.SC_OK);

        List<AndroidVariantEditor> editors = new ArrayList<AndroidVariantEditor>();

        JsonPath jsonPath = response.jsonPath();

        List<Map<String, ?>> items = jsonPath.getList("");

        for (int i = 0; i < items.size(); i++) {
            jsonPath.setRoot("[" + i + "]");

            AndroidVariantEditor editor = demarshall(context, jsonPath);
            editors.add(editor);
        }

        return editors;
    }

    @Override
    public AndroidVariantEditor read(AndroidVariantContext context, String id) {
        Response response = context.getSession().givenAuthorized()
                .contentType(getContentType())
                .header(Headers.acceptJson())
                .get("/rest/applications/{pushApplicationID}/android/{variantID}",
                        context.getParent().getPushApplicationID(), id);

        UnexpectedResponseException.verifyResponse(response, HttpStatus.SC_OK);

        return demarshall(context, response.jsonPath());
    }

    @Override
    public void update(AndroidVariantContext context, Collection<? extends AndroidVariant> entities) {
        for (AndroidVariant entity : entities) {
            Response response = context.getSession().givenAuthorized()
                    .contentType(getContentType())
                    .header(Headers.acceptJson())
                    .body(marshall(entity))
                    .put("/rest/applications/{pushApplicationID}/android/{variantID}",
                            context.getParent().getPushApplicationID(), context.getEntityID(entity));

            UnexpectedResponseException.verifyResponse(response, HttpStatus.SC_NO_CONTENT);

            // FIXME do we need to demarshall?
        }
    }

    @Override
    public void deleteById(AndroidVariantContext context, String id) {
        Response response = context.getSession().givenAuthorized()
                .contentType(getContentType())
                .header(Headers.acceptJson())
                .delete("/rest/applications/{pushApplicationID}/android/{variantID}",
                        context.getParent().getPushApplicationID(), id);

        UnexpectedResponseException.verifyResponse(response, HttpStatus.SC_NO_CONTENT);
    }

    @Override
    public void resetSecret(AndroidVariantContext context, String id) {
        Response response = context.getSession().givenAuthorized()
                .contentType(getContentType())
                .header(Headers.acceptJson())
                .body("[]")
                .put("/rest/applications/{pushApplicationID}/android/{variantID}/reset",
                        context.getParent().getPushApplicationID(), id);

        UnexpectedResponseException.verifyResponse(response, HttpStatus.SC_OK);
    }

    public static AndroidVariantWorker worker() {
        return new AndroidVariantWorker();
    }


}