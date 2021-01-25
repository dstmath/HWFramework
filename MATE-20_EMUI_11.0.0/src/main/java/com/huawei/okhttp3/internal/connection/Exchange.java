package com.huawei.okhttp3.internal.connection;

import com.huawei.okhttp3.Call;
import com.huawei.okhttp3.EventListener;
import com.huawei.okhttp3.Headers;
import com.huawei.okhttp3.Request;
import com.huawei.okhttp3.Response;
import com.huawei.okhttp3.ResponseBody;
import com.huawei.okhttp3.internal.Internal;
import com.huawei.okhttp3.internal.http.ExchangeCodec;
import com.huawei.okhttp3.internal.http.RealResponseBody;
import com.huawei.okhttp3.internal.ws.RealWebSocket;
import com.huawei.okio.Buffer;
import com.huawei.okio.ForwardingSink;
import com.huawei.okio.ForwardingSource;
import com.huawei.okio.Okio;
import com.huawei.okio.Sink;
import com.huawei.okio.Source;
import java.io.IOException;
import java.net.ProtocolException;
import java.net.SocketException;
import javax.annotation.Nullable;

public final class Exchange {
    final Call call;
    final ExchangeCodec codec;
    private boolean duplex;
    final EventListener eventListener;
    final ExchangeFinder finder;
    final Transmitter transmitter;

    public Exchange(Transmitter transmitter2, Call call2, EventListener eventListener2, ExchangeFinder finder2, ExchangeCodec codec2) {
        this.transmitter = transmitter2;
        this.call = call2;
        this.eventListener = eventListener2;
        this.finder = finder2;
        this.codec = codec2;
    }

    public RealConnection connection() {
        return this.codec.connection();
    }

    public boolean isDuplex() {
        return this.duplex;
    }

    public void writeRequestHeaders(Request request) throws IOException {
        try {
            this.eventListener.requestHeadersStart(this.call);
            this.codec.writeRequestHeaders(request);
            this.eventListener.requestHeadersEnd(this.call, request);
        } catch (IOException e) {
            this.eventListener.requestFailed(this.call, e);
            trackFailure(e);
            throw e;
        }
    }

    public Sink createRequestBody(Request request, boolean duplex2) throws IOException {
        this.duplex = duplex2;
        long contentLength = request.body().contentLength();
        this.eventListener.requestBodyStart(this.call);
        return new RequestBodySink(this.codec.createRequestBody(request, contentLength), contentLength);
    }

    public void flushRequest() throws IOException {
        try {
            this.codec.flushRequest();
        } catch (IOException e) {
            this.eventListener.requestFailed(this.call, e);
            trackFailure(e);
            throw e;
        }
    }

    public void finishRequest() throws IOException {
        try {
            this.codec.finishRequest();
        } catch (IOException e) {
            this.eventListener.requestFailed(this.call, e);
            trackFailure(e);
            throw e;
        }
    }

    public void responseHeadersStart() {
        this.eventListener.responseHeadersStart(this.call);
    }

    @Nullable
    public Response.Builder readResponseHeaders(boolean expectContinue) throws IOException {
        try {
            Response.Builder result = this.codec.readResponseHeaders(expectContinue);
            if (result != null) {
                Internal.instance.initExchange(result, this);
            }
            return result;
        } catch (IOException e) {
            this.eventListener.responseFailed(this.call, e);
            trackFailure(e);
            throw e;
        }
    }

    public void responseHeadersEnd(Response response) {
        this.eventListener.responseHeadersEnd(this.call, response);
    }

    public ResponseBody openResponseBody(Response response) throws IOException {
        try {
            this.eventListener.responseBodyStart(this.call);
            String contentType = response.header("Content-Type");
            long contentLength = this.codec.reportedContentLength(response);
            return new RealResponseBody(contentType, contentLength, Okio.buffer(new ResponseBodySource(this.codec.openResponseBodySource(response), contentLength)));
        } catch (IOException e) {
            this.eventListener.responseFailed(this.call, e);
            trackFailure(e);
            throw e;
        }
    }

    public Headers trailers() throws IOException {
        return this.codec.trailers();
    }

    public void timeoutEarlyExit() {
        this.transmitter.timeoutEarlyExit();
    }

    public RealWebSocket.Streams newWebSocketStreams() throws SocketException {
        this.transmitter.timeoutEarlyExit();
        return this.codec.connection().newWebSocketStreams(this);
    }

    public void webSocketUpgradeFailed() {
        bodyComplete(-1, true, true, null);
    }

    public void noNewExchangesOnConnection() {
        this.codec.connection().noNewExchanges();
    }

    public void cancel() {
        this.codec.cancel();
    }

    public void detachWithViolence() {
        this.codec.cancel();
        this.transmitter.exchangeMessageDone(this, true, true, null);
    }

