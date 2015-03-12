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
package org.jboss.aerogear.unifiedpush.test.sender;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import org.jboss.aerogear.unifiedpush.test.sender.apns.ApnsServerSimulator;
import org.jboss.aerogear.unifiedpush.test.sender.util.CertificateLoader;
import org.jboss.aerogear.unifiedpush.test.sender.gcm.HttpMockingServerInitializer;
import org.jboss.aerogear.unifiedpush.test.sender.util.InetAddressPropertyUtil;
import org.jboss.aerogear.unifiedpush.test.sender.util.PortVerifier;
import org.jboss.aerogear.unifiedpush.test.sender.util.PropertyResolver;
import org.littleshoot.proxy.HttpFilters;
import org.littleshoot.proxy.HttpFiltersAdapter;
import org.littleshoot.proxy.HttpFiltersSourceAdapter;
import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;

import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.net.ssl.SSLException;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class which creates a proxy when deployed and shuts it down when undeployed.
 *
 * FIXME missing proper implementation, currently only works as pass-through proxy, but it should validate the sent push
 * messages.
 */
@Startup
@Singleton
public class ProxySetup {

    private static final Logger logger = Logger.getLogger(ProxySetup.class.getName());

    private static final PropertyResolver<String> HTTP_PROXY_HOST = PropertyResolver
            .with("127.0.0.1", "http.proxyHost", "https.proxyHost");

    private static final PropertyResolver<Integer> HTTP_PROXY_PORT = PropertyResolver
            .with(16000, "http.proxyPort", "https.proxyPort")
            .verifyOutputWith(new PortVerifier());

    private static final PropertyResolver<Integer> GCM_MOCK_SERVER_PORT = PropertyResolver
            .with(16001, "gcm.mock.server.port")
            .verifyOutputWith(new PortVerifier());

    private static final PropertyResolver<File> GCM_CERTIFICATE_FILE = PropertyResolver
            .with(File.class, "gcm.mock.certificate.path")
            .required();

    private static final PropertyResolver<File> GCM_CERTIFICATE_KEY_FILE = PropertyResolver
            .with(File.class, "gcm.mock.certificate.password")
            .required();

    private static final PropertyResolver<InetAddress> APNS_MOCK_GATEWAY_HOST = PropertyResolver
            .with(InetAddress.getLoopbackAddress(), "custom.aerogear.apns.push.host")
            .instantiateWith(new InetAddressPropertyUtil.InetAddressInstantiator());

    private static final PropertyResolver<Integer> APNS_MOCK_GATEWAY_PORT = PropertyResolver
            .with(16002, "custom.aerogear.apns.push.port")
            .verifyOutputWith(new PortVerifier());

    private static final PropertyResolver<InetAddress> APNS_MOCK_FEEDBACK_HOST = PropertyResolver
            .with(InetAddress.getLoopbackAddress(), "custom.aerogear.apns.feedback.host")
            .instantiateWith(new InetAddressPropertyUtil.InetAddressInstantiator());

    private static final PropertyResolver<Integer> APNS_MOCK_FEEDBACK_PORT = PropertyResolver
            .with(16003, "custom.aerogear.apns.feedback.port")
            .verifyOutputWith(new PortVerifier());

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    private ApnsServerSimulator apnsServerSimulator;

    private static HttpProxyServer server;

    private static BackgroundThread backgroundThread;

    public boolean isActive() {
        return server != null;
    }

