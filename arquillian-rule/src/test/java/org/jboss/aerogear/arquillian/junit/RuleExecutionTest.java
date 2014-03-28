package org.jboss.aerogear.arquillian.junit;

import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.aerogear.arquillian.junit.ArquillianRule;
import org.jboss.aerogear.arquillian.junit.ArquillianRules;
import org.jboss.arquillian.junit.InSequence;
import org.junit.Assert;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.junit.runner.RunWith;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

@RunWith(ArquillianRules.class)
public class RuleExecutionTest {

    private AtomicInteger counter = new AtomicInteger(0);
    private static AtomicInteger staticCounter = new AtomicInteger(0);

    @ArquillianRule
    public MethodRule simpleRule = new MethodRule() {

        @Override
        public Statement apply(final Statement base, FrameworkMethod method, Object target) {

            return new Statement() {
                public void evaluate() throws Throwable {
                    counter.incrementAndGet();
                    staticCounter.incrementAndGet();
                    base.evaluate();
                    counter.incrementAndGet();
                    staticCounter.incrementAndGet();
                };
            };
        }
    };

    @Test
    @InSequence(1)
    public void applyRuleOnce() throws Exception {
        Assert.assertThat(counter, is(notNullValue()));
        Assert.assertThat(counter.get(), is(1));
        Assert.assertThat(staticCounter, is(notNullValue()));
        Assert.assertThat(staticCounter.get(), is(1));
    }

    @Test
    @InSequence(2)
    public void applyRuleSecond() throws Exception {
        Assert.assertThat(counter, is(notNullValue()));
        Assert.assertThat(counter.get(), is(1));
        Assert.assertThat(staticCounter, is(notNullValue()));
        Assert.assertThat(staticCounter.get(), is(3));
    }

}