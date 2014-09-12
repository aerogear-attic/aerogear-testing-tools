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

import java.util.Random;

/**
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class RandomUtils {

    private static final Random random = new Random();

    /**
     * Chooses random number.
     *
     * @param min minimum number to choose
     * @param max maximum number to choose
     * @return random number in range from {@code min} to {@code max} inclusive
     * @throws IllegalArgumentException if {@code max < min}
     */
    public static int randInt(int min, int max) {

        if (max < min) {
            throw new IllegalArgumentException("max is lower then min");
        }

        return random.nextInt((max - min) + 1) + min;
    }
}
