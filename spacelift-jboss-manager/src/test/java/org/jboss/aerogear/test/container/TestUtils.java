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
package org.jboss.aerogear.test.container;

import org.jboss.aerogear.test.container.manager.configuration.CONTAINER_TYPE;

/**
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class TestUtils {

    // default extracted container to target is JBoss AS 7
    private static final CONTAINER_TYPE defaultContainerType = CONTAINER_TYPE.AS7;

    public static String getJBossHome() {

        String home = System.getProperty("jboss.home");

        if (home == null) {
            // this is set in pom.xml upon jboss extraction
            home = "target/jboss";
        }

        return home;
    }

    public static CONTAINER_TYPE getContainerType() {

        String containerTypeName = System.getProperty("containerType");

        if (containerTypeName == null) {
            return defaultContainerType;
        }

        CONTAINER_TYPE containerType = null;

        try {
            containerType = Enum.valueOf(CONTAINER_TYPE.class, containerTypeName);
        } catch (IllegalArgumentException ex) {
            containerType = defaultContainerType;
        }

        return containerType;
    }
}
