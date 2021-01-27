package com.huawei.okhttp3.internal.connection;

import com.huawei.okhttp3.Address;
import com.huawei.okhttp3.Call;
import com.huawei.okhttp3.CertificatePinner;
import com.huawei.okhttp3.Connection;
import com.huawei.okhttp3.ConnectionSpec;
import com.huawei.okhttp3.EventListener;
import com.huawei.okhttp3.Handshake;
import com.huawei.okhttp3.HttpUrl;
import com.huawei.okhttp3.Interceptor;
import com.huawei.okhttp3.OkHttpClient;
import com.huawei.okhttp3.Protocol;
import com.huawei.okhttp3.Request;
import com.huawei.okhttp3.Response;
import com.huawei.okhttp3.Route;
import com.huawei.okhttp3.internal.Internal;
import com.huawei.okhttp3.internal.Util;
import com.huawei.okhttp3.internal.Version;
import com.huawei.okhttp3.internal.http.ExchangeCodec;
import com.huawei.okhttp3.internal.http1.Http1ExchangeCodec;
import com.huawei.okhttp3.internal.http2.ConnectionShutdownException;
import com.huawei.okhttp3.internal.http2.ErrorCode;
import com.huawei.okhttp3.internal.http2.Http2Connection;
import com.huawei.okhttp3.internal.http2.Http2ExchangeCodec;
import com.huawei.okhttp3.internal.http2.Http2Stream;
import com.huawei.okhttp3.internal.http2.StreamResetException;
import com.huawei.okhttp3.internal.platform.Platform;
import com.huawei.okhttp3.internal.tls.OkHostnameVerifier;
import com.huawei.okhttp3.internal.ws.RealWebSocket;
import com.huawei.okio.BufferedSink;
import com.huawei.okio.BufferedSource;
import com.huawei.okio.Okio;
import java.io.IOException;
import java.net.ConnectException;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownServiceException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

@Deprecated
public final class RealConnection extends Http2Connection.Listener implements Connection {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static final int MAX_TUNNEL_ATTEMPTS = 21;
    private static final String NPE_THROW_WITH_NULL = "throw with null exception";
    private Handshake handshake;
    private Http2Connection http2Connection;
    long idleAtNanos = Long.MAX_VALUE;
    private Protocol protocol;
    private int refusedStreamCount;
    int routeFailureCount;
    private BufferedSink sink;
    private Socket socket;
    private BufferedSource source;

    public RealConnection(RealConnectionPool connectionPool, Route route) {
        this.connectionPool = connectionPool;
        this.route = route;
    }

    public void noNewExchanges() {
        synchronized (this.connectionPool) {
            this.noNewExchanges = true;
        }
    }

    static RealConnection testConnection(RealConnectionPool connectionPool, Route route, Socket socket2, long idleAtNanos2) {
        RealConnection result = new RealConnection(connectionPool, route);
        result.socket = socket2;
        result.idleAtNanos = idleAtNanos2;
        return result;
    }

