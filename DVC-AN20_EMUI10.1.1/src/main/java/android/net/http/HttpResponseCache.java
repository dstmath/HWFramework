package android.net.http;

import com.android.okhttp.internalandroidapi.AndroidResponseCacheAdapter;
import com.android.okhttp.internalandroidapi.HasCacheHolder;
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

public final class HttpResponseCache extends ResponseCache implements HasCacheHolder, Closeable {
    private final AndroidResponseCacheAdapter mDelegate;

    private HttpResponseCache(AndroidResponseCacheAdapter delegate) {
        this.mDelegate = delegate;
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
                if (installedResponseCache.getCacheHolder().isEquivalent(directory, maxSize)) {
                    return installedResponseCache;
                }
                installedResponseCache.close();
            }
            HttpResponseCache responseCache = new HttpResponseCache(new AndroidResponseCacheAdapter(HasCacheHolder.CacheHolder.create(directory, maxSize)));
            ResponseCache.setDefault(responseCache);
            return responseCache;
        }
    }

    @Override // java.net.ResponseCache
    public CacheResponse get(URI uri, String requestMethod, Map<String, List<String>> requestHeaders) throws IOException {
        return this.mDelegate.get(uri, requestMethod, requestHeaders);
    }

    @Override // java.net.ResponseCache
    public CacheRequest put(URI uri, URLConnection urlConnection) throws IOException {
        return this.mDelegate.put(uri, urlConnection);
    }

    public long size() {
        try {
            return this.mDelegate.getSize();
        } catch (IOException e) {
            return -1;
        }
    }

    public long maxSize() {
        return this.mDelegate.getMaxSize();
    }

    public void flush() {
        try {
            this.mDelegate.flush();
        } catch (IOException e) {
        }
    }

    public int getNetworkCount() {
        return this.mDelegate.getNetworkCount();
    }

    public int getHitCount() {
        return this.mDelegate.getHitCount();
    }

    public int getRequestCount() {
        return this.mDelegate.getRequestCount();
    }

    @Override // java.io.Closeable, java.lang.AutoCloseable
    public void close() throws IOException {
        if (ResponseCache.getDefault() == this) {
            ResponseCache.setDefault(null);
        }
        this.mDelegate.close();
    }

    public void delete() throws IOException {
        if (ResponseCache.getDefault() == this) {
            ResponseCache.setDefault(null);
        }
        this.mDelegate.delete();
    }

    public HasCacheHolder.CacheHolder getCacheHolder() {
        return this.mDelegate.getCacheHolder();
    }
}
