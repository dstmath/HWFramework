package com.android.okhttp;

import com.android.okhttp.Headers;
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
    private static final List<ConnectionSpec> DEFAULT_CONNECTION_SPECS = Util.immutableList((T[]) new ConnectionSpec[]{ConnectionSpec.MODERN_TLS, ConnectionSpec.COMPATIBLE_TLS, ConnectionSpec.CLEARTEXT});
    private static final List<Protocol> DEFAULT_PROTOCOLS = Util.immutableList((T[]) new Protocol[]{Protocol.HTTP_2, Protocol.SPDY_3, Protocol.HTTP_1_1});
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
            public void addLenient(Headers.Builder builder, String line) {
                builder.addLenient(line);
            }

            public void addLenient(Headers.Builder builder, String name, String value) {
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
        } else if (unit != null) {
            long millis = unit.toMillis(timeout);
            if (millis > 2147483647L) {
                throw new IllegalArgumentException("Timeout too large.");
            } else if (millis != 0 || timeout <= 0) {
                this.connectTimeout = (int) millis;
            } else {
                throw new IllegalArgumentException("Timeout too small.");
            }
        } else {
            throw new IllegalArgumentException("unit == null");
        }
    }

    public int getConnectTimeout() {
        return this.connectTimeout;
    }

    public void setReadTimeout(long timeout, TimeUnit unit) {
        if (timeout < 0) {
            throw new IllegalArgumentException("timeout < 0");
        } else if (unit != null) {
            long millis = unit.toMillis(timeout);
            if (millis > 2147483647L) {
                throw new IllegalArgumentException("Timeout too large.");
            } else if (millis != 0 || timeout <= 0) {
                this.readTimeout = (int) millis;
            } else {
                throw new IllegalArgumentException("Timeout too small.");
            }
        } else {
            throw new IllegalArgumentException("unit == null");
        }
    }

    public int getReadTimeout() {
        return this.readTimeout;
    }

    public void setWriteTimeout(long timeout, TimeUnit unit) {
        if (timeout < 0) {
            throw new IllegalArgumentException("timeout < 0");
        } else if (unit != null) {
            long millis = unit.toMillis(timeout);
            if (millis > 2147483647L) {
                throw new IllegalArgumentException("Timeout too large.");
            } else if (millis != 0 || timeout <= 0) {
                this.writeTimeout = (int) millis;
            } else {
                throw new IllegalArgumentException("Timeout too small.");
            }
        } else {
            throw new IllegalArgumentException("unit == null");
        }
    }

    public int getWriteTimeout() {
        return this.writeTimeout;
    }

    public OkHttpClient setProxy(Proxy proxy2) {
        this.proxy = proxy2;
        return this;
    }

    public Proxy getProxy() {
        return this.proxy;
    }

    public OkHttpClient setProxySelector(ProxySelector proxySelector2) {
        this.proxySelector = proxySelector2;
        return this;
    }

    public ProxySelector getProxySelector() {
        return this.proxySelector;
    }

    public OkHttpClient setCookieHandler(CookieHandler cookieHandler2) {
        this.cookieHandler = cookieHandler2;
        return this;
    }

    public CookieHandler getCookieHandler() {
        return this.cookieHandler;
    }

    /* access modifiers changed from: package-private */
    public void setInternalCache(InternalCache internalCache2) {
        this.internalCache = internalCache2;
        this.cache = null;
    }

    /* access modifiers changed from: package-private */
    public InternalCache internalCache() {
        return this.internalCache;
    }

    public OkHttpClient setCache(Cache cache2) {
        this.cache = cache2;
        this.internalCache = null;
        return this;
    }

    public Cache getCache() {
        return this.cache;
    }

    public OkHttpClient setDns(Dns dns2) {
        this.dns = dns2;
        return this;
    }

    public Dns getDns() {
        return this.dns;
    }

    public OkHttpClient setSocketFactory(SocketFactory socketFactory2) {
        this.socketFactory = socketFactory2;
        return this;
    }

    public SocketFactory getSocketFactory() {
        return this.socketFactory;
    }

    public OkHttpClient setSslSocketFactory(SSLSocketFactory sslSocketFactory2) {
        this.sslSocketFactory = sslSocketFactory2;
        return this;
    }

    public SSLSocketFactory getSslSocketFactory() {
        return this.sslSocketFactory;
    }

    public OkHttpClient setHostnameVerifier(HostnameVerifier hostnameVerifier2) {
        this.hostnameVerifier = hostnameVerifier2;
        return this;
    }

    public HostnameVerifier getHostnameVerifier() {
        return this.hostnameVerifier;
    }

    public OkHttpClient setCertificatePinner(CertificatePinner certificatePinner2) {
        this.certificatePinner = certificatePinner2;
        return this;
    }

    public CertificatePinner getCertificatePinner() {
        return this.certificatePinner;
    }

    public OkHttpClient setAuthenticator(Authenticator authenticator2) {
        this.authenticator = authenticator2;
        return this;
    }

    public Authenticator getAuthenticator() {
        return this.authenticator;
    }

    public OkHttpClient setConnectionPool(ConnectionPool connectionPool2) {
        this.connectionPool = connectionPool2;
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

    public void setFollowRedirects(boolean followRedirects2) {
        this.followRedirects = followRedirects2;
    }

    public boolean getFollowRedirects() {
        return this.followRedirects;
    }

    public void setRetryOnConnectionFailure(boolean retryOnConnectionFailure2) {
        this.retryOnConnectionFailure = retryOnConnectionFailure2;
    }

    public boolean getRetryOnConnectionFailure() {
        return this.retryOnConnectionFailure;
    }

    /* access modifiers changed from: package-private */
    public RouteDatabase routeDatabase() {
        return this.routeDatabase;
    }

    public OkHttpClient setDispatcher(Dispatcher dispatcher2) {
        if (dispatcher2 != null) {
            this.dispatcher = dispatcher2;
            return this;
        }
        throw new IllegalArgumentException("dispatcher == null");
    }

    public Dispatcher getDispatcher() {
        return this.dispatcher;
    }

    public OkHttpClient setProtocols(List<Protocol> protocols2) {
        List<Protocol> protocols3 = Util.immutableList(protocols2);
        if (!protocols3.contains(Protocol.HTTP_1_1)) {
            throw new IllegalArgumentException("protocols doesn't contain http/1.1: " + protocols3);
        } else if (protocols3.contains(Protocol.HTTP_1_0)) {
            throw new IllegalArgumentException("protocols must not contain http/1.0: " + protocols3);
        } else if (!protocols3.contains(null)) {
            this.protocols = Util.immutableList(protocols3);
            return this;
        } else {
            throw new IllegalArgumentException("protocols must not contain null");
        }
    }

    public List<Protocol> getProtocols() {
        return this.protocols;
    }

    public OkHttpClient setConnectionSpecs(List<ConnectionSpec> connectionSpecs2) {
        this.connectionSpecs = Util.immutableList(connectionSpecs2);
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

    /* access modifiers changed from: package-private */
    public OkHttpClient copyWithDefaults() {
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