    /* JADX WARNING: Removed duplicated region for block: B:18:0x0091 A[Catch:{ IOException -> 0x0101 }] */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x00b0  */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x00d6  */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x00ec  */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x0138  */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x013f  */
    /* JADX WARNING: Removed duplicated region for block: B:66:? A[ORIG_RETURN, RETURN, SYNTHETIC] */
    public void connect(int connectTimeout, int readTimeout, int writeTimeout, int pingIntervalMillis, boolean connectionRetryEnabled, Call call, EventListener eventListener) {
        IOException e;
        if (this.protocol == null) {
            List<ConnectionSpec> connectionSpecs = this.route.address().connectionSpecs();
            ConnectionSpecSelector connectionSpecSelector = new ConnectionSpecSelector(connectionSpecs);
            if (this.route.address().sslSocketFactory() == null) {
                if (connectionSpecs.contains(ConnectionSpec.CLEARTEXT)) {
                    String host = this.route.address().url().host();
                    if (!Platform.get().isCleartextTrafficPermitted(host)) {
                        throw new RouteException(new UnknownServiceException("CLEARTEXT communication to " + host + " not permitted by network security policy"));
                    }
                } else {
                    throw new RouteException(new UnknownServiceException("CLEARTEXT communication not enabled for client"));
                }
            } else if (this.route.address().protocols().contains(Protocol.H2_PRIOR_KNOWLEDGE)) {
                throw new RouteException(new UnknownServiceException("H2_PRIOR_KNOWLEDGE cannot be used with HTTPS"));
            }
            RouteException routeException = null;
            do {
                try {
                    if (!this.route.requiresTunnel()) {
                        connectTunnel(connectTimeout, readTimeout, writeTimeout, call, eventListener);
                        if (this.rawSocket == null) {
                            if (!this.route.requiresTunnel() && this.rawSocket == null) {
                                throw new RouteException(new ProtocolException("Too many tunnel connections attempted: 21"));
                            } else if (this.http2Connection != null) {
                                synchronized (this.connectionPool) {
                                    this.allocationLimit = this.http2Connection.maxConcurrentStreams();
                                }
                                return;
                            } else {
                                return;
                            }
                        }
                    } else {
                        try {
                            connectSocket(connectTimeout, readTimeout, call, eventListener);
                        } catch (IOException e2) {
                            e = e2;
                            Util.closeQuietly(this.socket);
                            Util.closeQuietly(this.rawSocket);
                            this.socket = null;
                            this.rawSocket = null;
                            this.source = null;
                            this.sink = null;
                            this.handshake = null;
                            this.protocol = null;
                            this.http2Connection = null;
                            eventListener.connectFailed(call, this.route.socketAddress(), this.route.proxy(), null, e);
                            if (routeException != null) {
                            }
                            throw routeException;
                        }
                    }
                    try {
                        establishProtocol(connectionSpecSelector, pingIntervalMillis, call, eventListener);
                        eventListener.connectEnd(call, this.route.socketAddress(), this.route.proxy(), this.protocol);
                        if (!this.route.requiresTunnel()) {
                        }
                        if (this.http2Connection != null) {
                        }
                    } catch (IOException e3) {
                        e = e3;
                    }
                } catch (IOException e4) {
                    e = e4;
                    Util.closeQuietly(this.socket);
                    Util.closeQuietly(this.rawSocket);
                    this.socket = null;
                    this.rawSocket = null;
                    this.source = null;
                    this.sink = null;
                    this.handshake = null;
                    this.protocol = null;
                    this.http2Connection = null;
                    eventListener.connectFailed(call, this.route.socketAddress(), this.route.proxy(), null, e);
                    if (routeException != null) {
                        routeException = new RouteException(e);
                    } else {
                        routeException.addConnectException(e);
                    }
                    if (!connectionRetryEnabled || connectionSpecSelector.connectionFailed(e)) {
                        throw routeException;
                    }
                    do {
                        if (!this.route.requiresTunnel()) {
                        }
                        establishProtocol(connectionSpecSelector, pingIntervalMillis, call, eventListener);
                        eventListener.connectEnd(call, this.route.socketAddress(), this.route.proxy(), this.protocol);
                        if (!this.route.requiresTunnel()) {
                        }
                        if (this.http2Connection != null) {
                        }
                    } while (connectionSpecSelector.connectionFailed(e));
                    throw routeException;
                }
            } while (connectionSpecSelector.connectionFailed(e));
            throw routeException;
        }
        throw new IllegalStateException("already connected");
    }

    private void connectTunnel(int connectTimeout, int readTimeout, int writeTimeout, Call call, EventListener eventListener) throws IOException {
        Request tunnelRequest = createTunnelRequest();
        HttpUrl url = tunnelRequest.url();
        for (int i = 0; i < 21; i++) {
            connectSocket(connectTimeout, readTimeout, call, eventListener);
            tunnelRequest = createTunnel(readTimeout, writeTimeout, tunnelRequest, url);
            if (tunnelRequest != null) {
                Util.closeQuietly(this.rawSocket);
                this.rawSocket = null;
                this.sink = null;
                this.source = null;
                eventListener.connectEnd(call, this.route.socketAddress(), this.route.proxy(), null);
            } else {
                return;
            }
        }
    }

    private void connectSocket(int connectTimeout, int readTimeout, Call call, EventListener eventListener) throws IOException {
        Socket socket2;
        if (this.concurrentConnect == null || this.connectedRoute != null) {
            this.route = this.connectedRoute != null ? this.connectedRoute : this.route;
            Proxy proxy = this.route.proxy();
            Address address = this.route.address();
            if (proxy.type() == Proxy.Type.DIRECT || proxy.type() == Proxy.Type.HTTP) {
                socket2 = address.socketFactory().createSocket();
            } else {
                socket2 = new Socket(proxy);
            }
            this.rawSocket = socket2;
            eventListener.connectStart(call, this.route.socketAddress(), proxy);
            System.currentTimeMillis();
            this.rawSocket.setSoTimeout(readTimeout);
            try {
                Platform.get().connectSocket(this.rawSocket, this.route.socketAddress(), connectTimeout);
            } catch (ConnectException e) {
                ConnectException ce = new ConnectException("Failed to connect to " + this.route.socketAddress());
                ce.initCause(e);
                throw ce;
            }
        } else {
            System.currentTimeMillis();
            concurrentConnectSocket(connectTimeout, readTimeout, call, eventListener);
        }
        System.currentTimeMillis();
        try {
            this.source = Okio.buffer(Okio.source(this.rawSocket));
            this.sink = Okio.buffer(Okio.sink(this.rawSocket));
        } catch (NullPointerException npe) {
            if (NPE_THROW_WITH_NULL.equals(npe.getMessage())) {
                throw new IOException(npe);
            }
        }
    }

