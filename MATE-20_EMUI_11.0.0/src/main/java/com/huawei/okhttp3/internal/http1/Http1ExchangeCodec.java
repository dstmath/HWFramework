package com.huawei.okhttp3.internal.http1;

import com.huawei.android.smcs.SmartTrimProcessEvent;
import com.huawei.android.telephony.SignalStrengthEx;
import com.huawei.okhttp3.Headers;
import com.huawei.okhttp3.HttpUrl;
import com.huawei.okhttp3.OkHttpClient;
import com.huawei.okhttp3.Request;
import com.huawei.okhttp3.Response;
import com.huawei.okhttp3.internal.Internal;
import com.huawei.okhttp3.internal.Util;
import com.huawei.okhttp3.internal.connection.RealConnection;
import com.huawei.okhttp3.internal.http.ExchangeCodec;
import com.huawei.okhttp3.internal.http.HttpHeaders;
import com.huawei.okhttp3.internal.http.RequestLine;
import com.huawei.okhttp3.internal.http.StatusLine;
import com.huawei.okio.Buffer;
import com.huawei.okio.BufferedSink;
import com.huawei.okio.BufferedSource;
import com.huawei.okio.ForwardingTimeout;
import com.huawei.okio.Sink;
import com.huawei.okio.Source;
import com.huawei.okio.Timeout;
import java.io.EOFException;
import java.io.IOException;
import java.net.ProtocolException;
import java.util.concurrent.TimeUnit;

public final class Http1ExchangeCodec implements ExchangeCodec {
    private static final int HEADER_LIMIT = 262144;
    private static final int STATE_CLOSED = 6;
    private static final int STATE_IDLE = 0;
    private static final int STATE_OPEN_REQUEST_BODY = 1;
    private static final int STATE_OPEN_RESPONSE_BODY = 4;
    private static final int STATE_READING_RESPONSE_BODY = 5;
    private static final int STATE_READ_RESPONSE_HEADERS = 3;
    private static final int STATE_WRITING_REQUEST_BODY = 2;
    private final OkHttpClient client;
    private long headerLimit = 262144;
    private final RealConnection realConnection;
    private final BufferedSink sink;
    private final BufferedSource source;
    private int state = 0;
    private Headers trailers;

    public Http1ExchangeCodec(OkHttpClient client2, RealConnection realConnection2, BufferedSource source2, BufferedSink sink2) {
        this.client = client2;
        this.realConnection = realConnection2;
        this.source = source2;
        this.sink = sink2;
    }

    @Override // com.huawei.okhttp3.internal.http.ExchangeCodec
    public RealConnection connection() {
        return this.realConnection;
    }

    @Override // com.huawei.okhttp3.internal.http.ExchangeCodec
    public Sink createRequestBody(Request request, long contentLength) throws IOException {
        if (request.body() != null && request.body().isDuplex()) {
            throw new ProtocolException("Duplex connections are not supported for HTTP/1");
        } else if ("chunked".equalsIgnoreCase(request.header("Transfer-Encoding"))) {
            return newChunkedSink();
        } else {
            if (contentLength != -1) {
                return newKnownLengthSink();
            }
            throw new IllegalStateException("Cannot stream a request body without chunked encoding or a known content length!");
        }
    }

    @Override // com.huawei.okhttp3.internal.http.ExchangeCodec
    public void cancel() {
        RealConnection realConnection2 = this.realConnection;
        if (realConnection2 != null) {
            realConnection2.cancel();
        }
    }

    @Override // com.huawei.okhttp3.internal.http.ExchangeCodec
    public void writeRequestHeaders(Request request) throws IOException {
        writeRequest(request.headers(), RequestLine.get(request, this.realConnection.route().proxy().type()));
    }

    @Override // com.huawei.okhttp3.internal.http.ExchangeCodec
    public long reportedContentLength(Response response) {
        if (!HttpHeaders.hasBody(response)) {
            return 0;
        }
        if ("chunked".equalsIgnoreCase(response.header("Transfer-Encoding"))) {
            return -1;
        }
        return HttpHeaders.contentLength(response);
    }

    @Override // com.huawei.okhttp3.internal.http.ExchangeCodec
    public Source openResponseBodySource(Response response) {
        if (!HttpHeaders.hasBody(response)) {
            return newFixedLengthSource(0);
        }
        if ("chunked".equalsIgnoreCase(response.header("Transfer-Encoding"))) {
            return newChunkedSource(response.request().url());
        }
        long contentLength = HttpHeaders.contentLength(response);
        if (contentLength != -1) {
            return newFixedLengthSource(contentLength);
        }
        return newUnknownLengthSource();
    }

