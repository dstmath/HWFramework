package com.huawei.okhttp3;

import com.huawei.okhttp3.Call;
import com.huawei.okhttp3.EventListener;
import com.huawei.okhttp3.Headers;
import com.huawei.okhttp3.OkHttpClientBase;
import com.huawei.okhttp3.Response;
import com.huawei.okhttp3.WebSocket;
import com.huawei.okhttp3.internal.Internal;
import com.huawei.okhttp3.internal.Util;
import com.huawei.okhttp3.internal.cache.InternalCache;
import com.huawei.okhttp3.internal.connection.Exchange;
import com.huawei.okhttp3.internal.connection.RealConnectionPool;
import com.huawei.okhttp3.internal.platform.Platform;
import com.huawei.okhttp3.internal.proxy.NullProxySelector;
import com.huawei.okhttp3.internal.tls.CertificateChainCleaner;
import com.huawei.okhttp3.internal.tls.OkHostnameVerifier;
import com.huawei.okhttp3.internal.ws.RealWebSocket;
import java.net.Proxy;
import java.net.ProxySelector;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import javax.net.SocketFactory;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

@Deprecated
public class OkHttpClient extends OkHttpClientBase implements Cloneable, Call.Factory, WebSocket.Factory {
    static final List<ConnectionSpec> DEFAULT_CONNECTION_SPECS = Util.immutableList(ConnectionSpec.MODERN_TLS, ConnectionSpec.CLEARTEXT);
    static final List<Protocol> DEFAULT_PROTOCOLS = Util.immutableList(Protocol.HTTP_2, Protocol.HTTP_1_1);
    final Authenticator authenticator;
    @Nullable
    final Cache cache;
    final int callTimeout;
    final CertificateChainCleaner certificateChainCleaner;
    final CertificatePinner certificatePinner;
    final int connectTimeout;
    final List<ConnectionSpec> connectionSpecs;
    final CookieJar cookieJar;
    final Dns dns;
    final EventListener.Factory eventListenerFactory;
    final boolean followRedirects;
    final boolean followSslRedirects;
    final HostnameVerifier hostnameVerifier;
    final List<Interceptor> interceptors;
    @Nullable
    final InternalCache internalCache;
    final List<Interceptor> networkInterceptors;
    final int pingInterval;
    final List<Protocol> protocols;
    @Nullable
    final Proxy proxy;
    final Authenticator proxyAuthenticator;
    final ProxySelector proxySelector;
    final int readTimeout;
    final boolean retryOnConnectionFailure;
    final SocketFactory socketFactory;
    final SSLSocketFactory sslSocketFactory;
    final int writeTimeout;

    static {
        Internal.instance = new Internal() {
            /* class com.huawei.okhttp3.OkHttpClient.AnonymousClass1 */

            @Override // com.huawei.okhttp3.internal.Internal
            public void addLenient(Headers.Builder builder, String line) {
                builder.addLenient(line);
            }

            @Override // com.huawei.okhttp3.internal.Internal
            public void addLenient(Headers.Builder builder, String name, String value) {
                builder.addLenient(name, value);
            }

            @Override // com.huawei.okhttp3.internal.Internal
            public RealConnectionPool realConnectionPool(ConnectionPool connectionPool) {
                return connectionPool.delegate;
            }

            @Override // com.huawei.okhttp3.internal.Internal
            public boolean equalsNonHost(Address a, Address b) {
                return a.equalsNonHost(b);
            }

            @Override // com.huawei.okhttp3.internal.Internal
            public int code(Response.Builder responseBuilder) {
                return responseBuilder.code;
            }

            @Override // com.huawei.okhttp3.internal.Internal
            public void apply(ConnectionSpec tlsConfiguration, SSLSocket sslSocket, boolean isFallback) {
                tlsConfiguration.apply(sslSocket, isFallback);
            }

            @Override // com.huawei.okhttp3.internal.Internal
            public Call newWebSocketCall(OkHttpClient client, Request originalRequest) {
                return RealCall.newRealCall(client, originalRequest, true);
            }

            @Override // com.huawei.okhttp3.internal.Internal
            public void initExchange(Response.Builder responseBuilder, Exchange exchange) {
                responseBuilder.initExchange(exchange);
            }

            @Override // com.huawei.okhttp3.internal.Internal
            @Nullable
            public Exchange exchange(Response response) {
                return response.exchange;
            }
        };
    }

    public OkHttpClient() {
        this(new Builder());
    }