    private void establishProtocol(ConnectionSpecSelector connectionSpecSelector, int pingIntervalMillis, Call call, EventListener eventListener) throws IOException {
        if (this.route.address().sslSocketFactory() != null) {
            eventListener.secureConnectStart(call);
            connectTls(connectionSpecSelector);
            eventListener.secureConnectEnd(call, this.handshake);
            if (this.protocol == Protocol.HTTP_2) {
                startHttp2(pingIntervalMillis);
            }
        } else if (this.route.address().protocols().contains(Protocol.H2_PRIOR_KNOWLEDGE)) {
            this.socket = this.rawSocket;
            this.protocol = Protocol.H2_PRIOR_KNOWLEDGE;
            startHttp2(pingIntervalMillis);
        } else {
            this.socket = this.rawSocket;
            this.protocol = Protocol.HTTP_1_1;
        }
    }

    private void startHttp2(int pingIntervalMillis) throws IOException {
        this.socket.setSoTimeout(0);
        this.http2Connection = new Http2Connection.Builder(true).socket(this.socket, this.route.address().url().host(), this.source, this.sink).listener(this).pingIntervalMillis(pingIntervalMillis).build();
        this.http2Connection.start();
    }

    private void connectTls(ConnectionSpecSelector connectionSpecSelector) throws IOException {
        String maybeProtocol;
        Protocol protocol2;
        Address address = this.route.address();
        try {
            SSLSocket sslSocket = (SSLSocket) address.sslSocketFactory().createSocket(this.rawSocket, address.url().host(), address.url().port(), true);
            String headerHost = address.headerHost();
            if (headerHost == null || headerHost.length() == 0) {
                headerHost = address.url().host();
            }
            ConnectionSpec connectionSpec = connectionSpecSelector.configureSecureSocket(sslSocket);
            if (connectionSpec.supportsTlsExtensions()) {
                Platform.get().configureTlsExtensions(sslSocket, headerHost, address.protocols());
            }
            sslSocket.startHandshake();
            SSLSession sslSocketSession = sslSocket.getSession();
            Handshake unverifiedHandshake = Handshake.get(sslSocketSession);
            if (!address.hostnameVerifier().verify(headerHost, sslSocketSession)) {
                List<Certificate> peerCertificates = unverifiedHandshake.peerCertificates();
                if (!peerCertificates.isEmpty()) {
                    X509Certificate cert = (X509Certificate) peerCertificates.get(0);
                    throw new SSLPeerUnverifiedException("Hostname " + headerHost + " not verified:\n    certificate: " + CertificatePinner.pin(cert) + "\n    DN: " + cert.getSubjectDN().getName() + "\n    subjectAltNames: " + OkHostnameVerifier.allSubjectAltNames(cert));
                }
                throw new SSLPeerUnverifiedException("Hostname " + address.url().host() + " not verified (no certificates)");
            }
            address.certificatePinner().check(address.url().host(), unverifiedHandshake.peerCertificates());
            if (connectionSpec.supportsTlsExtensions()) {
                maybeProtocol = Platform.get().getSelectedProtocol(sslSocket);
            } else {
                maybeProtocol = null;
            }
            this.socket = sslSocket;
            this.source = Okio.buffer(Okio.source(this.socket));
            this.sink = Okio.buffer(Okio.sink(this.socket));
            this.handshake = unverifiedHandshake;
            if (maybeProtocol != null) {
                protocol2 = Protocol.get(maybeProtocol);
            } else {
                protocol2 = Protocol.HTTP_1_1;
            }
            this.protocol = protocol2;
            Platform.get().afterHandshake(sslSocket);
            if (1 == 0) {
                Util.closeQuietly((Socket) sslSocket);
            }
        } catch (AssertionError e) {
            if (Util.isAndroidGetsocknameError(e)) {
                throw new IOException(e);
            }
            throw e;
        } catch (Throwable th) {
            if (0 != 0) {
                Platform.get().afterHandshake(null);
            }
            if (0 == 0) {
                Util.closeQuietly((Socket) null);
            }
            throw th;
        }
    }

