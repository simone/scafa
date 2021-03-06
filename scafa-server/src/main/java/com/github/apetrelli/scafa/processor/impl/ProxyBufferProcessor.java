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
package com.github.apetrelli.scafa.processor.impl;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.apetrelli.scafa.http.HttpInput;
import com.github.apetrelli.scafa.processor.ByteSink;

public class ProxyBufferProcessor<S extends ByteSink<HttpInput>> extends AbstractBufferProcessor<HttpInput, S> {

    private static final Logger LOG = Logger.getLogger(ProxyBufferProcessor.class.getName());

    public ProxyBufferProcessor(S sink) {
        super(sink);
    }

    protected <T extends Exception> void manageException(HttpInput input, T e, String message) throws T {
        if (input.isHttpConnected()) {
            throw e;
        } else {
            LOG.log(Level.INFO, message, e);
            input.setCaughtError(true);
        }
    }

}
