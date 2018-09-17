package com.android.okhttp.internal.http;

import com.android.okhttp.Connection;
import com.android.okhttp.ConnectionPool;
import com.android.okhttp.Headers;
import com.android.okhttp.Headers.Builder;
import com.android.okhttp.Response;
import com.android.okhttp.internal.Internal;
import com.android.okhttp.internal.Util;
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
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;

public final class HttpConnection {
    private static final int ON_IDLE_CLOSE = 2;
    private static final int ON_IDLE_HOLD = 0;
    private static final int ON_IDLE_POOL = 1;
    private static final int STATE_CLOSED = 6;
    private static final int STATE_IDLE = 0;
    private static final int STATE_OPEN_REQUEST_BODY = 1;
    private static final int STATE_OPEN_RESPONSE_BODY = 4;
    private static final int STATE_READING_RESPONSE_BODY = 5;
    private static final int STATE_READ_RESPONSE_HEADERS = 3;
    private static final int STATE_WRITING_REQUEST_BODY = 2;
    private final Connection connection;
    private int onIdle;
    private final ConnectionPool pool;
    private final BufferedSink sink;
    private final Socket socket;
    private final BufferedSource source;
    private int state;

    private abstract class AbstractSource implements Source {
        protected boolean closed;
        protected final ForwardingTimeout timeout;

        private AbstractSource() {
            this.timeout = new ForwardingTimeout(HttpConnection.this.source.timeout());
        }

        public Timeout timeout() {
            return this.timeout;
        }

        protected final void endOfInput(boolean recyclable) throws IOException {
            if (HttpConnection.this.state != HttpConnection.STATE_READING_RESPONSE_BODY) {
                throw new IllegalStateException("state: " + HttpConnection.this.state);
            }
            HttpConnection.this.detachTimeout(this.timeout);
            HttpConnection.this.state = HttpConnection.STATE_IDLE;
            if (recyclable && HttpConnection.this.onIdle == HttpConnection.STATE_OPEN_REQUEST_BODY) {
                HttpConnection.this.onIdle = HttpConnection.STATE_IDLE;
                Internal.instance.recycle(HttpConnection.this.pool, HttpConnection.this.connection);
            } else if (HttpConnection.this.onIdle == HttpConnection.STATE_WRITING_REQUEST_BODY) {
                HttpConnection.this.state = HttpConnection.STATE_CLOSED;
                HttpConnection.this.connection.getSocket().close();
            }
        }

        protected final void unexpectedEndOfInput() {
            Util.closeQuietly(HttpConnection.this.connection.getSocket());
            HttpConnection.this.state = HttpConnection.STATE_CLOSED;
        }
    }

    private final class ChunkedSink implements Sink {
        private boolean closed;
        private final ForwardingTimeout timeout;

        private ChunkedSink() {
            this.timeout = new ForwardingTimeout(HttpConnection.this.sink.timeout());
        }

        public Timeout timeout() {
            return this.timeout;
        }

        public void write(Buffer source, long byteCount) throws IOException {
            if (this.closed) {
                throw new IllegalStateException("closed");
            } else if (byteCount != 0) {
                HttpConnection.this.sink.writeHexadecimalUnsignedLong(byteCount);
                HttpConnection.this.sink.writeUtf8("\r\n");
                HttpConnection.this.sink.write(source, byteCount);
                HttpConnection.this.sink.writeUtf8("\r\n");
            }
        }

        public synchronized void flush() throws IOException {
            if (!this.closed) {
                HttpConnection.this.sink.flush();
            }
        }

        public synchronized void close() throws IOException {
            if (!this.closed) {
                this.closed = true;
                HttpConnection.this.sink.writeUtf8("0\r\n\r\n");
                HttpConnection.this.detachTimeout(this.timeout);
                HttpConnection.this.state = HttpConnection.STATE_READ_RESPONSE_HEADERS;
            }
        }
    }

    private class ChunkedSource extends AbstractSource {
        private static final long NO_CHUNK_YET = -1;
        private long bytesRemainingInChunk;
        private boolean hasMoreChunks;
        private final HttpEngine httpEngine;

        ChunkedSource(HttpEngine httpEngine) throws IOException {
            super(null);
            this.bytesRemainingInChunk = NO_CHUNK_YET;
            this.hasMoreChunks = true;
            this.httpEngine = httpEngine;
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
                long read = HttpConnection.this.source.read(sink, Math.min(byteCount, this.bytesRemainingInChunk));
                if (read == NO_CHUNK_YET) {
                    unexpectedEndOfInput();
                    throw new ProtocolException("unexpected end of stream");
                }
                this.bytesRemainingInChunk -= read;
                return read;
            }
        }

