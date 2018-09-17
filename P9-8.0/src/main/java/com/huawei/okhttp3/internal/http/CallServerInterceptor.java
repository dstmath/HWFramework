package com.huawei.okhttp3.internal.http;

import com.huawei.okhttp3.Interceptor;
import com.huawei.okhttp3.Interceptor.Chain;
import com.huawei.okhttp3.Request;
import com.huawei.okhttp3.Response;
import com.huawei.okhttp3.internal.Util;
import com.huawei.okhttp3.internal.connection.StreamAllocation;
import com.huawei.okio.BufferedSink;
import com.huawei.okio.Okio;
import java.io.IOException;
import java.net.ProtocolException;

public final class CallServerInterceptor implements Interceptor {
    private final boolean forWebSocket;

    public CallServerInterceptor(boolean forWebSocket) {
        this.forWebSocket = forWebSocket;
    }

    public Response intercept(Chain chain) throws IOException {
        HttpCodec httpCodec = ((RealInterceptorChain) chain).httpStream();
        StreamAllocation streamAllocation = ((RealInterceptorChain) chain).streamAllocation();
        Request request = chain.request();
        long sentRequestMillis = System.currentTimeMillis();
        httpCodec.writeRequestHeaders(request);
        if (HttpMethod.permitsRequestBody(request.method()) && request.body() != null) {
            BufferedSink bufferedRequestBody = Okio.buffer(httpCodec.createRequestBody(request, request.body().contentLength()));
            request.body().writeTo(bufferedRequestBody);
            bufferedRequestBody.close();
        }
        httpCodec.finishRequest();
        Response response = httpCodec.readResponseHeaders().request(request).handshake(streamAllocation.connection().handshake()).sentRequestAtMillis(sentRequestMillis).receivedResponseAtMillis(System.currentTimeMillis()).build();
        int code = response.code();
        if (this.forWebSocket && code == 101) {
            response = response.newBuilder().body(Util.EMPTY_RESPONSE).build();
        } else {
            response = response.newBuilder().body(httpCodec.openResponseBody(response)).build();
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
