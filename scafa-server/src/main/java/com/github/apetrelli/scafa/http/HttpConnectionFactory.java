/**
 * Scafa - A universal non-caching proxy for the road warrior
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
package com.github.apetrelli.scafa.http;

import java.io.IOException;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.List;
import java.util.Map;

import com.github.apetrelli.scafa.http.impl.HostPort;

public interface HttpConnectionFactory {

    HttpConnection create(AsynchronousSocketChannel sourceChannel, String method, String url,
            Map<String, List<String>> headers, String httpVersion) throws IOException;

    HttpConnection create(AsynchronousSocketChannel sourceChannel, String method, String host, int port,
            Map<String, List<String>> headers, String httpVersion) throws IOException;

    void disconnectAll() throws IOException;

    void dispose(HostPort target);
}
