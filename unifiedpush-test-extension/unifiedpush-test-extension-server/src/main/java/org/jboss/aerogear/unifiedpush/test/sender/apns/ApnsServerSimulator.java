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

import org.jboss.aerogear.unifiedpush.test.Tokens;
import org.jboss.aerogear.unifiedpush.test.sender.SenderStatisticsEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ServerSocketFactory;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class ApnsServerSimulator {

    private static final Logger logger = LoggerFactory.getLogger(ApnsServerSimulator.class);
    private static final char base[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E',
            'F' };
    private static AtomicInteger threadNameCount = new AtomicInteger(0);

    private final Semaphore startUp = new Semaphore(0);
    private final ServerSocketFactory sslFactory;

    private final InetAddress gatewayHost;
    private final int gatewayPort;
    private final InetAddress feedbackHost;
    private final int feedbackPort;

    private int effectiveGatewayPort;
    private int effectiveFeedbackPort;

    private final List<byte[]> badTokens = new ArrayList<byte[]>();

    public ApnsServerSimulator(ServerSocketFactory sslFactory,
                               InetAddress gatewayHost, int gatewayPort,
                               InetAddress feedbackHost, int feedbackPort) {
        this.sslFactory = sslFactory;

        this.gatewayHost = gatewayHost;
        this.gatewayPort = gatewayPort;
        this.feedbackHost = feedbackHost;
        this.feedbackPort = feedbackPort;
    }

    Thread gatewayThread;
    Thread feedbackThread;
    ServerSocket gatewaySocket;
    ServerSocket feedbackSocket;

    public void start() {
        logger.debug("Starting APNSServerSimulator");
        gatewayThread = new GatewayListener();
        feedbackThread = new FeedbackRunner();
        gatewayThread.start();
        feedbackThread.start();
        startUp.acquireUninterruptibly(2);
    }

    public void stop() {
        logger.debug("Stopping APNSServerSimulator");
        try {
            if (gatewaySocket != null) {
                gatewaySocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (feedbackSocket != null) {
                feedbackSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (gatewayThread != null) {
            gatewayThread.interrupt();
        }

        if (feedbackThread != null) {
            feedbackThread.interrupt();
        }
        logger.debug("Stopped - APNSServerSimulator");

    }

    public int getEffectiveGatewayPort() {
        return effectiveGatewayPort;
    }

    public int getEffectiveFeedbackPort() {
        return effectiveFeedbackPort;
    }

    protected void fail(final byte status, final int identifier, final InputOutputSocket inputOutputSocket) throws
            IOException {
        logger.debug("FAIL {} {}", status, identifier);

        // Here comes the fun ... we need to write the feedback packet as one single packet
        // or the client will notice the connection to be closed before it read the complete packet.
        // But - only on linux, however. (I was not able to see that problem on Windows 7 or OS X)
        // What also helped was inserting a little sleep between the flush and closing the connection.
        //
        // I believe this is irregular (writing to a tcp socket then closing it should result in ALL data
        // being visible at the client) but interestingly in Netty there is (was) a similar problem:
        // https://github.com/netty/netty/issues/1952
        //
        // Funnily that appeared as somebody ported this library to use netty.
        //
        //
        //
        ByteBuffer bb = ByteBuffer.allocate(6);
        bb.put((byte) 8);
        bb.put(status);
        bb.putInt(identifier);
        inputOutputSocket.syncWrite(bb.array());
        inputOutputSocket.close();
        logger.debug("FAIL - closed");
    }

    protected void onNotification(final Notification notification, final InputOutputSocket inputOutputSocket) throws
            IOException {

        logger.info("Notification: " + notification.toString());

        SenderStatisticsEndpoint.addAPNSNotification(notification);
    }


    protected List<byte[]> getBadTokens() {
        synchronized (badTokens) {
            List<byte[]> result = new ArrayList<byte[]>(badTokens);
            badTokens.clear();
            return result;
        }
    }

    public static ApnsServerSimulator prepareAndStart(ServerSocketFactory socketFactory,
                                                      InetAddress gatewayHost, int gatewayPort,
                                                      InetAddress feedbackHost, int feedbackPort) {
        ApnsServerSimulator simulator =
                new ApnsServerSimulator(socketFactory, gatewayHost, gatewayPort, feedbackHost, feedbackPort);
        simulator.start();
        return simulator;
    }

    public static String encodeHex(final byte[] bytes) {
        final char[] chars = new char[bytes.length * 2];

        for (int i = 0; i < bytes.length; ++i) {
            final int b = (bytes[i]) & 0xFF;
            chars[2 * i] = base[b >>> 4];
            chars[2 * i + 1] = base[b & 0xF];
        }

        return new String(chars);
    }

    @SuppressWarnings("UnusedDeclaration")
    public class Notification {
        private final int type;
        private final int identifier;
        private final int expiry;
        private final byte[] deviceToken;
        private final byte[] payload;
        private final byte priority;

        public Notification(final int type, final byte[] deviceToken, final byte[] payload) {
            this(type, 0, 0, deviceToken, payload);
        }

        public Notification(final int type, final int identifier, final int expiry, final byte[] deviceToken, final
        byte[] payload) {
            this(type, identifier, expiry, deviceToken, payload, (byte) 10);

        }

        public Notification(final int type, final int identifier, final int expiry, final byte[] deviceToken, final
        byte[] payload,
                            final byte priority) {
            this.priority = priority;
            this.type = type;
            this.identifier = identifier;
            this.expiry = expiry;
            this.deviceToken = deviceToken;
            this.payload = payload;
        }

        public byte[] getPayload() {
            return payload.clone();
        }

        public byte[] getDeviceToken() {
            return deviceToken.clone();
        }

        public int getType() {
            return type;
        }

        public int getExpiry() {
            return expiry;
        }

        public int getIdentifier() {
            return identifier;
        }

        public byte getPriority() {
            return priority;
        }

        @Override
        public String toString() {
            return "Notification{" +
                    "type=" + type +
                    ", identifier=" + identifier +
                    ", expiry=" + expiry +
                    ", deviceToken=" + encodeHex(deviceToken) +
                    ", payload=" + encodeHex(payload) +
                    ", priority=" + priority +
                    '}';
        }
    }

    private class GatewayListener extends Thread {

        private GatewayListener() {
            super(new ThreadGroup("GatewayListener" + threadNameCount.incrementAndGet()), "");
            setName(getThreadGroup().getName());
        }

        public void run() {
            logger.debug("Launched " + Thread.currentThread().getName());
            try {

                try {
                    gatewaySocket = sslFactory.createServerSocket(gatewayPort, 0, gatewayHost);
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }

                effectiveGatewayPort = gatewaySocket.getLocalPort();

                // Listen for connections
                startUp.release();

                while (!isInterrupted()) {
                    try {
                        handleGatewayConnection(new InputOutputSocket(gatewaySocket.accept()));
                    } catch (SocketException ex) {
                        interrupt();
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                }
            } finally {
                logger.debug("Terminating " + Thread.currentThread().getName());
                getThreadGroup().list();
                getThreadGroup().interrupt();
            }
        }

        private void handleGatewayConnection(final InputOutputSocket inputOutputSocket) throws IOException {
            Thread gatewayConnectionTread = new Thread() {
                @Override
                public void run() {
                    try {
                        parseNotifications(inputOutputSocket);
                    } finally {
                        inputOutputSocket.close();
                    }
                }
            };
            gatewayConnectionTread.start();
        }

        private void parseNotifications(final InputOutputSocket inputOutputSocket) {
            logger.debug("Running parseNotifications {}", inputOutputSocket.getSocket());
            while (!Thread.interrupted()) {
                try {
                    final ApnsInputStream inputStream = inputOutputSocket.getInputStream();
                    byte notificationType = inputStream.readByte();
                    logger.debug("Received Notification (type {})", notificationType);
                    switch (notificationType) {
                        case 0:
                            readLegacyNotification(inputOutputSocket);
                            break;
                        case 1:
                            readEnhancedNotification(inputOutputSocket);
                            break;
                        case 2:
                            readFramedNotifications(inputOutputSocket);
                            break;
                    }
                } catch (IOException ioe) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        private void readFramedNotifications(final InputOutputSocket inputOutputSocket) throws IOException {

            Map<Byte, ApnsInputStream.Item> map = new HashMap<Byte, ApnsInputStream.Item>();

            ApnsInputStream frameStream = inputOutputSocket.getInputStream().readFrame();
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    final ApnsInputStream.Item item = frameStream.readItem();
                    map.put(item.getItemId(), item);
                }
            } catch (EOFException eof) {
                // Done reading.
            }

            byte[] deviceToken = get(map, ApnsInputStream.Item.ID_DEVICE_TOKEN).getBlob();
            byte[] payload = get(map, ApnsInputStream.Item.ID_PAYLOAD).getBlob();
            int identifier = get(map, ApnsInputStream.Item.ID_NOTIFICATION_IDENTIFIER).getInt();
            int expiry = get(map, ApnsInputStream.Item.ID_EXPIRATION_DATE).getInt();
            byte priority = get(map, ApnsInputStream.Item.ID_PRIORITY).getByte();

            final Notification notification = new Notification(2, identifier, expiry, deviceToken, payload, priority);
            logger.debug("Read framed notification {}", notification);

            resolveBadToken(deviceToken);

            onNotification(notification, inputOutputSocket);
        }

        private ApnsInputStream.Item get(final Map<Byte, ApnsInputStream.Item> map, final byte idDeviceToken) {
            ApnsInputStream.Item item = map.get(idDeviceToken);
            if (item == null) {
                item = ApnsInputStream.Item.DEFAULT;
            }
            return item;
        }

        private void readEnhancedNotification(final InputOutputSocket inputOutputSocket) throws IOException {
            ApnsInputStream inputStream = inputOutputSocket.getInputStream();

            int identifier = inputStream.readInt();
            int expiry = inputStream.readInt();
            final byte[] deviceToken = inputStream.readBlob();
            final byte[] payload = inputStream.readBlob();
            final Notification notification = new Notification(1, identifier, expiry, deviceToken, payload);
            logger.debug("Read enhanced notification {}", notification);

            resolveBadToken(deviceToken);

            onNotification(notification, inputOutputSocket);
        }

        private void readLegacyNotification(final InputOutputSocket inputOutputSocket) throws IOException {
            ApnsInputStream inputStream = inputOutputSocket.getInputStream();

            final byte[] deviceToken = inputStream.readBlob();
            final byte[] payload = inputStream.readBlob();
            final Notification notification = new Notification(0, deviceToken, payload);
            logger.debug("Read legacy notification {}", notification);

            resolveBadToken(deviceToken);

            onNotification(notification, inputOutputSocket);
        }

        private void resolveBadToken(byte[] deviceToken) {
            synchronized (badTokens) {
                String encoded = encodeHex(deviceToken);
                if (encoded.toLowerCase().startsWith(Tokens.TOKEN_INVALIDATION_PREFIX)) {
                    badTokens.add(deviceToken);
                }
            }
        }

        @Override
        public void interrupt() {
            logger.debug("Interrupt, closing socket");
            super.interrupt();
            try {
                gatewaySocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class FeedbackRunner extends Thread {

        private FeedbackRunner() {
            super(new ThreadGroup("FeedbackRunner" + threadNameCount.incrementAndGet()), "");
            setName(getThreadGroup().getName());
        }

        public void run() {
            try {
                logger.debug("Launched " + Thread.currentThread().getName());
                try {
                    feedbackSocket = sslFactory.createServerSocket(feedbackPort, 0, feedbackHost);
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }

                effectiveFeedbackPort = feedbackSocket.getLocalPort();

                startUp.release();

                while (!isInterrupted()) {
                    try {
                        handleFeedbackConnection(new InputOutputSocket(feedbackSocket.accept()));
                    } catch (SocketException ex) {
                        interrupt();
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                }
            } finally {
                logger.debug("Terminating " + Thread.currentThread().getName());
                getThreadGroup().list();
                getThreadGroup().interrupt();
            }
        }

        private void handleFeedbackConnection(final InputOutputSocket inputOutputSocket) {
            Thread feedbackConnectionTread = new Thread() {
                @Override
                public void run() {
                    try {
                        logger.debug("Feedback connection sending feedback");
                        sendFeedback(inputOutputSocket);
                    } catch (IOException ioe) {
                        // An exception is unexpected here. Close the current connection and bail out.
                        ioe.printStackTrace();
                    } finally {
                        inputOutputSocket.close();
                    }

                }
            };
            feedbackConnectionTread.start();
        }

        private void sendFeedback(final InputOutputSocket inputOutputSocket) throws IOException {
            List<byte[]> badTokens = getBadTokens();

            for (byte[] token : badTokens) {
                writeFeedback(inputOutputSocket, token);
            }

            // Write -1 to indicate a closing socket. This might be a workaround, I'm not sure if it should be done this way.
            inputOutputSocket.syncWrite(new byte[] { -1 });
        }

        private void writeFeedback(final InputOutputSocket inputOutputSocket, final byte[] token) throws IOException {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(os);
            final int unixTime = (int) (new Date().getTime() / 1000);
            dos.writeInt(unixTime);
            dos.writeShort((short) token.length);
            dos.write(token);
            dos.close();
            inputOutputSocket.syncWrite(os.toByteArray());
        }
    }

}