    @Override // com.huawei.okhttp3.internal.http.ExchangeCodec
    public Headers trailers() {
        if (this.state == 6) {
            Headers headers = this.trailers;
            return headers != null ? headers : Util.EMPTY_HEADERS;
        }
        throw new IllegalStateException("too early; can't read the trailers yet");
    }

    public boolean isClosed() {
        return this.state == 6;
    }

    @Override // com.huawei.okhttp3.internal.http.ExchangeCodec
    public void flushRequest() throws IOException {
        this.sink.flush();
    }

    @Override // com.huawei.okhttp3.internal.http.ExchangeCodec
    public void finishRequest() throws IOException {
        this.sink.flush();
    }

    public void writeRequest(Headers headers, String requestLine) throws IOException {
        if (this.state == 0) {
            this.sink.writeUtf8(requestLine).writeUtf8("\r\n");
            int size = headers.size();
            for (int i = 0; i < size; i++) {
                this.sink.writeUtf8(headers.name(i)).writeUtf8(": ").writeUtf8(headers.value(i)).writeUtf8("\r\n");
            }
            this.sink.writeUtf8("\r\n");
            this.state = 1;
            return;
        }
        throw new IllegalStateException("state: " + this.state);
    }

    @Override // com.huawei.okhttp3.internal.http.ExchangeCodec
    public Response.Builder readResponseHeaders(boolean expectContinue) throws IOException {
        int i = this.state;
        if (i == 1 || i == 3) {
            try {
                StatusLine statusLine = StatusLine.parse(readHeaderLine());
                Response.Builder responseBuilder = new Response.Builder().protocol(statusLine.protocol).code(statusLine.code).message(statusLine.message).headers(readHeaders());
                if (expectContinue && statusLine.code == 100) {
                    return null;
                }
                if (statusLine.code == 100) {
                    this.state = 3;
                    return responseBuilder;
                }
                this.state = 4;
                return responseBuilder;
            } catch (EOFException e) {
                String address = "unknown";
                RealConnection realConnection2 = this.realConnection;
                if (realConnection2 != null) {
                    address = realConnection2.route().address().url().redact();
                }
                throw new IOException("unexpected end of stream on " + address, e);
            }
        } else {
            throw new IllegalStateException("state: " + this.state);
        }
    }

