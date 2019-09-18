package com.android.okhttp.internal.http;

import com.android.okhttp.Headers;
import com.android.okhttp.Request;
import com.android.okhttp.Response;
import com.android.okhttp.ResponseBody;
import com.android.okhttp.internal.Internal;
import com.android.okhttp.internal.Util;
import com.android.okhttp.internal.io.RealConnection;
import com.android.okhttp.okio.Buffer;
import com.android.okhttp.okio.BufferedSink;
import com.android.okhttp.okio.BufferedSource;
import com.android.okhttp.okio.ForwardingTimeout;
import com.android.okhttp.okio.Okio;
import com.android.okhttp.okio.Sink;
import com.android.okhttp.okio.Source;
import com.android.okhttp.okio.Timeout;
import java.io.EOFException;
import java.io.IOException;
import java.net.ProtocolException;
import java.util.concurrent.TimeUnit;

public final class Http1xStream implements HttpStream {
    private static final int STATE_CLOSED = 6;
    private static final int STATE_IDLE = 0;
    private static final int STATE_OPEN_REQUEST_BODY = 1;
    private static final int STATE_OPEN_RESPONSE_BODY = 4;
    private static final int STATE_READING_RESPONSE_BODY = 5;
    private static final int STATE_READ_RESPONSE_HEADERS = 3;
    private static final int STATE_WRITING_REQUEST_BODY = 2;
    private HttpEngine httpEngine;
    /* access modifiers changed from: private */
    public final BufferedSink sink;
    /* access modifiers changed from: private */
    public final BufferedSource source;
    /* access modifiers changed from: private */
    public int state = STATE_IDLE;
    /* access modifiers changed from: private */
    public final StreamAllocation streamAllocation;

    private abstract class AbstractSource implements Source {
        protected boolean closed;
        protected final ForwardingTimeout timeout;

        private AbstractSource() {
            this.timeout = new ForwardingTimeout(Http1xStream.this.source.timeout());
        }

        public Timeout timeout() {
            return this.timeout;
        }

        /* access modifiers changed from: protected */
        public final void endOfInput() throws IOException {
            if (Http1xStream.this.state == Http1xStream.STATE_READING_RESPONSE_BODY) {
                Http1xStream.this.detachTimeout(this.timeout);
                int unused = Http1xStream.this.state = Http1xStream.STATE_CLOSED;
                if (Http1xStream.this.streamAllocation != null) {
                    Http1xStream.this.streamAllocation.streamFinished(Http1xStream.this);
                    return;
                }
                return;
            }
            throw new IllegalStateException("state: " + Http1xStream.this.state);
        }

        /* access modifiers changed from: protected */
        public final void unexpectedEndOfInput() {
            if (Http1xStream.this.state != Http1xStream.STATE_CLOSED) {
                int unused = Http1xStream.this.state = Http1xStream.STATE_CLOSED;
                if (Http1xStream.this.streamAllocation != null) {
                    Http1xStream.this.streamAllocation.noNewStreams();
                    Http1xStream.this.streamAllocation.streamFinished(Http1xStream.this);
                }
            }
        }
    }

    private final class ChunkedSink implements Sink {
        private boolean closed;
        private final ForwardingTimeout timeout;

        private ChunkedSink() {
            this.timeout = new ForwardingTimeout(Http1xStream.this.sink.timeout());
        }

        public Timeout timeout() {
            return this.timeout;
        }

        public void write(Buffer source, long byteCount) throws IOException {
            if (this.closed) {
                throw new IllegalStateException("closed");
            } else if (byteCount != 0) {
                Http1xStream.this.sink.writeHexadecimalUnsignedLong(byteCount);
                Http1xStream.this.sink.writeUtf8("\r\n");
                Http1xStream.this.sink.write(source, byteCount);
                Http1xStream.this.sink.writeUtf8("\r\n");
            }
        }

        public synchronized void flush() throws IOException {
            if (!this.closed) {
                Http1xStream.this.sink.flush();
            }
        }

