package ohos.net;

import com.android.okhttp.Cache;
import com.android.okhttp.Response;
import com.android.okhttp.internal.huc.JavaApiConverter;
import java.io.File;
import java.io.IOException;
import java.net.CacheRequest;
import java.net.CacheResponse;
import java.net.ResponseCache;
import java.net.URI;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

public final class HttpResponseCache extends ResponseCache {
    private Cache mCache;

    private HttpResponseCache(File file, long j) {
        this.mCache = new Cache(file, j);
    }

    public static HttpResponseCache getInstalled() {
        ResponseCache responseCache = ResponseCache.getDefault();
        if (responseCache instanceof HttpResponseCache) {
            return (HttpResponseCache) responseCache;
        }
        return null;
    }

    public static synchronized HttpResponseCache install(File file, long j) throws IOException {
        HttpResponseCache httpResponseCache;
        synchronized (HttpResponseCache.class) {
            httpResponseCache = new HttpResponseCache(file, j);
            ResponseCache.setDefault(httpResponseCache);
        }
        return httpResponseCache;
    }

    @Override // java.net.ResponseCache
    public CacheResponse get(URI uri, String str, Map<String, List<String>> map) throws IOException {
        Response response = this.mCache.internalCache.get(JavaApiConverter.createOkRequest(uri, str, map));
        if (response == null) {
            return null;
        }
        return JavaApiConverter.createJavaCacheResponse(response);
    }

    @Override // java.net.ResponseCache
    public CacheRequest put(URI uri, URLConnection uRLConnection) throws IOException {
        com.android.okhttp.internal.http.CacheRequest put;
        Response createOkResponseForCachePut = JavaApiConverter.createOkResponseForCachePut(uri, uRLConnection);
        if (createOkResponseForCachePut == null || (put = this.mCache.internalCache.put(createOkResponseForCachePut)) == null) {
            return null;
        }
        return JavaApiConverter.createJavaCacheRequest(put);
    }

    public long getSize() throws IOException {
        return this.mCache.getSize();
    }

    public long getMaxSize() {
        return this.mCache.getMaxSize();
    }

    public void flush() throws IOException {
        this.mCache.flush();
    }

    public int getNetworkCount() {
        return this.mCache.getNetworkCount();
    }

    public int getHitCount() {
        return this.mCache.getHitCount();
    }

    public int getRequestCount() {
        return this.mCache.getRequestCount();
    }

    public void close() throws IOException {
        this.mCache.close();
    }

    public void delete() throws IOException {
        this.mCache.delete();
    }
}
