package org.jboss.aerogear.test;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import com.jayway.restassured.response.Response;

/**
 * @author <a href="mailto:tkriz@redhat.com">Tadeas Kriz</a>
 */
public class UnexpectedResponseException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private final Response response;
    private final int actualStatusCode;
    private final int expectedStatusCode;

    public UnexpectedResponseException(Response response) {
        this(response, -1);
    }

    public UnexpectedResponseException(Response response, int expectedStatus) {
        this(response, response.statusCode(), expectedStatus);
    }

    public UnexpectedResponseException(Response response, int actualStatusCode, int expectedStatusCode) {
        super("Unexpected response status code: " + actualStatusCode + "! (expected: " + expectedStatusCode + ")");
        this.response = response;
        this.actualStatusCode = actualStatusCode;
        this.expectedStatusCode = expectedStatusCode;
    }

    public Response getResponse() {
        return response;
    }

    public int getActualStatusCode() {
        return actualStatusCode;
    }

    public int getExpectedStatusCode() {
        return expectedStatusCode;
    }

    public static void verifyStatusCode(int actualStatusCode, int expectedStatusCode)
            throws UnexpectedResponseException {
        if(actualStatusCode != expectedStatusCode) {
            throw new UnexpectedResponseException(null, actualStatusCode, expectedStatusCode);
        }
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
            this.foundResponseCode = e.getActualStatusCode();
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