        public synchronized void close() throws IOException {
            if (!this.closed) {
                this.closed = true;
                Http1xStream.this.sink.writeUtf8("0\r\n\r\n");
                Http1xStream.this.detachTimeout(this.timeout);
                int unused = Http1xStream.this.state = Http1xStream.STATE_READ_RESPONSE_HEADERS;
            }
        }
    }

    private class ChunkedSource extends AbstractSource {
        private static final long NO_CHUNK_YET = -1;
        private long bytesRemainingInChunk = NO_CHUNK_YET;
        private boolean hasMoreChunks = true;
        private final HttpEngine httpEngine;

        ChunkedSource(HttpEngine httpEngine2) throws IOException {
            super();
            this.httpEngine = httpEngine2;
        }

        public long read(Buffer sink, long byteCount) throws IOException {
            if (byteCount < 0) {
                throw new IllegalArgumentException("byteCount < 0: " + byteCount);
            } else if (this.closed) {
                throw new IllegalStateException("closed");
            } else if (!this.hasMoreChunks) {
                return NO_CHUNK_YET;
            } else {
                if (this.bytesRemainingInChunk == 0 || this.bytesRemainingInChunk == NO_CHUNK_YET) {
                    readChunkSize();
                    if (!this.hasMoreChunks) {
                        return NO_CHUNK_YET;
                    }
                }
                long read = Http1xStream.this.source.read(sink, Math.min(byteCount, this.bytesRemainingInChunk));
                if (read != NO_CHUNK_YET) {
                    this.bytesRemainingInChunk -= read;
                    return read;
                }
                unexpectedEndOfInput();
                throw new ProtocolException("unexpected end of stream");
            }
        }

        private void readChunkSize() throws IOException {
            if (this.bytesRemainingInChunk != NO_CHUNK_YET) {
                Http1xStream.this.source.readUtf8LineStrict();
            }
            try {
                this.bytesRemainingInChunk = Http1xStream.this.source.readHexadecimalUnsignedLong();
                String extensions = Http1xStream.this.source.readUtf8LineStrict().trim();
                if (this.bytesRemainingInChunk < 0 || (!extensions.isEmpty() && !extensions.startsWith(";"))) {
                    throw new ProtocolException("expected chunk size and optional extensions but was \"" + this.bytesRemainingInChunk + extensions + "\"");
                } else if (this.bytesRemainingInChunk == 0) {
                    this.hasMoreChunks = false;
                    this.httpEngine.receiveHeaders(Http1xStream.this.readHeaders());
                    endOfInput();
                }
            } catch (NumberFormatException e) {
                throw new ProtocolException(e.getMessage());
            }
        }

        public void close() throws IOException {
            if (!this.closed) {
                if (this.hasMoreChunks && !Util.discard(this, 100, TimeUnit.MILLISECONDS)) {
                    unexpectedEndOfInput();
                }
                this.closed = true;
            }
        }
    }

    private final class FixedLengthSink implements Sink {
        private long bytesRemaining;
        private boolean closed;
        private final ForwardingTimeout timeout;

        private FixedLengthSink(long bytesRemaining2) {
            this.timeout = new ForwardingTimeout(Http1xStream.this.sink.timeout());
            this.bytesRemaining = bytesRemaining2;
        }

        public Timeout timeout() {
            return this.timeout;
        }

        public void write(Buffer source, long byteCount) throws IOException {
            if (!this.closed) {
                Util.checkOffsetAndCount(source.size(), 0, byteCount);
                if (byteCount <= this.bytesRemaining) {
                    Http1xStream.this.sink.write(source, byteCount);
                    this.bytesRemaining -= byteCount;
                    return;
                }
                throw new ProtocolException("expected " + this.bytesRemaining + " bytes but received " + byteCount);
            }
            throw new IllegalStateException("closed");
        }

        public void flush() throws IOException {
            if (!this.closed) {
                Http1xStream.this.sink.flush();
            }
        }

        public void close() throws IOException {
            if (!this.closed) {
                this.closed = true;
                if (this.bytesRemaining <= 0) {
                    Http1xStream.this.detachTimeout(this.timeout);
                    int unused = Http1xStream.this.state = Http1xStream.STATE_READ_RESPONSE_HEADERS;
                    return;
                }
                throw new ProtocolException("unexpected end of stream");
            }
        }
    }

