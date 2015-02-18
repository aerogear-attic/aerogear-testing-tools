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
package org.jboss.aerogear.unifiedpush.test.sender.apns;

import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public class FixedCertificates {

    public static final String CLIENT_STORE = "clientStore.p12";
    public static final String CLIENT_PASSWORD = "123456";

    public static final String SERVER_STORE = "serverStore.p12";
    public static final String SERVER_PASSWORD = "123456";

    public static final String LOCALHOST = "localhost";

    public static SSLContext serverContext() {
        try {
            //System.setProperty("javax.net.ssl.trustStore", ClassLoader.getSystemResource(CLIENT_STORE).getPath());
            InputStream stream = FixedCertificates.class.getResourceAsStream("/" + SERVER_STORE);
            assert stream != null;
            return newSSLContext(stream, SERVER_PASSWORD, "PKCS12", "sunx509");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static SSLContext clientContext() {
        try {
            InputStream stream = FixedCertificates.class.getResourceAsStream("/" + CLIENT_STORE);
            assert stream != null;
            SSLContext context = newSSLContext(stream, CLIENT_PASSWORD, "PKCS12", "sunx509");
            context.init(null, new TrustManager[] { new X509TrustManagerTrustAll() }, new SecureRandom());
            return context;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String clientCertPath() {
        return ClassLoader.getSystemResource(CLIENT_STORE).getPath();
    }

    public static SSLSocketFactory newSSLSocketFactory(final InputStream cert, final String password,
                                                       final String ksType, final String ksAlgorithm) throws InvalidSSLConfig {
        final SSLContext context = newSSLContext(cert, password, ksType, ksAlgorithm);
        return context.getSocketFactory();
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
            tmf.init((KeyStore)null);

            // Get the SSLContext to help create SSLSocketFactory
            final SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
            return sslContext;
        } catch (final GeneralSecurityException e) {
            throw new InvalidSSLConfig(e);
        }
    }

    static class X509TrustManagerTrustAll implements X509TrustManager {
        public boolean checkClientTrusted(java.security.cert.X509Certificate[] chain){
            return true;
        }

        public boolean isServerTrusted(java.security.cert.X509Certificate[] chain){
            return true;
        }

        public boolean isClientTrusted(java.security.cert.X509Certificate[] chain){
            return true;
        }

        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {}

        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {}
    }

}