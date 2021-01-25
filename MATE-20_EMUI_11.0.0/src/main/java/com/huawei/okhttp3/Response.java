package com.huawei.okhttp3;

import com.huawei.motiondetection.MotionTypeApps;
import com.huawei.okhttp3.Headers;
import com.huawei.okhttp3.internal.connection.Exchange;
import com.huawei.okhttp3.internal.http.HttpHeaders;
import com.huawei.okio.Buffer;
import com.huawei.okio.BufferedSource;
import java.io.Closeable;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;

public final class Response implements Closeable {
    @Nullable
    final ResponseBody body;
    @Nullable
    private volatile CacheControl cacheControl;
    @Nullable
    final Response cacheResponse;
    final int code;
    @Nullable
    final Exchange exchange;
    @Nullable
    final Handshake handshake;
    final Headers headers;
    final String message;
    @Nullable
    final Response networkResponse;
    @Nullable
    final Response priorResponse;
    final Protocol protocol;
    final long receivedResponseAtMillis;
    final Request request;
    final long sentRequestAtMillis;

    Response(Builder builder) {
        this.request = builder.request;
        this.protocol = builder.protocol;
        this.code = builder.code;
        this.message = builder.message;
        this.handshake = builder.handshake;
        this.headers = builder.headers.build();
        this.body = builder.body;
        this.networkResponse = builder.networkResponse;
        this.cacheResponse = builder.cacheResponse;
        this.priorResponse = builder.priorResponse;
        this.sentRequestAtMillis = builder.sentRequestAtMillis;
        this.receivedResponseAtMillis = builder.receivedResponseAtMillis;
        this.exchange = builder.exchange;
    }

    public Request request() {
        return this.request;
    }

    public Protocol protocol() {
        return this.protocol;
    }

    public int code() {
        return this.code;
    }

    public boolean isSuccessful() {
        int i = this.code;
        return i >= 200 && i < 300;
    }

    public String message() {
        return this.message;
    }

    @Nullable
    public Handshake handshake() {
        return this.handshake;
    }

    public List<String> headers(String name) {
        return this.headers.values(name);
    }

    @Nullable
    public String header(String name) {
        return header(name, null);
    }

    @Nullable
    public String header(String name, @Nullable String defaultValue) {
        String result = this.headers.get(name);
        return result != null ? result : defaultValue;
    }

    public Headers headers() {
        return this.headers;
    }

    public Headers trailers() throws IOException {
        Exchange exchange2 = this.exchange;
        if (exchange2 != null) {
            return exchange2.trailers();
        }
        throw new IllegalStateException("trailers not available");
    }

    public ResponseBody peekBody(long byteCount) throws IOException {
        BufferedSource peeked = this.body.source().peek();
        Buffer buffer = new Buffer();
        peeked.request(byteCount);
        buffer.write(peeked, Math.min(byteCount, peeked.getBuffer().size()));
        return ResponseBody.create(this.body.contentType(), buffer.size(), buffer);
    }

    @Nullable
    public ResponseBody body() {
        return this.body;
    }

    public Builder newBuilder() {
        return new Builder(this);
    }

    public boolean isRedirect() {
        int i = this.code;
        if (i == 307 || i == 308) {
            return true;
        }
        switch (i) {
            case 300:
            case MotionTypeApps.TYPE_PROXIMITY_ANSWER /* 301 */:
            case MotionTypeApps.TYPE_PROXIMITY_DIAL /* 302 */:
            case MotionTypeApps.TYPE_PROXIMITY_SCREEN_OFF /* 303 */:
                return true;
            default:
                return false;
        }
    }

    @Nullable
    public Response networkResponse() {
        return this.networkResponse;
    }

    @Nullable
    public Response cacheResponse() {
        return this.cacheResponse;
    }

    @Nullable
    public Response priorResponse() {
        return this.priorResponse;
    }

    public List<Challenge> challenges() {
        String responseField;
        int i = this.code;
        if (i == 401) {
            responseField = "WWW-Authenticate";
        } else if (i != 407) {
            return Collections.emptyList();
        } else {
            responseField = "Proxy-Authenticate";
        }
        return HttpHeaders.parseChallenges(headers(), responseField);
    }

    public CacheControl cacheControl() {
        CacheControl result = this.cacheControl;
        if (result != null) {
            return result;
        }
        CacheControl parse = CacheControl.parse(this.headers);
        this.cacheControl = parse;
        return parse;
    }

    public long sentRequestAtMillis() {
        return this.sentRequestAtMillis;
    }

    public long receivedResponseAtMillis() {
        return this.receivedResponseAtMillis;
    }

