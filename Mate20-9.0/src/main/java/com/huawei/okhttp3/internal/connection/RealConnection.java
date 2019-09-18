package com.huawei.okhttp3.internal.connection;

import com.huawei.okhttp3.Address;
import com.huawei.okhttp3.Call;
import com.huawei.okhttp3.CertificatePinner;
import com.huawei.okhttp3.Connection;
import com.huawei.okhttp3.ConnectionPool;
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
import com.huawei.okhttp3.internal.connection.RouteSelector;
import com.huawei.okhttp3.internal.http.HttpCodec;
import com.huawei.okhttp3.internal.http.HttpHeaders;
import com.huawei.okhttp3.internal.http1.Http1Codec;
import com.huawei.okhttp3.internal.http2.ErrorCode;
import com.huawei.okhttp3.internal.http2.Http2Codec;
import com.huawei.okhttp3.internal.http2.Http2Connection;
import com.huawei.okhttp3.internal.http2.Http2Stream;
import com.huawei.okhttp3.internal.platform.Platform;
import com.huawei.okhttp3.internal.tls.OkHostnameVerifier;
import com.huawei.okhttp3.internal.ws.RealWebSocket;
import com.huawei.okio.BufferedSink;
import com.huawei.okio.BufferedSource;
import com.huawei.okio.Okio;
import com.huawei.okio.Source;
import java.io.IOException;
import java.lang.ref.Reference;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownServiceException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

public final class RealConnection extends Http2Connection.Listener implements Connection {
    private static final int MAX_TUNNEL_ATTEMPTS = 21;
    private static final String NPE_THROW_WITH_NULL = "throw with null exception";
    public static final long maxReserveDurationNs = 1000000000;
    public int allocationLimit = 1;
    public final List<Reference<StreamAllocation>> allocations = new ArrayList();
    private ConcurrentConnect concurrentConnect = null;
    private Route connectedRoute = null;
    private final ConnectionPool connectionPool;
    private Handshake handshake;
    private Http2Connection http2Connection;
    public long idleAtNanos = Long.MAX_VALUE;
    public long keepaliveTimestampNs = 0;
    public boolean noNewStreams;
    private Protocol protocol;
    private Socket rawSocket;
    private Route route;
    private RouteSelector.Selection routeSelection = null;
    private BufferedSink sink;
    private Socket socket;
    private BufferedSource source;
    public int successCount;

    public RealConnection(ConnectionPool connectionPool2, Route route2) {
        this.connectionPool = connectionPool2;
        this.route = route2;
    }

    public static RealConnection testConnection(ConnectionPool connectionPool2, Route route2, Socket socket2, long idleAtNanos2) {
        RealConnection result = new RealConnection(connectionPool2, route2);
        result.socket = socket2;
        result.idleAtNanos = idleAtNanos2;
        return result;
    }

    public void prepareConcurrentConnect(ArrayList<InetSocketAddress> concurrentInetSocketAddresses, int connectionAttemptDelay) {
        if (concurrentInetSocketAddresses != null) {
            this.concurrentConnect = new ConcurrentConnect(concurrentInetSocketAddresses, connectionAttemptDelay);
        }
    }

    public void setRouteSelection(RouteSelector.Selection routeSelection2) {
        this.routeSelection = routeSelection2;
    }

