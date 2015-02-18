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

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class ApnsInputStream extends DataInputStream {
    public ApnsInputStream(final InputStream inputStream) {
        super(inputStream);
    }

    byte[] readBlob() throws IOException {
        int length = readUnsignedShort();
        byte[] blob = new byte[length];
        readFully(blob);
        return blob;
    }

    ApnsInputStream readFrame() throws IOException {
        int length = readInt();
        byte[] buffer = new byte[length];
        readFully(buffer);
        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(buffer);
        return new ApnsInputStream(byteArrayInputStream);
    }

    public Item readItem() throws IOException {
        byte itemId = readByte();
        byte[] blob = readBlob();
        return new Item(itemId, blob);
    }

    public static class Item {
        public final static byte ID_DEVICE_TOKEN = 1;
        public final static byte ID_PAYLOAD = 2;
        public final static byte ID_NOTIFICATION_IDENTIFIER = 3;
        public final static byte ID_EXPIRATION_DATE = 4;
        public final static byte ID_PRIORITY = 5;
        public final static Item DEFAULT = new Item((byte)0, new byte[0]);

        private final byte itemId;
        private final byte[] blob;

        public Item(final byte itemId, final byte[] blob) {

            this.itemId = itemId;
            this.blob = blob;
        }

        public byte getItemId() {
            return itemId;
        }

        public byte[] getBlob() {
            return blob.clone();
        }

        public int getInt() { return blob.length < 4 ? 0 : ByteBuffer.wrap(blob).getInt(); }

        public byte getByte() { return blob.length < 1 ? 0 : blob[0]; }
    }
}