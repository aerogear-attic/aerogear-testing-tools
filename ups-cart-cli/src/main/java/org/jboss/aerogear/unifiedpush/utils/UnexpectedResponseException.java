package org.jboss.aerogear.unifiedpush.utils;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import com.jayway.restassured.response.Response;

/**
 * @author <a href="mailto:tkriz@redhat.com">Tadeas Kriz</a>
 */
public class UnexpectedResponseException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private final Response response;

    public UnexpectedResponseException(Response response) {
        this(response, -1);
    }

    public UnexpectedResponseException(Response response, int expectedStatus) {
        super("Unexpected response status code: " + response.getStatusCode() + "!" +
            "(expected: " + expectedStatus + ")");
        this.response = response;
    }

    public Response getResponse() {
        return response;
    }

    public static void verifyResponse(Response response, int expectedStatus) throws UnexpectedResponseException,
        NullPointerException {
        Validate.notNull(expectedStatus);
        if (response.statusCode() != expectedStatus) {
            throw new UnexpectedResponseException(response, expectedStatus);
        }
    }

    public static class Matcher extends TypeSafeMatcher<UnexpectedResponseException> {
        private int expectedResponseCode;
        private int foundResponseCode;

        private Matcher(int expectedResponseCode) {
            this.expectedResponseCode = expectedResponseCode;
        }

        @Override
        protected boolean matchesSafely(UnexpectedResponseException e) {
            this.foundResponseCode = e.getResponse().statusCode();
            return foundResponseCode == expectedResponseCode;
        }

        @Override
        public void describeTo(Description description) {
            description
                .appendText("The found response code: ")
                .appendValue(foundResponseCode)
                .appendText(" doesn't match expected code: ")
                .appendValue(expectedResponseCode);
        }

        public static Matcher expect(int expectedStatus) {
            return new Matcher(expectedStatus);
        }
    }
}