    /* JADX WARNING: Removed duplicated region for block: B:36:0x00ea  */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x0134  */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x013b  */
    /* JADX WARNING: Removed duplicated region for block: B:55:0x0140  */
    /* JADX WARNING: Removed duplicated region for block: B:64:0x0148 A[EDGE_INSN: B:64:0x0148->B:57:0x0148 ?: BREAK  , SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:66:? A[ORIG_RETURN, RETURN, SYNTHETIC] */
    public void connect(int connectTimeout, int readTimeout, int writeTimeout, int pingIntervalMillis, boolean connectionRetryEnabled, Call call, EventListener eventListener) {
        Call call2 = call;
        EventListener eventListener2 = eventListener;
        if (this.protocol == null) {
            List<ConnectionSpec> connectionSpecs = this.route.address().connectionSpecs();
            ConnectionSpecSelector connectionSpecSelector = new ConnectionSpecSelector(connectionSpecs);
            if (this.route.address().sslSocketFactory() == null) {
                if (connectionSpecs.contains(ConnectionSpec.CLEARTEXT)) {
                    if (!Platform.get().isCleartextTrafficPermitted(this.route.address().url().host())) {
                        throw new RouteException(new UnknownServiceException("CLEARTEXT communication to " + host + " not permitted by network security policy"));
                    }
                } else {
                    throw new RouteException(new UnknownServiceException("CLEARTEXT communication not enabled for client"));
                }
            } else if (this.route.address().protocols().contains(Protocol.H2_PRIOR_KNOWLEDGE)) {
                int i = connectTimeout;
                int i2 = readTimeout;
                int i3 = pingIntervalMillis;
                throw new RouteException(new UnknownServiceException("H2_PRIOR_KNOWLEDGE cannot be used with HTTPS"));
            }
            RouteException routeException = null;
            do {
                try {
                    if (this.route.requiresTunnel()) {
                        connectTunnel(connectTimeout, readTimeout, writeTimeout, call2, eventListener2);
                        if (this.rawSocket == null) {
                            int i4 = connectTimeout;
                            int i5 = readTimeout;
                            int i6 = pingIntervalMillis;
                            if (!this.route.requiresTunnel() && this.rawSocket == null) {
                                throw new RouteException(new ProtocolException("Too many tunnel connections attempted: 21"));
                            } else if (this.http2Connection == null) {
                                synchronized (this.connectionPool) {
                                    this.allocationLimit = this.http2Connection.maxConcurrentStreams();
                                }
                                return;
                            } else {
                                return;
                            }
                        } else {
                            int i7 = connectTimeout;
                            int i8 = readTimeout;
                        }
                    } else {
                        try {
                            connectSocket(connectTimeout, readTimeout, call2, eventListener2);
                        } catch (IOException e) {
                            e = e;
                            int i9 = pingIntervalMillis;
                            Util.closeQuietly(this.socket);
                            Util.closeQuietly(this.rawSocket);
                            this.socket = null;
                            this.rawSocket = null;
                            this.source = null;
                            this.sink = null;
                            this.handshake = null;
                            this.protocol = null;
                            this.http2Connection = null;
                            eventListener2.connectFailed(call2, this.route.socketAddress(), this.route.proxy(), null, e);
                            if (routeException != null) {
                            }
                            if (connectionRetryEnabled) {
                            }
                            throw routeException;
                        }
                    }
                    try {
                        establishProtocol(connectionSpecSelector, pingIntervalMillis, call2, eventListener2);
                        eventListener2.connectEnd(call2, this.route.socketAddress(), this.route.proxy(), this.protocol);
                        if (!this.route.requiresTunnel()) {
                        }
                        if (this.http2Connection == null) {
                        }
                    } catch (IOException e2) {
                        e = e2;
                    }
                } catch (IOException e3) {
                    e = e3;
                    int i10 = connectTimeout;
                    int i11 = readTimeout;
                    int i92 = pingIntervalMillis;
                    Util.closeQuietly(this.socket);
                    Util.closeQuietly(this.rawSocket);
                    this.socket = null;
                    this.rawSocket = null;
                    this.source = null;
                    this.sink = null;
                    this.handshake = null;
                    this.protocol = null;
                    this.http2Connection = null;
                    eventListener2.connectFailed(call2, this.route.socketAddress(), this.route.proxy(), null, e);
                    if (routeException != null) {
                        routeException = new RouteException(e);
                    } else {
                        routeException.addConnectException(e);
                    }
                    if (connectionRetryEnabled) {
                        break;
                    } else if (!connectionSpecSelector.connectionFailed(e)) {
                    }
                    throw routeException;
                }
            } while (!connectionSpecSelector.connectionFailed(e));
            throw routeException;
        }
        int i12 = connectTimeout;
        int i13 = readTimeout;
        int i14 = pingIntervalMillis;
        throw new IllegalStateException("already connected");
    }

    private void connectTunnel(int connectTimeout, int readTimeout, int writeTimeout, Call call, EventListener eventListener) throws IOException {
        Request tunnelRequest = createTunnelRequest();
        HttpUrl url = tunnelRequest.url();
        int i = 0;
        while (i < 21) {
            connectSocket(connectTimeout, readTimeout, call, eventListener);
            tunnelRequest = createTunnel(readTimeout, writeTimeout, tunnelRequest, url);
            if (tunnelRequest != null) {
                Util.closeQuietly(this.rawSocket);
                this.rawSocket = null;
                this.sink = null;
                this.source = null;
                eventListener.connectEnd(call, this.route.socketAddress(), this.route.proxy(), null);
                i++;
            } else {
                return;
            }
        }
    }