    private class FixedLengthSource extends AbstractSource {
        private long bytesRemaining;

        public FixedLengthSource(long length) throws IOException {
            super();
            this.bytesRemaining = length;
            if (this.bytesRemaining == 0) {
                endOfInput();
            }
        }

        public long read(Buffer sink, long byteCount) throws IOException {
            if (byteCount < 0) {
                throw new IllegalArgumentException("byteCount < 0: " + byteCount);
            } else if (this.closed) {
                throw new IllegalStateException("closed");
            } else if (this.bytesRemaining == 0) {
                return -1;
            } else {
                long read = Http1xStream.this.source.read(sink, Math.min(this.bytesRemaining, byteCount));
                if (read != -1) {
                    this.bytesRemaining -= read;
                    if (this.bytesRemaining == 0) {
                        endOfInput();
                    }
                    return read;
                }
                unexpectedEndOfInput();
                throw new ProtocolException("unexpected end of stream");
            }
        }

        public void close() throws IOException {
            if (!this.closed) {
                if (this.bytesRemaining != 0 && !Util.discard(this, 100, TimeUnit.MILLISECONDS)) {
                    unexpectedEndOfInput();
                }
                this.closed = true;
            }
        }
    }

    private class UnknownLengthSource extends AbstractSource {
        private boolean inputExhausted;

        private UnknownLengthSource() {
            super();
        }

        public long read(Buffer sink, long byteCount) throws IOException {
            if (byteCount < 0) {
                throw new IllegalArgumentException("byteCount < 0: " + byteCount);
            } else if (this.closed) {
                throw new IllegalStateException("closed");
            } else if (this.inputExhausted) {
                return -1;
            } else {
                long read = Http1xStream.this.source.read(sink, byteCount);
                if (read != -1) {
                    return read;
                }
                this.inputExhausted = true;
                endOfInput();
                return -1;
            }
        }

        public void close() throws IOException {
            if (!this.closed) {
                if (!this.inputExhausted) {
                    unexpectedEndOfInput();
                }
                this.closed = true;
            }
        }
    }

    public Http1xStream(StreamAllocation streamAllocation2, BufferedSource source2, BufferedSink sink2) {
        this.streamAllocation = streamAllocation2;
        this.source = source2;
        this.sink = sink2;
    }

    public void setHttpEngine(HttpEngine httpEngine2) {
        this.httpEngine = httpEngine2;
    }

    public Sink createRequestBody(Request request, long contentLength) throws IOException {
        if ("chunked".equalsIgnoreCase(request.header("Transfer-Encoding"))) {
            return newChunkedSink();
        }
        if (contentLength != -1) {
            return newFixedLengthSink(contentLength);
        }
        throw new IllegalStateException("Cannot stream a request body without chunked encoding or a known content length!");
    }

    public void cancel() {
        RealConnection connection = this.streamAllocation.connection();
        if (connection != null) {
            connection.cancel();
        }
    }

    public void writeRequestHeaders(Request request) throws IOException {
        this.httpEngine.writingRequestHeaders();
        writeRequest(request.headers(), RequestLine.get(request, this.httpEngine.getConnection().getRoute().getProxy().type()));
    }

    public Response.Builder readResponseHeaders() throws IOException {
        return readResponse();
    }

    public ResponseBody openResponseBody(Response response) throws IOException {
        return new RealResponseBody(response.headers(), Okio.buffer(getTransferStream(response)));
    }

    private Source getTransferStream(Response response) throws IOException {
        if (!HttpEngine.hasBody(response)) {
            return newFixedLengthSource(0);
        }
        if ("chunked".equalsIgnoreCase(response.header("Transfer-Encoding"))) {
            return newChunkedSource(this.httpEngine);
        }
        long contentLength = OkHeaders.contentLength(response);
        if (contentLength != -1) {
            return newFixedLengthSource(contentLength);
        }
        return newUnknownLengthSource();
    }

    public boolean isClosed() {
        return this.state == STATE_CLOSED;
    }

    public void finishRequest() throws IOException {
        this.sink.flush();
    }

