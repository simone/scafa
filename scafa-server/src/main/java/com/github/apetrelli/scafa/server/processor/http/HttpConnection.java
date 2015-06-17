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
package com.github.apetrelli.scafa.server.processor.http;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;


public interface HttpConnection {

    void sendHeader(String method, String url,
            Map<String, List<String>> headers, String httpVersion)
            throws IOException;

    void connect(String method, String host, int port, Map<String, List<String>> headers, String httpVersion) throws IOException;

    void send(ByteBuffer buffer) throws IOException;

    void send(byte currentByte) throws IOException;

    void end() throws IOException;

    boolean isOpen();

    void close() throws IOException;
}
