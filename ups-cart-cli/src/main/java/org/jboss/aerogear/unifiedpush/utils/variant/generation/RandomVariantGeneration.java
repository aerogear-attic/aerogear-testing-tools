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
package org.jboss.aerogear.unifiedpush.utils.variant.generation;

import java.util.ArrayList;
import java.util.List;

import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.api.VariantType;
import org.jboss.aerogear.unifiedpush.utils.RandomUtils;
import org.jboss.aerogear.unifiedpush.utils.variant.VariantMetadata;

/**
 * Type of generated variant will be random.
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class RandomVariantGeneration implements VariantGenerationStrategy {

    @Override
    public List<Variant> generate(int count, VariantMetadata metadata) {

        List<Variant> variants = new ArrayList<Variant>();

        for (int i = 0; i < count; i++) {
            variants.add(VariantGeneratorUtil.getVariant(VariantType.values()[RandomUtils.randInt(0, 2)], metadata));
        }

        return variants;
    }
}
