package org.jboss.aerogear.unifiedpush.utils;

/**
 * @author <a href="mailto:tkriz@redhat.com">Tadeas Kriz</a>
 */
public class ContentTypes {

    public static String json() {
        return "application/json";
    }

    public static String jsonUTF8() {
        return "application/json; charset=utf-8";
    }

    public static String multipartFormData() {
        return "multipart/form-data";
    }

    public static String octetStream() {
        return "application/octet-stream";
    }

}
