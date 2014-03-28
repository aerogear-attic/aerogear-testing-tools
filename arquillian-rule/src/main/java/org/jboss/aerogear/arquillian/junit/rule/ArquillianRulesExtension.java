package org.jboss.aerogear.arquillian.junit.rule;

import org.jboss.arquillian.core.spi.LoadableExtension;

public class ArquillianRulesExtension implements LoadableExtension {

    @Override
    public void register(ExtensionBuilder builder) {
        builder.observer(ArquillianRuleInjector.class);
    }

}
