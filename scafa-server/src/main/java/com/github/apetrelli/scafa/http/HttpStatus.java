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

import com.github.apetrelli.scafa.server.Status;


public enum HttpStatus implements Status<HttpInput, HttpByteSink> {

    IDLE() {

        @Override
        public HttpStatus next(HttpInput input) {
            return REQUEST_LINE;
        }

        @Override
        public void out(HttpInput input, HttpByteSink sink) {
            sink.start(input);
        }
    },
    REQUEST_LINE() {

        @Override
        public HttpStatus next(HttpInput input) {
            return input.peekNextByte() == CR ? REQUEST_LINE_CR : REQUEST_LINE;
        }

        @Override
        public void out(HttpInput input, HttpByteSink sink) {
            sink.appendRequestLine(input.getBuffer().get());
        }

    },
    REQUEST_LINE_CR() {

        @Override
        public HttpStatus next(HttpInput input) {
            return REQUEST_LINE_LF;
        }

        @Override
        public void out(HttpInput input, HttpByteSink sink) {
            sink.endRequestLine(input);
        }

    },
    REQUEST_LINE_LF() {

        @Override
        public HttpStatus next(HttpInput input) {
            return input.peekNextByte() == CR ? POSSIBLE_HEADER_CR : HEADER;
        }

        @Override
        public void out(HttpInput input, HttpByteSink sink) {
            sink.beforeHeader(input.getBuffer().get());
        }

    },
    POSSIBLE_HEADER() {

        @Override
        public HttpStatus next(HttpInput input) {
            return input.peekNextByte() == CR ? POSSIBLE_HEADER_CR : HEADER;
        }

        @Override
        public void out(HttpInput input, HttpByteSink sink) {
            sink.appendHeader(input.getBuffer().get());
        }

    },
    POSSIBLE_HEADER_CR() {

        @Override
        public HttpStatus next(HttpInput input) {
            return input.getBodyMode() == HttpBodyMode.EMPTY ? SEND_HEADER_AND_END : POSSIBLE_HEADER_LF;
        }

        @Override
        public void out(HttpInput input, HttpByteSink sink) {
            sink.preEndHeader(input);
        }

    },
    SEND_HEADER_AND_END() {

        @Override
        public HttpStatus next(HttpInput input) {
            return input.isHttpConnected() ? CONNECT : IDLE;
        }

        @Override
        public void out(HttpInput input, HttpByteSink sink) throws IOException {
            sink.endHeaderAndRequest(input);
        }

    },
    POSSIBLE_HEADER_LF() {

        @Override
        public HttpStatus next(HttpInput input) {
            HttpStatus retValue = null;
            switch (input.getBodyMode()) {
            case EMPTY:
                retValue = IDLE;
                break;
            case BODY:
                retValue = HttpStatus.BODY;
                break;
            case CHUNKED:
                retValue = HttpStatus.CHUNK_COUNT;
            }
            return retValue;
        }

        @Override
        public void out(HttpInput input, HttpByteSink sink) throws IOException {
            sink.endHeader(input);
        }

    },
    HEADER() {

        @Override
        public HttpStatus next(HttpInput input) {
            return input.peekNextByte() == CR ? HEADER_CR : HEADER;
        }

        @Override
        public void out(HttpInput input, HttpByteSink sink) {
            sink.appendHeader(input.getBuffer().get());
        }

    },
    HEADER_CR() {

        @Override
        public HttpStatus next(HttpInput input) {
            return HEADER_LF;
        }

        @Override
        public void out(HttpInput input, HttpByteSink sink) {
            sink.endHeaderLine(input.getBuffer().get());
        }

    },
    HEADER_LF() {

        @Override
        public HttpStatus next(HttpInput input) {
            return input.peekNextByte() == CR ? POSSIBLE_HEADER_CR : HEADER;
        }

        @Override
        public void out(HttpInput input, HttpByteSink sink) {
            sink.beforeHeaderLine(input.getBuffer().get());
        }

    },
    BODY() {

        @Override
        public HttpStatus next(HttpInput input) {
            return input.getCountdown() > 0L ? BODY : IDLE;
        }

        @Override
        public void out(HttpInput input, HttpByteSink sink) throws IOException {
            sink.send(input);
        }

    },
    PENULTIMATE_BYTE() {

        @Override
        public HttpStatus next(HttpInput input) {
            return LAST_BYTE;
        }

        @Override
        public void out(HttpInput input, HttpByteSink sink) throws IOException {
            sink.chunkedTransferLastCr(input.getBuffer().get());
        }

    },
    LAST_BYTE() {

        @Override
        public HttpStatus next(HttpInput input) {
            return IDLE;
        }

        @Override
        public void out(HttpInput input, HttpByteSink sink) throws IOException {
            sink.chunkedTransferLastLf(input.getBuffer().get());
        }

    },
    CHUNK_COUNT() {

        @Override
        public HttpStatus next(HttpInput input) {
            return input.peekNextByte() == CR ? CHUNK_COUNT_CR : CHUNK_COUNT;
        }

        @Override
        public void out(HttpInput input, HttpByteSink sink) throws IOException {
            sink.appendChunkCount(input.getBuffer().get());
        }
    },
    CHUNK_COUNT_CR() {

        @Override
        public HttpStatus next(HttpInput input) {
            return CHUNK_COUNT_LF;
        }

        @Override
        public void out(HttpInput input, HttpByteSink sink) throws IOException {
            sink.preEndChunkCount(input.getBuffer().get());
        }

    },
    CHUNK_COUNT_LF() {

        @Override
        public HttpStatus next(HttpInput input) {
            return input.getCountdown() > 0L ? CHUNK : PENULTIMATE_BYTE;
        }

        @Override
        public void out(HttpInput input, HttpByteSink sink) throws IOException {
            sink.endChunkCount(input);
        }

    },
    CHUNK() {

        @Override
        public HttpStatus next(HttpInput input) {
            return input.getCountdown() > 0L ? CHUNK : CHUNK_CR;
        }

        @Override
        public void out(HttpInput input, HttpByteSink sink) throws IOException {
            sink.sendChunkData(input);
        }

    },
    CHUNK_CR() {

        @Override
        public HttpStatus next(HttpInput input) {
            return CHUNK_LF;
        }

        @Override
        public void out(HttpInput input, HttpByteSink sink) throws IOException {
            sink.afterEndOfChunk(input.getBuffer().get());
        }

    },
    CHUNK_LF() {

        @Override
        public HttpStatus next(HttpInput input) {
            return CHUNK_COUNT;
        }

        @Override
        public void out(HttpInput input, HttpByteSink sink) throws IOException {
            sink.beforeChunkCount(input.getBuffer().get());
        }

    },
    CONNECT() {

        @Override
        public Status<HttpInput, HttpByteSink> next(HttpInput input) {
            return CONNECT;
        }

        @Override
        public void out(HttpInput input, HttpByteSink sink) throws IOException {
            sink.send(input);
        }

    };

    private static final byte CR = 13; // LF is implicit.
}
