package org.jboss.aerogear.test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.keycloak.representations.AccessTokenResponse;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Header;
import com.jayway.restassured.specification.RequestSpecification;

public class Session {

    private URL baseUrl;
    private String baseUri;
    private int port;
    private String basePath;

    private AccessTokenResponse accessTokenResponse;

    private Map<String, ?> cookies;
    private boolean invalid;

    public Session(URL baseUrl, AccessTokenResponse accessTokenResponse) {
        this.baseUrl = baseUrl;
        this.baseUri = baseUrl.getProtocol() + "://" + baseUrl.getHost();
        this.port = baseUrl.getPort() == -1 ? ("https".equals(baseUrl.getProtocol()) ? 443 : 80) : baseUrl.getPort();
        this.basePath = baseUrl.getPath();

        this.accessTokenResponse = accessTokenResponse;

        this.cookies = new HashMap<String, Object>();
        this.invalid = false;

    }

    public Session(String baseUrl, AccessTokenResponse accessTokenResponse) {
        this(UrlUtils.from(baseUrl), accessTokenResponse);
    }

    // FIXME chaining with given() would be better
    public RequestSpecification givenAuthorized() {
        if(accessTokenResponse.getToken() == null) {
            return given();
        }

        return given().header(getAuthorization());
    }

    public RequestSpecification given() {

        RestAssured.baseURI = baseUri;
        RestAssured.port = port;
        RestAssured.basePath = basePath;

        return RestAssured
                .given()
                .redirects().follow(false)
                .cookies(cookies);
    }

    public Map<String, ?> getCookies() {
        return cookies;
    }

    public Header getAuthorization() {
        String accessToken = "";
        if(accessTokenResponse.getToken() != null) {
            accessToken = accessTokenResponse.getToken();
        }
        return new Header("Authorization", "Bearer " + accessToken);
    }

    public URL getBaseUrl() {
        return baseUrl;
    }

    public boolean isValid() {
        return !invalid;
    }

    public static Session newSession(String url) {
        return new Session(url, new AccessTokenResponse());
    }

    public Session invalidate() {
        this.invalid = true;
        this.cookies = new HashMap<String, Object>();
        this.baseUrl = null;
        this.accessTokenResponse = new AccessTokenResponse();
        return this;
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