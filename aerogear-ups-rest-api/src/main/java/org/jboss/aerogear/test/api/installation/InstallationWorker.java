package org.jboss.aerogear.test.api.installation;

import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.jboss.aerogear.test.Headers;
import org.jboss.aerogear.test.UnexpectedResponseException;
import org.jboss.aerogear.test.api.AbstractUPSWorker;
import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public abstract class InstallationWorker<
        BLUEPRINT extends InstallationBlueprint<BLUEPRINT, EDITOR, PARENT, WORKER, CONTEXT>,
        EDITOR extends InstallationEditor<BLUEPRINT, EDITOR, PARENT, WORKER, CONTEXT>,
        PARENT extends Variant,
        CONTEXT extends InstallationContext<BLUEPRINT, EDITOR, PARENT, WORKER, CONTEXT>,
        WORKER extends InstallationWorker<BLUEPRINT, EDITOR, PARENT, CONTEXT, WORKER>>

        extends AbstractUPSWorker<Installation, String, BLUEPRINT, EDITOR, PARENT, CONTEXT, WORKER> {

    @Override
    public JSONObject marshall(Installation entity) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("deviceToken", entity.getDeviceToken());
        jsonObject.put("deviceType", entity.getDeviceType());
        jsonObject.put("operatingSystem", entity.getOperatingSystem());
        jsonObject.put("osVersion", entity.getOsVersion());
        jsonObject.put("alias", entity.getAlias());

        if (entity.getCategories() != null) {
            // JSONObject doesn't understand Set<String>
            JSONArray categories = new JSONArray();
            for (String category : entity.getCategories()) {
                categories.add(category);
            }
            jsonObject.put("categories", categories);
        }
        return jsonObject;
    }

    @Override
    public EDITOR demarshall(CONTEXT context, JsonPath jsonPath) {
        EDITOR editor = context.createEditor();
        editor.setId(jsonPath.getString("id"));
        editor.setPlatform(jsonPath.getString("platform"));
        editor.setEnabled(jsonPath.getBoolean("enabled"));
        editor.setOperatingSystem(jsonPath.getString("operatingSystem"));
        editor.setOsVersion(jsonPath.getString("osVersion"));
        editor.setAlias(jsonPath.getString("alias"));
        editor.setDeviceType(jsonPath.getString("deviceType"));
        editor.setDeviceToken(jsonPath.getString("deviceToken"));
        HashSet<String> categories = new HashSet<String>();
        List<String> jsonCategories = jsonPath.getList("categories");
        if (jsonCategories != null) {
            for (String jsonCategory : jsonCategories) {
                categories.add(jsonCategory);
            }
        }
        editor.setCategories(categories);
        return editor;
    }

    @Override
    public List<EDITOR> create(CONTEXT context, Collection<? extends BLUEPRINT> blueprints) {
        List<EDITOR> editors = new ArrayList<EDITOR>();
        for (InstallationBlueprint blueprint : blueprints) {
            Response response = context.getSession().given()
                    .contentType(getContentType())
                    .auth().basic(context.getParent().getVariantID(), context.getParent().getSecret())
                    .header(Headers.acceptJson())
                    .body(marshall(blueprint))
                    .post("/rest/registry/device");

            UnexpectedResponseException.verifyResponse(response, HttpStatus.SC_OK);

            editors.add(demarshall(context, response.jsonPath()));
        }
        return editors;
    }

    public void unregister(CONTEXT context, Collection<? extends Installation> entities) {
        for (Installation entity : entities) {
            Response response = context.getSession().given().log().all()
                    .contentType(getContentType())
                    .auth().basic(context.getParent().getVariantID(), context.getParent().getSecret())
                    .urlEncodingEnabled(false)
                    .delete("/rest/registry/device/{token}", entity.getDeviceToken());

            UnexpectedResponseException.verifyResponse(response, HttpStatus.SC_NO_CONTENT);
        }
    }

    @Override
    public List<EDITOR> readAll(CONTEXT context) {
        Response response = context.getSession().givenAuthorized()
                .contentType(getContentType())
                .header(Headers.acceptJson())
                .get("/rest/applications/{variantID}/installations", context.getParent().getVariantID());

        UnexpectedResponseException.verifyResponse(response, HttpStatus.SC_OK);

        List<EDITOR> editors = new ArrayList<EDITOR>();

        JsonPath jsonPath = response.jsonPath();

        List<Map<String, ?>> items = jsonPath.getList("");

        for (int i = 0; i < items.size(); i++) {
            jsonPath.setRoot("[" + i + "]");

            EDITOR editor = demarshall(context, jsonPath);
            editors.add(editor);
        }

        return editors;
    }

    @Override
    public EDITOR read(CONTEXT context, String id) {
        Response response = context.getSession().givenAuthorized()
                .contentType(getContentType())
                .header(Headers.acceptJson())
                .get("/rest/applications/{variantID}/installations/{installationID}",
                        context.getParent().getVariantID(), id);

        UnexpectedResponseException.verifyResponse(response, HttpStatus.SC_OK);

        return demarshall(context, response.jsonPath());
    }

    @Override
    public void update(CONTEXT context, Collection<? extends Installation> entities) {
        for (Installation entity : entities) {
            Response response = context.getSession().givenAuthorized()
                    .contentType(getContentType())
                    .header(Headers.acceptJson())
                    .body(marshall(entity))
                    .put("/rest/applications/{variantID}/installations/{installationID}",
                            context.getParent().getVariantID(), context.getEntityID(entity));

            UnexpectedResponseException.verifyResponse(response, HttpStatus.SC_NO_CONTENT);

            // FIXME do we need to demarshall?
        }
    }

    @Override
    public void deleteById(CONTEXT context, String id) {
        Response response = context.getSession().givenAuthorized()
                .contentType(getContentType())
                .header(Headers.acceptJson())
                .delete("/rest/applications/{variantID}/installations/{installationID}",
                        context.getParent().getVariantID(), id);

        UnexpectedResponseException.verifyResponse(response, HttpStatus.SC_NO_CONTENT);
    }
}