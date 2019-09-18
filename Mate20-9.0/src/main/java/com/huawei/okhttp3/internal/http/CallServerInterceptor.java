package com.huawei.okhttp3.internal.http;

import com.huawei.okhttp3.Interceptor;
import com.huawei.okhttp3.Request;
import com.huawei.okhttp3.Response;
import com.huawei.okhttp3.internal.Util;
import com.huawei.okhttp3.internal.connection.RealConnection;
import com.huawei.okhttp3.internal.connection.StreamAllocation;
import com.huawei.okio.Buffer;
import com.huawei.okio.BufferedSink;
import com.huawei.okio.ForwardingSink;
import com.huawei.okio.Okio;
import com.huawei.okio.Sink;
import java.io.IOException;
import java.net.ProtocolException;

public final class CallServerInterceptor implements Interceptor {
    private final boolean forWebSocket;

    static final class CountingSink extends ForwardingSink {
        long successfulCount;

        CountingSink(Sink delegate) {
            super(delegate);
        }

        public void write(Buffer source, long byteCount) throws IOException {
            super.write(source, byteCount);
            this.successfulCount += byteCount;
        }
    }

    public CallServerInterceptor(boolean forWebSocket2) {
        this.forWebSocket = forWebSocket2;
    }

    public Response intercept(Interceptor.Chain chain) throws IOException {
        Response.Builder responseBuilder;
        Response response;
        RealInterceptorChain realChain = (RealInterceptorChain) chain;
        HttpCodec httpCodec = realChain.httpStream();
        StreamAllocation streamAllocation = realChain.streamAllocation();
        RealConnection connection = (RealConnection) realChain.connection();
        Request request = realChain.request();
        long sentRequestMillis = System.currentTimeMillis();
        realChain.eventListener().requestHeadersStart(realChain.call());
        httpCodec.writeRequestHeaders(request);
        realChain.eventListener().requestHeadersEnd(realChain.call(), request);
        Response.Builder responseBuilder2 = null;
        if (!HttpMethod.permitsRequestBody(request.method()) || request.body() == null) {
            responseBuilder = null;
        } else {
            if ("100-continue".equalsIgnoreCase(request.header("Expect"))) {
                httpCodec.flushRequest();
                realChain.eventListener().responseHeadersStart(realChain.call());
                responseBuilder2 = httpCodec.readResponseHeaders(true);
            }
            if (responseBuilder2 == null) {
                realChain.eventListener().requestBodyStart(realChain.call());
                long contentLength = request.body().contentLength();
                CountingSink requestBodyOut = new CountingSink(httpCodec.createRequestBody(request, contentLength));
                BufferedSink bufferedRequestBody = Okio.buffer((Sink) requestBodyOut);
                request.body().writeTo(bufferedRequestBody);
                bufferedRequestBody.close();
                responseBuilder = responseBuilder2;
                long j = contentLength;
                realChain.eventListener().requestBodyEnd(realChain.call(), requestBodyOut.successfulCount);
            } else {
                responseBuilder = responseBuilder2;
                if (!connection.isMultiplexed()) {
                    streamAllocation.noNewStreams();
                }
            }
        }
        httpCodec.finishRequest();
        if (responseBuilder == null) {
            realChain.eventListener().responseHeadersStart(realChain.call());
            responseBuilder = httpCodec.readResponseHeaders(false);
        }
        Response response2 = responseBuilder.request(request).handshake(streamAllocation.connection().handshake()).sentRequestAtMillis(sentRequestMillis).receivedResponseAtMillis(System.currentTimeMillis()).build();
        int code = response2.code();
        if (code == 100) {
            response2 = httpCodec.readResponseHeaders(false).request(request).handshake(streamAllocation.connection().handshake()).sentRequestAtMillis(sentRequestMillis).receivedResponseAtMillis(System.currentTimeMillis()).build();
            code = response2.code();
        }
        realChain.eventListener().responseHeadersEnd(realChain.call(), response2);
        if (!this.forWebSocket || code != 101) {
            response = response2.newBuilder().body(httpCodec.openResponseBody(response2)).build();
        } else {
            response = response2.newBuilder().body(Util.EMPTY_RESPONSE).build();
        }
        if ("close".equalsIgnoreCase(response.request().header("Connection")) || "close".equalsIgnoreCase(response.header("Connection"))) {
            streamAllocation.noNewStreams();
        }
        if ((code != 204 && code != 205) || response.body().contentLength() <= 0) {
            return response;
        }
        throw new ProtocolException("HTTP " + code + " had non-zero Content-Length: " + response.body().contentLength());
    }
}