    OkHttpClient(Builder builder) {
        super(builder);
        this.dispatcher = builder.dispatcher;
        this.proxy = builder.proxy;
        this.protocols = builder.protocols;
        this.connectionSpecs = builder.connectionSpecs;
        this.interceptors = Util.immutableList(builder.interceptors);
        this.networkInterceptors = Util.immutableList(builder.networkInterceptors);
        this.eventListenerFactory = builder.eventListenerFactory;
        this.proxySelector = builder.proxySelector;
        this.cookieJar = builder.cookieJar;
        this.cache = builder.cache;
        this.internalCache = builder.internalCache;
        this.socketFactory = builder.socketFactory;
        boolean isTLS = false;
        for (ConnectionSpec spec : this.connectionSpecs) {
            isTLS = isTLS || spec.isTls();
        }
        if (builder.sslSocketFactory != null || !isTLS) {
            this.sslSocketFactory = builder.sslSocketFactory;
            this.certificateChainCleaner = builder.certificateChainCleaner;
        } else {
            X509TrustManager trustManager = Util.platformTrustManager();
            this.sslSocketFactory = newSslSocketFactory(trustManager);
            this.certificateChainCleaner = CertificateChainCleaner.get(trustManager);
        }
        if (this.sslSocketFactory != null) {
            Platform.get().configureSslSocketFactory(this.sslSocketFactory);
        }
        this.hostnameVerifier = builder.hostnameVerifier;
        this.certificatePinner = builder.certificatePinner.withCertificateChainCleaner(this.certificateChainCleaner);
        this.proxyAuthenticator = builder.proxyAuthenticator;
        this.authenticator = builder.authenticator;
        this.dns = builder.dns;
        this.followSslRedirects = builder.followSslRedirects;
        this.followRedirects = builder.followRedirects;
        this.retryOnConnectionFailure = builder.retryOnConnectionFailure;
        this.callTimeout = builder.callTimeout;
        this.connectTimeout = builder.connectTimeout;
        this.readTimeout = builder.readTimeout;
        this.writeTimeout = builder.writeTimeout;
        this.pingInterval = builder.pingInterval;
        if (this.interceptors.contains(null)) {
            throw new IllegalStateException("Null interceptor: " + this.interceptors);
        } else if (this.networkInterceptors.contains(null)) {
            throw new IllegalStateException("Null network interceptor: " + this.networkInterceptors);
        }
    }

    private static SSLSocketFactory newSslSocketFactory(X509TrustManager trustManager) {
        try {
            SSLContext sslContext = Platform.get().getSSLContext();
            sslContext.init(null, new TrustManager[]{trustManager}, null);
            return sslContext.getSocketFactory();
        } catch (GeneralSecurityException e) {
            throw new AssertionError("No System TLS", e);
        }
    }

    public int callTimeoutMillis() {
        return this.callTimeout;
    }

    public int connectTimeoutMillis() {
        return this.connectTimeout;
    }

    public int readTimeoutMillis() {
        return this.readTimeout;
    }

    public int writeTimeoutMillis() {
        return this.writeTimeout;
    }

    public int pingIntervalMillis() {
        return this.pingInterval;
    }

    @Nullable
    public Proxy proxy() {
        return this.proxy;
    }

    public ProxySelector proxySelector() {
        return this.proxySelector;
    }

    public CookieJar cookieJar() {
        return this.cookieJar;
    }

    @Nullable
    public Cache cache() {
        return this.cache;
    }

    /* access modifiers changed from: package-private */
    @Nullable
    public InternalCache internalCache() {
        Cache cache2 = this.cache;
        return cache2 != null ? cache2.internalCache : this.internalCache;
    }

    public Dns dns() {
        return this.dns;
    }

    public SocketFactory socketFactory() {
        return this.socketFactory;
    }

    public SSLSocketFactory sslSocketFactory() {
        return this.sslSocketFactory;
    }

    public HostnameVerifier hostnameVerifier() {
        return this.hostnameVerifier;
    }

    public CertificatePinner certificatePinner() {
        return this.certificatePinner;
    }

    public Authenticator authenticator() {
        return this.authenticator;
    }

    public Authenticator proxyAuthenticator() {
        return this.proxyAuthenticator;
    }

    public ConnectionPool connectionPool() {
        return this.connectionPool;
    }

    public boolean followSslRedirects() {
        return this.followSslRedirects;
    }

    public boolean followRedirects() {
        return this.followRedirects;
    }

    public boolean retryOnConnectionFailure() {
        return this.retryOnConnectionFailure;
    }

    public AbsDispatcher dispatcher() {
        return this.dispatcher;
    }

