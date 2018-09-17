package com.android.okhttp;

import com.android.okhttp.internal.ConnectionSpecSelector;
import com.android.okhttp.internal.Platform;
import com.android.okhttp.internal.Util;
import com.android.okhttp.internal.framed.FramedConnection;
import com.android.okhttp.internal.framed.FramedConnection.Builder;
import com.android.okhttp.internal.http.FramedTransport;
import com.android.okhttp.internal.http.HttpConnection;
import com.android.okhttp.internal.http.HttpEngine;
import com.android.okhttp.internal.http.HttpTransport;
import com.android.okhttp.internal.http.OkHeaders;
import com.android.okhttp.internal.http.RouteException;
import com.android.okhttp.internal.http.Transport;
import com.android.okhttp.internal.tls.OkHostnameVerifier;
import com.android.okhttp.okio.BufferedSink;
import com.android.okhttp.okio.BufferedSource;
import com.android.okhttp.okio.Source;
import java.io.IOException;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.Socket;
import java.net.UnknownServiceException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSocket;

public final class Connection {
    private boolean connected;
    private FramedConnection framedConnection;
    private Handshake handshake;
    private HttpConnection httpConnection;
    private long idleStartTimeNs;
    private Object owner;
    private final ConnectionPool pool;
    private Protocol protocol;
    private int recycleCount;
    private final Route route;
    private Socket socket;

    public Connection(ConnectionPool pool, Route route) {
        this.connected = false;
        this.protocol = Protocol.HTTP_1_1;
        this.pool = pool;
        this.route = route;
    }

    Object getOwner() {
        Object obj;
        synchronized (this.pool) {
            obj = this.owner;
        }
        return obj;
    }

    void setOwner(Object owner) {
        if (!isFramed()) {
            synchronized (this.pool) {
                if (this.owner != null) {
                    throw new IllegalStateException("Connection already has an owner!");
                }
                this.owner = owner;
            }
        }
    }

    boolean clearOwner() {
        synchronized (this.pool) {
            if (this.owner == null) {
                return false;
            }
            this.owner = null;
            return true;
        }
    }

    void closeIfOwnedBy(Object owner) throws IOException {
        if (isFramed()) {
            throw new IllegalStateException();
        }
        synchronized (this.pool) {
            if (this.owner != owner) {
                return;
            }
            this.owner = null;
            if (this.socket != null) {
                this.socket.close();
            }
        }
    }

    void connect(int connectTimeout, int readTimeout, int writeTimeout, Request request, List<ConnectionSpec> connectionSpecs, boolean connectionRetryEnabled) throws RouteException {
        if (this.connected) {
            throw new IllegalStateException("already connected");
        }
        RouteException routeException = null;
        ConnectionSpecSelector connectionSpecSelector = new ConnectionSpecSelector(connectionSpecs);
        Proxy proxy = this.route.getProxy();
        Address address = this.route.getAddress();
        if (this.route.address.getSslSocketFactory() != null || connectionSpecs.contains(ConnectionSpec.CLEARTEXT)) {
            while (!this.connected) {
                try {
                    Socket createSocket;
                    if (proxy.type() == Type.DIRECT || proxy.type() == Type.HTTP) {
                        createSocket = address.getSocketFactory().createSocket();
                    } else {
                        createSocket = new Socket(proxy);
                    }
                    this.socket = createSocket;
                    connectSocket(connectTimeout, readTimeout, writeTimeout, request, connectionSpecSelector);
                    this.connected = true;
                } catch (IOException e) {
                    Util.closeQuietly(this.socket);
                    this.socket = null;
                    if (routeException == null) {
                        routeException = new RouteException(e);
                    } else {
                        routeException.addConnectException(e);
                    }
                    if (!connectionRetryEnabled || !connectionSpecSelector.connectionFailed(e)) {
                        throw routeException;
                    }
                }
            }
            return;
        }
        throw new RouteException(new UnknownServiceException("CLEARTEXT communication not supported: " + connectionSpecs));
    }

    private void connectSocket(int connectTimeout, int readTimeout, int writeTimeout, Request request, ConnectionSpecSelector connectionSpecSelector) throws IOException {
        this.socket.setSoTimeout(readTimeout);
        Platform.get().connectSocket(this.socket, this.route.getSocketAddress(), connectTimeout);
        if (this.route.address.getSslSocketFactory() != null) {
            connectTls(readTimeout, writeTimeout, request, connectionSpecSelector);
        }
        if (this.protocol == Protocol.SPDY_3 || this.protocol == Protocol.HTTP_2) {
            this.socket.setSoTimeout(0);
            this.framedConnection = new Builder(this.route.address.uriHost, true, this.socket).protocol(this.protocol).build();
            this.framedConnection.sendConnectionPreface();
            return;
        }
        this.httpConnection = new HttpConnection(this.pool, this, this.socket);
    }

