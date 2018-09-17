package com.android.okhttp.internal.huc;

import com.android.okhttp.Handshake;
import com.android.okhttp.OkHttpClient;
import com.android.okhttp.internal.URLFilter;
import java.net.URL;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;

public final class HttpsURLConnectionImpl extends DelegatingHttpsURLConnection {
    private final HttpURLConnectionImpl delegate;

    public HttpsURLConnectionImpl(URL url, OkHttpClient client) {
        this(new HttpURLConnectionImpl(url, client));
    }

    public HttpsURLConnectionImpl(URL url, OkHttpClient client, URLFilter filter) {
        this(new HttpURLConnectionImpl(url, client, filter));
    }

    public HttpsURLConnectionImpl(HttpURLConnectionImpl delegate) {
        super(delegate);
        this.delegate = delegate;
    }

    protected Handshake handshake() {
        if (this.delegate.httpEngine == null) {
            throw new IllegalStateException("Connection has not yet been established");
        } else if (this.delegate.httpEngine.hasResponse()) {
            return this.delegate.httpEngine.getResponse().handshake();
        } else {
            return this.delegate.handshake;
        }
    }

    public void setHostnameVerifier(HostnameVerifier hostnameVerifier) {
        this.delegate.client.setHostnameVerifier(hostnameVerifier);
    }

    public HostnameVerifier getHostnameVerifier() {
        return this.delegate.client.getHostnameVerifier();
    }

    public void setSSLSocketFactory(SSLSocketFactory sslSocketFactory) {
        this.delegate.client.setSslSocketFactory(sslSocketFactory);
    }

    public SSLSocketFactory getSSLSocketFactory() {
        return this.delegate.client.getSslSocketFactory();
    }

    public long getContentLengthLong() {
        return this.delegate.getContentLengthLong();
    }

    public void setFixedLengthStreamingMode(long contentLength) {
        this.delegate.setFixedLengthStreamingMode(contentLength);
    }

    public long getHeaderFieldLong(String field, long defaultValue) {
        return this.delegate.getHeaderFieldLong(field, defaultValue);
    }
}
