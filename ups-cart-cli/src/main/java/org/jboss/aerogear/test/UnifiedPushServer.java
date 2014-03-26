package org.jboss.aerogear.test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.logging.impl.AvalonLogger;
import org.jboss.aerogear.unifiedpush.model.AndroidVariant;
import org.jboss.aerogear.unifiedpush.model.PushApplication;
import org.jboss.aerogear.unifiedpush.model.SimplePushVariant;
import org.jboss.aerogear.unifiedpush.model.iOSVariant;
import org.jboss.aerogear.unifiedpush.utils.AndroidVariantUtils;
import org.jboss.aerogear.unifiedpush.utils.AuthenticationUtils;
import org.jboss.aerogear.unifiedpush.utils.PushApplicationUtils;
import org.jboss.aerogear.unifiedpush.utils.Session;
import org.jboss.aerogear.unifiedpush.utils.SimplePushVariantUtils;
import org.jboss.aerogear.unifiedpush.utils.iOSVariantUtils;
import org.json.simple.JSONObject;

public class UnifiedPushServer {
    private static final Logger log = Logger.getLogger(UnifiedPushServer.class.getName());

    private final String rootUrl;

    private Session session;

    public UnifiedPushServer(String rootUrl) {
        this.rootUrl = rootUrl;
        this.session = Session.forceCreateValidWithEmptyCookies(rootUrl);
    }

    public void dump(File directory, boolean ignoreRedirects, String alias) throws IOException {

        // set default values
        Properties properties = new Properties();
        properties.put("rootUrl", rootUrl);
        properties.put("password", session.getPassword());
        properties.put("username", session.getLoginName());

        // get registered applications
        List<PushApplication> apps = PushApplicationUtils.listAll(session);
        String delimiter = "";
        StringBuilder appIds = new StringBuilder();
        for (PushApplication app : apps) {
            appIds.append(delimiter);
            appIds.append(app.getPushApplicationID());
            delimiter = ",";
        }
        properties.put("followRedirects", Boolean.toString(!ignoreRedirects));
        properties.put("registeredAppIds", appIds.toString());

        /*
         * certificatePath : src/main/resources/certs/qaAerogear.p12
         * certificatePass : aerogear
         */

        FileWriter w = new FileWriter(new File(directory, "ups.properties"));
        properties.store(w, null);
        w.close();

        log.log(Level.INFO, "Dumped UPS properties to {0} directory", directory);

        for (PushApplication app : apps) {
            // store Android Variants as JSONs
            List<AndroidVariant> avs = AndroidVariantUtils.listAll(app, session);
            for (AndroidVariant av : avs) {
                JSONObject avJson = new JSONObject();
                // NOTE, URL must be converted to String
                avJson.put("url", session.getBaseUrl().toString());
                avJson.put("variantId", av.getVariantID());
                avJson.put("secret", av.getSecret());
                avJson.put("alias", alias);
                avJson.put("senderID", av.getProjectNumber());

                w = new FileWriter(new File(directory, av.getName().toLowerCase().trim().replaceAll("\\s", "") + ".json"));
                w.append(avJson.toJSONString());
                w.close();
            }
            if (avs.size() > 0) {
                log.log(Level.INFO, "Dumped {0} AndroidVariants to {1} directory", new Object[] { avs.size(), directory });
            }

            // store iOS Variants as JSONs
            List<iOSVariant> ivs = iOSVariantUtils.listAll(app, session);
            for (iOSVariant iv : ivs) {
                JSONObject avJson = new JSONObject();
                avJson.put("url", session.getBaseUrl().toString());
                // FIXME one is lowercase, other is uppercase
                avJson.put("variantID", iv.getVariantID());
                avJson.put("secret", iv.getSecret());
                avJson.put("alias", alias);

                w = new FileWriter(new File(directory, iv.getName().toLowerCase().trim().replaceAll("\\s", "") + ".json"));
                w.append(avJson.toJSONString());
                w.close();
            }

            if (ivs.size() > 0) {
                log.log(Level.INFO, "Dumped {0} iOSVariants to {1} directory", new Object[] { ivs.size(), directory });
            }
        }

    }

    public void login(String loginName, String oldPassword, String newPassword) {
        this.session = AuthenticationUtils.completeLogin(loginName, oldPassword, newPassword, rootUrl);
    }

    public List<PushApplication> deletePushApplications() {
        List<PushApplication> apps = PushApplicationUtils.listAll(session);
        for (PushApplication app : apps) {
            PushApplicationUtils.delete(app, session);
        }
        return apps;
    }

    public PushApplication addPushApplication(String name) {
        PushApplication app = PushApplicationUtils.create(name, name + "'s description");
        PushApplicationUtils.register(app, session);

        return app;
    }

    public AndroidVariant addAndroidVariant(PushApplication app, String googleKey, String projectNumber) {
        AndroidVariant av = AndroidVariantUtils.create(app.getName() + " Android",
            app.getDescription() + " Android",
            googleKey,
            projectNumber);

        AndroidVariantUtils.register(av, app, session);
        return av;
    }

    public SimplePushVariant addSimplePushVariant(PushApplication app) {
        SimplePushVariant spv = SimplePushVariantUtils.create(app.getName() + " SP", app.getDescription() + " SP");
        SimplePushVariantUtils.register(spv, app, session);
        return spv;
    }

    public iOSVariant addiOSVariant(PushApplication app, String certificatePath, String certificatePass, boolean production) {
        iOSVariant iv = iOSVariantUtils.create(app.getName() + " iOS",
            app.getDescription() + " iOS",
            certificatePath,
            certificatePass,
            production);

        iOSVariantUtils.register(iv, app, session);
        return iv;
    }

}
