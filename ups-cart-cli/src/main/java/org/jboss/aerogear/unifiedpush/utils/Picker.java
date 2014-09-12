/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.aerogear.unifiedpush.utils;

import java.util.Set;

/**
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public interface Picker<T> {

    /**
     * Selects {@code count} number of elements from {@code elements} randomly.
     *
     * @param elements set to randomly pick {@code count} of elements from
     * @param count number of elements to randomly pick from {@code elements} set
     * @throws IllegalArgumentException iff {@code count is bigger then elements.size()}
     * @return randomly picked subset of {@code elements}
     */
    Set<T> pick(Set<T> elements, int count) throws IllegalArgumentException;
}
