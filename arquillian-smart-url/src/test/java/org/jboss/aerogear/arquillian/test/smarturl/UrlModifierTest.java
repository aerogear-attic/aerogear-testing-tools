package org.jboss.aerogear.arquillian.test.smarturl;

import java.lang.annotation.Annotation;
import java.net.URL;

import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;

public class UrlModifierTest {

    @Test
    public void changePort() throws Exception {
        URL url = new URL("http://localhost:80/1234");
        URL modified = URIModifier.modify(url, create(SchemeName.HTTP, "", "", 8080));

        Assert.assertThat(modified.getPort(), is(8080));
        Assert.assertThat(modified.getHost(), is("localhost"));
        Assert.assertThat(url.getFile(), is(modified.getFile()));
    }

    @Test
    public void changeHostname() throws Exception {
        URL url = new URL("http://127.0.0.1:8080/1234");
        URL modified = URIModifier.modify(url, create(SchemeName.HTTP, "", "", 8080));

        Assert.assertThat(modified.getPort(), is(8080));
        Assert.assertThat(modified.getHost(), is("localhost"));
        Assert.assertThat(url.getFile(), is(modified.getFile()));
    }

    @Test
    public void addBasicAuth() throws Exception {
        URL url = new URL("http://localhost:8080/1234");
        URL modified = URIModifier.modify(url, create(SchemeName.HTTP, "user", "pass", 8080));

        Assert.assertThat(modified.getPort(), is(8080));
        Assert.assertThat(modified.getHost(), is("localhost"));
        Assert.assertThat(modified.getAuthority(), is("user:pass@localhost:8080"));
        Assert.assertThat(url.getFile(), is(modified.getFile()));
    }

    @Test
    public void notSpecifiedPortWithHTTP() throws Exception {
        URL url = new URL("http://localhost/1234");
        
        URL modified = URIModifier.modify(url, create(SchemeName.HTTP, "user", "pass", -1));

        Assert.assertThat(modified.getPort(), is(-1));
        Assert.assertThat(modified.getHost(), is("localhost"));
        Assert.assertThat(modified.getAuthority(), is("user:pass@localhost"));
        Assert.assertThat(url.getFile(), is(modified.getFile()));
    }    

    private static UriScheme create(final SchemeName scheme, final String user, final String password, final int port) {
        return new UriScheme() {

            @Override
            public Class<? extends Annotation> annotationType() {
                return UriScheme.class;
            }

            @Override
            public String user() {
                return user;
            }

            @Override
            public int port() {
                return port;
            }

            @Override
            public String password() {
                return password;
            }

            @Override
            public SchemeName name() {
                return scheme;
            }
        };
    }
}
