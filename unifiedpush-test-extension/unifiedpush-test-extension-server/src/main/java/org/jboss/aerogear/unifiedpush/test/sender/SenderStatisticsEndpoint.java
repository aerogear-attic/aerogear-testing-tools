/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc. and/or its affiliates, and individual
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
package org.jboss.aerogear.unifiedpush.test.sender;

import com.google.android.gcm.server.Message;
import org.jboss.aerogear.unifiedpush.test.SenderStatistics;
import org.jboss.aerogear.unifiedpush.test.sender.apns.ApnsServerSimulator;
import org.jboss.aerogear.unifiedpush.test.sender.gcm.GCMMessage;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by asaleh on 12/11/14.
 */
@Stateless
@TransactionAttribute
@Path("/senderStats")
public class SenderStatisticsEndpoint {

    private static final AtomicReference<SenderStatistics> senderStatisticsRef =
            new AtomicReference<SenderStatistics>(new SenderStatistics());

    public static void addGCMMessage(GCMMessage message) {
        synchronized (senderStatisticsRef) {
            SenderStatistics senderStatistics = senderStatisticsRef.get();

            Message.Builder gcmMessage = new Message.Builder();

            gcmMessage.setData(message.data);
            if (message.collapseKey != null) {
                gcmMessage.collapseKey(message.collapseKey);
            }
            if (message.delayWhileIdle != null) {
                gcmMessage.delayWhileIdle(message.delayWhileIdle);
            }
            if (message.timeToLive != null) {
                gcmMessage.timeToLive(message.timeToLive);
            }

            senderStatistics.gcmMessage = gcmMessage.build();
            senderStatistics.deviceTokens.addAll(message.registrationIds);
        }
    }

    public static void addAPNSNotification(ApnsServerSimulator.Notification notification) {
        synchronized (senderStatisticsRef) {
            SenderStatistics senderStatistics = senderStatisticsRef.get();
            senderStatistics.deviceTokens.add(ApnsServerSimulator.encodeHex(notification.getDeviceToken()));
            senderStatistics.apnsPayload = new String(notification.getPayload());
            senderStatistics.apnsExpiry = notification.getExpiry();
        }
    }

    public static void clearSenderStatistics() {
        synchronized (senderStatisticsRef) {
            senderStatisticsRef.set(new SenderStatistics());
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listAllStatistics() {
        synchronized (senderStatisticsRef) {
            return Response.ok(senderStatisticsRef.get()).build();
        }
    }

    @DELETE
    public Response resetStatistics() {
        clearSenderStatistics();
        return Response.noContent().build();
    }
}
