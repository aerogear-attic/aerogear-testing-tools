package org.jboss.aerogear.test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.aerogear.test.api.auth.LoginRequest;
import org.jboss.aerogear.unifiedpush.api.AndroidVariant;
import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.api.SimplePushVariant;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.api.iOSVariant;
import org.jboss.aerogear.unifiedpush.utils.application.PushApplicationUtils;
import org.jboss.aerogear.unifiedpush.utils.installation.InstallationUtils;
import org.jboss.aerogear.unifiedpush.utils.variant.AndroidVariantUtils;
import org.jboss.aerogear.unifiedpush.utils.variant.SimplePushVariantUtils;
import org.jboss.aerogear.unifiedpush.utils.variant.iOSVariantUtils;
import org.json.simple.JSONObject;

public class UnifiedPushServer {

    private static final Logger log = Logger.getLogger(UnifiedPushServer.class.getName());

    private final URL unifiedPushServerUrl;

    private final URL authServerUrl;

    private String username;

    private String password;

    protected Session session;

    public UnifiedPushServer(URL unifiedPushServerUrl, URL authServerUrl) {
        this.unifiedPushServerUrl = unifiedPushServerUrl;
        this.authServerUrl = authServerUrl;
    }

    public UnifiedPushServer(String unifiedPushServerUrl, String authServerUrl) throws MalformedURLException {
        this(new URL(unifiedPushServerUrl), new URL(authServerUrl));
    }

    public void dumpCategories(File directory) throws IOException {

        File categoryFile = new File(directory, "categories.json");

        log.log(Level.INFO, "Dumping categories to {0}.", new Object[] { categoryFile.getAbsoluteFile() });

        List<String> categories = InstallationUtils.getAllCategories(session);

        FileWriter w = new FileWriter(categoryFile);

        for (String category : categories) {
            w.append(category).append("\n");
        }

        w.close();
    }

    public void dump(File directory, boolean ignoreRedirects, String alias) throws IOException {

        // set default values
        Properties properties = new Properties();
        properties.put("rootUrl", unifiedPushServerUrl.toExternalForm());
        properties.put("authUrl", authServerUrl.toExternalForm());
        properties.put("password", password);
        properties.put("username", username);

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
         * certificatePath : src/main/resources/certs/qaAerogear.p12 certificatePass : aerogear
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

    public void login(String username, String password) {
        this.session = LoginRequest
            .request()
            .setUnifiedPushServerUrl(unifiedPushServerUrl)
            .setAuthServerUrl(authServerUrl)
            .username(username)
            .password(password)
            .login();

        this.username = username;
        this.password = password;
    }

    public List<PushApplication> deletePushApplications() {
        List<PushApplication> apps = PushApplicationUtils.listAll(session);
        for (PushApplication app : apps) {
            PushApplicationUtils.delete(app, session);
        }
        return apps;
    }

    public PushApplication addPushApplication(String name) {
        PushApplication app = PushApplicationUtils.create(name, name + "'s description", "admin");
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
        SimplePushVariant spv = SimplePushVariantUtils.create(app.getName() + " SP", app.getDescription() + " SP", "admin");
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

    public void registerInstallations(List<Installation> installations, Variant variant) {
        InstallationUtils.registerAll(installations, variant, session);
    }

    public void registerInstallation(Installation installation, Variant variant) {
        InstallationUtils.register(installation, variant, session);
    }

}