    /* access modifiers changed from: package-private */
    public void trackFailure(IOException e) {
        this.finder.trackFailure();
        this.codec.connection().trackFailure(e);
    }

    /* access modifiers changed from: package-private */
    @Nullable
    public IOException bodyComplete(long bytesRead, boolean responseDone, boolean requestDone, @Nullable IOException e) {
        if (e != null) {
            trackFailure(e);
        }
        if (requestDone) {
            if (e != null) {
                this.eventListener.requestFailed(this.call, e);
            } else {
                this.eventListener.requestBodyEnd(this.call, bytesRead);
            }
        }
        if (responseDone) {
            if (e != null) {
                this.eventListener.responseFailed(this.call, e);
            } else {
                this.eventListener.responseBodyEnd(this.call, bytesRead);
            }
        }
        return this.transmitter.exchangeMessageDone(this, requestDone, responseDone, e);
    }

    public void noRequestBody() {
        this.transmitter.exchangeMessageDone(this, true, false, null);
    }

    private final class RequestBodySink extends ForwardingSink {
        private long bytesReceived;
        private boolean closed;
        private boolean completed;
        private long contentLength;

        RequestBodySink(Sink delegate, long contentLength2) {
            super(delegate);
            this.contentLength = contentLength2;
        }

        @Override // com.huawei.okio.ForwardingSink, com.huawei.okio.Sink
        public void write(Buffer source, long byteCount) throws IOException {
            if (!this.closed) {
                long j = this.contentLength;
                if (j == -1 || this.bytesReceived + byteCount <= j) {
                    try {
                        super.write(source, byteCount);
                        this.bytesReceived += byteCount;
                    } catch (IOException e) {
                        throw complete(e);
                    }
                } else {
                    throw new ProtocolException("expected " + this.contentLength + " bytes but received " + (this.bytesReceived + byteCount));
                }
            } else {
                throw new IllegalStateException("closed");
            }
        }

        @Override // com.huawei.okio.ForwardingSink, com.huawei.okio.Sink, java.io.Flushable
        public void flush() throws IOException {
            try {
                super.flush();
            } catch (IOException e) {
                throw complete(e);
            }
        }

        @Override // com.huawei.okio.ForwardingSink, com.huawei.okio.Sink, java.io.Closeable, java.lang.AutoCloseable
        public void close() throws IOException {
            if (!this.closed) {
                this.closed = true;
                long j = this.contentLength;
                if (j == -1 || this.bytesReceived == j) {
                    try {
                        super.close();
                        complete(null);
                    } catch (IOException e) {
                        throw complete(e);
                    }
                } else {
                    throw new ProtocolException("unexpected end of stream");
                }
            }
        }

        @Nullable
        private IOException complete(@Nullable IOException e) {
            if (this.completed) {
                return e;
            }
            this.completed = true;
            return Exchange.this.bodyComplete(this.bytesReceived, false, true, e);
        }
    }

    final class ResponseBodySource extends ForwardingSource {
        private long bytesReceived;
        private boolean closed;
        private boolean completed;
        private final long contentLength;

        ResponseBodySource(Source delegate, long contentLength2) {
            super(delegate);
            this.contentLength = contentLength2;
            if (contentLength2 == 0) {
                complete(null);
            }
        }

        @Override // com.huawei.okio.ForwardingSource, com.huawei.okio.Source
        public long read(Buffer sink, long byteCount) throws IOException {
            if (!this.closed) {
                try {
                    long read = delegate().read(sink, byteCount);
                    if (read == -1) {
                        complete(null);
                        return -1;
                    }
                    long newBytesReceived = this.bytesReceived + read;
                    if (this.contentLength != -1) {
                        if (newBytesReceived > this.contentLength) {
                            throw new ProtocolException("expected " + this.contentLength + " bytes but received " + newBytesReceived);
                        }
                    }
                    this.bytesReceived = newBytesReceived;
                    if (newBytesReceived == this.contentLength) {
                        complete(null);
                    }
                    return read;
                } catch (IOException e) {
                    throw complete(e);
                }
            } else {
                throw new IllegalStateException("closed");
            }
        }

        @Override // com.huawei.okio.ForwardingSource, com.huawei.okio.Source, java.io.Closeable, java.lang.AutoCloseable
        public void close() throws IOException {
            if (!this.closed) {
                this.closed = true;
                try {
                    super.close();
                    complete(null);
                } catch (IOException e) {
                    throw complete(e);
                }
            }
        }

        /* access modifiers changed from: package-private */
        @Nullable
        public IOException complete(@Nullable IOException e) {
            if (this.completed) {
                return e;
            }
            this.completed = true;
            return Exchange.this.bodyComplete(this.bytesReceived, true, false, e);
        }
    }
}