    private void connectTls(int readTimeout, int writeTimeout, Request request, ConnectionSpecSelector connectionSpecSelector) throws IOException {
        if (this.route.requiresTunnel()) {
            createTunnel(readTimeout, writeTimeout, request);
        }
        Address address = this.route.getAddress();
        Socket socket = null;
        try {
            socket = (SSLSocket) address.getSslSocketFactory().createSocket(this.socket, address.getRfc2732Host(), address.getUriPort(), true);
            ConnectionSpec connectionSpec = connectionSpecSelector.configureSecureSocket(socket);
            if (connectionSpec.supportsTlsExtensions()) {
                Platform.get().configureTlsExtensions(socket, address.getRfc2732Host(), address.getProtocols());
            }
            socket.startHandshake();
            Handshake unverifiedHandshake = Handshake.get(socket.getSession());
            if (address.getHostnameVerifier().verify(address.getRfc2732Host(), socket.getSession())) {
                String maybeProtocol;
                Protocol protocol;
                address.getCertificatePinner().check(address.getRfc2732Host(), unverifiedHandshake.peerCertificates());
                if (connectionSpec.supportsTlsExtensions()) {
                    maybeProtocol = Platform.get().getSelectedProtocol(socket);
                } else {
                    maybeProtocol = null;
                }
                if (maybeProtocol != null) {
                    protocol = Protocol.get(maybeProtocol);
                } else {
                    protocol = Protocol.HTTP_1_1;
                }
                this.protocol = protocol;
                this.handshake = unverifiedHandshake;
                this.socket = socket;
                if (socket != null) {
                    Platform.get().afterHandshake(socket);
                }
                if (!true) {
                    Util.closeQuietly(socket);
                    return;
                }
                return;
            }
            X509Certificate cert = (X509Certificate) unverifiedHandshake.peerCertificates().get(0);
            throw new SSLPeerUnverifiedException("Hostname " + address.getRfc2732Host() + " not verified:" + "\n    certificate: " + CertificatePinner.pin(cert) + "\n    DN: " + cert.getSubjectDN().getName() + "\n    subjectAltNames: " + OkHostnameVerifier.allSubjectAltNames(cert));
        } catch (AssertionError e) {
            if (Util.isAndroidGetsocknameError(e)) {
                throw new IOException(e);
            }
            throw e;
        } catch (Throwable th) {
            if (socket != null) {
                Platform.get().afterHandshake(socket);
            }
            if (!false) {
                Util.closeQuietly(socket);
            }
        }
    }

    private void createTunnel(int readTimeout, int writeTimeout, Request request) throws IOException {
        Request tunnelRequest = createTunnelRequest(request);
        HttpConnection tunnelConnection = new HttpConnection(this.pool, this, this.socket);
        tunnelConnection.setTimeouts(readTimeout, writeTimeout);
        HttpUrl url = tunnelRequest.httpUrl();
        String requestLine = "CONNECT " + url.rfc2732host() + ":" + url.port() + " HTTP/1.1";
        do {
            tunnelConnection.writeRequest(tunnelRequest.headers(), requestLine);
            tunnelConnection.flush();
            Response response = tunnelConnection.readResponse().request(tunnelRequest).build();
            long contentLength = OkHeaders.contentLength(response);
            if (contentLength == -1) {
                contentLength = 0;
            }
            Source body = tunnelConnection.newFixedLengthSource(contentLength);
            Util.skipAll(body, Integer.MAX_VALUE, TimeUnit.MILLISECONDS);
            body.close();
            switch (response.code()) {
                case 200:
                    if (tunnelConnection.bufferSize() > 0) {
                        throw new IOException("TLS tunnel buffered too many bytes!");
                    }
                    return;
                case 407:
                    tunnelRequest = OkHeaders.processAuthHeader(this.route.getAddress().getAuthenticator(), response, this.route.getProxy());
                    break;
                default:
                    throw new IOException("Unexpected response code for CONNECT: " + response.code());
            }
        } while (tunnelRequest != null);
        throw new IOException("Failed to authenticate with proxy");
    }

