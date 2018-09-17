package com.android.okhttp.internal.huc;

import com.android.okhttp.Request;
import com.android.okhttp.Response;
import com.android.okhttp.internal.InternalCache;
import com.android.okhttp.internal.http.CacheRequest;
import com.android.okhttp.internal.http.CacheStrategy;
import com.android.okhttp.okio.Okio;
import com.android.okhttp.okio.Sink;
import java.io.IOException;
import java.io.OutputStream;
import java.net.CacheResponse;
import java.net.ResponseCache;

public final class CacheAdapter implements InternalCache {
    private final ResponseCache delegate;

    public CacheAdapter(ResponseCache delegate) {
        this.delegate = delegate;
    }

    public ResponseCache getDelegate() {
        return this.delegate;
    }

    public Response get(Request request) throws IOException {
        CacheResponse javaResponse = getJavaCachedResponse(request);
        if (javaResponse == null) {
            return null;
        }
        return JavaApiConverter.createOkResponseForCacheGet(request, javaResponse);
    }

    public CacheRequest put(Response response) throws IOException {
        final java.net.CacheRequest request = this.delegate.put(response.request().uri(), JavaApiConverter.createJavaUrlConnectionForCachePut(response));
        if (request == null) {
            return null;
        }
        return new CacheRequest() {
            public Sink body() throws IOException {
                OutputStream body = request.getBody();
                if (body != null) {
                    return Okio.sink(body);
                }
                return null;
            }

            public void abort() {
                request.abort();
            }
        };
    }

    public void remove(Request request) throws IOException {
    }

    public void update(Response cached, Response network) throws IOException {
    }

    public void trackConditionalCacheHit() {
    }

    public void trackResponse(CacheStrategy cacheStrategy) {
    }

    private CacheResponse getJavaCachedResponse(Request request) throws IOException {
        return this.delegate.get(request.uri(), request.method(), JavaApiConverter.extractJavaHeaders(request));
    }
}
