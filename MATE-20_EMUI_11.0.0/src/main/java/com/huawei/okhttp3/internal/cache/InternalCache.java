package com.huawei.okhttp3.internal.cache;

import com.huawei.okhttp3.Request;
import com.huawei.okhttp3.Response;
import java.io.IOException;
import javax.annotation.Nullable;

public interface InternalCache {
    @Nullable
    Response get(Request request) throws IOException;

    @Nullable
    CacheRequest put(Response response) throws IOException;

    void remove(Request request) throws IOException;

    void trackConditionalCacheHit();

    void trackResponse(CacheStrategy cacheStrategy);

    void update(Response response, Response response2);
}
