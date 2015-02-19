package org.jboss.aerogear.arquillian.junit;

import org.jboss.arquillian.junit.InSequence;
import org.junit.Assert;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

@RunWith(ArquillianRules.class)
public class MethodRuleExecutionTest {

    private AtomicInteger methodRuleCounter = new AtomicInteger(0);
    private static AtomicInteger staticMethodRuleCounter = new AtomicInteger(0);
    
    @ArquillianRule
    public MethodRule simpleMethodRule = new MethodRule() {

        @Override
        public Statement apply(final Statement base, FrameworkMethod method, Object target) {

            return new Statement() {
                public void evaluate() throws Throwable {
                    methodRuleCounter.incrementAndGet();
                    staticMethodRuleCounter.incrementAndGet();
                    base.evaluate();
                    methodRuleCounter.incrementAndGet();
                    staticMethodRuleCounter.incrementAndGet();
                };
            };
        }
    };

    @Test
    @InSequence(1)
    public void applyRuleOnce() throws Exception {
        Assert.assertThat(methodRuleCounter, is(notNullValue()));
        Assert.assertThat(methodRuleCounter.get(), is(1));
        Assert.assertThat(staticMethodRuleCounter, is(notNullValue()));
        Assert.assertThat(staticMethodRuleCounter.get(), is(1));
    }

    @Test
    @InSequence(2)
    public void applyRuleSecond() throws Exception {
        Assert.assertThat(methodRuleCounter, is(notNullValue()));
        Assert.assertThat(methodRuleCounter.get(), is(1));
        Assert.assertThat(staticMethodRuleCounter, is(notNullValue()));
        Assert.assertThat(staticMethodRuleCounter.get(), is(3));
    }

}