    public void startProxyServer() {
        if (backgroundThread == null) {
            backgroundThread = startBackgroundThread();
        }

        apnsServerSimulator = ApnsServerSimulator.prepareAndStart(
                CertificateLoader.apnsSocketFactory(),
                APNS_MOCK_GATEWAY_HOST.resolve(),
                APNS_MOCK_GATEWAY_PORT.resolve(),
                APNS_MOCK_FEEDBACK_HOST.resolve(),
                APNS_MOCK_FEEDBACK_PORT.resolve());

        server = DefaultHttpProxyServer.bootstrap()
                .withAddress(resolveBindAddress())
                .withFiltersSource(new HttpFiltersSourceAdapter() {

                    @Override
                    public HttpFilters filterRequest(HttpRequest originalRequest) {

                        return new HttpFiltersAdapter(originalRequest) {

                            @Override
                            public HttpResponse clientToProxyRequest(HttpObject httpObject) {

                                HttpRequest request = (HttpRequest) httpObject;

                                if (request.getUri().contains("google")) {
                                    request.setUri("localhost:" + backgroundThread.getGcmMockServePort());
                                }

                                super.clientToProxyRequest(request);

                                return null;
                            }

                            @Override
                            public HttpResponse proxyToServerRequest(HttpObject httpObject) {
                                return null;
                            }

                            @Override
                            public HttpObject serverToProxyResponse(HttpObject httpObject) {

                                if (httpObject instanceof HttpResponse) {
                                    originalRequest.getMethod();
                                } else if (httpObject instanceof HttpContent) {
                                    ((HttpContent) httpObject).content().toString(Charset.forName("UTF-8"));
                                }

                                return httpObject;
                            }

                            @Override
                            public HttpObject proxyToClientResponse(HttpObject httpObject) {

                                if (httpObject instanceof HttpResponse) {
                                    originalRequest.getMethod();
                                } else if (httpObject instanceof HttpContent) {
                                    ((HttpContent) httpObject).content().toString(Charset.forName("UTF-8"));
                                }

                                return httpObject;
                            }
                        };
                    }
                })
                .start();

        logger.log(Level.INFO, "Proxy server started.");
    }

    @PreDestroy
    public void stopProxyServer() {
        if (server != null) {
            server.stop();
            server = null;
            logger.log(Level.INFO, "Proxy server stopped.");
        }

        if (apnsServerSimulator != null) {
            apnsServerSimulator.stop();
            apnsServerSimulator = null;
            logger.log(Level.INFO, "APNS mock server stopped.");
        }

        if (backgroundThread != null) {
            if (backgroundThread.isAlive() && !backgroundThread.isInterrupted()) {
                backgroundThread.closeChannel();
                backgroundThread.interrupt();
                logger.log(Level.INFO, "Background thread interrupted in ProxySetup.");
            }
            backgroundThread = null;
        }
    }

    private BackgroundThread startBackgroundThread() {

        int gcmMockServePort = GCM_MOCK_SERVER_PORT.resolve();

        BackgroundThread backgroundThread = new BackgroundThread(gcmMockServePort);

        backgroundThread.start();

        logger.log(Level.INFO, "Background thread started in ProxySetup.");

        return backgroundThread;
    }

    private static class BackgroundThread extends Thread {

        private final int gcmMockServePort;

        private Channel channel;

        public BackgroundThread(int gcmMockServePort) {
            this.gcmMockServePort = gcmMockServePort;
        }

        public int getGcmMockServePort() {
            return gcmMockServePort;
        }

        @Override
        public void run() {
            // Configure SSL.
            SslContext sslCtx;
            try {
                File certificateFile = GCM_CERTIFICATE_FILE.resolve();
                File certificateKeyFile = GCM_CERTIFICATE_KEY_FILE.resolve();

                if (!certificateFile.exists()) {
                    throw new FileNotFoundException("File " + certificateFile.getAbsolutePath() + " needs to exist.");
                }

                if (!certificateKeyFile.exists()) {
                    throw new FileNotFoundException("File " + certificateKeyFile.getAbsolutePath() + " needs to exist.");
                }
                sslCtx = SslContext.newServerContext(certificateFile, certificateKeyFile);
            } catch (SSLException e) {
                sslCtx = null;
            } catch (FileNotFoundException e) {
                e.printStackTrace();

                sslCtx = null;
            }
            // Configure the server.
            EventLoopGroup bossGroup = new NioEventLoopGroup(1);
            EventLoopGroup workerGroup = new NioEventLoopGroup();

            try {
                ServerBootstrap serverBootstrap = new ServerBootstrap();
                serverBootstrap.group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .handler(new LoggingHandler(LogLevel.INFO))
                        .childHandler(new HttpMockingServerInitializer(sslCtx));

                channel = serverBootstrap.bind(HTTP_PROXY_HOST.resolve(), gcmMockServePort).sync().channel();

                channel.closeFuture().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
            }
        }

        public void closeChannel() {

            if (channel != null) {

                try {
                    channel.close().get();
                } catch (Exception ex) {
                    logger.log(Level.INFO, "Error while closing a channel.", ex.getCause());
                } finally {
                    channel = null;
                }
            }
        }

    }

    private InetSocketAddress resolveBindAddress() {
        return new InetSocketAddress(HTTP_PROXY_HOST.resolve(), HTTP_PROXY_PORT.resolve());
    }
}