    public List<Protocol> protocols() {
        return this.protocols;
    }

    public List<ConnectionSpec> connectionSpecs() {
        return this.connectionSpecs;
    }

    public List<Interceptor> interceptors() {
        return this.interceptors;
    }

    public List<Interceptor> networkInterceptors() {
        return this.networkInterceptors;
    }

    public EventListener.Factory eventListenerFactory() {
        return this.eventListenerFactory;
    }

    @Override // com.huawei.okhttp3.Call.Factory
    public Call newCall(Request request) {
        return RealCall.newRealCall(this, request, false);
    }

    @Override // com.huawei.okhttp3.WebSocket.Factory
    public WebSocket newWebSocket(Request request, WebSocketListener listener) {
        RealWebSocket webSocket = new RealWebSocket(request, listener, new Random(), (long) this.pingInterval);
        webSocket.connect(this);
        return webSocket;
    }

    public Builder newBuilder() {
        return new Builder(this);
    }

    public static final class Builder extends OkHttpClientBase.BuilderBase<Builder> {
        Authenticator authenticator;
        @Nullable
        Cache cache;
        int callTimeout;
        @Nullable
        CertificateChainCleaner certificateChainCleaner;
        CertificatePinner certificatePinner;
        List<ConnectionSpec> connectionSpecs;
        CookieJar cookieJar;
        AbsDispatcher dispatcher;
        Dns dns;
        EventListener.Factory eventListenerFactory;
        boolean followRedirects;
        boolean followSslRedirects;
        HostnameVerifier hostnameVerifier;
        final List<Interceptor> interceptors;
        @Nullable
        InternalCache internalCache;
        final List<Interceptor> networkInterceptors;
        int pingInterval;
        List<Protocol> protocols;
        @Nullable
        Proxy proxy;
        Authenticator proxyAuthenticator;
        ProxySelector proxySelector;
        int readTimeout;
        boolean retryOnConnectionFailure;
        SocketFactory socketFactory;
        @Nullable
        SSLSocketFactory sslSocketFactory;
        int writeTimeout;

        public Builder() {
            this.interceptors = new ArrayList();
            this.networkInterceptors = new ArrayList();
            this.dispatcher = new Dispatcher();
            this.protocols = OkHttpClient.DEFAULT_PROTOCOLS;
            this.connectionSpecs = OkHttpClient.DEFAULT_CONNECTION_SPECS;
            this.eventListenerFactory = EventListener.factory(EventListener.NONE);
            this.proxySelector = ProxySelector.getDefault();
            if (this.proxySelector == null) {
                this.proxySelector = new NullProxySelector();
            }
            this.cookieJar = CookieJar.NO_COOKIES;
            this.socketFactory = SocketFactory.getDefault();
            this.hostnameVerifier = OkHostnameVerifier.INSTANCE;
            this.certificatePinner = CertificatePinner.DEFAULT;
            this.proxyAuthenticator = Authenticator.NONE;
            this.authenticator = Authenticator.NONE;
            this.dns = Dns.SYSTEM;
            this.followSslRedirects = true;
            this.followRedirects = true;
            this.retryOnConnectionFailure = true;
            this.callTimeout = 0;
            this.connectTimeout = 10000;
            this.readTimeout = 10000;
            this.writeTimeout = 10000;
            this.pingInterval = 0;
        }

        Builder(OkHttpClient okHttpClient) {
            this.interceptors = new ArrayList();
            this.networkInterceptors = new ArrayList();
            this.dispatcher = okHttpClient.dispatcher;
            this.proxy = okHttpClient.proxy;
            this.protocols = okHttpClient.protocols;
            this.connectionSpecs = okHttpClient.connectionSpecs;
            this.interceptors.addAll(okHttpClient.interceptors);
            this.networkInterceptors.addAll(okHttpClient.networkInterceptors);
            this.eventListenerFactory = okHttpClient.eventListenerFactory;
            this.proxySelector = okHttpClient.proxySelector;
            this.cookieJar = okHttpClient.cookieJar;
            this.internalCache = okHttpClient.internalCache;
            this.cache = okHttpClient.cache;
            this.socketFactory = okHttpClient.socketFactory;
            this.sslSocketFactory = okHttpClient.sslSocketFactory;
            this.certificateChainCleaner = okHttpClient.certificateChainCleaner;
            this.hostnameVerifier = okHttpClient.hostnameVerifier;
            this.certificatePinner = okHttpClient.certificatePinner;
            this.proxyAuthenticator = okHttpClient.proxyAuthenticator;
            this.authenticator = okHttpClient.authenticator;
            this.connectionPool = okHttpClient.connectionPool;
            this.dns = okHttpClient.dns;
            this.followSslRedirects = okHttpClient.followSslRedirects;
            this.followRedirects = okHttpClient.followRedirects;
            this.retryOnConnectionFailure = okHttpClient.retryOnConnectionFailure;
            this.callTimeout = okHttpClient.callTimeout;
            this.connectTimeout = okHttpClient.connectTimeout;
            this.readTimeout = okHttpClient.readTimeout;
            this.writeTimeout = okHttpClient.writeTimeout;
            this.pingInterval = okHttpClient.pingInterval;
        }

