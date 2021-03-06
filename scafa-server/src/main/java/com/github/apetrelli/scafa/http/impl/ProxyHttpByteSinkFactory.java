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
package com.github.apetrelli.scafa.http.impl;

import java.nio.channels.AsynchronousSocketChannel;

import com.github.apetrelli.scafa.config.Configuration;
import com.github.apetrelli.scafa.http.HttpByteSink;
import com.github.apetrelli.scafa.http.HttpInput;
import com.github.apetrelli.scafa.processor.ByteSinkFactory;

public class ProxyHttpByteSinkFactory implements ByteSinkFactory<HttpInput, HttpByteSink> {

    private Configuration configuration;

    public ProxyHttpByteSinkFactory(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public HttpByteSink create(AsynchronousSocketChannel client) {
        return new ProxyHttpByteSink(client, new DefaultProxyHttpHandler(
                new DefaultHttpConnectionFactory(configuration), client));
    }

}