    private Request createTunnelRequest(Request request) throws IOException {
        HttpUrl tunnelUrl = new HttpUrl.Builder().scheme("https").host(request.httpUrl().host()).port(request.httpUrl().port()).build();
        Request.Builder result = new Request.Builder().url(tunnelUrl).header("Host", Util.hostHeader(tunnelUrl)).header("Proxy-Connection", "Keep-Alive");
        String userAgent = request.header("User-Agent");
        if (userAgent != null) {
            result.header("User-Agent", userAgent);
        }
        String proxyAuthorization = request.header("Proxy-Authorization");
        if (proxyAuthorization != null) {
            result.header("Proxy-Authorization", proxyAuthorization);
        }
        return result.build();
    }

    void connectAndSetOwner(OkHttpClient client, Object owner, Request request) throws RouteException {
        setOwner(owner);
        if (!isConnected()) {
            Request request2 = request;
            connect(client.getConnectTimeout(), client.getReadTimeout(), client.getWriteTimeout(), request2, this.route.address.getConnectionSpecs(), client.getRetryOnConnectionFailure());
            if (isFramed()) {
                client.getConnectionPool().share(this);
            }
            client.routeDatabase().connected(getRoute());
        }
        setTimeouts(client.getReadTimeout(), client.getWriteTimeout());
    }

    boolean isConnected() {
        return this.connected;
    }

    public Route getRoute() {
        return this.route;
    }

    public Socket getSocket() {
        return this.socket;
    }

    BufferedSource rawSource() {
        if (this.httpConnection != null) {
            return this.httpConnection.rawSource();
        }
        throw new UnsupportedOperationException();
    }

    BufferedSink rawSink() {
        if (this.httpConnection != null) {
            return this.httpConnection.rawSink();
        }
        throw new UnsupportedOperationException();
    }

    boolean isAlive() {
        return (this.socket.isClosed() || this.socket.isInputShutdown() || this.socket.isOutputShutdown()) ? false : true;
    }

    boolean isReadable() {
        if (this.httpConnection != null) {
            return this.httpConnection.isReadable();
        }
        return true;
    }

    void resetIdleStartTime() {
        if (this.framedConnection != null) {
            throw new IllegalStateException("framedConnection != null");
        }
        this.idleStartTimeNs = System.nanoTime();
    }

    boolean isIdle() {
        return this.framedConnection != null ? this.framedConnection.isIdle() : true;
    }

    long getIdleStartTimeNs() {
        return this.framedConnection == null ? this.idleStartTimeNs : this.framedConnection.getIdleStartTimeNs();
    }

    public Handshake getHandshake() {
        return this.handshake;
    }

    Transport newTransport(HttpEngine httpEngine) throws IOException {
        if (this.framedConnection != null) {
            return new FramedTransport(httpEngine, this.framedConnection);
        }
        return new HttpTransport(httpEngine, this.httpConnection);
    }

    boolean isFramed() {
        return this.framedConnection != null;
    }

    public Protocol getProtocol() {
        return this.protocol;
    }

    void setProtocol(Protocol protocol) {
        if (protocol == null) {
            throw new IllegalArgumentException("protocol == null");
        }
        this.protocol = protocol;
    }

    void setTimeouts(int readTimeoutMillis, int writeTimeoutMillis) throws RouteException {
        if (!this.connected) {
            throw new IllegalStateException("setTimeouts - not connected");
        } else if (this.httpConnection != null) {
            try {
                this.socket.setSoTimeout(readTimeoutMillis);
                this.httpConnection.setTimeouts(readTimeoutMillis, writeTimeoutMillis);
            } catch (IOException e) {
                throw new RouteException(e);
            }
        }
    }

    void incrementRecycleCount() {
        this.recycleCount++;
    }

    int recycleCount() {
        return this.recycleCount;
    }

    public String toString() {
        String cipherSuite;
        StringBuilder append = new StringBuilder().append("Connection{").append(this.route.address.uriHost).append(":").append(this.route.address.uriPort).append(", proxy=").append(this.route.proxy).append(" hostAddress=").append(this.route.inetSocketAddress.getAddress().getHostAddress()).append(" cipherSuite=");
        if (this.handshake != null) {
            cipherSuite = this.handshake.cipherSuite();
        } else {
            cipherSuite = "none";
        }
        return append.append(cipherSuite).append(" protocol=").append(this.protocol).append('}').toString();
    }
}