        private void readChunkSize() throws IOException {
            if (this.bytesRemainingInChunk != NO_CHUNK_YET) {
                HttpConnection.this.source.readUtf8LineStrict();
            }
            try {
                this.bytesRemainingInChunk = HttpConnection.this.source.readHexadecimalUnsignedLong();
                String extensions = HttpConnection.this.source.readUtf8LineStrict().trim();
                if (this.bytesRemainingInChunk < 0 || !(extensions.isEmpty() || extensions.startsWith(";"))) {
                    throw new ProtocolException("expected chunk size and optional extensions but was \"" + this.bytesRemainingInChunk + extensions + "\"");
                } else if (this.bytesRemainingInChunk == 0) {
                    this.hasMoreChunks = false;
                    Builder trailersBuilder = new Builder();
                    HttpConnection.this.readHeaders(trailersBuilder);
                    this.httpEngine.receiveHeaders(trailersBuilder.build());
                    endOfInput(true);
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

        private FixedLengthSink(long bytesRemaining) {
            this.timeout = new ForwardingTimeout(HttpConnection.this.sink.timeout());
            this.bytesRemaining = bytesRemaining;
        }

        public Timeout timeout() {
            return this.timeout;
        }

        public void write(Buffer source, long byteCount) throws IOException {
            if (this.closed) {
                throw new IllegalStateException("closed");
            }
            Util.checkOffsetAndCount(source.size(), 0, byteCount);
            if (byteCount > this.bytesRemaining) {
                throw new ProtocolException("expected " + this.bytesRemaining + " bytes but received " + byteCount);
            }
            HttpConnection.this.sink.write(source, byteCount);
            this.bytesRemaining -= byteCount;
        }

        public void flush() throws IOException {
            if (!this.closed) {
                HttpConnection.this.sink.flush();
            }
        }

        public void close() throws IOException {
            if (!this.closed) {
                this.closed = true;
                if (this.bytesRemaining > 0) {
                    throw new ProtocolException("unexpected end of stream");
                }
                HttpConnection.this.detachTimeout(this.timeout);
                HttpConnection.this.state = HttpConnection.STATE_READ_RESPONSE_HEADERS;
            }
        }
    }

    private class FixedLengthSource extends AbstractSource {
        private long bytesRemaining;

        public FixedLengthSource(long length) throws IOException {
            super(null);
            this.bytesRemaining = length;
            if (this.bytesRemaining == 0) {
                endOfInput(true);
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
                long read = HttpConnection.this.source.read(sink, Math.min(this.bytesRemaining, byteCount));
                if (read == -1) {
                    unexpectedEndOfInput();
                    throw new ProtocolException("unexpected end of stream");
                }
                this.bytesRemaining -= read;
                if (this.bytesRemaining == 0) {
                    endOfInput(true);
                }
                return read;
            }
        }

        public void close() throws IOException {
            if (!this.closed) {
                if (!(this.bytesRemaining == 0 || Util.discard(this, 100, TimeUnit.MILLISECONDS))) {
                    unexpectedEndOfInput();
                }
                this.closed = true;
            }
        }
    }

    private class UnknownLengthSource extends AbstractSource {
        private boolean inputExhausted;

        private UnknownLengthSource() {
            super(null);
        }

        public long read(Buffer sink, long byteCount) throws IOException {
            if (byteCount < 0) {
                throw new IllegalArgumentException("byteCount < 0: " + byteCount);
            } else if (this.closed) {
                throw new IllegalStateException("closed");
            } else if (this.inputExhausted) {
                return -1;
            } else {
                long read = HttpConnection.this.source.read(sink, byteCount);
                if (read != -1) {
                    return read;
                }
                this.inputExhausted = true;
                endOfInput(false);
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

    public HttpConnection(ConnectionPool pool, Connection connection, Socket socket) throws IOException {
        this.state = STATE_IDLE;
        this.onIdle = STATE_IDLE;
        this.pool = pool;
        this.connection = connection;
        this.socket = socket;
        this.source = Okio.buffer(Okio.source(socket));
        this.sink = Okio.buffer(Okio.sink(socket));
    }

    public void setTimeouts(int readTimeoutMillis, int writeTimeoutMillis) {
        if (readTimeoutMillis != 0) {
            this.source.timeout().timeout((long) readTimeoutMillis, TimeUnit.MILLISECONDS);
        }
        if (writeTimeoutMillis != 0) {
            this.sink.timeout().timeout((long) writeTimeoutMillis, TimeUnit.MILLISECONDS);
        }
    }

    public void poolOnIdle() {
        this.onIdle = STATE_OPEN_REQUEST_BODY;
        if (this.state == 0) {
            this.onIdle = STATE_IDLE;
            Internal.instance.recycle(this.pool, this.connection);
        }
    }

    public void closeOnIdle() throws IOException {
        this.onIdle = STATE_WRITING_REQUEST_BODY;
        if (this.state == 0) {
            this.state = STATE_CLOSED;
            this.connection.getSocket().close();
        }
    }

    public boolean isClosed() {
        return this.state == STATE_CLOSED;
    }

    public void closeIfOwnedBy(Object owner) throws IOException {
        Internal.instance.closeIfOwnedBy(this.connection, owner);
    }

    public void flush() throws IOException {
        this.sink.flush();
    }

    public long bufferSize() {
        return this.source.buffer().size();
    }

    public boolean isReadable() {
        int readTimeout;
        try {
            readTimeout = this.socket.getSoTimeout();
            this.socket.setSoTimeout(STATE_OPEN_REQUEST_BODY);
            if (this.source.exhausted()) {
                this.socket.setSoTimeout(readTimeout);
                return false;
            }
            this.socket.setSoTimeout(readTimeout);
            return true;
        } catch (SocketTimeoutException e) {
            return true;
        } catch (IOException e2) {
            return false;
        } catch (Throwable th) {
            this.socket.setSoTimeout(readTimeout);
        }
    }

    public void writeRequest(Headers headers, String requestLine) throws IOException {
        if (this.state != 0) {
            throw new IllegalStateException("state: " + this.state);
        }
        this.sink.writeUtf8(requestLine).writeUtf8("\r\n");
        int size = headers.size();
        for (int i = STATE_IDLE; i < size; i += STATE_OPEN_REQUEST_BODY) {
            this.sink.writeUtf8(headers.name(i)).writeUtf8(": ").writeUtf8(headers.value(i)).writeUtf8("\r\n");
        }
        this.sink.writeUtf8("\r\n");
        this.state = STATE_OPEN_REQUEST_BODY;
    }

    public Response.Builder readResponse() throws IOException {
        if (this.state == STATE_OPEN_REQUEST_BODY || this.state == STATE_READ_RESPONSE_HEADERS) {
            Response.Builder responseBuilder;
            StatusLine statusLine;
            do {
                try {
                    statusLine = StatusLine.parse(this.source.readUtf8LineStrict());
                    responseBuilder = new Response.Builder().protocol(statusLine.protocol).code(statusLine.code).message(statusLine.message);
                    Builder headersBuilder = new Builder();
                    readHeaders(headersBuilder);
                    headersBuilder.add(OkHeaders.SELECTED_PROTOCOL, statusLine.protocol.toString());
                    responseBuilder.headers(headersBuilder.build());
                } catch (EOFException e) {
                    IOException exception = new IOException("unexpected end of stream on " + this.connection + " (recycle count=" + Internal.instance.recycleCount(this.connection) + ")");
                    exception.initCause(e);
                    throw exception;
                }
            } while (statusLine.code == 100);
            this.state = STATE_OPEN_RESPONSE_BODY;
            return responseBuilder;
        }
        throw new IllegalStateException("state: " + this.state);
    }

    public void readHeaders(Builder builder) throws IOException {
        while (true) {
            String line = this.source.readUtf8LineStrict();
            if (line.length() != 0) {
                Internal.instance.addLenient(builder, line);
            } else {
                return;
            }
        }
    }

    public Sink newChunkedSink() {
        if (this.state != STATE_OPEN_REQUEST_BODY) {
            throw new IllegalStateException("state: " + this.state);
        }
        this.state = STATE_WRITING_REQUEST_BODY;
        return new ChunkedSink();
    }

    public Sink newFixedLengthSink(long contentLength) {
        if (this.state != STATE_OPEN_REQUEST_BODY) {
            throw new IllegalStateException("state: " + this.state);
        }
        this.state = STATE_WRITING_REQUEST_BODY;
        return new FixedLengthSink(contentLength, null);
    }

    public void writeRequestBody(RetryableSink requestBody) throws IOException {
        if (this.state != STATE_OPEN_REQUEST_BODY) {
            throw new IllegalStateException("state: " + this.state);
        }
        this.state = STATE_READ_RESPONSE_HEADERS;
        requestBody.writeToSocket(this.sink);
    }

    public Source newFixedLengthSource(long length) throws IOException {
        if (this.state != STATE_OPEN_RESPONSE_BODY) {
            throw new IllegalStateException("state: " + this.state);
        }
        this.state = STATE_READING_RESPONSE_BODY;
        return new FixedLengthSource(length);
    }

    public Source newChunkedSource(HttpEngine httpEngine) throws IOException {
        if (this.state != STATE_OPEN_RESPONSE_BODY) {
            throw new IllegalStateException("state: " + this.state);
        }
        this.state = STATE_READING_RESPONSE_BODY;
        return new ChunkedSource(httpEngine);
    }

    public Source newUnknownLengthSource() throws IOException {
        if (this.state != STATE_OPEN_RESPONSE_BODY) {
            throw new IllegalStateException("state: " + this.state);
        }
        this.state = STATE_READING_RESPONSE_BODY;
        return new UnknownLengthSource();
    }

    public BufferedSink rawSink() {
        return this.sink;
    }

    public BufferedSource rawSource() {
        return this.source;
    }

    private void detachTimeout(ForwardingTimeout timeout) {
        Timeout oldDelegate = timeout.delegate();
        timeout.setDelegate(Timeout.NONE);
        oldDelegate.clearDeadline();
        oldDelegate.clearTimeout();
    }
}
