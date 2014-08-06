package org.jboss.aerogear.test.api.variant.simplepush;

import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.jboss.aerogear.test.Headers;
import org.jboss.aerogear.test.Session;
import org.jboss.aerogear.test.UnexpectedResponseException;
import org.jboss.aerogear.test.api.variant.VariantWorker;
import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.api.SimplePushVariant;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class SimplePushVariantWorker extends VariantWorker<SimplePushVariant, String, SimplePushVariantBlueprint,
        SimplePushVariantEditor, PushApplication, SimplePushVariantContext, SimplePushVariantWorker> {

    private SimplePushVariantWorker() {

    }

    @Override
    public SimplePushVariantContext createContext(Session session, PushApplication parent) {
        if (parent == null) {
            throw new IllegalArgumentException("Parent cannot be null!");
        }
        return new SimplePushVariantContext(this, parent, session);
    }

    @Override
    public JSONObject marshall(SimplePushVariant entity) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", entity.getName());
        jsonObject.put("description", entity.getDescription());
        return jsonObject;
    }

    @Override
    public SimplePushVariantEditor demarshall(SimplePushVariantContext context, JsonPath jsonPath) {
        SimplePushVariantEditor editor = new SimplePushVariantEditor(context);
        editor.setName(jsonPath.getString("name"));
        editor.setDescription(jsonPath.getString("description"));
        editor.setVariantID(jsonPath.getString("variantID"));
        editor.setSecret(jsonPath.getString("secret"));
        editor.setDeveloper(jsonPath.getString("developer"));
        editor.setId(jsonPath.getString("id"));
        return editor;
    }

    @Override
    public List<SimplePushVariantEditor> create(SimplePushVariantContext context, Collection<? extends

            SimplePushVariantBlueprint> blueprints) {
        List<SimplePushVariantEditor> editors = new ArrayList<SimplePushVariantEditor>();
        for (SimplePushVariantBlueprint blueprint : blueprints) {
            Response response = context.getSession().givenAuthorized()
                    .contentType(getContentType())
                    .header(Headers.acceptJson())
                    .body(marshall(blueprint))
                    .post("/rest/applications/{pushApplicationID}/simplePush",
                            context.getParent().getPushApplicationID());

            UnexpectedResponseException.verifyResponse(response, HttpStatus.SC_CREATED);

            editors.add(demarshall(context, response.jsonPath()));
        }
        return editors;
    }

    @Override
    public List<SimplePushVariantEditor> readAll(SimplePushVariantContext context) {
        Response response = context.getSession().givenAuthorized()
                .contentType(getContentType())
                .header(Headers.acceptJson())
                .get("/rest/applications/{pushApplicationID}/simplePush", context.getParent().getPushApplicationID());

        UnexpectedResponseException.verifyResponse(response, HttpStatus.SC_OK);

        List<SimplePushVariantEditor> editors = new ArrayList<SimplePushVariantEditor>();

        JsonPath jsonPath = response.jsonPath();

        List<Map<String, ?>> items = jsonPath.getList("");

        for (int i = 0; i < items.size(); i++) {
            jsonPath.setRoot("[" + i + "]");

            SimplePushVariantEditor editor = demarshall(context, jsonPath);
            editors.add(editor);
        }

        return editors;
    }

    @Override
    public SimplePushVariantEditor read(SimplePushVariantContext context, String id) {
        Response response = context.getSession().givenAuthorized()
                .contentType(getContentType())
                .header(Headers.acceptJson())
                .get("/rest/applications/{pushApplicationID}/simplePush/{variantID}",
                        context.getParent().getPushApplicationID(), id);

        UnexpectedResponseException.verifyResponse(response, HttpStatus.SC_OK);

        return demarshall(context, response.jsonPath());
    }

    @Override
    public void update(SimplePushVariantContext context, Collection<? extends SimplePushVariant> entities) {
        for (SimplePushVariant entity : entities) {
            Response response = context.getSession().givenAuthorized()
                    .contentType(getContentType())
                    .header(Headers.acceptJson())
                    .body(marshall(entity))
                    .put("/rest/applications/{pushApplicationID}/simplePush/{variantID}",
                            context.getParent().getPushApplicationID(), context.getEntityID(entity));

            UnexpectedResponseException.verifyResponse(response, HttpStatus.SC_NO_CONTENT);

            // FIXME do we need to demarshall?
        }
    }

    @Override
    public void deleteById(SimplePushVariantContext context, String id) {
        Response response = context.getSession().givenAuthorized()
                .contentType(getContentType())
                .header(Headers.acceptJson())
                .delete("/rest/applications/{pushApplicationID}/simplePush/{variantID}",
                        context.getParent().getPushApplicationID(), id);

        UnexpectedResponseException.verifyResponse(response, HttpStatus.SC_NO_CONTENT);
    }

    @Override
    public void resetSecret(SimplePushVariantContext context, String id) {
        Response response = context.getSession().givenAuthorized()
                .contentType(getContentType())
                .header(Headers.acceptJson())
                .body("[]")
                .put("/rest/applications/{pushApplicationID}/simplePush/{variantID}/reset",
                        context.getParent().getPushApplicationID(), id);

        UnexpectedResponseException.verifyResponse(response, HttpStatus.SC_OK);
    }

    public static SimplePushVariantWorker worker() {
        return new SimplePushVariantWorker();
    }


}