    private void connectSocket(int connectTimeout, int readTimeout, Call call, EventListener eventListener) throws IOException {
        Socket socket2;
        if (this.concurrentConnect == null || this.connectedRoute != null) {
            Route targetRoute = this.connectedRoute != null ? this.connectedRoute : this.route;
            Proxy proxy = targetRoute.proxy();
            Address address = targetRoute.address();
            if (proxy.type() == Proxy.Type.DIRECT || proxy.type() == Proxy.Type.HTTP) {
                socket2 = address.socketFactory().createSocket();
            } else {
                socket2 = new Socket(proxy);
            }
            this.rawSocket = socket2;
            eventListener.connectStart(call, this.route.socketAddress(), proxy);
            this.rawSocket.setSoTimeout(readTimeout);
            try {
                Platform.get().connectSocket(this.rawSocket, targetRoute.socketAddress(), connectTimeout);
            } catch (ConnectException e) {
                ConnectException ce = new ConnectException("Failed to connect to " + targetRoute.socketAddress());
                ce.initCause(e);
                throw ce;
            }
        } else {
            this.rawSocket = this.concurrentConnect.getConnectedSocket((long) connectTimeout);
            if (this.routeSelection != null) {
                this.routeSelection.setFailedTcpAddresses(this.concurrentConnect.failedAddressList());
                if (this.rawSocket != null) {
                    this.routeSelection.setConnectedTcpAddress((InetSocketAddress) this.rawSocket.getRemoteSocketAddress());
                }
            }
            if (this.rawSocket != null) {
                this.connectedRoute = new Route(this.route.address(), this.route.proxy(), (InetSocketAddress) this.rawSocket.getRemoteSocketAddress());
                this.route = this.connectedRoute;
                this.rawSocket.setSoTimeout(readTimeout);
            } else {
                throw new ConnectException("Failed to connect to host " + this.route.address().url().host());
            }
        }
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
        Protocol protocol2;
        Address address = this.route.address();
        String maybeProtocol = null;
        SSLSocket sslSocket = null;
        try {
            sslSocket = (SSLSocket) address.sslSocketFactory().createSocket(this.rawSocket, address.url().host(), address.url().port(), true);
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
            if (address.hostnameVerifier().verify(headerHost, sslSocketSession)) {
                address.certificatePinner().check(address.url().host(), unverifiedHandshake.peerCertificates());
                if (connectionSpec.supportsTlsExtensions()) {
                    maybeProtocol = Platform.get().getSelectedProtocol(sslSocket);
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
                if (sslSocket != null) {
                    Platform.get().afterHandshake(sslSocket);
                }
                if (1 == 0) {
                    Util.closeQuietly((Socket) sslSocket);
                    return;
                }
                return;
            }
            X509Certificate cert = (X509Certificate) unverifiedHandshake.peerCertificates().get(0);
            throw new SSLPeerUnverifiedException("Hostname " + address.url().host() + " not verified:\n    certificate: " + CertificatePinner.pin(cert) + "\n    DN: " + cert.getSubjectDN().getName() + "\n    subjectAltNames: " + OkHostnameVerifier.allSubjectAltNames(cert));
        } catch (AssertionError e) {
            if (Util.isAndroidGetsocknameError(e)) {
                throw new IOException(e);
            }
            throw e;
        } catch (Throwable th) {
            if (sslSocket != null) {
                Platform.get().afterHandshake(sslSocket);
            }
            if (0 == 0) {
                Util.closeQuietly((Socket) sslSocket);
            }
            throw th;
        }
    }

    private Request createTunnel(int readTimeout, int writeTimeout, Request tunnelRequest, HttpUrl url) throws IOException {
        Response response;
        String requestLine = "CONNECT " + Util.hostHeader(url, true) + " HTTP/1.1";
        do {
            Http1Codec tunnelConnection = new Http1Codec(null, null, this.source, this.sink);
            this.source.timeout().timeout((long) readTimeout, TimeUnit.MILLISECONDS);
            this.sink.timeout().timeout((long) writeTimeout, TimeUnit.MILLISECONDS);
            tunnelConnection.writeRequest(tunnelRequest.headers(), requestLine);
            tunnelConnection.finishRequest();
            response = tunnelConnection.readResponseHeaders(false).request(tunnelRequest).build();
            long contentLength = HttpHeaders.contentLength(response);
            if (contentLength == -1) {
                contentLength = 0;
            }
            Source body = tunnelConnection.newFixedLengthSource(contentLength);
            Util.skipAll(body, Integer.MAX_VALUE, TimeUnit.MILLISECONDS);
            body.close();
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
            } else if (this.source.buffer().exhausted() && this.sink.buffer().exhausted()) {
                return null;
            } else {
                throw new IOException("TLS tunnel buffered too many bytes!");
            }
        } while (!"close".equalsIgnoreCase(response.header("Connection")));
        return tunnelRequest;
    }

