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

import com.github.apetrelli.scafa.processor.ByteSink;

public interface HttpByteSink extends ByteSink<HttpInput>{

    void appendRequestLine(byte currentByte);

    void endRequestLine(HttpInput input);

    void beforeHeader(byte currentByte);

    void beforeHeaderLine(byte currentByte);

    void appendHeader(byte currentByte);

    void endHeaderLine(byte currentByte);

    void preEndHeader(HttpInput input);

    void endHeader(HttpInput input) throws IOException;

    void endHeaderAndRequest(HttpInput input) throws IOException;

    void afterEndOfChunk(byte currentByte) throws IOException;

    void beforeChunkCount(byte currentByte) throws IOException;

    void appendChunkCount(byte currentByte) throws IOException;

    void preEndChunkCount(byte currentByte) throws IOException;

    void endChunkCount(HttpInput input) throws IOException;

    void chunkedTransferLastCr(byte currentByte);

    void chunkedTransferLastLf(byte currentByte);
}
