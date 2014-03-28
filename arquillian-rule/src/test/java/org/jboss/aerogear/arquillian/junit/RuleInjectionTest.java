package org.jboss.aerogear.arquillian.junit;

import org.jboss.aerogear.arquillian.junit.ArquillianRulesTestExtension.DummyObject;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Assert;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.junit.runner.RunWith;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

@RunWith(ArquillianRules.class)
public class RuleInjectionTest {

    @ArquillianRule
    public MethodRule simpleRule = new MethodRule() {

        @ArquillianResource
        DummyObject dummy;

        @Override
        public Statement apply(final Statement base, FrameworkMethod method, Object target) {

            return new Statement() {
                public void evaluate() throws Throwable {
                    Assert.assertThat(dummy, is(notNullValue()));
                    base.evaluate();
                    Assert.assertThat(dummy, is(notNullValue()));
                };
            };
        }
    };

    @Test
    public void applyRuleOnce() throws Exception {
        Assert.assertTrue("Do nothing", true);
    }
}