    private Request createTunnel(int readTimeout, int writeTimeout, Request tunnelRequest, HttpUrl url) throws IOException {
        Response response;
        String requestLine = "CONNECT " + Util.hostHeader(url, true) + " HTTP/1.1";
        do {
            Http1ExchangeCodec tunnelCodec = new Http1ExchangeCodec(null, null, this.source, this.sink);
            this.source.timeout().timeout((long) readTimeout, TimeUnit.MILLISECONDS);
            this.sink.timeout().timeout((long) writeTimeout, TimeUnit.MILLISECONDS);
            tunnelCodec.writeRequest(tunnelRequest.headers(), requestLine);
            tunnelCodec.finishRequest();
            response = tunnelCodec.readResponseHeaders(false).request(tunnelRequest).build();
            tunnelCodec.skipConnectBody(response);
            int code = response.code();
            if (code != 200) {
                if (code == 407) {
                    tunnelRequest = this.route.address().proxyAuthenticator().authenticate(this.route, response);
                    if (tunnelRequest == null) {
                        throw new IOException("Failed to authenticate with proxy");
                    }
                } else {
                    throw new IOException("Unexpected response code for CONNECT: " + response.code());
                }
            } else if (this.source.getBuffer().exhausted() && this.sink.buffer().exhausted()) {
                return null;
            } else {
                throw new IOException("TLS tunnel buffered too many bytes!");
            }
        } while (!"close".equalsIgnoreCase(response.header("Connection")));
        return tunnelRequest;
    }

    private Request createTunnelRequest() throws IOException {
        Request proxyConnectRequest = new Request.Builder().url(this.route.address().url()).method("CONNECT", null).header("Host", Util.hostHeader(this.route.address().url(), true)).header("Proxy-Connection", "Keep-Alive").header("User-Agent", Version.userAgent()).build();
        Request authenticatedRequest = this.route.address().proxyAuthenticator().authenticate(this.route, new Response.Builder().request(proxyConnectRequest).protocol(Protocol.HTTP_1_1).code(407).message("Preemptive Authenticate").body(Util.EMPTY_RESPONSE).sentRequestAtMillis(-1).receivedResponseAtMillis(-1).header("Proxy-Authenticate", "OkHttp-Preemptive").build());
        return authenticatedRequest != null ? authenticatedRequest : proxyConnectRequest;
    }

    /* access modifiers changed from: package-private */
    public boolean isEligible(Address address, @Nullable List<Route> routes) {
        if (this.transmitters.size() >= this.allocationLimit || this.noNewExchanges || !Internal.instance.equalsNonHost(this.route.address(), address)) {
            return false;
        }
        if (address.url().host().equals(route().address().url().host())) {
            return true;
        }
        if (this.http2Connection == null || routes == null || !routeMatchesAny(routes) || address.hostnameVerifier() != OkHostnameVerifier.INSTANCE || !supportsUrl(address.url())) {
            return false;
        }
        try {
            address.certificatePinner().check(address.url().host(), handshake().peerCertificates());
            return true;
        } catch (SSLPeerUnverifiedException e) {
            return false;
        }
    }

    private boolean routeMatchesAny(List<Route> candidates) {
        int size = candidates.size();
        for (int i = 0; i < size; i++) {
            Route candidate = candidates.get(i);
            if (candidate.proxy().type() == Proxy.Type.DIRECT && this.route.proxy().type() == Proxy.Type.DIRECT && this.route.socketAddress().equals(candidate.socketAddress())) {
                return true;
            }
        }
        return false;
    }

