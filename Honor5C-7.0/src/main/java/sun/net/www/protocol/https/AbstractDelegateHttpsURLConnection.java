package sun.net.www.protocol.https;

import java.io.IOException;
import java.net.Proxy;
import java.net.SecureCacheResponse;
import java.net.URL;
import java.security.Principal;
import java.security.cert.Certificate;
import java.util.List;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSocketFactory;
import javax.security.cert.X509Certificate;
import sun.net.www.http.HttpClient;
import sun.net.www.protocol.http.Handler;
import sun.net.www.protocol.http.HttpURLConnection;

public abstract class AbstractDelegateHttpsURLConnection extends HttpURLConnection {
    protected abstract HostnameVerifier getHostnameVerifier();

    protected abstract SSLSocketFactory getSSLSocketFactory();

    protected AbstractDelegateHttpsURLConnection(URL url, Handler handler) throws IOException {
        this(url, null, handler);
    }

    protected AbstractDelegateHttpsURLConnection(URL url, Proxy p, Handler handler) throws IOException {
        super(url, p, handler);
    }

    public void setNewClient(URL url) throws IOException {
        setNewClient(url, false);
    }

    public void setNewClient(URL url, boolean useCache) throws IOException {
        this.http = HttpsClient.New(getSSLSocketFactory(), url, getHostnameVerifier(), useCache, this);
        ((HttpsClient) this.http).afterConnect();
    }

    public void setProxiedClient(URL url, String proxyHost, int proxyPort) throws IOException {
        setProxiedClient(url, proxyHost, proxyPort, false);
    }

    public void setProxiedClient(URL url, String proxyHost, int proxyPort, boolean useCache) throws IOException {
        proxiedConnect(url, proxyHost, proxyPort, useCache);
        if (!this.http.isCachedConnection()) {
            doTunneling();
        }
        ((HttpsClient) this.http).afterConnect();
    }

    protected void proxiedConnect(URL url, String proxyHost, int proxyPort, boolean useCache) throws IOException {
        if (!this.connected) {
            this.http = HttpsClient.New(getSSLSocketFactory(), url, getHostnameVerifier(), proxyHost, proxyPort, useCache, (HttpURLConnection) this);
            this.connected = true;
        }
    }

    public boolean isConnected() {
        return this.connected;
    }

    public void setConnected(boolean conn) {
        this.connected = conn;
    }

    public void connect() throws IOException {
        if (!this.connected) {
            plainConnect();
            if (this.cachedResponse == null) {
                if (!this.http.isCachedConnection() && this.http.needsTunneling()) {
                    doTunneling();
                }
                ((HttpsClient) this.http).afterConnect();
            }
        }
    }

    protected HttpClient getNewHttpClient(URL url, Proxy p, int connectTimeout) throws IOException {
        return HttpsClient.New(getSSLSocketFactory(), url, getHostnameVerifier(), p, true, connectTimeout, (HttpURLConnection) this);
    }

    protected HttpClient getNewHttpClient(URL url, Proxy p, int connectTimeout, boolean useCache) throws IOException {
        return HttpsClient.New(getSSLSocketFactory(), url, getHostnameVerifier(), p, useCache, connectTimeout, (HttpURLConnection) this);
    }

    public String getCipherSuite() {
        if (this.cachedResponse != null) {
            return ((SecureCacheResponse) this.cachedResponse).getCipherSuite();
        }
        if (this.http != null) {
            return ((HttpsClient) this.http).getCipherSuite();
        }
        throw new IllegalStateException("connection not yet open");
    }

    public Certificate[] getLocalCertificates() {
        if (this.cachedResponse != null) {
            List l = ((SecureCacheResponse) this.cachedResponse).getLocalCertificateChain();
            if (l == null) {
                return null;
            }
            return (Certificate[]) l.toArray();
        } else if (this.http != null) {
            return ((HttpsClient) this.http).getLocalCertificates();
        } else {
            throw new IllegalStateException("connection not yet open");
        }
    }

    public Certificate[] getServerCertificates() throws SSLPeerUnverifiedException {
        if (this.cachedResponse != null) {
            List l = ((SecureCacheResponse) this.cachedResponse).getServerCertificateChain();
            if (l == null) {
                return null;
            }
            return (Certificate[]) l.toArray();
        } else if (this.http != null) {
            return ((HttpsClient) this.http).getServerCertificates();
        } else {
            throw new IllegalStateException("connection not yet open");
        }
    }

    public X509Certificate[] getServerCertificateChain() throws SSLPeerUnverifiedException {
        if (this.cachedResponse != null) {
            throw new UnsupportedOperationException("this method is not supported when using cache");
        } else if (this.http != null) {
            return ((HttpsClient) this.http).getServerCertificateChain();
        } else {
            throw new IllegalStateException("connection not yet open");
        }
    }

    Principal getPeerPrincipal() throws SSLPeerUnverifiedException {
        if (this.cachedResponse != null) {
            return ((SecureCacheResponse) this.cachedResponse).getPeerPrincipal();
        }
        if (this.http != null) {
            return ((HttpsClient) this.http).getPeerPrincipal();
        }
        throw new IllegalStateException("connection not yet open");
    }

    Principal getLocalPrincipal() {
        if (this.cachedResponse != null) {
            return ((SecureCacheResponse) this.cachedResponse).getLocalPrincipal();
        }
        if (this.http != null) {
            return ((HttpsClient) this.http).getLocalPrincipal();
        }
        throw new IllegalStateException("connection not yet open");
    }
}
