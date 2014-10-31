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

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jboss.aerogear.unifiedpush.api.Category;

/**
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 * 
 */
public class CategoryPicker implements Picker<Category> {

    @Override
    public Set<Category> pick(Set<Category> elements, int count) throws IllegalArgumentException {

        if (elements.size() > count) {
            throw new IllegalArgumentException("count of elements to choose is bigger then the size of the set to choose from");
        }

        List<Category> list = new LinkedList<Category>(elements);

        Collections.shuffle(list);

        return new HashSet<Category>(list.subList(0, count));
    }

}
