package com.android.okhttp;

import com.android.okhttp.internal.InternalCache;
import com.android.okhttp.internal.huc.CacheAdapter;
import java.net.ResponseCache;

public class AndroidInternal {
    private AndroidInternal() {
    }

    public static void setResponseCache(OkUrlFactory okUrlFactory, ResponseCache responseCache) {
        InternalCache internalCache = null;
        OkHttpClient client = okUrlFactory.client();
        if (responseCache instanceof OkCacheContainer) {
            client.setCache(((OkCacheContainer) responseCache).getCache());
            return;
        }
        if (responseCache != null) {
            internalCache = new CacheAdapter(responseCache);
        }
        client.setInternalCache(internalCache);
    }
}
