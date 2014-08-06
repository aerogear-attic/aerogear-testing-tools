package org.jboss.aerogear.test.api.variant.ios;

import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.jboss.aerogear.test.ContentTypes;
import org.jboss.aerogear.test.FileUtils;
import org.jboss.aerogear.test.Headers;
import org.jboss.aerogear.test.Session;
import org.jboss.aerogear.test.UnexpectedResponseException;
import org.jboss.aerogear.test.api.variant.VariantWorker;
import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.api.iOSVariant;
import org.json.simple.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class iOSVariantWorker extends VariantWorker<iOSVariant, String, iOSVariantBlueprint, iOSVariantEditor,
        PushApplication, iOSVariantContext, iOSVariantWorker> {

    private byte[] defaultCertificate;
    private String defaultPassphrase;

    private iOSVariantWorker() {

    }

    @Override
    public iOSVariantContext createContext(Session session, PushApplication parent) {
        if (parent == null) {
            throw new IllegalArgumentException("Parent cannot be null!");
        }
        return new iOSVariantContext(this, parent, session);
    }

    @Override
    public JSONObject marshall(iOSVariant entity) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", entity.getName());
        jsonObject.put("description", entity.getDescription());
        jsonObject.put("production", entity.isProduction());
        return jsonObject;
    }

    @Override
    public iOSVariantEditor demarshall(iOSVariantContext context, JsonPath jsonPath) {
        iOSVariantEditor editor = new iOSVariantEditor(context);
        editor.setName(jsonPath.getString("name"));
        editor.setDescription(jsonPath.getString("description"));
        editor.setVariantID(jsonPath.getString("variantID"));
        editor.setSecret(jsonPath.getString("secret"));
        editor.setDeveloper(jsonPath.getString("developer"));
        editor.setProduction(jsonPath.getBoolean("production"));
        editor.setId(jsonPath.getString("id"));
        editor.setPassphrase(jsonPath.getString("passphrase"));
        return editor;
    }

    @Override
    public List<iOSVariantEditor> create(iOSVariantContext context, Collection<? extends
            iOSVariantBlueprint> blueprints) {
        List<iOSVariantEditor> editors = new ArrayList<iOSVariantEditor>();
        for (iOSVariantBlueprint blueprint : blueprints) {
            Response response = context.getSession().givenAuthorized()
                    .contentType(ContentTypes.multipartFormData())
                    .header(Headers.acceptJson())
                    .multiPart("certificate", "certificate.p12", blueprint.getCertificate(), ContentTypes.octetStream())
                    .multiPart("production", String.valueOf(blueprint.isProduction()))
                    .multiPart("passphrase", blueprint.getPassphrase())
                    .multiPart("name", blueprint.getName())
                    .multiPart("description", blueprint.getDescription())
                    .post("/rest/applications/{pushApplicationID}/ios", context.getParent().getPushApplicationID());

            UnexpectedResponseException.verifyResponse(response, HttpStatus.SC_CREATED);

            editors.add(demarshall(context, response.jsonPath()));
        }
        return editors;
    }

    @Override
    public List<iOSVariantEditor> readAll(iOSVariantContext context) {
        Response response = context.getSession().givenAuthorized()
                .contentType(getContentType())
                .header(Headers.acceptJson())
                .get("/rest/applications/{pushApplicationID}/ios", context.getParent().getPushApplicationID());

        UnexpectedResponseException.verifyResponse(response, HttpStatus.SC_OK);

        List<iOSVariantEditor> editors = new ArrayList<iOSVariantEditor>();

        JsonPath jsonPath = response.jsonPath();

        List<Map<String, ?>> items = jsonPath.getList("");

        for (int i = 0; i < items.size(); i++) {
            jsonPath.setRoot("[" + i + "]");

            iOSVariantEditor editor = demarshall(context, jsonPath);
            editors.add(editor);
        }

        return editors;
    }

    @Override
    public iOSVariantEditor read(iOSVariantContext context, String id) {
        Response response = context.getSession().givenAuthorized()
                .contentType(getContentType())
                .header(Headers.acceptJson())
                .get("/rest/applications/{pushApplicationID}/ios/{variantID}",
                        context.getParent().getPushApplicationID(), id);

        UnexpectedResponseException.verifyResponse(response, HttpStatus.SC_OK);

        return demarshall(context, response.jsonPath());
    }

    @Override
    public void update(iOSVariantContext context, Collection<? extends iOSVariant> entities) {
        for (iOSVariant entity : entities) {
            Response response = context.getSession().givenAuthorized()
                    .contentType(ContentTypes.multipartFormData())
                    .header(Headers.acceptJson())
                    .multiPart("certificate", "certificate.p12", entity.getCertificate(), ContentTypes.octetStream())
                    .multiPart("production", String.valueOf(entity.isProduction()))
                    .multiPart("passphrase", entity.getPassphrase())
                    .multiPart("name", entity.getName())
                    .multiPart("description", entity.getDescription())
                    .put("/rest/applications/{pushApplicationID}/ios/{variantID}",
                            context.getParent().getPushApplicationID(), context.getEntityID(entity));

            UnexpectedResponseException.verifyResponse(response, HttpStatus.SC_NO_CONTENT);

            // FIXME do we need to demarshall?
        }
    }

    public void updatePatch(iOSVariantContext context, Collection<? extends iOSVariant> entities) {
        for (iOSVariant entity : entities) {
            Response response = context.getSession().givenAuthorized()
                    .contentType(getContentType())
                    .header(Headers.acceptJson())
                    .body(marshall(entity))
                    .patch("/rest/applications/{pushApplicationID}/ios/{variantID}",
                            context.getParent().getPushApplicationID(), context.getEntityID(entity));

            UnexpectedResponseException.verifyResponse(response, HttpStatus.SC_NO_CONTENT);
        }
    }

    @Override
    public void deleteById(iOSVariantContext context, String id) {
        Response response = context.getSession().givenAuthorized()
                .contentType(getContentType())
                .header(Headers.acceptJson())
                .delete("/rest/applications/{pushApplicationID}/ios/{variantID}",
                        context.getParent().getPushApplicationID(), id);

        UnexpectedResponseException.verifyResponse(response, HttpStatus.SC_NO_CONTENT);
    }

    @Override
    public void resetSecret(iOSVariantContext context, String id) {
        Response response = context.getSession().givenAuthorized()
                .contentType(getContentType())
                .header(Headers.acceptJson())
                .body("[]")
                .put("/rest/applications/{pushApplicationID}/ios/{variantID}/reset",
                        context.getParent().getPushApplicationID(), id);

        UnexpectedResponseException.verifyResponse(response, HttpStatus.SC_OK);
    }

    public byte[] getDefaultCertificate() {
        return defaultCertificate;
    }

    public iOSVariantWorker defaultCertificate(String defaultCertificate) {
        return defaultCertificate(new File(defaultCertificate));
    }

    public iOSVariantWorker defaultCertificate(File defaultCertificate) {
        return defaultCertificate(FileUtils.toByteArray(defaultCertificate));
    }

    public iOSVariantWorker defaultCertificate(byte[] defaultCertificate) {
        this.defaultCertificate = defaultCertificate;
        return this;
    }

    public String getDefaultPassphrase() {
        return defaultPassphrase;
    }

    public iOSVariantWorker defaultPassphrase(String defaultPassphrase) {
        this.defaultPassphrase = defaultPassphrase;
        return this;
    }

    public static iOSVariantWorker worker() {
        return new iOSVariantWorker();
    }


}