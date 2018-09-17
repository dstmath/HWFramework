package android.net.http;

import com.android.okhttp.AndroidShimResponseCache;
import com.android.okhttp.Cache;
import com.android.okhttp.OkCacheContainer;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.CacheRequest;
import java.net.CacheResponse;
import java.net.ResponseCache;
import java.net.URI;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

public final class HttpResponseCache extends ResponseCache implements Closeable, OkCacheContainer {
    private final AndroidShimResponseCache delegate;

    private HttpResponseCache(AndroidShimResponseCache delegate) {
        this.delegate = delegate;
    }

    public static HttpResponseCache getInstalled() {
        ResponseCache installed = ResponseCache.getDefault();
        if (installed instanceof HttpResponseCache) {
            return (HttpResponseCache) installed;
        }
        return null;
    }

    public static synchronized HttpResponseCache install(File directory, long maxSize) throws IOException {
        synchronized (HttpResponseCache.class) {
            ResponseCache installed = ResponseCache.getDefault();
            if (installed instanceof HttpResponseCache) {
                HttpResponseCache installedResponseCache = (HttpResponseCache) installed;
                AndroidShimResponseCache trueResponseCache = installedResponseCache.delegate;
                if (trueResponseCache.isEquivalent(directory, maxSize)) {
                    return installedResponseCache;
                }
                trueResponseCache.close();
            }
            HttpResponseCache newResponseCache = new HttpResponseCache(AndroidShimResponseCache.create(directory, maxSize));
            ResponseCache.setDefault(newResponseCache);
            return newResponseCache;
        }
    }

    public CacheResponse get(URI uri, String requestMethod, Map<String, List<String>> requestHeaders) throws IOException {
        return this.delegate.get(uri, requestMethod, requestHeaders);
    }

    public CacheRequest put(URI uri, URLConnection urlConnection) throws IOException {
        return this.delegate.put(uri, urlConnection);
    }

    public long size() {
        try {
            return this.delegate.size();
        } catch (IOException e) {
            return -1;
        }
    }

    public long maxSize() {
        return this.delegate.maxSize();
    }

    public void flush() {
        try {
            this.delegate.flush();
        } catch (IOException e) {
        }
    }

    public int getNetworkCount() {
        return this.delegate.getNetworkCount();
    }

    public int getHitCount() {
        return this.delegate.getHitCount();
    }

    public int getRequestCount() {
        return this.delegate.getRequestCount();
    }

    public void close() throws IOException {
        if (ResponseCache.getDefault() == this) {
            ResponseCache.setDefault(null);
        }
        this.delegate.close();
    }

    public void delete() throws IOException {
        if (ResponseCache.getDefault() == this) {
            ResponseCache.setDefault(null);
        }
        this.delegate.delete();
    }

    public Cache getCache() {
        return this.delegate.getCache();
    }
}
