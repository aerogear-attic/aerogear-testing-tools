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

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Wrap some of the boilerplate code using socket, enable passing around a socket together with its streams.
 */
public class InputOutputSocket {
    private final Socket socket;
    private final ApnsInputStream inputStream;
    private final DataOutputStream outputStream;

    public InputOutputSocket(final Socket socket) throws IOException {
        if (socket == null) {
            throw new NullPointerException("socket may not be null");
        }

        this.socket = socket;

        // Hack, work around JVM deadlock ... https://community.oracle.com/message/10989561#10989561
        socket.setSoLinger(true, 1);
        outputStream = new DataOutputStream(socket.getOutputStream());
        inputStream = new ApnsInputStream(socket.getInputStream());
    }

    public Socket getSocket() {
        return socket;
    }

    public ApnsInputStream getInputStream() {
        return inputStream;
    }

    /*
    public DataOutputStream getOutputStream() {
        return outputStream;
    }
    */



    public synchronized void close() {
        try {
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Write data to the output stream while synchronized against close(). This hopefully fixes
     * sporadic test failures caused by a deadlock of write() and close()
     * @param bytes The data to write
     * @throws IOException if an error occurs
     */
    public void syncWrite(byte[] bytes) throws IOException {
        outputStream.write(bytes);
        outputStream.flush();
    }
}