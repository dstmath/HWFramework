package com.android.okhttp;

import com.android.okhttp.Headers.Builder;
import com.android.okhttp.internal.Internal;
import com.android.okhttp.internal.InternalCache;
import com.android.okhttp.internal.RouteDatabase;
import com.android.okhttp.internal.Util;
import com.android.okhttp.internal.http.AuthenticatorAdapter;
import com.android.okhttp.internal.http.StreamAllocation;
import com.android.okhttp.internal.io.RealConnection;
import com.android.okhttp.internal.tls.OkHostnameVerifier;
import java.net.CookieHandler;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.net.SocketFactory;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class OkHttpClient implements Cloneable {
    private static final List<ConnectionSpec> DEFAULT_CONNECTION_SPECS = Util.immutableList(ConnectionSpec.MODERN_TLS, ConnectionSpec.COMPATIBLE_TLS, ConnectionSpec.CLEARTEXT);
    private static final List<Protocol> DEFAULT_PROTOCOLS = Util.immutableList(Protocol.HTTP_2, Protocol.SPDY_3, Protocol.HTTP_1_1);
    private static SSLSocketFactory defaultSslSocketFactory;
    private Authenticator authenticator;
    private Cache cache;
    private CertificatePinner certificatePinner;
    private int connectTimeout;
    private ConnectionPool connectionPool;
    private List<ConnectionSpec> connectionSpecs;
    private CookieHandler cookieHandler;
    private Dispatcher dispatcher;
    private Dns dns;
    private boolean followRedirects;
    private boolean followSslRedirects;
    private HostnameVerifier hostnameVerifier;
    private final List<Interceptor> interceptors;
    private InternalCache internalCache;
    private final List<Interceptor> networkInterceptors;
    private List<Protocol> protocols;
    private Proxy proxy;
    private ProxySelector proxySelector;
    private int readTimeout;
    private boolean retryOnConnectionFailure;
    private final RouteDatabase routeDatabase;
    private SocketFactory socketFactory;
    private SSLSocketFactory sslSocketFactory;
    private int writeTimeout;

    static {
        Internal.instance = new Internal() {
            public void addLenient(Builder builder, String line) {
                builder.addLenient(line);
            }

            public void addLenient(Builder builder, String name, String value) {
                builder.addLenient(name, value);
            }

            public void setCache(OkHttpClient client, InternalCache internalCache) {
                client.setInternalCache(internalCache);
            }

            public InternalCache internalCache(OkHttpClient client) {
                return client.internalCache();
            }

            public boolean connectionBecameIdle(ConnectionPool pool, RealConnection connection) {
                return pool.connectionBecameIdle(connection);
            }

            public RealConnection get(ConnectionPool pool, Address address, StreamAllocation streamAllocation) {
                return pool.get(address, streamAllocation);
            }

            public void put(ConnectionPool pool, RealConnection connection) {
                pool.put(connection);
            }

            public RouteDatabase routeDatabase(ConnectionPool connectionPool) {
                return connectionPool.routeDatabase;
            }

            public void callEnqueue(Call call, Callback responseCallback, boolean forWebSocket) {
                call.enqueue(responseCallback, forWebSocket);
            }

            public StreamAllocation callEngineGetStreamAllocation(Call call) {
                return call.engine.streamAllocation;
            }

            public void apply(ConnectionSpec tlsConfiguration, SSLSocket sslSocket, boolean isFallback) {
                tlsConfiguration.apply(sslSocket, isFallback);
            }

            public HttpUrl getHttpUrlChecked(String url) throws MalformedURLException, UnknownHostException {
                return HttpUrl.getChecked(url);
            }
        };
    }

    public OkHttpClient() {
        this.interceptors = new ArrayList();
        this.networkInterceptors = new ArrayList();
        this.followSslRedirects = true;
        this.followRedirects = true;
        this.retryOnConnectionFailure = true;
        this.connectTimeout = 10000;
        this.readTimeout = 10000;
        this.writeTimeout = 10000;
        this.routeDatabase = new RouteDatabase();
        this.dispatcher = new Dispatcher();
    }

    private OkHttpClient(OkHttpClient okHttpClient) {
        this.interceptors = new ArrayList();
        this.networkInterceptors = new ArrayList();
        this.followSslRedirects = true;
        this.followRedirects = true;
        this.retryOnConnectionFailure = true;
        this.connectTimeout = 10000;
        this.readTimeout = 10000;
        this.writeTimeout = 10000;
        this.routeDatabase = okHttpClient.routeDatabase;
        this.dispatcher = okHttpClient.dispatcher;
        this.proxy = okHttpClient.proxy;
        this.protocols = okHttpClient.protocols;
        this.connectionSpecs = okHttpClient.connectionSpecs;
        this.interceptors.addAll(okHttpClient.interceptors);
        this.networkInterceptors.addAll(okHttpClient.networkInterceptors);
        this.proxySelector = okHttpClient.proxySelector;
        this.cookieHandler = okHttpClient.cookieHandler;
        this.cache = okHttpClient.cache;
        this.internalCache = this.cache != null ? this.cache.internalCache : okHttpClient.internalCache;
        this.socketFactory = okHttpClient.socketFactory;
        this.sslSocketFactory = okHttpClient.sslSocketFactory;
        this.hostnameVerifier = okHttpClient.hostnameVerifier;
        this.certificatePinner = okHttpClient.certificatePinner;
        this.authenticator = okHttpClient.authenticator;
        this.connectionPool = okHttpClient.connectionPool;
        this.dns = okHttpClient.dns;
        this.followSslRedirects = okHttpClient.followSslRedirects;
        this.followRedirects = okHttpClient.followRedirects;
        this.retryOnConnectionFailure = okHttpClient.retryOnConnectionFailure;
        this.connectTimeout = okHttpClient.connectTimeout;
        this.readTimeout = okHttpClient.readTimeout;
        this.writeTimeout = okHttpClient.writeTimeout;
    }

    public void setConnectTimeout(long timeout, TimeUnit unit) {
        if (timeout < 0) {
            throw new IllegalArgumentException("timeout < 0");
        } else if (unit == null) {
            throw new IllegalArgumentException("unit == null");
        } else {
            long millis = unit.toMillis(timeout);
            if (millis > 2147483647L) {
                throw new IllegalArgumentException("Timeout too large.");
            } else if (millis != 0 || timeout <= 0) {
                this.connectTimeout = (int) millis;
            } else {
                throw new IllegalArgumentException("Timeout too small.");
            }
        }
    }

    public int getConnectTimeout() {
        return this.connectTimeout;
    }

    public void setReadTimeout(long timeout, TimeUnit unit) {
        if (timeout < 0) {
            throw new IllegalArgumentException("timeout < 0");
        } else if (unit == null) {
            throw new IllegalArgumentException("unit == null");
        } else {
            long millis = unit.toMillis(timeout);
            if (millis > 2147483647L) {
                throw new IllegalArgumentException("Timeout too large.");
            } else if (millis != 0 || timeout <= 0) {
                this.readTimeout = (int) millis;
            } else {
                throw new IllegalArgumentException("Timeout too small.");
            }
        }
    }

    public int getReadTimeout() {
        return this.readTimeout;
    }

    public void setWriteTimeout(long timeout, TimeUnit unit) {
        if (timeout < 0) {
            throw new IllegalArgumentException("timeout < 0");
        } else if (unit == null) {
            throw new IllegalArgumentException("unit == null");
        } else {
            long millis = unit.toMillis(timeout);
            if (millis > 2147483647L) {
                throw new IllegalArgumentException("Timeout too large.");
            } else if (millis != 0 || timeout <= 0) {
                this.writeTimeout = (int) millis;
            } else {
                throw new IllegalArgumentException("Timeout too small.");
            }
        }
    }

    public int getWriteTimeout() {
        return this.writeTimeout;
    }

    public OkHttpClient setProxy(Proxy proxy) {
        this.proxy = proxy;
        return this;
    }

    public Proxy getProxy() {
        return this.proxy;
    }

    public OkHttpClient setProxySelector(ProxySelector proxySelector) {
        this.proxySelector = proxySelector;
        return this;
    }

    public ProxySelector getProxySelector() {
        return this.proxySelector;
    }

    public OkHttpClient setCookieHandler(CookieHandler cookieHandler) {
        this.cookieHandler = cookieHandler;
        return this;
    }

    public CookieHandler getCookieHandler() {
        return this.cookieHandler;
    }

    void setInternalCache(InternalCache internalCache) {
        this.internalCache = internalCache;
        this.cache = null;
    }

    InternalCache internalCache() {
        return this.internalCache;
    }

    public OkHttpClient setCache(Cache cache) {
        this.cache = cache;
        this.internalCache = null;
        return this;
    }

    public Cache getCache() {
        return this.cache;
    }

    public OkHttpClient setDns(Dns dns) {
        this.dns = dns;
        return this;
    }

    public Dns getDns() {
        return this.dns;
    }

    public OkHttpClient setSocketFactory(SocketFactory socketFactory) {
        this.socketFactory = socketFactory;
        return this;
    }

    public SocketFactory getSocketFactory() {
        return this.socketFactory;
    }

    public OkHttpClient setSslSocketFactory(SSLSocketFactory sslSocketFactory) {
        this.sslSocketFactory = sslSocketFactory;
        return this;
    }

    public SSLSocketFactory getSslSocketFactory() {
        return this.sslSocketFactory;
    }

    public OkHttpClient setHostnameVerifier(HostnameVerifier hostnameVerifier) {
        this.hostnameVerifier = hostnameVerifier;
        return this;
    }

    public HostnameVerifier getHostnameVerifier() {
        return this.hostnameVerifier;
    }

    public OkHttpClient setCertificatePinner(CertificatePinner certificatePinner) {
        this.certificatePinner = certificatePinner;
        return this;
    }

    public CertificatePinner getCertificatePinner() {
        return this.certificatePinner;
    }

    public OkHttpClient setAuthenticator(Authenticator authenticator) {
        this.authenticator = authenticator;
        return this;
    }

    public Authenticator getAuthenticator() {
        return this.authenticator;
    }

    public OkHttpClient setConnectionPool(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
        return this;
    }

    public ConnectionPool getConnectionPool() {
        return this.connectionPool;
    }

    public OkHttpClient setFollowSslRedirects(boolean followProtocolRedirects) {
        this.followSslRedirects = followProtocolRedirects;
        return this;
    }

    public boolean getFollowSslRedirects() {
        return this.followSslRedirects;
    }

    public void setFollowRedirects(boolean followRedirects) {
        this.followRedirects = followRedirects;
    }

    public boolean getFollowRedirects() {
        return this.followRedirects;
    }

    public void setRetryOnConnectionFailure(boolean retryOnConnectionFailure) {
        this.retryOnConnectionFailure = retryOnConnectionFailure;
    }

    public boolean getRetryOnConnectionFailure() {
        return this.retryOnConnectionFailure;
    }

    RouteDatabase routeDatabase() {
        return this.routeDatabase;
    }

    public OkHttpClient setDispatcher(Dispatcher dispatcher) {
        if (dispatcher == null) {
            throw new IllegalArgumentException("dispatcher == null");
        }
        this.dispatcher = dispatcher;
        return this;
    }

    public Dispatcher getDispatcher() {
        return this.dispatcher;
    }

    public OkHttpClient setProtocols(List<Protocol> protocols) {
        List protocols2 = Util.immutableList((List) protocols);
        if (!protocols2.contains(Protocol.HTTP_1_1)) {
            throw new IllegalArgumentException("protocols doesn't contain http/1.1: " + protocols2);
        } else if (protocols2.contains(Protocol.HTTP_1_0)) {
            throw new IllegalArgumentException("protocols must not contain http/1.0: " + protocols2);
        } else if (protocols2.contains(null)) {
            throw new IllegalArgumentException("protocols must not contain null");
        } else {
            this.protocols = Util.immutableList(protocols2);
            return this;
        }
    }

    public List<Protocol> getProtocols() {
        return this.protocols;
    }

    public OkHttpClient setConnectionSpecs(List<ConnectionSpec> connectionSpecs) {
        this.connectionSpecs = Util.immutableList((List) connectionSpecs);
        return this;
    }

    public List<ConnectionSpec> getConnectionSpecs() {
        return this.connectionSpecs;
    }

    public List<Interceptor> interceptors() {
        return this.interceptors;
    }

    public List<Interceptor> networkInterceptors() {
        return this.networkInterceptors;
    }

    public Call newCall(Request request) {
        return new Call(this, request);
    }

    public OkHttpClient cancel(Object tag) {
        getDispatcher().cancel(tag);
        return this;
    }

    OkHttpClient copyWithDefaults() {
        OkHttpClient result = new OkHttpClient(this);
        if (result.proxySelector == null) {
            result.proxySelector = ProxySelector.getDefault();
        }
        if (result.cookieHandler == null) {
            result.cookieHandler = CookieHandler.getDefault();
        }
        if (result.socketFactory == null) {
            result.socketFactory = SocketFactory.getDefault();
        }
        if (result.sslSocketFactory == null) {
            result.sslSocketFactory = getDefaultSSLSocketFactory();
        }
        if (result.hostnameVerifier == null) {
            result.hostnameVerifier = OkHostnameVerifier.INSTANCE;
        }
        if (result.certificatePinner == null) {
            result.certificatePinner = CertificatePinner.DEFAULT;
        }
        if (result.authenticator == null) {
            result.authenticator = AuthenticatorAdapter.INSTANCE;
        }
        if (result.connectionPool == null) {
            result.connectionPool = ConnectionPool.getDefault();
        }
        if (result.protocols == null) {
            result.protocols = DEFAULT_PROTOCOLS;
        }
        if (result.connectionSpecs == null) {
            result.connectionSpecs = DEFAULT_CONNECTION_SPECS;
        }
        if (result.dns == null) {
            result.dns = Dns.SYSTEM;
        }
        return result;
    }

    private synchronized SSLSocketFactory getDefaultSSLSocketFactory() {
        if (defaultSslSocketFactory == null) {
            try {
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, null, null);
                defaultSslSocketFactory = sslContext.getSocketFactory();
            } catch (GeneralSecurityException e) {
                throw new AssertionError();
            }
        }
        return defaultSslSocketFactory;
    }

    public OkHttpClient clone() {
        return new OkHttpClient(this);
    }
}