        public Builder callTimeout(long timeout, TimeUnit unit) {
            this.callTimeout = Util.checkDuration("timeout", timeout, unit);
            return this;
        }

        public Builder callTimeout(Duration duration) {
            this.callTimeout = Util.checkDuration("timeout", duration.toMillis(), TimeUnit.MILLISECONDS);
            return this;
        }

        public Builder connectTimeout(long timeout, TimeUnit unit) {
            this.connectTimeout = Util.checkDuration("timeout", timeout, unit);
            super.checkConnectionAttempt(this.connectTimeout);
            return this;
        }

        public Builder connectTimeout(Duration duration) {
            this.connectTimeout = Util.checkDuration("timeout", duration.toMillis(), TimeUnit.MILLISECONDS);
            super.checkConnectionAttempt(this.connectTimeout);
            return this;
        }

        public Builder readTimeout(long timeout, TimeUnit unit) {
            this.readTimeout = Util.checkDuration("timeout", timeout, unit);
            return this;
        }

        public Builder readTimeout(Duration duration) {
            this.readTimeout = Util.checkDuration("timeout", duration.toMillis(), TimeUnit.MILLISECONDS);
            return this;
        }

        public Builder writeTimeout(long timeout, TimeUnit unit) {
            this.writeTimeout = Util.checkDuration("timeout", timeout, unit);
            return this;
        }

        public Builder writeTimeout(Duration duration) {
            this.writeTimeout = Util.checkDuration("timeout", duration.toMillis(), TimeUnit.MILLISECONDS);
            return this;
        }

        public Builder pingInterval(long interval, TimeUnit unit) {
            this.pingInterval = Util.checkDuration("interval", interval, unit);
            return this;
        }

        public Builder pingInterval(Duration duration) {
            this.pingInterval = Util.checkDuration("timeout", duration.toMillis(), TimeUnit.MILLISECONDS);
            return this;
        }

        public Builder proxy(@Nullable Proxy proxy2) {
            this.proxy = proxy2;
            return this;
        }

        public Builder proxySelector(ProxySelector proxySelector2) {
            if (proxySelector2 != null) {
                this.proxySelector = proxySelector2;
                return this;
            }
            throw new NullPointerException("proxySelector == null");
        }

        public Builder cookieJar(CookieJar cookieJar2) {
            if (cookieJar2 != null) {
                this.cookieJar = cookieJar2;
                return this;
            }
            throw new NullPointerException("cookieJar == null");
        }

        public Builder cache(@Nullable Cache cache2) {
            this.cache = cache2;
            this.internalCache = null;
            return this;
        }

        public Builder dns(Dns dns2) {
            if (dns2 != null) {
                this.dns = dns2;
                return this;
            }
            throw new NullPointerException("dns == null");
        }

        public Builder socketFactory(SocketFactory socketFactory2) {
            if (socketFactory2 == null) {
                throw new NullPointerException("socketFactory == null");
            } else if (!(socketFactory2 instanceof SSLSocketFactory)) {
                this.socketFactory = socketFactory2;
                return this;
            } else {
                throw new IllegalArgumentException("socketFactory instanceof SSLSocketFactory");
            }
        }

        public Builder sslSocketFactory(SSLSocketFactory sslSocketFactory2) {
            if (sslSocketFactory2 != null) {
                this.sslSocketFactory = sslSocketFactory2;
                this.certificateChainCleaner = Platform.get().buildCertificateChainCleaner(sslSocketFactory2);
                return this;
            }
            throw new NullPointerException("sslSocketFactory == null");
        }

        public Builder sslSocketFactory(SSLSocketFactory sslSocketFactory2, X509TrustManager trustManager) {
            if (sslSocketFactory2 == null) {
                throw new NullPointerException("sslSocketFactory == null");
            } else if (trustManager != null) {
                this.sslSocketFactory = sslSocketFactory2;
                this.certificateChainCleaner = CertificateChainCleaner.get(trustManager);
                return this;
            } else {
                throw new NullPointerException("trustManager == null");
            }
        }

