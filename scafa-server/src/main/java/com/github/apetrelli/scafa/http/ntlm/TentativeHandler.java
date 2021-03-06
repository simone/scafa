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
package com.github.apetrelli.scafa.http.ntlm;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.List;
import java.util.Map;

import com.github.apetrelli.scafa.util.HttpUtils;

public class TentativeHandler extends CapturingHandler {

    private boolean needsAuthorizing = false;

    private boolean onlyCaptureMode = false;

    private ByteBuffer buffer = ByteBuffer.allocate(16384);

    private AsynchronousSocketChannel sourceChannel;

    public TentativeHandler(AsynchronousSocketChannel sourceChannel) {
        this.sourceChannel = sourceChannel;
    }

    public void reset() {
        needsAuthorizing = false;
        onlyCaptureMode = false;
        super.reset();
    }

    public boolean isNeedsAuthorizing() {
        return needsAuthorizing;
    }

    public void setOnlyCaptureMode(boolean onlyCaptureMode) {
        this.onlyCaptureMode = onlyCaptureMode;
    }

    @Override
    public void onResponseHeader(String httpVersion, int responseCode, String responseMessage,
            Map<String, List<String>> headers) throws IOException {
        super.onResponseHeader(httpVersion, responseCode, responseMessage, headers);
        if (onlyCaptureMode || responseCode == 407) {
            needsAuthorizing = true;
        } else {
            HttpUtils.sendHeader(httpVersion + " " + responseCode + " " + responseMessage, headers, buffer,
                    sourceChannel);
        }
    }

    @Override
    public void onBody(ByteBuffer buffer, long offset, long length) throws IOException {
        if (needsAuthorizing) {
            super.onBody(buffer, offset, length);
        } else {
            HttpUtils.getFuture(sourceChannel.write(buffer));
        }
    }

    @Override
    public void onChunkStart(long totalOffset, long chunkLength) throws IOException {
        if (needsAuthorizing) {
            super.onChunkStart(totalOffset, chunkLength);
        } else {
            HttpUtils.sendChunkSize(chunkLength, buffer, sourceChannel);
        }
    }

    @Override
    public void onChunk(byte[] buffer, int position, int length, long totalOffset, long chunkOffset, long chunkLength)
            throws IOException {
        if (needsAuthorizing) {
            super.onChunk(buffer, position, length, totalOffset, chunkOffset, chunkLength);
        } else {
            ByteBuffer readBuffer = ByteBuffer.wrap(buffer, position, length);
            sourceChannel.write(readBuffer);
        }
    }

    @Override
    public void onChunkEnd() throws IOException {
        if (needsAuthorizing) {
            super.onChunkEnd();
        } else {
            HttpUtils.sendNewline(buffer, sourceChannel);
        }
    }

    @Override
    public void onChunkedTransferEnd() throws IOException {
        if (needsAuthorizing) {
            super.onChunkedTransferEnd();
        } else {
            HttpUtils.sendNewline(buffer, sourceChannel);
        }
    }
}
