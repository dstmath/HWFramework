package com.android.okhttp.internal.huc;

import com.android.okhttp.Handshake;
import com.android.okhttp.OkHttpClient;
import com.android.okhttp.internal.URLFilter;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.security.Permission;
import java.security.Principal;
import java.security.cert.Certificate;
import java.util.Map;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;

public final class HttpsURLConnectionImpl extends DelegatingHttpsURLConnection {
    private final HttpURLConnectionImpl delegate;

    public /* bridge */ /* synthetic */ void addRequestProperty(String field, String newValue) {
        super.addRequestProperty(field, newValue);
    }

    public /* bridge */ /* synthetic */ void connect() {
        super.connect();
    }

    public /* bridge */ /* synthetic */ void disconnect() {
        super.disconnect();
    }

    public /* bridge */ /* synthetic */ boolean getAllowUserInteraction() {
        return super.getAllowUserInteraction();
    }

    public /* bridge */ /* synthetic */ String getCipherSuite() {
        return super.getCipherSuite();
    }

    public /* bridge */ /* synthetic */ int getConnectTimeout() {
        return super.getConnectTimeout();
    }

    public /* bridge */ /* synthetic */ Object getContent() {
        return super.getContent();
    }

    public /* bridge */ /* synthetic */ Object getContent(Class[] types) {
        return super.getContent(types);
    }

    public /* bridge */ /* synthetic */ String getContentEncoding() {
        return super.getContentEncoding();
    }

    public /* bridge */ /* synthetic */ int getContentLength() {
        return super.getContentLength();
    }

    public /* bridge */ /* synthetic */ String getContentType() {
        return super.getContentType();
    }

    public /* bridge */ /* synthetic */ long getDate() {
        return super.getDate();
    }

    public /* bridge */ /* synthetic */ boolean getDefaultUseCaches() {
        return super.getDefaultUseCaches();
    }

    public /* bridge */ /* synthetic */ boolean getDoInput() {
        return super.getDoInput();
    }

    public /* bridge */ /* synthetic */ boolean getDoOutput() {
        return super.getDoOutput();
    }

    public /* bridge */ /* synthetic */ InputStream getErrorStream() {
        return super.getErrorStream();
    }

    public /* bridge */ /* synthetic */ long getExpiration() {
        return super.getExpiration();
    }

    public /* bridge */ /* synthetic */ long getHeaderFieldDate(String field, long defaultValue) {
        return super.getHeaderFieldDate(field, defaultValue);
    }

    public /* bridge */ /* synthetic */ int getHeaderFieldInt(String field, int defaultValue) {
        return super.getHeaderFieldInt(field, defaultValue);
    }

    public /* bridge */ /* synthetic */ String getHeaderFieldKey(int position) {
        return super.getHeaderFieldKey(position);
    }

    public /* bridge */ /* synthetic */ Map getHeaderFields() {
        return super.getHeaderFields();
    }

    public /* bridge */ /* synthetic */ long getIfModifiedSince() {
        return super.getIfModifiedSince();
    }

    public /* bridge */ /* synthetic */ InputStream getInputStream() {
        return super.getInputStream();
    }

    public /* bridge */ /* synthetic */ boolean getInstanceFollowRedirects() {
        return super.getInstanceFollowRedirects();
    }

    public /* bridge */ /* synthetic */ long getLastModified() {
        return super.getLastModified();
    }

    public /* bridge */ /* synthetic */ Certificate[] getLocalCertificates() {
        return super.getLocalCertificates();
    }

    public /* bridge */ /* synthetic */ Principal getLocalPrincipal() {
        return super.getLocalPrincipal();
    }

    public /* bridge */ /* synthetic */ OutputStream getOutputStream() {
        return super.getOutputStream();
    }

    public /* bridge */ /* synthetic */ Principal getPeerPrincipal() {
        return super.getPeerPrincipal();
    }

    public /* bridge */ /* synthetic */ Permission getPermission() {
        return super.getPermission();
    }

    public /* bridge */ /* synthetic */ int getReadTimeout() {
        return super.getReadTimeout();
    }

    public /* bridge */ /* synthetic */ String getRequestMethod() {
        return super.getRequestMethod();
    }

    public /* bridge */ /* synthetic */ Map getRequestProperties() {
        return super.getRequestProperties();
    }

    public /* bridge */ /* synthetic */ String getRequestProperty(String field) {
        return super.getRequestProperty(field);
    }

    public /* bridge */ /* synthetic */ int getResponseCode() {
        return super.getResponseCode();
    }

    public /* bridge */ /* synthetic */ String getResponseMessage() {
        return super.getResponseMessage();
    }

    public /* bridge */ /* synthetic */ Certificate[] getServerCertificates() {
        return super.getServerCertificates();
    }

    public /* bridge */ /* synthetic */ URL getURL() {
        return super.getURL();
    }

    public /* bridge */ /* synthetic */ boolean getUseCaches() {
        return super.getUseCaches();
    }

    public /* bridge */ /* synthetic */ void setAllowUserInteraction(boolean newValue) {
        super.setAllowUserInteraction(newValue);
    }

    public /* bridge */ /* synthetic */ void setChunkedStreamingMode(int chunkLength) {
        super.setChunkedStreamingMode(chunkLength);
    }

    public /* bridge */ /* synthetic */ void setConnectTimeout(int timeoutMillis) {
        super.setConnectTimeout(timeoutMillis);
    }

    public /* bridge */ /* synthetic */ void setDefaultUseCaches(boolean newValue) {
        super.setDefaultUseCaches(newValue);
    }

    public /* bridge */ /* synthetic */ void setDoInput(boolean newValue) {
        super.setDoInput(newValue);
    }

    public /* bridge */ /* synthetic */ void setDoOutput(boolean newValue) {
        super.setDoOutput(newValue);
    }

    public /* bridge */ /* synthetic */ void setIfModifiedSince(long newValue) {
        super.setIfModifiedSince(newValue);
    }

    public /* bridge */ /* synthetic */ void setInstanceFollowRedirects(boolean followRedirects) {
        super.setInstanceFollowRedirects(followRedirects);
    }

    public /* bridge */ /* synthetic */ void setReadTimeout(int timeoutMillis) {
        super.setReadTimeout(timeoutMillis);
    }

    public /* bridge */ /* synthetic */ void setRequestMethod(String method) {
        super.setRequestMethod(method);
    }

    public /* bridge */ /* synthetic */ void setRequestProperty(String field, String newValue) {
        super.setRequestProperty(field, newValue);
    }

    public /* bridge */ /* synthetic */ void setUseCaches(boolean newValue) {
        super.setUseCaches(newValue);
    }

    public /* bridge */ /* synthetic */ String toString() {
        return super.toString();
    }

    public /* bridge */ /* synthetic */ boolean usingProxy() {
        return super.usingProxy();
    }

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

    public void setFixedLengthStreamingMode(long contentLength) {
        this.delegate.setFixedLengthStreamingMode(contentLength);
    }
}