    public void writeRequest(Headers headers, String requestLine) throws IOException {
        if (this.state == 0) {
            this.sink.writeUtf8(requestLine).writeUtf8("\r\n");
            int size = headers.size();
            for (int i = STATE_IDLE; i < size; i += STATE_OPEN_REQUEST_BODY) {
                this.sink.writeUtf8(headers.name(i)).writeUtf8(": ").writeUtf8(headers.value(i)).writeUtf8("\r\n");
            }
            this.sink.writeUtf8("\r\n");
            this.state = STATE_OPEN_REQUEST_BODY;
            return;
        }
        throw new IllegalStateException("state: " + this.state);
    }

    public Response.Builder readResponse() throws IOException {
        StatusLine statusLine;
        Response.Builder responseBuilder;
        if (this.state == STATE_OPEN_REQUEST_BODY || this.state == STATE_READ_RESPONSE_HEADERS) {
            do {
                try {
                    statusLine = StatusLine.parse(this.source.readUtf8LineStrict());
                    responseBuilder = new Response.Builder().protocol(statusLine.protocol).code(statusLine.code).message(statusLine.message).headers(readHeaders());
                } catch (EOFException e) {
                    IOException exception = new IOException("unexpected end of stream on " + this.streamAllocation);
                    exception.initCause(e);
                    throw exception;
                }
            } while (statusLine.code == 100);
            this.state = STATE_OPEN_RESPONSE_BODY;
            return responseBuilder;
        }
        throw new IllegalStateException("state: " + this.state);
    }

    public Headers readHeaders() throws IOException {
        Headers.Builder headers = new Headers.Builder();
        while (true) {
            String readUtf8LineStrict = this.source.readUtf8LineStrict();
            String line = readUtf8LineStrict;
            if (readUtf8LineStrict.length() == 0) {
                return headers.build();
            }
            Internal.instance.addLenient(headers, line);
        }
    }

    public Sink newChunkedSink() {
        if (this.state == STATE_OPEN_REQUEST_BODY) {
            this.state = STATE_WRITING_REQUEST_BODY;
            return new ChunkedSink();
        }
        throw new IllegalStateException("state: " + this.state);
    }

    public Sink newFixedLengthSink(long contentLength) {
        if (this.state == STATE_OPEN_REQUEST_BODY) {
            this.state = STATE_WRITING_REQUEST_BODY;
            return new FixedLengthSink(contentLength);
        }
        throw new IllegalStateException("state: " + this.state);
    }

    public void writeRequestBody(RetryableSink requestBody) throws IOException {
        if (this.state == STATE_OPEN_REQUEST_BODY) {
            this.state = STATE_READ_RESPONSE_HEADERS;
            requestBody.writeToSocket(this.sink);
            return;
        }
        throw new IllegalStateException("state: " + this.state);
    }

    public Source newFixedLengthSource(long length) throws IOException {
        if (this.state == STATE_OPEN_RESPONSE_BODY) {
            this.state = STATE_READING_RESPONSE_BODY;
            return new FixedLengthSource(length);
        }
        throw new IllegalStateException("state: " + this.state);
    }

    public Source newChunkedSource(HttpEngine httpEngine2) throws IOException {
        if (this.state == STATE_OPEN_RESPONSE_BODY) {
            this.state = STATE_READING_RESPONSE_BODY;
            return new ChunkedSource(httpEngine2);
        }
        throw new IllegalStateException("state: " + this.state);
    }

    public Source newUnknownLengthSource() throws IOException {
        if (this.state != STATE_OPEN_RESPONSE_BODY) {
            throw new IllegalStateException("state: " + this.state);
        } else if (this.streamAllocation != null) {
            this.state = STATE_READING_RESPONSE_BODY;
            this.streamAllocation.noNewStreams();
            return new UnknownLengthSource();
        } else {
            throw new IllegalStateException("streamAllocation == null");
        }
    }

    /* access modifiers changed from: private */
    public void detachTimeout(ForwardingTimeout timeout) {
        Timeout oldDelegate = timeout.delegate();
        timeout.setDelegate(Timeout.NONE);
        oldDelegate.clearDeadline();
        oldDelegate.clearTimeout();
    }
}
