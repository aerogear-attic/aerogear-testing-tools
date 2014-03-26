/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.unifiedpush.utils;

/**
 * This class should be removed. No hardcoder references should be used.
 *
 *
 */
@Deprecated
public final class Constants {

    private Constants() {
    }

    public static final String SECURE_AG_PUSH_ENDPOINT = "https://localhost:8443/ag-push/";

    public static final String INSECURE_AG_PUSH_ENDPOINT = "http://localhost:8080/ag-push/";

    public static final String KEYSTORE_PATH = "jboss-as-conf/aerogear.keystore";

    public static final String KEYSTORE_PASSWORD = "aerogear";

    public static final int SOCKET_SERVER_PORT = 8081;

}
