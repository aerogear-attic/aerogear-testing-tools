package org.jboss.aerogear.arquillian.junit;

import java.lang.annotation.Annotation;

import org.jboss.arquillian.core.spi.LoadableExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;

public class ArquillianRulesTestExtension implements LoadableExtension {

    public static class DummyObject {

    }

    public static class DummyObjectResourceProvider implements ResourceProvider {

        @Override
        public boolean canProvide(Class<?> type) {
            return type.isAssignableFrom(DummyObject.class);
        }

        @Override
        public Object lookup(ArquillianResource resource, Annotation... qualifiers) {
            return new DummyObject();
        }

    }

    @Override
    public void register(ExtensionBuilder builder) {
        builder.service(ResourceProvider.class, DummyObjectResourceProvider.class);
    }

}
