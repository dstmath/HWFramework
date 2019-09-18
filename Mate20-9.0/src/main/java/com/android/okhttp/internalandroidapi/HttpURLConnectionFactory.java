package com.android.okhttp.internalandroidapi;

import com.android.okhttp.ConnectionPool;
import com.android.okhttp.Dns;
import com.android.okhttp.HttpHandler;
import com.android.okhttp.HttpsHandler;
import com.android.okhttp.OkHttpClient;
import com.android.okhttp.OkUrlFactories;
import com.android.okhttp.OkUrlFactory;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import javax.net.SocketFactory;

public final class HttpURLConnectionFactory {
    private ConnectionPool connectionPool;
    private Dns dns;

    static final class DnsAdapter implements Dns {
        private final Dns adaptee;

        DnsAdapter(Dns adaptee2) {
            this.adaptee = (Dns) Objects.requireNonNull(adaptee2);
        }

        public List<InetAddress> lookup(String hostname) throws UnknownHostException {
            return this.adaptee.lookup(hostname);
        }

        public int hashCode() {
            return (31 * DnsAdapter.class.hashCode()) + this.adaptee.hashCode();
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof DnsAdapter)) {
                return false;
            }
            return this.adaptee.equals(((DnsAdapter) obj).adaptee);
        }

        public String toString() {
            return this.adaptee.toString();
        }
    }

    public void setNewConnectionPool(int maxIdleConnections, long keepAliveDuration, TimeUnit timeUnit) {
        this.connectionPool = new ConnectionPool(maxIdleConnections, keepAliveDuration, timeUnit);
    }

    public void setDns(Dns dns2) {
        Objects.requireNonNull(dns2);
        this.dns = new DnsAdapter(dns2);
    }

    public URLConnection openConnection(URL url) throws IOException {
        return internalOpenConnection(url, null, null);
    }

    public URLConnection openConnection(URL url, Proxy proxy) throws IOException {
        Objects.requireNonNull(proxy);
        return internalOpenConnection(url, null, proxy);
    }

    public URLConnection openConnection(URL url, SocketFactory socketFactory) throws IOException {
        Objects.requireNonNull(socketFactory);
        return internalOpenConnection(url, socketFactory, null);
    }

    public URLConnection openConnection(URL url, SocketFactory socketFactory, Proxy proxy) throws IOException {
        Objects.requireNonNull(socketFactory);
        Objects.requireNonNull(proxy);
        return internalOpenConnection(url, socketFactory, proxy);
    }

    private URLConnection internalOpenConnection(URL url, SocketFactory socketFactoryOrNull, Proxy proxyOrNull) throws IOException {
        OkUrlFactory okUrlFactory;
        String protocol = url.getProtocol();
        if (protocol.equals("http")) {
            okUrlFactory = HttpHandler.createHttpOkUrlFactory(proxyOrNull);
        } else if (protocol.equals("https")) {
            okUrlFactory = HttpsHandler.createHttpsOkUrlFactory(proxyOrNull);
        } else {
            throw new MalformedURLException("Invalid URL or unrecognized protocol " + protocol);
        }
        OkHttpClient client = okUrlFactory.client();
        if (this.connectionPool != null) {
            client.setConnectionPool(this.connectionPool);
        }
        if (this.dns != null) {
            client.setDns(this.dns);
        }
        if (socketFactoryOrNull != null) {
            client.setSocketFactory(socketFactoryOrNull);
        }
        if (proxyOrNull == null) {
            return okUrlFactory.open(url);
        }
        return OkUrlFactories.open(okUrlFactory, url, proxyOrNull);
    }
}
