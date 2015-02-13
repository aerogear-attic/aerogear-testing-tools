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
package org.jboss.aerogear.unifiedpush.test;

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

import org.littleshoot.proxy.HttpFilters;
import org.littleshoot.proxy.HttpFiltersAdapter;
import org.littleshoot.proxy.HttpFiltersSourceAdapter;
import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;

import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.net.ssl.SSLException;

import java.io.File;
import java.io.FileNotFoundException;
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

    /**
     * First found will be used. If none found, the {@link #DEFAULT_BIND_IP} will be used.
     */
    private static final String[] POSSIBLE_BIND_IP_PROPERTIES = {
        "OPENSHIFT_AEROGEAR_PUSH_IP",
        "OPENSHIFT_JBOSS_UNIFIED_PUSH_IP",
        "MYTESTIP_1",
        "OPENSHIFT_UNIFIED_PUSH_IP"
    };

    private static final String DEFAULT_BIND_IP = "127.0.0.1";

    // TODO add a possibility to change the port using an env property
    private static final int HTTP_PROXY_PORT = 16000;

    private static HttpProxyServer server;

    private static BackgroundThread backgroundThread;

    public boolean isActive() {
        return server != null;
    }

    public void startProxyServer() {

        if (backgroundThread == null) {
            backgroundThread = startBackgroundThread();
        }

        server = DefaultHttpProxyServer.bootstrap()
            // Include a ChainedProxyManager to make sure that MITM setting
            // overrides this
            .withAddress(resolveBindAddress())
            // .withManInTheMiddle(new SignedMitmManager())
            .withFiltersSource(new HttpFiltersSourceAdapter() {

                @Override
                public HttpFilters filterRequest(HttpRequest originalRequest) {

                    return new HttpFiltersAdapter(originalRequest) {

                        @Override
                        public HttpResponse clientToProxyRequest(HttpObject httpObject) {

                            HttpRequest request = (HttpRequest) httpObject;

                            if (request.getUri().contains("google")) {
                                request.setUri("localhost:16001");
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

    public void stopProxyServer() {
        if (server != null) {
            server.stop();
            server = null;
            logger.log(Level.INFO, "Proxy server stopped.");
        }

        if (backgroundThread != null) {
            if (backgroundThread.isAlive() && !backgroundThread.isInterrupted()) {
                backgroundThread.closeChannel();
                backgroundThread.interrupt();
                backgroundThread = null;
                logger.log(Level.INFO, "Background thread interrupted in ProxySetup.");
            }
        }
    }

    private BackgroundThread startBackgroundThread() {

        BackgroundThread backgroundThread = new BackgroundThread();

        backgroundThread.start();

        logger.log(Level.INFO, "Background thread started in ProxySetup.");

        return backgroundThread;
    }

    private static class BackgroundThread extends Thread {

        private Channel channel;

        @Override
        public void run() {
            final int PORT = 16001;
            // Configure SSL.
            SslContext sslCtx;
            try {
                String cf = System.getProperty("gcmMockCrt");
                File certfile = new File(cf);
                String kf = System.getProperty("gcmMockKey");
                File keyfile = new File(kf);

                if (certfile.exists() == false) {
                    throw new FileNotFoundException("File " + cf + " needs to exist.");
                }

                if (keyfile.exists() == false) {
                    throw new FileNotFoundException("File " + kf + " needs to exist.");
                }
                sslCtx = SslContext.newServerContext(certfile, keyfile);
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

                channel = serverBootstrap.bind(PORT).sync().channel();

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

        String ip = DEFAULT_BIND_IP;

        for (String propertyName : POSSIBLE_BIND_IP_PROPERTIES) {
            String propertyValue = System.getenv(propertyName);
            // TODO it might be a good idea to add additional validation
            if (propertyValue != null && propertyValue.length() != 0) {
                ip = propertyValue;
                break;
            }
        }

        return new InetSocketAddress(ip, HTTP_PROXY_PORT);
    }
}
