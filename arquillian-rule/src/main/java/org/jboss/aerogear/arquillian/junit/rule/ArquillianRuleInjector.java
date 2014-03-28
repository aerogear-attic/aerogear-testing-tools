package org.jboss.aerogear.arquillian.junit.rule;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;

import org.jboss.aerogear.arquillian.junit.ArquillianRule;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.test.spi.TestEnricher;
import org.jboss.arquillian.test.spi.event.suite.Before;
import org.junit.rules.MethodRule;

/**
 * Injector for enrichable fields in {@link ArquillianRule} annotated JUnit {@link MethodRule}.
 *
 * Rules are injected in Before event. Their execution does not fire any events and they directly encapsule Test execution.
 *
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 *
 */
public class ArquillianRuleInjector {

    private static final ThreadLocal<Collection<TestEnricher>> enrichers = new ThreadLocal<Collection<TestEnricher>>();

    @Inject
    Instance<ServiceLoader> serviceLoader;

    public void injectRule(@Observes Before enrichRule) {
        if (getEnrichers() == null || getEnrichers().isEmpty()) {
            Collection<TestEnricher> testEnrichers = serviceLoader.get().all(TestEnricher.class);
            enrichers.set(testEnrichers);
        }

        Class<?> clazz = enrichRule.getTestClass().getJavaClass();
        List<Field> rules = SecurityActions.getFieldsWithAnnotation(clazz, ArquillianRule.class);
        for (Field f : rules) {
            Object value = SecurityActions.getFieldValue(enrichRule.getTestInstance(), f);
            for (TestEnricher enricher : getEnrichers()) {
                enricher.enrich(value);
            }
        }
    }

    public static synchronized Collection<TestEnricher> getEnrichers() {
        return enrichers.get();
    }

}