        public Builder hostnameVerifier(HostnameVerifier hostnameVerifier2) {
            if (hostnameVerifier2 != null) {
                this.hostnameVerifier = hostnameVerifier2;
                return this;
            }
            throw new NullPointerException("hostnameVerifier == null");
        }

        public Builder certificatePinner(CertificatePinner certificatePinner2) {
            if (certificatePinner2 != null) {
                this.certificatePinner = certificatePinner2;
                return this;
            }
            throw new NullPointerException("certificatePinner == null");
        }

        public Builder authenticator(Authenticator authenticator2) {
            if (authenticator2 != null) {
                this.authenticator = authenticator2;
                return this;
            }
            throw new NullPointerException("authenticator == null");
        }

        public Builder proxyAuthenticator(Authenticator proxyAuthenticator2) {
            if (proxyAuthenticator2 != null) {
                this.proxyAuthenticator = proxyAuthenticator2;
                return this;
            }
            throw new NullPointerException("proxyAuthenticator == null");
        }

        public Builder connectionPool(ConnectionPool connectionPool) {
            if (connectionPool != null) {
                this.connectionPool = connectionPool;
                return this;
            }
            throw new NullPointerException("connectionPool == null");
        }

        public Builder followSslRedirects(boolean followProtocolRedirects) {
            this.followSslRedirects = followProtocolRedirects;
            return this;
        }

        public Builder followRedirects(boolean followRedirects2) {
            this.followRedirects = followRedirects2;
            return this;
        }

        public Builder retryOnConnectionFailure(boolean retryOnConnectionFailure2) {
            this.retryOnConnectionFailure = retryOnConnectionFailure2;
            return this;
        }

        public Builder dispatcher(AbsDispatcher dispatcher2) {
            if (dispatcher2 != null) {
                this.dispatcher = dispatcher2;
                return this;
            }
            throw new IllegalArgumentException("dispatcher == null");
        }

        public Builder protocols(List<Protocol> protocols2) {
            List<Protocol> protocols3 = new ArrayList<>(protocols2);
            if (!protocols3.contains(Protocol.H2_PRIOR_KNOWLEDGE) && !protocols3.contains(Protocol.HTTP_1_1)) {
                throw new IllegalArgumentException("protocols must contain h2_prior_knowledge or http/1.1: " + protocols3);
            } else if (protocols3.contains(Protocol.H2_PRIOR_KNOWLEDGE) && protocols3.size() > 1) {
                throw new IllegalArgumentException("protocols containing h2_prior_knowledge cannot use other protocols: " + protocols3);
            } else if (protocols3.contains(Protocol.HTTP_1_0)) {
                throw new IllegalArgumentException("protocols must not contain http/1.0: " + protocols3);
            } else if (!protocols3.contains(null)) {
                protocols3.remove(Protocol.SPDY_3);
                this.protocols = Collections.unmodifiableList(protocols3);
                return this;
            } else {
                throw new IllegalArgumentException("protocols must not contain null");
            }
        }

        public Builder connectionSpecs(List<ConnectionSpec> connectionSpecs2) {
            this.connectionSpecs = Util.immutableList(connectionSpecs2);
            return this;
        }

        public List<Interceptor> interceptors() {
            return this.interceptors;
        }

        public Builder addInterceptor(Interceptor interceptor) {
            if (interceptor != null) {
                this.interceptors.add(interceptor);
                return this;
            }
            throw new IllegalArgumentException("interceptor == null");
        }

        public List<Interceptor> networkInterceptors() {
            return this.networkInterceptors;
        }

        public Builder addNetworkInterceptor(Interceptor interceptor) {
            if (interceptor != null) {
                this.networkInterceptors.add(interceptor);
                return this;
            }
            throw new IllegalArgumentException("interceptor == null");
        }

        public Builder eventListener(EventListener eventListener) {
            if (eventListener != null) {
                this.eventListenerFactory = EventListener.factory(eventListener);
                return this;
            }
            throw new NullPointerException("eventListener == null");
        }

        public Builder eventListenerFactory(EventListener.Factory eventListenerFactory2) {
            if (eventListenerFactory2 != null) {
                this.eventListenerFactory = eventListenerFactory2;
                return this;
            }
            throw new NullPointerException("eventListenerFactory == null");
        }

        public OkHttpClient build() {
            return new OkHttpClient(this);
        }
    }
}