    private String readHeaderLine() throws IOException {
        String line = this.source.readUtf8LineStrict(this.headerLimit);
        this.headerLimit -= (long) line.length();
        return line;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private Headers readHeaders() throws IOException {
        Headers.Builder headers = new Headers.Builder();
        while (true) {
            String line = readHeaderLine();
            if (line.length() == 0) {
                return headers.build();
            }
            Internal.instance.addLenient(headers, line);
        }
    }

    private Sink newChunkedSink() {
        if (this.state == 1) {
            this.state = 2;
            return new ChunkedSink();
        }
        throw new IllegalStateException("state: " + this.state);
    }

    private Sink newKnownLengthSink() {
        if (this.state == 1) {
            this.state = 2;
            return new KnownLengthSink();
        }
        throw new IllegalStateException("state: " + this.state);
    }

    private Source newFixedLengthSource(long length) {
        if (this.state == 4) {
            this.state = 5;
            return new FixedLengthSource(length);
        }
        throw new IllegalStateException("state: " + this.state);
    }

    private Source newChunkedSource(HttpUrl url) {
        if (this.state == 4) {
            this.state = 5;
            return new ChunkedSource(url);
        }
        throw new IllegalStateException("state: " + this.state);
    }

    private Source newUnknownLengthSource() {
        if (this.state == 4) {
            this.state = 5;
            this.realConnection.noNewExchanges();
            return new UnknownLengthSource();
        }
        throw new IllegalStateException("state: " + this.state);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void detachTimeout(ForwardingTimeout timeout) {
        Timeout oldDelegate = timeout.delegate();
        timeout.setDelegate(Timeout.NONE);
        oldDelegate.clearDeadline();
        oldDelegate.clearTimeout();
    }

    public void skipConnectBody(Response response) throws IOException {
        long contentLength = HttpHeaders.contentLength(response);
        if (contentLength != -1) {
            Source body = newFixedLengthSource(contentLength);
            Util.skipAll(body, SignalStrengthEx.INVALID, TimeUnit.MILLISECONDS);
            body.close();
        }
    }

    /* access modifiers changed from: private */
    public final class KnownLengthSink implements Sink {
        private boolean closed;
        private final ForwardingTimeout timeout;

        private KnownLengthSink() {
            this.timeout = new ForwardingTimeout(Http1ExchangeCodec.this.sink.timeout());
        }

        @Override // com.huawei.okio.Sink
        public Timeout timeout() {
            return this.timeout;
        }

        @Override // com.huawei.okio.Sink
        public void write(Buffer source, long byteCount) throws IOException {
            if (!this.closed) {
                Util.checkOffsetAndCount(source.size(), 0, byteCount);
                Http1ExchangeCodec.this.sink.write(source, byteCount);
                return;
            }
            throw new IllegalStateException("closed");
        }

        @Override // com.huawei.okio.Sink, java.io.Flushable
        public void flush() throws IOException {
            if (!this.closed) {
                Http1ExchangeCodec.this.sink.flush();
            }
        }

        @Override // com.huawei.okio.Sink, java.io.Closeable, java.lang.AutoCloseable
        public void close() throws IOException {
            if (!this.closed) {
                this.closed = true;
                Http1ExchangeCodec.this.detachTimeout(this.timeout);
                Http1ExchangeCodec.this.state = 3;
            }
        }
    }

    /* access modifiers changed from: private */
    public final class ChunkedSink implements Sink {
        private boolean closed;
        private final ForwardingTimeout timeout = new ForwardingTimeout(Http1ExchangeCodec.this.sink.timeout());

        ChunkedSink() {
        }

        @Override // com.huawei.okio.Sink
        public Timeout timeout() {
            return this.timeout;
        }

        @Override // com.huawei.okio.Sink
        public void write(Buffer source, long byteCount) throws IOException {
            if (this.closed) {
                throw new IllegalStateException("closed");
            } else if (byteCount != 0) {
                Http1ExchangeCodec.this.sink.writeHexadecimalUnsignedLong(byteCount);
                Http1ExchangeCodec.this.sink.writeUtf8("\r\n");
                Http1ExchangeCodec.this.sink.write(source, byteCount);
                Http1ExchangeCodec.this.sink.writeUtf8("\r\n");
            }
        }

        @Override // com.huawei.okio.Sink, java.io.Flushable
        public synchronized void flush() throws IOException {
            if (!this.closed) {
                Http1ExchangeCodec.this.sink.flush();
            }
        }

        @Override // com.huawei.okio.Sink, java.io.Closeable, java.lang.AutoCloseable
        public synchronized void close() throws IOException {
            if (!this.closed) {
                this.closed = true;
                Http1ExchangeCodec.this.sink.writeUtf8("0\r\n\r\n");
                Http1ExchangeCodec.this.detachTimeout(this.timeout);
                Http1ExchangeCodec.this.state = 3;
            }
        }
    }

    private abstract class AbstractSource implements Source {
        protected boolean closed;
        protected final ForwardingTimeout timeout;

        private AbstractSource() {
            this.timeout = new ForwardingTimeout(Http1ExchangeCodec.this.source.timeout());
        }

        @Override // com.huawei.okio.Source
        public Timeout timeout() {
            return this.timeout;
        }

        @Override // com.huawei.okio.Source
        public long read(Buffer sink, long byteCount) throws IOException {
            try {
                return Http1ExchangeCodec.this.source.read(sink, byteCount);
            } catch (IOException e) {
                Http1ExchangeCodec.this.realConnection.noNewExchanges();
                responseBodyComplete();
                throw e;
            }
        }

        /* access modifiers changed from: package-private */
        public final void responseBodyComplete() {
            if (Http1ExchangeCodec.this.state != 6) {
                if (Http1ExchangeCodec.this.state == 5) {
                    Http1ExchangeCodec.this.detachTimeout(this.timeout);
                    Http1ExchangeCodec.this.state = 6;
                    return;
                }
                throw new IllegalStateException("state: " + Http1ExchangeCodec.this.state);
            }
        }
    }

    /* access modifiers changed from: private */
    public class FixedLengthSource extends AbstractSource {
        private long bytesRemaining;

        FixedLengthSource(long length) {
            super();
            this.bytesRemaining = length;
            if (this.bytesRemaining == 0) {
                responseBodyComplete();
            }
        }

        @Override // com.huawei.okhttp3.internal.http1.Http1ExchangeCodec.AbstractSource, com.huawei.okio.Source
        public long read(Buffer sink, long byteCount) throws IOException {
            if (byteCount < 0) {
                throw new IllegalArgumentException("byteCount < 0: " + byteCount);
            } else if (!this.closed) {
                long j = this.bytesRemaining;
                if (j == 0) {
                    return -1;
                }
                long read = super.read(sink, Math.min(j, byteCount));
                if (read != -1) {
                    this.bytesRemaining -= read;
                    if (this.bytesRemaining == 0) {
                        responseBodyComplete();
                    }
                    return read;
                }
                Http1ExchangeCodec.this.realConnection.noNewExchanges();
                ProtocolException e = new ProtocolException("unexpected end of stream");
                responseBodyComplete();
                throw e;
            } else {
                throw new IllegalStateException("closed");
            }
        }

        @Override // com.huawei.okio.Source, java.io.Closeable, java.lang.AutoCloseable
        public void close() throws IOException {
            if (!this.closed) {
                if (this.bytesRemaining != 0 && !Util.discard(this, 100, TimeUnit.MILLISECONDS)) {
                    Http1ExchangeCodec.this.realConnection.noNewExchanges();
                    responseBodyComplete();
                }
                this.closed = true;
            }
        }
    }

    /* access modifiers changed from: private */
    public class ChunkedSource extends AbstractSource {
        private static final long NO_CHUNK_YET = -1;
        private long bytesRemainingInChunk = NO_CHUNK_YET;
        private boolean hasMoreChunks = true;
        private final HttpUrl url;

        ChunkedSource(HttpUrl url2) {
            super();
            this.url = url2;
        }

        @Override // com.huawei.okhttp3.internal.http1.Http1ExchangeCodec.AbstractSource, com.huawei.okio.Source
        public long read(Buffer sink, long byteCount) throws IOException {
            if (byteCount < 0) {
                throw new IllegalArgumentException("byteCount < 0: " + byteCount);
            } else if (this.closed) {
                throw new IllegalStateException("closed");
            } else if (!this.hasMoreChunks) {
                return NO_CHUNK_YET;
            } else {
                long j = this.bytesRemainingInChunk;
                if (j == 0 || j == NO_CHUNK_YET) {
                    readChunkSize();
                    if (!this.hasMoreChunks) {
                        return NO_CHUNK_YET;
                    }
                }
                long read = super.read(sink, Math.min(byteCount, this.bytesRemainingInChunk));
                if (read != NO_CHUNK_YET) {
                    this.bytesRemainingInChunk -= read;
                    return read;
                }
                Http1ExchangeCodec.this.realConnection.noNewExchanges();
                ProtocolException e = new ProtocolException("unexpected end of stream");
                responseBodyComplete();
                throw e;
            }
        }

        private void readChunkSize() throws IOException {
            if (this.bytesRemainingInChunk != NO_CHUNK_YET) {
                Http1ExchangeCodec.this.source.readUtf8LineStrict();
            }
            try {
                this.bytesRemainingInChunk = Http1ExchangeCodec.this.source.readHexadecimalUnsignedLong();
                String extensions = Http1ExchangeCodec.this.source.readUtf8LineStrict().trim();
                if (this.bytesRemainingInChunk < 0 || (!extensions.isEmpty() && !extensions.startsWith(SmartTrimProcessEvent.ST_EVENT_INTER_STRING_TOKEN))) {
                    throw new ProtocolException("expected chunk size and optional extensions but was \"" + this.bytesRemainingInChunk + extensions + "\"");
                } else if (this.bytesRemainingInChunk == 0) {
                    this.hasMoreChunks = false;
                    Http1ExchangeCodec http1ExchangeCodec = Http1ExchangeCodec.this;
                    http1ExchangeCodec.trailers = http1ExchangeCodec.readHeaders();
                    HttpHeaders.receiveHeaders(Http1ExchangeCodec.this.client.cookieJar(), this.url, Http1ExchangeCodec.this.trailers);
                    responseBodyComplete();
                }
            } catch (NumberFormatException e) {
                throw new ProtocolException(e.getMessage());
            }
        }

        @Override // com.huawei.okio.Source, java.io.Closeable, java.lang.AutoCloseable
        public void close() throws IOException {
            if (!this.closed) {
                if (this.hasMoreChunks && !Util.discard(this, 100, TimeUnit.MILLISECONDS)) {
                    Http1ExchangeCodec.this.realConnection.noNewExchanges();
                    responseBodyComplete();
                }
                this.closed = true;
            }
        }
    }

    /* access modifiers changed from: private */
    public class UnknownLengthSource extends AbstractSource {
        private boolean inputExhausted;

        private UnknownLengthSource() {
            super();
        }

        @Override // com.huawei.okhttp3.internal.http1.Http1ExchangeCodec.AbstractSource, com.huawei.okio.Source
        public long read(Buffer sink, long byteCount) throws IOException {
            if (byteCount < 0) {
                throw new IllegalArgumentException("byteCount < 0: " + byteCount);
            } else if (this.closed) {
                throw new IllegalStateException("closed");
            } else if (this.inputExhausted) {
                return -1;
            } else {
                long read = super.read(sink, byteCount);
                if (read != -1) {
                    return read;
                }
                this.inputExhausted = true;
                responseBodyComplete();
                return -1;
            }
        }

        @Override // com.huawei.okio.Source, java.io.Closeable, java.lang.AutoCloseable
        public void close() throws IOException {
            if (!this.closed) {
                if (!this.inputExhausted) {
                    responseBodyComplete();
                }
                this.closed = true;
            }
        }
    }
}
