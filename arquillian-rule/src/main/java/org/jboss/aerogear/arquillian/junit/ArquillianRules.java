/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.arquillian.junit;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.rules.MethodRule;
import org.junit.rules.TestRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import java.util.List;

/**
 * Arquillian JUnit runner with support for {@link ArquillianRule}. ArquillianRule is like a JUnit {@link MethodRule},
 * it is executed around a test execution and it is automatically enriched by test
 *
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 */
public class ArquillianRules extends Arquillian {

    public ArquillianRules(Class<?> klass) throws InitializationError {
        super(klass);
    }

    @Override
    protected Statement methodInvoker(final FrameworkMethod method, final Object test) {

        final Statement originalStatement = super.methodInvoker(method, test);

        final List<MethodRule> methodRules = getTestClass().getAnnotatedFieldValues(test, ArquillianRule.class,
                MethodRule.class);

        final List<TestRule> testRules = getTestClass().getAnnotatedFieldValues(test, ArquillianRule.class, TestRule
                .class);

        if (methodRules.size() == 0 && testRules.size() == 0) {
            return originalStatement;
        }

        return new Statement() {

            @Override
            public void evaluate() throws Throwable {

                Statement statement = originalStatement;
                for (MethodRule rule : methodRules) {
                    statement = rule.apply(statement, method, test);
                }
                for (TestRule rule : testRules) {
                    statement = rule.apply(statement, describeChild(method));
                }
                statement.evaluate();
            }
        };
    }
}