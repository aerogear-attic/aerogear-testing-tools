/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.aerogear.unifiedpush.test.sender.util;

import org.jboss.aerogear.unifiedpush.test.sender.apns.InvalidSSLConfig;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

public class CertificateLoader {


    private static final PropertyResolver<File> APNS_KEYSTORE_FILE = PropertyResolver
            .with(File.class, "custom.aerogear.apns.keystore.path");

    private static final PropertyResolver<String> APNS_KEYSTORE_PASSWORD = PropertyResolver
            .with("123456", "custom.aerogear.apns.keystore.password");

    private static final PropertyResolver<String> APNS_KEYSTORE_TYPE = PropertyResolver
            .with("PKCS12", "custom.aerogear.apns.keystore.type");

    private static final PropertyResolver<String> APNS_KEYSTORE_ALGORITHM = PropertyResolver
            .with("sunx509", "custom.aerogear.apns.keystore.algorithm");

    public static final String RESOURCE_SERVER_STORE = "serverStore.p12";

    public static SSLServerSocketFactory apnsSocketFactory() {
        try {
            InputStream stream;
            File externalApnsCertificateFile = APNS_KEYSTORE_FILE.resolve();
            if(externalApnsCertificateFile != null) {
                stream = new FileInputStream(externalApnsCertificateFile);
            } else {
                stream = CertificateLoader.class.getResourceAsStream("/" + RESOURCE_SERVER_STORE);
            }
            assert stream != null;
            return newSSLSocketFactory(stream,
                    APNS_KEYSTORE_PASSWORD.resolve(),
                    APNS_KEYSTORE_TYPE.resolve(),
                    APNS_KEYSTORE_ALGORITHM.resolve());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static SSLServerSocketFactory newSSLSocketFactory(final InputStream cert, final String password,
                                                       final String ksType, final String ksAlgorithm) throws
            InvalidSSLConfig {
        final SSLContext context = newSSLContext(cert, password, ksType, ksAlgorithm);
        return context.getServerSocketFactory();
    }

    public static SSLContext newSSLContext(final InputStream cert, final String password,
                                           final String ksType, final String ksAlgorithm) throws InvalidSSLConfig {
        try {
            final KeyStore ks = KeyStore.getInstance(ksType);
            ks.load(cert, password.toCharArray());
            return newSSLContext(ks, password, ksAlgorithm);
        } catch (final Exception e) {
            throw new InvalidSSLConfig(e);
        }
    }

    public static SSLContext newSSLContext(final KeyStore ks, final String password,
                                           final String ksAlgorithm) throws InvalidSSLConfig {
        try {
            // Get a KeyManager and initialize it
            final KeyManagerFactory kmf = KeyManagerFactory.getInstance(ksAlgorithm);
            kmf.init(ks, password.toCharArray());

            // Get a TrustManagerFactory with the DEFAULT KEYSTORE, so we have all
            // the certificates in cacerts trusted
            final TrustManagerFactory tmf = TrustManagerFactory.getInstance(ksAlgorithm);
            tmf.init((KeyStore) null);

            // Get the SSLContext to help create SSLSocketFactory
            final SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
            return sslContext;
        } catch (final GeneralSecurityException e) {
            throw new InvalidSSLConfig(e);
        }
    }

}