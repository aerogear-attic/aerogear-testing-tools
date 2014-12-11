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

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.net.ssl.SSLException;
import java.io.File;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class which creates a proxy when deployed and shuts it down when undeployed.
 *
 * FIXME missing proper implementation, currently only works as pass-through proxy, but it should validate the sent
 * push messages.
 */
@Startup
@Singleton
public class ProxySetup {

    private static final Logger LOGGER = Logger.getLogger(ProxySetup.class.getName());

    /**
     * First found will be used. If none found, the {@link #DEFAULT_BIND_IP} will be used.
     */
    private static final String[] POSSIBLE_BIND_IP_PROPERTIES = { "OPENSHIFT_AEROGEAR_PUSH_IP",
            "OPENSHIFT_JBOSS_UNIFIED_PUSH_IP", "MYTESTIP_1", "OPENSHIFT_UNIFIED_PUSH_IP" };
    private static final String DEFAULT_BIND_IP = "127.0.0.1";
    // TODO add a possibility to change the port using an env property
    private static final int HTTP_PROXY_PORT = 16000;

    private HttpProxyServer server;


    private Thread performOnBackgroundThread(final Runnable runnable) {
        final Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    runnable.run();
                } finally {
                }
            }
        };
        t.start();
        return t;
    }

    public Thread doWork() {
        final Runnable runnable = new Runnable() {
            public void run() {
                final int PORT = 16001;
                // Configure SSL.
                SslContext sslCtx;
                try {
                    java.util.Map<String, String> env = System.getenv();

                    String path_to_cert = env.getOrDefault("GCM_MOCK_CRT","/tmp/gcm_mock.crt");

                    String path_to_key = env.getOrDefault("GCM_MOCK_KEY","/tmp/gcm_mock.key");

                    sslCtx = SslContext.newServerContext(new File(path_to_cert), new File(path_to_key));
                } catch (SSLException e) {
                    sslCtx = null;
                }
                // Configure the server.
                EventLoopGroup bossGroup = new NioEventLoopGroup(1);
                EventLoopGroup workerGroup = new NioEventLoopGroup();
                try {
                    ServerBootstrap b = new ServerBootstrap();
                    b.group(bossGroup, workerGroup)
                            .channel(NioServerSocketChannel.class)
                            .handler(new LoggingHandler(LogLevel.INFO))
                            .childHandler(new HttpMockingServerInitializer(sslCtx));

                    Channel ch = b.bind(PORT).sync().channel();

                    ch.closeFuture().sync();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    bossGroup.shutdownGracefully();
                    workerGroup.shutdownGracefully();
                }
                System.out.println("Background Task here");
            }
        };

        // run on background thread.
        return performOnBackgroundThread(runnable);
    }

    @PostConstruct
    public void init() {
        LOGGER.log(Level.INFO, "Initializing proxy");

        Thread t = this.doWork();
        InetSocketAddress address = resolveBindAddress();
        server = DefaultHttpProxyServer.bootstrap()
                // Include a ChainedProxyManager to make sure that MITM setting
                // overrides this
                .withAddress(address)
                        // .withManInTheMiddle(new SignedMitmManager())
                .withFiltersSource(new HttpFiltersSourceAdapter() {
                    @Override
                    public HttpFilters filterRequest(HttpRequest originalRequest) {
                        return new HttpFiltersAdapter(originalRequest) {
                            @Override
                            public HttpResponse clientToProxyRequest(
                                    HttpObject httpObject) {
                                HttpRequest request = (HttpRequest) httpObject;

                                if (request.getUri().contains("google")) {
                                    request.setUri("localhost:16001");
                                }
                                super.clientToProxyRequest(request);

                                return null;
                            }

                            @Override
                            public HttpResponse proxyToServerRequest(
                                    HttpObject httpObject) {

                                return null;
                            }

                            @Override
                            public HttpObject serverToProxyResponse(
                                    HttpObject httpObject) {
                                if (httpObject instanceof HttpResponse) {
                                    originalRequest.getMethod();
                                } else if (httpObject instanceof HttpContent) {
                                    ((HttpContent) httpObject)
                                            .content().toString(
                                            Charset.forName("UTF-8"));
                                }
                                return httpObject;
                            }

                            @Override
                            public HttpObject proxyToClientResponse(
                                    HttpObject httpObject) {
                                if (httpObject instanceof HttpResponse) {
                                    originalRequest.getMethod();
                                } else if (httpObject instanceof HttpContent) {
                                    ((HttpContent) httpObject)
                                            .content().toString(
                                            Charset.forName("UTF-8"));
                                }
                                return httpObject;
                            }
                        };
                    }
                })
                .start();

    }

    @PreDestroy
    public void destroy() {
        if (server != null) {
            server.stop();
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
