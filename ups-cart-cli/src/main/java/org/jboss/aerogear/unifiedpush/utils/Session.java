package org.jboss.aerogear.unifiedpush.utils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.specification.RequestSpecification;

public class Session {

    private URL baseUrl;
    private String baseUri;
    private int port;
    private String basePath;

    private String loginName;
    private String password;
    private Map<String, ?> cookies;
    private boolean invalid;

    public Session(URL baseUrl, String loginName, String password, Map<String, ?> cookies) {

        this.baseUrl = baseUrl;
        this.baseUri = baseUrl.getProtocol() + "://" + baseUrl.getHost();
        this.port = baseUrl.getPort() == -1 ? ("https".equals(baseUrl.getProtocol()) ? 443 : 80) : baseUrl.getPort();
        this.basePath = baseUrl.getPath();

        this.loginName = loginName;
        this.password = password;
        this.cookies = Collections.unmodifiableMap(cookies); // TODO should the be modifiable or not?
        this.invalid = false;

    }

    public Session(String baseUrl, String loginName, String password, Map<String, ?> cookies) {
        this(UrlUtils.from(baseUrl), loginName, password, cookies);
    }

    public RequestSpecification given() {

        RestAssured.baseURI = baseUri;
        RestAssured.port = port;
        RestAssured.basePath = basePath;

        return RestAssured.given().cookies(cookies);
    }

    public Map<String, ?> getCookies() {
        return cookies;
    }

    public URL getBaseUrl() {
        return baseUrl;
    }

    public String getLoginName() {
        return loginName;
    }

    public String getPassword() {
        return password;
    }

    public boolean isValid() {
        return !invalid;
    }

    public static Session newSession(String baseUrl) {
        return new Session(baseUrl, null, null, new HashMap<String, Object>());
    }

    public Session invalidate() {
        this.invalid = true;
        this.cookies = new HashMap<String, Object>();
        this.loginName = "";
        this.password = "";
        return this;
    }

    public static Session createInvalid(String root) {
        Session session = forceCreateValidWithEmptyCookies(root);
        session.invalid = true;
        return session;
    }

    public static Session forceCreateValidWithEmptyCookies(String root) {
        return new Session(root, "", "", new HashMap<String, Object>());
    }

    private static final class UrlUtils {
        static final URL from(String url) throws IllegalArgumentException {
            try {
                return new URL(url);
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException("Unable to convert " + url + "to URL object");
            }
        }
    }
}