/**
 * Scafa - Universal roadwarrior non-caching proxy
 * Copyright (C) 2015  Antonio Petrelli
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.apetrelli.scafa.http.impl;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.apetrelli.scafa.http.HttpConnection;
import com.github.apetrelli.scafa.http.HttpConnectionFactory;

public abstract class AbstractHttpConnection implements HttpConnection {

    protected static <T> T getFuture(Future<T> future) throws IOException {
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new IOException("Future problem", e);
        }
    }

    private static final Logger LOG = Logger.getLogger(AbstractHttpConnection.class.getName());

    protected static final byte CR = 13;

    protected static final byte LF = 10;

    protected static final byte SPACE = 32;

    private static final byte COLON = 58;

    private static final byte A_UPPER = 65;

    private static final byte Z_UPPER = 90;

    private static final byte A_LOWER = 97;

    private static final byte Z_LOWER = 122;

    private static final byte CAPITALIZE_CONST = A_LOWER - A_UPPER;
    
    protected HttpConnectionFactory factory;

    protected AsynchronousSocketChannel channel, sourceChannel;

    protected ByteBuffer buffer = ByteBuffer.allocate(16384);

    protected ByteBuffer readBuffer = ByteBuffer.allocate(16384);

    public AbstractHttpConnection(HttpConnectionFactory factory,
            AsynchronousSocketChannel sourceChannel)
            throws IOException {
        this.factory = factory;
        this.sourceChannel = sourceChannel;
        channel = AsynchronousSocketChannel.open();
    }

    @Override
    public void send(ByteBuffer buffer) throws IOException {
        getFuture(channel.write(buffer));
    }

    @Override
    public void end() throws IOException {
        // Does nothing.
    }

    @Override
    public boolean isOpen() {
        return channel.isOpen();
    }

    @Override
    public void close() throws IOException {
        if (channel.isOpen()) {
            channel.close();
        }
    }

    protected void prepareChannel(HttpConnectionFactory factory, AsynchronousSocketChannel sourceChannel,
            HostPort socketAddress) throws IOException {
        SocketAddress source = sourceChannel.getRemoteAddress();
        channel.read(readBuffer, readBuffer, new CompletionHandler<Integer, ByteBuffer>() {

            @Override
            public void completed(Integer result, ByteBuffer attachment) {
                if (result >= 0) {
                    attachment.flip();
                    try {
                        sourceChannel.write(attachment).get();
                        attachment.clear();
                        channel.read(attachment, attachment, this);
                    } catch (InterruptedException | ExecutionException e) {
                        failed(e, attachment);
                    }
                } else {
                    try {
                        factory.dispose(source, socketAddress);
                    } catch (IOException e) {
                        failed(e, attachment);
                    }
                }
            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {
                LOG.log(Level.SEVERE, "Error when writing to source", exc);
            }
        });
    }

    protected Integer sendHeader(String requestLine, Map<String, List<String>> headers) throws IOException {
        Charset charset = StandardCharsets.US_ASCII;
        buffer.put(requestLine.getBytes(charset)).put(CR).put(LF);
        headers.entrySet().stream().forEach(t -> {
            String key = t.getKey();
            byte[] convertedKey = putCapitalized(key);
            t.getValue().forEach(u -> {
                buffer.put(convertedKey).put(COLON).put(SPACE).put(u.getBytes(charset)).put(CR).put(LF);
            });
        });
        buffer.put(CR).put(LF);
        return flushBuffer();
    }

    private Integer flushBuffer() throws IOException {
        buffer.flip();
        if (LOG.isLoggable(Level.FINEST)) {
            int position = buffer.position();
            String request = new String(buffer.array(), position, buffer.limit() - position);
            LOG.finest("-- Real request sent to " + channel.getRemoteAddress() + " --");
            LOG.finest(request);
            LOG.finest("-- End of request --");
        }
        Integer result = getFuture(channel.write(buffer));
        buffer.clear();
        return result;
    }

    private byte[] putCapitalized(String string) {
        byte[] array = string.getBytes(StandardCharsets.US_ASCII);
        byte[] converted = new byte[array.length];
        boolean capitalize = true;
        for (int i = 0; i < array.length; i++) {
            byte currentByte = array[i];
            if (capitalize) {
                if (currentByte >= A_LOWER && currentByte <= Z_LOWER) {
                    currentByte -= CAPITALIZE_CONST;
                    capitalize = false;
                } else if (currentByte >= A_UPPER && currentByte <= Z_UPPER) {
                    capitalize = false;
                }
            } else {
                if (currentByte >= A_UPPER && currentByte <= Z_UPPER) {
                    currentByte += CAPITALIZE_CONST;
                } else if (currentByte < A_LOWER || currentByte > Z_LOWER) {
                    capitalize = true;
                }
            }
            converted[i] = currentByte;
        }
        return converted;
    }
}