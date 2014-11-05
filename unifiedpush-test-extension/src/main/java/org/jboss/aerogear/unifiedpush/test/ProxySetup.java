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

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import org.littleshoot.proxy.HttpFilters;
import org.littleshoot.proxy.HttpFiltersAdapter;
import org.littleshoot.proxy.HttpFiltersSourceAdapter;
import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import java.net.InetSocketAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class which creates a proxy when deployed and shuts it down when undeployed.
 * <p/>
 * FIXME missing propert implementation, currently only works as pass-through proxy, but it should validate the sent
 * push messages.
 */
@Startup
@Singleton
public class ProxySetup {

    private static final Logger LOGGER = Logger.getLogger(ProxySetup.class.getName());

    /**
     * First found will be used. If none found, the {@link #DEFAULT_BIND_IP} will be used.
     */
    private static final String[] POSSIBLE_BIND_IP_PROPERTIES =
            { "OPENSHIFT_AEROGEAR_PUSH_IP", "OPENSHIFT_JBOSS_UNIFIED_PUSH_IP", "MYTESTIP_1" };
    private static final String DEFAULT_BIND_IP = "127.0.0.1";
    // TODO add a possibility to change the port using an env property
    private static final int HTTP_PROXY_PORT = 16000;

    private HttpProxyServer server;

    @PostConstruct
    public void init() {
        LOGGER.log(Level.INFO, "Initializing proxy");


        InetSocketAddress address = resolveBindAddress();

        server = DefaultHttpProxyServer.bootstrap()
                .withAddress(address)
                .withFiltersSource(new HttpFiltersSourceAdapter() {
                    public HttpFilters filterRequest(HttpRequest originalRequest, ChannelHandlerContext ctx) {
                        LOGGER.log(Level.INFO, "original request: {0}", originalRequest);

                        return new HttpFiltersAdapter(originalRequest, ctx) {
                            @Override
                            public HttpResponse requestPre(HttpObject httpObject) {
                                LOGGER.log(Level.INFO, "before requestPre {0}", httpObject);

                                HttpResponse response = super.requestPre(httpObject);

                                LOGGER.log(Level.INFO, "after requestPre {0}", httpObject);

                                return response;

                            }

                            @Override
                            public HttpResponse requestPost(HttpObject httpObject) {
                                LOGGER.log(Level.INFO, "before requestPost {0}", httpObject);

                                HttpResponse response = super.requestPost(httpObject);

                                LOGGER.log(Level.INFO, "after requestPost {0}", httpObject);

                                return response;
                            }

                            @Override
                            public HttpObject responsePre(HttpObject httpObject) {
                                LOGGER.log(Level.INFO, "before responsePre {0}", httpObject);

                                HttpObject object = super.responsePre(httpObject);

                                LOGGER.log(Level.INFO, "after responsePre {0}", httpObject);

                                return object;
                            }

                            @Override
                            public HttpObject responsePost(HttpObject httpObject) {
                                LOGGER.log(Level.INFO, "before responsePost {0}", httpObject);

                                HttpObject object = super.responsePost(httpObject);

                                LOGGER.log(Level.INFO, "after responsePost {0}", httpObject);

                                return object;
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
