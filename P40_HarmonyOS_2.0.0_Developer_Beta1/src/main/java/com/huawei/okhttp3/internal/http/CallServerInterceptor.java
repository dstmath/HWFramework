package com.huawei.okhttp3.internal.http;

import com.huawei.okhttp3.Interceptor;
import com.huawei.okhttp3.Request;
import com.huawei.okhttp3.Response;
import com.huawei.okhttp3.internal.Util;
import com.huawei.okhttp3.internal.connection.Exchange;
import com.huawei.okio.BufferedSink;
import com.huawei.okio.Okio;
import java.io.IOException;
import java.net.ProtocolException;

@Deprecated
public final class CallServerInterceptor implements Interceptor {
    private final boolean forWebSocket;

    public CallServerInterceptor(boolean forWebSocket2) {
        this.forWebSocket = forWebSocket2;
    }

    @Override // com.huawei.okhttp3.Interceptor
    public Response intercept(Interceptor.Chain chain) throws IOException {
        Response response;
        RealInterceptorChain realChain = (RealInterceptorChain) chain;
        Exchange exchange = realChain.exchange();
        Request request = realChain.request();
        long sentRequestMillis = System.currentTimeMillis();
        exchange.writeRequestHeaders(request);
        boolean responseHeadersStarted = false;
        Response.Builder responseBuilder = null;
        if (!HttpMethod.permitsRequestBody(request.method()) || request.body() == null) {
            exchange.noRequestBody();
        } else {
            if ("100-continue".equalsIgnoreCase(request.header("Expect"))) {
                exchange.flushRequest();
                responseHeadersStarted = true;
                exchange.responseHeadersStart();
                responseBuilder = exchange.readResponseHeaders(true);
            }
            if (responseBuilder != null) {
                exchange.noRequestBody();
                if (!exchange.connection().isMultiplexed()) {
                    exchange.noNewExchangesOnConnection();
                }
            } else if (request.body().isDuplex()) {
                exchange.flushRequest();
                request.body().writeTo(Okio.buffer(exchange.createRequestBody(request, true)));
            } else {
                BufferedSink bufferedRequestBody = Okio.buffer(exchange.createRequestBody(request, false));
                request.body().writeTo(bufferedRequestBody);
                bufferedRequestBody.close();
            }
        }
        if (request.body() == null || !request.body().isDuplex()) {
            exchange.finishRequest();
        }
        if (!responseHeadersStarted) {
            exchange.responseHeadersStart();
        }
        if (responseBuilder == null) {
            responseBuilder = exchange.readResponseHeaders(false);
        }
        Response response2 = responseBuilder.request(request).handshake(exchange.connection().handshake()).sentRequestAtMillis(sentRequestMillis).receivedResponseAtMillis(System.currentTimeMillis()).build();
        int code = response2.code();
        if (code == 100) {
            response2 = exchange.readResponseHeaders(false).request(request).handshake(exchange.connection().handshake()).sentRequestAtMillis(sentRequestMillis).receivedResponseAtMillis(System.currentTimeMillis()).build();
            code = response2.code();
        }
        exchange.responseHeadersEnd(response2);
        if (!this.forWebSocket || code != 101) {
            response = response2.newBuilder().body(exchange.openResponseBody(response2)).build();
        } else {
            response = response2.newBuilder().body(Util.EMPTY_RESPONSE).build();
        }
        if ("close".equalsIgnoreCase(response.request().header("Connection")) || "close".equalsIgnoreCase(response.header("Connection"))) {
            exchange.noNewExchangesOnConnection();
        }
        if ((code != 204 && code != 205) || response.body().contentLength() <= 0) {
            return response;
        }
        throw new ProtocolException("HTTP " + code + " had non-zero Content-Length: " + response.body().contentLength());
    }
}