    private Request createTunnelRequest() {
        return new Request.Builder().url(this.route.address().url()).header("Host", Util.hostHeader(this.route.address().url(), true)).header("Proxy-Connection", "Keep-Alive").header("User-Agent", Version.userAgent()).build();
    }

    public boolean isEligible(Address address, @Nullable Route route2) {
        if (this.allocations.size() >= this.allocationLimit || this.noNewStreams || !Internal.instance.equalsNonHost(this.route.address(), address)) {
            return false;
        }
        if (address.url().host().equals(route().address().url().host())) {
            return true;
        }
        if (this.http2Connection == null || route2 == null || route2.proxy().type() != Proxy.Type.DIRECT || this.route.proxy().type() != Proxy.Type.DIRECT || !this.route.socketAddress().equals(route2.socketAddress()) || route2.address().hostnameVerifier() != OkHostnameVerifier.INSTANCE || !supportsUrl(address.url())) {
            return false;
        }
        try {
            address.certificatePinner().check(address.url().host(), handshake().peerCertificates());
            return true;
        } catch (SSLPeerUnverifiedException e) {
            return false;
        }
    }

    public boolean supportsUrl(HttpUrl url) {
        if (url.port() != this.route.address().url().port()) {
            return false;
        }
        boolean z = true;
        if (url.host().equals(this.route.address().url().host())) {
            return true;
        }
        if (this.handshake == null || !OkHostnameVerifier.INSTANCE.verify(url.host(), (X509Certificate) this.handshake.peerCertificates().get(0))) {
            z = false;
        }
        return z;
    }

    public HttpCodec newCodec(OkHttpClient client, Interceptor.Chain chain, StreamAllocation streamAllocation) throws SocketException {
        if (this.http2Connection != null) {
            return new Http2Codec(client, chain, streamAllocation, this.http2Connection);
        }
        this.socket.setSoTimeout(chain.readTimeoutMillis());
        this.source.timeout().timeout((long) chain.readTimeoutMillis(), TimeUnit.MILLISECONDS);
        this.sink.timeout().timeout((long) chain.writeTimeoutMillis(), TimeUnit.MILLISECONDS);
        return new Http1Codec(client, streamAllocation, this.source, this.sink);
    }

    public RealWebSocket.Streams newWebSocketStreams(StreamAllocation streamAllocation) {
        final StreamAllocation streamAllocation2 = streamAllocation;
        AnonymousClass1 r0 = new RealWebSocket.Streams(true, this.source, this.sink) {
            public void close() throws IOException {
                streamAllocation2.streamFinished(true, streamAllocation2.codec(), -1, null);
            }
        };
        return r0;
    }

    public Route route() {
        return this.route;
    }

    public void cancel() {
        if (this.concurrentConnect != null) {
            this.concurrentConnect.cancel();
        }
        Util.closeQuietly(this.rawSocket);
    }

    public Socket socket() {
        return this.socket;
    }

    public boolean isHealthy(boolean doExtensiveChecks) {
        int readTimeout;
        if (this.socket.isClosed() || this.socket.isInputShutdown() || this.socket.isOutputShutdown()) {
            return false;
        }
        if (this.http2Connection != null) {
            return !this.http2Connection.isShutdown();
        }
        if (doExtensiveChecks) {
            try {
                readTimeout = this.socket.getSoTimeout();
                this.socket.setSoTimeout(1);
                if (this.source.exhausted()) {
                    this.socket.setSoTimeout(readTimeout);
                    return false;
                }
                this.socket.setSoTimeout(readTimeout);
                return true;
            } catch (SocketTimeoutException e) {
            } catch (IOException e2) {
                return false;
            } catch (Throwable th) {
                this.socket.setSoTimeout(readTimeout);
                throw th;
            }
        }
        return true;
    }

    public void onStream(Http2Stream stream) throws IOException {
        stream.close(ErrorCode.REFUSED_STREAM);
    }

    public void onSettings(Http2Connection connection) {
        synchronized (this.connectionPool) {
            this.allocationLimit = connection.maxConcurrentStreams();
        }
    }

    public Handshake handshake() {
        return this.handshake;
    }

    public boolean isMultiplexed() {
        return this.http2Connection != null;
    }

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
        sb.append(this.handshake != null ? this.handshake.cipherSuite() : "none");
        sb.append(" protocol=");
        sb.append(this.protocol);
        sb.append('}');
        return sb.toString();
    }
}
