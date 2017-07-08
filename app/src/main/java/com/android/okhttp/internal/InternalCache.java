package com.android.okhttp.internal;

import com.android.okhttp.Request;
import com.android.okhttp.Response;
import com.android.okhttp.internal.http.CacheRequest;
import com.android.okhttp.internal.http.CacheStrategy;
import java.io.IOException;

public interface InternalCache {
    Response get(Request request) throws IOException;

    CacheRequest put(Response response) throws IOException;

    void remove(Request request) throws IOException;

    void trackConditionalCacheHit();

    void trackResponse(CacheStrategy cacheStrategy);

    void update(Response response, Response response2) throws IOException;
}