    @Override // java.io.Closeable, java.lang.AutoCloseable
    public void close() {
        ResponseBody responseBody = this.body;
        if (responseBody != null) {
            responseBody.close();
            return;
        }
        throw new IllegalStateException("response is not eligible for a body and must not be closed");
    }

    @Override // java.lang.Object
    public String toString() {
        return "Response{protocol=" + this.protocol + ", code=" + this.code + ", message=" + this.message + ", url=" + this.request.url() + '}';
    }

    public static class Builder {
        @Nullable
        ResponseBody body;
        @Nullable
        Response cacheResponse;
        int code;
        @Nullable
        Exchange exchange;
        @Nullable
        Handshake handshake;
        Headers.Builder headers;
        String message;
        @Nullable
        Response networkResponse;
        @Nullable
        Response priorResponse;
        @Nullable
        Protocol protocol;
        long receivedResponseAtMillis;
        @Nullable
        Request request;
        long sentRequestAtMillis;

        public Builder() {
            this.code = -1;
            this.headers = new Headers.Builder();
        }

        Builder(Response response) {
            this.code = -1;
            this.request = response.request;
            this.protocol = response.protocol;
            this.code = response.code;
            this.message = response.message;
            this.handshake = response.handshake;
            this.headers = response.headers.newBuilder();
            this.body = response.body;
            this.networkResponse = response.networkResponse;
            this.cacheResponse = response.cacheResponse;
            this.priorResponse = response.priorResponse;
            this.sentRequestAtMillis = response.sentRequestAtMillis;
            this.receivedResponseAtMillis = response.receivedResponseAtMillis;
            this.exchange = response.exchange;
        }

        public Builder request(Request request2) {
            this.request = request2;
            return this;
        }

        public Builder protocol(Protocol protocol2) {
            this.protocol = protocol2;
            return this;
        }

        public Builder code(int code2) {
            this.code = code2;
            return this;
        }

        public Builder message(String message2) {
            this.message = message2;
            return this;
        }

        public Builder handshake(@Nullable Handshake handshake2) {
            this.handshake = handshake2;
            return this;
        }

        public Builder header(String name, String value) {
            this.headers.set(name, value);
            return this;
        }

        public Builder addHeader(String name, String value) {
            this.headers.add(name, value);
            return this;
        }

        public Builder removeHeader(String name) {
            this.headers.removeAll(name);
            return this;
        }

        public Builder headers(Headers headers2) {
            this.headers = headers2.newBuilder();
            return this;
        }

        public Builder body(@Nullable ResponseBody body2) {
            this.body = body2;
            return this;
        }

        public Builder networkResponse(@Nullable Response networkResponse2) {
            if (networkResponse2 != null) {
                checkSupportResponse("networkResponse", networkResponse2);
            }
            this.networkResponse = networkResponse2;
            return this;
        }

        public Builder cacheResponse(@Nullable Response cacheResponse2) {
            if (cacheResponse2 != null) {
                checkSupportResponse("cacheResponse", cacheResponse2);
            }
            this.cacheResponse = cacheResponse2;
            return this;
        }

        private void checkSupportResponse(String name, Response response) {
            if (response.body != null) {
                throw new IllegalArgumentException(name + ".body != null");
            } else if (response.networkResponse != null) {
                throw new IllegalArgumentException(name + ".networkResponse != null");
            } else if (response.cacheResponse != null) {
                throw new IllegalArgumentException(name + ".cacheResponse != null");
            } else if (response.priorResponse != null) {
                throw new IllegalArgumentException(name + ".priorResponse != null");
            }
        }

        public Builder priorResponse(@Nullable Response priorResponse2) {
            if (priorResponse2 != null) {
                checkPriorResponse(priorResponse2);
            }
            this.priorResponse = priorResponse2;
            return this;
        }

        private void checkPriorResponse(Response response) {
            if (response.body != null) {
                throw new IllegalArgumentException("priorResponse.body != null");
            }
        }

        public Builder sentRequestAtMillis(long sentRequestAtMillis2) {
            this.sentRequestAtMillis = sentRequestAtMillis2;
            return this;
        }

        public Builder receivedResponseAtMillis(long receivedResponseAtMillis2) {
            this.receivedResponseAtMillis = receivedResponseAtMillis2;
            return this;
        }

        /* access modifiers changed from: package-private */
        public void initExchange(Exchange deferredTrailers) {
            this.exchange = deferredTrailers;
        }

        public Response build() {
            if (this.request == null) {
                throw new IllegalStateException("request == null");
            } else if (this.protocol == null) {
                throw new IllegalStateException("protocol == null");
            } else if (this.code < 0) {
                throw new IllegalStateException("code < 0: " + this.code);
            } else if (this.message != null) {
                return new Response(this);
            } else {
                throw new IllegalStateException("message == null");
            }
        }
    }
}
