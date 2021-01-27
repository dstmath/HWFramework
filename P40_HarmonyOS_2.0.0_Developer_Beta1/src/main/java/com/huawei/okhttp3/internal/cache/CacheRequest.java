package com.huawei.okhttp3.internal.cache;

import com.huawei.okio.Sink;
import java.io.IOException;

@Deprecated
public interface CacheRequest {
    void abort();

    Sink body() throws IOException;
}
