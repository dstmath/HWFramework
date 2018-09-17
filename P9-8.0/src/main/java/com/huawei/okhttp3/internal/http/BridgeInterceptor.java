package com.huawei.okhttp3.internal.http;

import com.huawei.okhttp3.CookieJar;
import com.huawei.okhttp3.Headers;
import com.huawei.okhttp3.Interceptor;
import com.huawei.okhttp3.Interceptor.Chain;
import com.huawei.okhttp3.MediaType;
import com.huawei.okhttp3.Request;
import com.huawei.okhttp3.Request.Builder;
import com.huawei.okhttp3.RequestBody;
import com.huawei.okhttp3.Response;
import com.huawei.okhttp3.internal.Util;
import com.huawei.okhttp3.internal.Version;
import com.huawei.okio.GzipSource;
import com.huawei.okio.Okio;
import com.huawei.okio.Source;
import java.io.IOException;
import java.util.List;
import okhttp3.Cookie;

public final class BridgeInterceptor implements Interceptor {
    private final CookieJar cookieJar;

    public BridgeInterceptor(CookieJar cookieJar) {
        this.cookieJar = cookieJar;
    }

    public Response intercept(Chain chain) throws IOException {
        Request userRequest = chain.request();
        Builder requestBuilder = userRequest.newBuilder();
        RequestBody body = userRequest.body();
        if (body != null) {
            MediaType contentType = body.contentType();
            if (contentType != null) {
                requestBuilder.header("Content-Type", contentType.toString());
            }
            long contentLength = body.contentLength();
            if (contentLength != -1) {
                requestBuilder.header("Content-Length", Long.toString(contentLength));
                requestBuilder.removeHeader("Transfer-Encoding");
            } else {
                requestBuilder.header("Transfer-Encoding", "chunked");
                requestBuilder.removeHeader("Content-Length");
            }
        }
        if (userRequest.header("Host") == null) {
            requestBuilder.header("Host", Util.hostHeader(userRequest.url(), false));
        }
        if (userRequest.header("Connection") == null) {
            requestBuilder.header("Connection", "Keep-Alive");
        }
        boolean transparentGzip = false;
        if (userRequest.header("Accept-Encoding") == null) {
            transparentGzip = true;
            requestBuilder.header("Accept-Encoding", "gzip");
        }
        List<Cookie> cookies = this.cookieJar.loadForRequest(userRequest.url());
        if (!cookies.isEmpty()) {
            requestBuilder.header("Cookie", cookieHeader(cookies));
        }
        if (userRequest.header("User-Agent") == null) {
            requestBuilder.header("User-Agent", Version.userAgent());
        }
        Response networkResponse = chain.proceed(requestBuilder.build());
        HttpHeaders.receiveHeaders(this.cookieJar, userRequest.url(), networkResponse.headers());
        Response.Builder responseBuilder = networkResponse.newBuilder().request(userRequest);
        if (transparentGzip && "gzip".equalsIgnoreCase(networkResponse.header("Content-Encoding")) && HttpHeaders.hasBody(networkResponse)) {
            Source responseBody = new GzipSource(networkResponse.body().source());
            Headers strippedHeaders = networkResponse.headers().newBuilder().removeAll("Content-Encoding").removeAll("Content-Length").build();
            responseBuilder.headers(strippedHeaders);
            responseBuilder.body(new RealResponseBody(strippedHeaders, Okio.buffer(responseBody)));
        }
        return responseBuilder.build();
    }

    private String cookieHeader(List<com.huawei.okhttp3.Cookie> cookies) {
        StringBuilder cookieHeader = new StringBuilder();
        int size = cookies.size();
        for (int i = 0; i < size; i++) {
            if (i > 0) {
                cookieHeader.append("; ");
            }
            com.huawei.okhttp3.Cookie cookie = (com.huawei.okhttp3.Cookie) cookies.get(i);
            cookieHeader.append(cookie.name()).append('=').append(cookie.value());
        }
        return cookieHeader.toString();
    }
}
