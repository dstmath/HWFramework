package com.android.okhttp;

import com.android.okhttp.internal.URLFilter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.ResponseCache;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import libcore.net.NetworkSecurityPolicy;

public class HttpHandler extends URLStreamHandler {
    private static final CleartextURLFilter CLEARTEXT_FILTER = new CleartextURLFilter();
    private static final List<ConnectionSpec> CLEARTEXT_ONLY = Collections.singletonList(ConnectionSpec.CLEARTEXT);
    private final ConfigAwareConnectionPool configAwareConnectionPool = ConfigAwareConnectionPool.getInstance();

    private static final class CleartextURLFilter implements URLFilter {
        /* synthetic */ CleartextURLFilter(CleartextURLFilter -this0) {
            this();
        }

        private CleartextURLFilter() {
        }

        public void checkURLPermitted(URL url) throws IOException {
            String host = url.getHost();
            if (!NetworkSecurityPolicy.getInstance().isCleartextTrafficPermitted(host)) {
                throw new IOException("Cleartext HTTP traffic to " + host + " not permitted");
            }
        }
    }

    protected URLConnection openConnection(URL url) throws IOException {
        return newOkUrlFactory(null).open(url);
    }

    protected URLConnection openConnection(URL url, Proxy proxy) throws IOException {
        if (url != null && proxy != null) {
            return newOkUrlFactory(proxy).open(url);
        }
        throw new IllegalArgumentException("url == null || proxy == null");
    }

    protected int getDefaultPort() {
        return 80;
    }

    protected OkUrlFactory newOkUrlFactory(Proxy proxy) {
        OkUrlFactory okUrlFactory = createHttpOkUrlFactory(proxy);
        okUrlFactory.client().setConnectionPool(this.configAwareConnectionPool.get());
        return okUrlFactory;
    }

    public static OkUrlFactory createHttpOkUrlFactory(Proxy proxy) {
        OkHttpClient client = new OkHttpClient();
        client.setConnectTimeout(0, TimeUnit.MILLISECONDS);
        client.setReadTimeout(0, TimeUnit.MILLISECONDS);
        client.setWriteTimeout(0, TimeUnit.MILLISECONDS);
        client.setFollowRedirects(HttpURLConnection.getFollowRedirects());
        client.setFollowSslRedirects(false);
        client.setConnectionSpecs(CLEARTEXT_ONLY);
        if (proxy != null) {
            client.setProxy(proxy);
        }
        OkUrlFactory okUrlFactory = new OkUrlFactory(client);
        okUrlFactory.setUrlFilter(CLEARTEXT_FILTER);
        ResponseCache responseCache = ResponseCache.getDefault();
        if (responseCache != null) {
            AndroidInternal.setResponseCache(okUrlFactory, responseCache);
        }
        return okUrlFactory;
    }
}
