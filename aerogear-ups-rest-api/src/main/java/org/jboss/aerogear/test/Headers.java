package org.jboss.aerogear.test;

import com.jayway.restassured.response.Header;

/**
 * @author <a href="mailto:tkriz@redhat.com">Tadeas Kriz</a>
 */
public class Headers {

    public static Header acceptJson() {
        return new Header("Accept", "application/json");
    }

}