    public boolean supportsUrl(HttpUrl url) {
        if (url.port() != this.route.address().url().port()) {
            return false;
        }
        if (url.host().equals(this.route.address().url().host())) {
            return true;
        }
        if (this.handshake == null || !OkHostnameVerifier.INSTANCE.verify(url.host(), (X509Certificate) this.handshake.peerCertificates().get(0))) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public ExchangeCodec newCodec(OkHttpClient client, Interceptor.Chain chain) throws SocketException {
        Http2Connection http2Connection2 = this.http2Connection;
        if (http2Connection2 != null) {
            return new Http2ExchangeCodec(client, this, chain, http2Connection2);
        }
        this.socket.setSoTimeout(chain.readTimeoutMillis());
        this.source.timeout().timeout((long) chain.readTimeoutMillis(), TimeUnit.MILLISECONDS);
        this.sink.timeout().timeout((long) chain.writeTimeoutMillis(), TimeUnit.MILLISECONDS);
        return new Http1ExchangeCodec(client, this, this.source, this.sink);
    }

    /* access modifiers changed from: package-private */
    public RealWebSocket.Streams newWebSocketStreams(final Exchange exchange) throws SocketException {
        this.socket.setSoTimeout(0);
        noNewExchanges();
        return new RealWebSocket.Streams(true, this.source, this.sink) {
            /* class com.huawei.okhttp3.internal.connection.RealConnection.AnonymousClass1 */

            @Override // java.io.Closeable, java.lang.AutoCloseable
            public void close() throws IOException {
                exchange.bodyComplete(-1, true, true, null);
            }
        };
    }

    @Override // com.huawei.okhttp3.Connection
    public Route route() {
        return this.route;
    }

    public void cancel() {
        if (this.concurrentConnect != null) {
            this.concurrentConnect.cancel();
        }
        Util.closeQuietly(this.rawSocket);
    }

    @Override // com.huawei.okhttp3.Connection
    public Socket socket() {
        return this.socket;
    }

    /* JADX INFO: finally extract failed */
    public boolean isHealthy(boolean doExtensiveChecks) {
        if (this.socket.isClosed() || this.socket.isInputShutdown() || this.socket.isOutputShutdown()) {
            return false;
        }
        Http2Connection http2Connection2 = this.http2Connection;
        if (http2Connection2 != null) {
            return !http2Connection2.isShutdown();
        }
        if (doExtensiveChecks) {
            try {
                int readTimeout = this.socket.getSoTimeout();
                try {
                    this.socket.setSoTimeout(1);
                    if (this.source.exhausted()) {
                        this.socket.setSoTimeout(readTimeout);
                        return false;
                    }
                    this.socket.setSoTimeout(readTimeout);
                    return true;
                } catch (Throwable th) {
                    this.socket.setSoTimeout(readTimeout);
                    throw th;
                }
            } catch (SocketTimeoutException e) {
            } catch (IOException e2) {
                return false;
            }
        }
        return true;
    }

    @Override // com.huawei.okhttp3.internal.http2.Http2Connection.Listener
    public void onStream(Http2Stream stream) throws IOException {
        stream.close(ErrorCode.REFUSED_STREAM, null);
    }

    @Override // com.huawei.okhttp3.internal.http2.Http2Connection.Listener
    public void onSettings(Http2Connection connection) {
        synchronized (this.connectionPool) {
            this.allocationLimit = connection.maxConcurrentStreams();
        }
    }

    @Override // com.huawei.okhttp3.Connection
    public Handshake handshake() {
        return this.handshake;
    }

    public boolean isMultiplexed() {
        return this.http2Connection != null;
    }

    /* access modifiers changed from: package-private */
    public void trackFailure(@Nullable IOException e) {
        synchronized (this.connectionPool) {
            if (e instanceof StreamResetException) {
                ErrorCode errorCode = ((StreamResetException) e).errorCode;
                if (errorCode == ErrorCode.REFUSED_STREAM) {
                    this.refusedStreamCount++;
                    if (this.refusedStreamCount > 1) {
                        this.noNewExchanges = true;
                        this.routeFailureCount++;
                    }
                } else if (errorCode != ErrorCode.CANCEL) {
                    this.noNewExchanges = true;
                    this.routeFailureCount++;
                }
            } else {
                if (isMultiplexed()) {
                    if (e instanceof ConnectionShutdownException) {
                    }
                }
                this.noNewExchanges = true;
                if (this.successCount == 0) {
                    if (e != null) {
                        this.connectionPool.connectFailed(this.route, e);
                    }
                    this.routeFailureCount++;
                }
            }
        }
    }

    @Override // com.huawei.okhttp3.Connection
    public Protocol protocol() {
        return this.protocol;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Connection{");
        sb.append(this.route.address().url().host());
        sb.append(":");
        sb.append(this.route.address().url().port());
        sb.append(", proxy=");
        sb.append(this.route.proxy());
        sb.append(" hostAddress=");
        sb.append(this.route.socketAddress());
        sb.append(" cipherSuite=");
        Handshake handshake2 = this.handshake;
        sb.append(handshake2 != null ? handshake2.cipherSuite() : "none");
        sb.append(" protocol=");
        sb.append(this.protocol);
        sb.append('}');
        return sb.toString();
    }
}
