package org.apache.http.impl.client;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.URI;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.ProtocolVersion;
import org.apache.http.auth.AuthSchemeRegistry;
import org.apache.http.client.AuthenticationHandler;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.RedirectHandler;
import org.apache.http.client.RequestDirector;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.UserTokenHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.cookie.CookieSpecRegistry;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpProcessor;
import org.apache.http.protocol.DefaultedHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpRequestExecutor;

@Deprecated
public abstract class AbstractHttpClient implements HttpClient {
    private static final int FAKE_HTTP_RESPONSE_CODE = 404;
    private static final String FAKE_HTTP_RESPONSE_REASON = "error";
    private static final int FAKE_PROTOCOL_MAJOR_VERSION = 100;
    private static final int FAKE_PROTOCOL_MINOR_VERSION = 80;
    private static final String FAKE_PROTOCOL_NAME = "HTTP";
    private static final int MAX_PDU_PARSER_LENGTH = 1000;
    private static final String MMS_CONTENT_TYPE = "application/vnd.wap.mms-message";
    private static final int MMS_SEND_TYPE_NAME = 140;
    private static final int MMS_SEND_TYPE_VALUE = 128;
    private ClientConnectionManager connManager;
    private CookieStore cookieStore;
    private CredentialsProvider credsProvider;
    private HttpParams defaultParams;
    private BasicHttpProcessor httpProcessor;
    private ConnectionKeepAliveStrategy keepAliveStrategy;
    private final Log log = LogFactory.getLog((Class) getClass());
    private AuthenticationHandler proxyAuthHandler;
    private RedirectHandler redirectHandler;
    private HttpRequestExecutor requestExec;
    private HttpRequestRetryHandler retryHandler;
    private ConnectionReuseStrategy reuseStrategy;
    private HttpRoutePlanner routePlanner;
    private AuthSchemeRegistry supportedAuthSchemes;
    private CookieSpecRegistry supportedCookieSpecs;
    private AuthenticationHandler targetAuthHandler;
    private UserTokenHandler userTokenHandler;

    /* access modifiers changed from: protected */
    public abstract AuthSchemeRegistry createAuthSchemeRegistry();

    /* access modifiers changed from: protected */
    public abstract ClientConnectionManager createClientConnectionManager();

    /* access modifiers changed from: protected */
    public abstract ConnectionKeepAliveStrategy createConnectionKeepAliveStrategy();

    /* access modifiers changed from: protected */
    public abstract ConnectionReuseStrategy createConnectionReuseStrategy();

    /* access modifiers changed from: protected */
    public abstract CookieSpecRegistry createCookieSpecRegistry();

    /* access modifiers changed from: protected */
    public abstract CookieStore createCookieStore();

    /* access modifiers changed from: protected */
    public abstract CredentialsProvider createCredentialsProvider();

    /* access modifiers changed from: protected */
    public abstract HttpContext createHttpContext();

    /* access modifiers changed from: protected */
    public abstract HttpParams createHttpParams();

    /* access modifiers changed from: protected */
    public abstract BasicHttpProcessor createHttpProcessor();

    /* access modifiers changed from: protected */
    public abstract HttpRequestRetryHandler createHttpRequestRetryHandler();

    /* access modifiers changed from: protected */
    public abstract HttpRoutePlanner createHttpRoutePlanner();

    /* access modifiers changed from: protected */
    public abstract AuthenticationHandler createProxyAuthenticationHandler();

    /* access modifiers changed from: protected */
    public abstract RedirectHandler createRedirectHandler();

    /* access modifiers changed from: protected */
    public abstract HttpRequestExecutor createRequestExecutor();

    /* access modifiers changed from: protected */
    public abstract AuthenticationHandler createTargetAuthenticationHandler();

    /* access modifiers changed from: protected */
    public abstract UserTokenHandler createUserTokenHandler();

    protected AbstractHttpClient(ClientConnectionManager conman, HttpParams params) {
        this.defaultParams = params;
        this.connManager = conman;
    }

    public final synchronized HttpParams getParams() {
        if (this.defaultParams == null) {
            this.defaultParams = createHttpParams();
        }
        return this.defaultParams;
    }

    public synchronized void setParams(HttpParams params) {
        this.defaultParams = params;
    }

    public final synchronized ClientConnectionManager getConnectionManager() {
        if (this.connManager == null) {
            this.connManager = createClientConnectionManager();
        }
        return this.connManager;
    }

    public final synchronized HttpRequestExecutor getRequestExecutor() {
        if (this.requestExec == null) {
            this.requestExec = createRequestExecutor();
        }
        return this.requestExec;
    }

    public final synchronized AuthSchemeRegistry getAuthSchemes() {
        if (this.supportedAuthSchemes == null) {
            this.supportedAuthSchemes = createAuthSchemeRegistry();
        }
        return this.supportedAuthSchemes;
    }

    public synchronized void setAuthSchemes(AuthSchemeRegistry authSchemeRegistry) {
        this.supportedAuthSchemes = authSchemeRegistry;
    }

    public final synchronized CookieSpecRegistry getCookieSpecs() {
        if (this.supportedCookieSpecs == null) {
            this.supportedCookieSpecs = createCookieSpecRegistry();
        }
        return this.supportedCookieSpecs;
    }

    public synchronized void setCookieSpecs(CookieSpecRegistry cookieSpecRegistry) {
        this.supportedCookieSpecs = cookieSpecRegistry;
    }

    public final synchronized ConnectionReuseStrategy getConnectionReuseStrategy() {
        if (this.reuseStrategy == null) {
            this.reuseStrategy = createConnectionReuseStrategy();
        }
        return this.reuseStrategy;
    }

    public synchronized void setReuseStrategy(ConnectionReuseStrategy reuseStrategy2) {
        this.reuseStrategy = reuseStrategy2;
    }

    public final synchronized ConnectionKeepAliveStrategy getConnectionKeepAliveStrategy() {
        if (this.keepAliveStrategy == null) {
            this.keepAliveStrategy = createConnectionKeepAliveStrategy();
        }
        return this.keepAliveStrategy;
    }

    public synchronized void setKeepAliveStrategy(ConnectionKeepAliveStrategy keepAliveStrategy2) {
        this.keepAliveStrategy = keepAliveStrategy2;
    }

    public final synchronized HttpRequestRetryHandler getHttpRequestRetryHandler() {
        if (this.retryHandler == null) {
            this.retryHandler = createHttpRequestRetryHandler();
        }
        return this.retryHandler;
    }

    public synchronized void setHttpRequestRetryHandler(HttpRequestRetryHandler retryHandler2) {
        this.retryHandler = retryHandler2;
    }

    public final synchronized RedirectHandler getRedirectHandler() {
        if (this.redirectHandler == null) {
            this.redirectHandler = createRedirectHandler();
        }
        return this.redirectHandler;
    }

    public synchronized void setRedirectHandler(RedirectHandler redirectHandler2) {
        this.redirectHandler = redirectHandler2;
    }

    public final synchronized AuthenticationHandler getTargetAuthenticationHandler() {
        if (this.targetAuthHandler == null) {
            this.targetAuthHandler = createTargetAuthenticationHandler();
        }
        return this.targetAuthHandler;
    }

    public synchronized void setTargetAuthenticationHandler(AuthenticationHandler targetAuthHandler2) {
        this.targetAuthHandler = targetAuthHandler2;
    }

    public final synchronized AuthenticationHandler getProxyAuthenticationHandler() {
        if (this.proxyAuthHandler == null) {
            this.proxyAuthHandler = createProxyAuthenticationHandler();
        }
        return this.proxyAuthHandler;
    }

    public synchronized void setProxyAuthenticationHandler(AuthenticationHandler proxyAuthHandler2) {
        this.proxyAuthHandler = proxyAuthHandler2;
    }

    public final synchronized CookieStore getCookieStore() {
        if (this.cookieStore == null) {
            this.cookieStore = createCookieStore();
        }
        return this.cookieStore;
    }

    public synchronized void setCookieStore(CookieStore cookieStore2) {
        this.cookieStore = cookieStore2;
    }

    public final synchronized CredentialsProvider getCredentialsProvider() {
        if (this.credsProvider == null) {
            this.credsProvider = createCredentialsProvider();
        }
        return this.credsProvider;
    }

    public synchronized void setCredentialsProvider(CredentialsProvider credsProvider2) {
        this.credsProvider = credsProvider2;
    }

    public final synchronized HttpRoutePlanner getRoutePlanner() {
        if (this.routePlanner == null) {
            this.routePlanner = createHttpRoutePlanner();
        }
        return this.routePlanner;
    }

    public synchronized void setRoutePlanner(HttpRoutePlanner routePlanner2) {
        this.routePlanner = routePlanner2;
    }

    public final synchronized UserTokenHandler getUserTokenHandler() {
        if (this.userTokenHandler == null) {
            this.userTokenHandler = createUserTokenHandler();
        }
        return this.userTokenHandler;
    }

    public synchronized void setUserTokenHandler(UserTokenHandler userTokenHandler2) {
        this.userTokenHandler = userTokenHandler2;
    }

    /* access modifiers changed from: protected */
    public final synchronized BasicHttpProcessor getHttpProcessor() {
        if (this.httpProcessor == null) {
            this.httpProcessor = createHttpProcessor();
        }
        return this.httpProcessor;
    }

    public synchronized void addResponseInterceptor(HttpResponseInterceptor itcp) {
        getHttpProcessor().addInterceptor(itcp);
    }

    public synchronized void addResponseInterceptor(HttpResponseInterceptor itcp, int index) {
        getHttpProcessor().addInterceptor(itcp, index);
    }

    public synchronized HttpResponseInterceptor getResponseInterceptor(int index) {
        return getHttpProcessor().getResponseInterceptor(index);
    }

    public synchronized int getResponseInterceptorCount() {
        return getHttpProcessor().getResponseInterceptorCount();
    }

    public synchronized void clearResponseInterceptors() {
        getHttpProcessor().clearResponseInterceptors();
    }

    public void removeResponseInterceptorByClass(Class<? extends HttpResponseInterceptor> clazz) {
        getHttpProcessor().removeResponseInterceptorByClass(clazz);
    }

    public synchronized void addRequestInterceptor(HttpRequestInterceptor itcp) {
        getHttpProcessor().addInterceptor(itcp);
    }

    public synchronized void addRequestInterceptor(HttpRequestInterceptor itcp, int index) {
        getHttpProcessor().addInterceptor(itcp, index);
    }

    public synchronized HttpRequestInterceptor getRequestInterceptor(int index) {
        return getHttpProcessor().getRequestInterceptor(index);
    }

    public synchronized int getRequestInterceptorCount() {
        return getHttpProcessor().getRequestInterceptorCount();
    }

    public synchronized void clearRequestInterceptors() {
        getHttpProcessor().clearRequestInterceptors();
    }

    public void removeRequestInterceptorByClass(Class<? extends HttpRequestInterceptor> clazz) {
        getHttpProcessor().removeRequestInterceptorByClass(clazz);
    }

    public final HttpResponse execute(HttpUriRequest request) throws IOException, ClientProtocolException {
        return execute(request, (HttpContext) null);
    }

    public final HttpResponse execute(HttpUriRequest request, HttpContext context) throws IOException, ClientProtocolException {
        if (request != null) {
            return execute(determineTarget(request), (HttpRequest) request, context);
        }
        throw new IllegalArgumentException("Request must not be null.");
    }

    private HttpHost determineTarget(HttpUriRequest request) {
        URI requestURI = request.getURI();
        if (requestURI.isAbsolute()) {
            return new HttpHost(requestURI.getHost(), requestURI.getPort(), requestURI.getScheme());
        }
        return null;
    }

    public final HttpResponse execute(HttpHost target, HttpRequest request) throws IOException, ClientProtocolException {
        return execute(target, request, (HttpContext) null);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0060, code lost:
        if (r16 == false) goto L_0x006e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0066, code lost:
        return getFakeResponse();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0067, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0068, code lost:
        r2 = r22;
        r4 = r15;
        r3 = r23;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0077, code lost:
        return r1.execute(r22, r23, r15);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0078, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x007e, code lost:
        throw new org.apache.http.client.ClientProtocolException((java.lang.Throwable) r0);
     */
    public final HttpResponse execute(HttpHost target, HttpRequest request, HttpContext context) throws IOException, ClientProtocolException {
        HttpContext execContext;
        HttpContext httpContext;
        HttpRequest httpRequest = request;
        HttpContext httpContext2 = context;
        if (httpRequest != null) {
            boolean isBlocked = checkRequestForMms(httpRequest);
            synchronized (this) {
                try {
                    HttpContext defaultContext = createHttpContext();
                    if (httpContext2 == null) {
                        execContext = defaultContext;
                    } else {
                        execContext = new DefaultedHttpContext(httpContext2, defaultContext);
                    }
                    HttpContext execContext2 = execContext;
                    try {
                        HttpRequestExecutor requestExecutor = getRequestExecutor();
                        ClientConnectionManager connectionManager = getConnectionManager();
                        ConnectionReuseStrategy connectionReuseStrategy = getConnectionReuseStrategy();
                        ConnectionKeepAliveStrategy connectionKeepAliveStrategy = getConnectionKeepAliveStrategy();
                        HttpRoutePlanner routePlanner2 = getRoutePlanner();
                        BasicHttpProcessor copy = getHttpProcessor().copy();
                        HttpRequestRetryHandler httpRequestRetryHandler = getHttpRequestRetryHandler();
                        RedirectHandler redirectHandler2 = getRedirectHandler();
                        AuthenticationHandler targetAuthenticationHandler = getTargetAuthenticationHandler();
                        AuthenticationHandler proxyAuthenticationHandler = getProxyAuthenticationHandler();
                        UserTokenHandler userTokenHandler2 = getUserTokenHandler();
                        HttpParams determineParams = determineParams(httpRequest);
                        HttpContext execContext3 = execContext2;
                        try {
                            RequestDirector director = createClientRequestDirector(requestExecutor, connectionManager, connectionReuseStrategy, connectionKeepAliveStrategy, routePlanner2, copy, httpRequestRetryHandler, redirectHandler2, targetAuthenticationHandler, proxyAuthenticationHandler, userTokenHandler2, determineParams);
                            try {
                            } catch (Throwable th) {
                                httpException = th;
                                HttpHost httpHost = target;
                                httpContext = execContext3;
                                HttpRequest httpRequest2 = request;
                                RequestDirector requestDirector = director;
                                HttpContext execContext4 = httpContext;
                                while (true) {
                                    try {
                                        break;
                                    } catch (Throwable th2) {
                                        httpException = th2;
                                    }
                                }
                                throw httpException;
                            }
                        } catch (Throwable th3) {
                            httpException = th3;
                            HttpHost httpHost2 = target;
                            httpContext = execContext3;
                            HttpRequest httpRequest3 = request;
                            HttpContext execContext42 = httpContext;
                            while (true) {
                                break;
                            }
                            throw httpException;
                        }
                    } catch (Throwable th4) {
                        httpException = th4;
                        HttpHost httpHost3 = target;
                        HttpRequest httpRequest4 = httpRequest;
                        HttpContext httpContext3 = execContext2;
                        while (true) {
                            break;
                        }
                        throw httpException;
                    }
                } catch (Throwable th5) {
                    httpException = th5;
                    HttpHost httpHost4 = target;
                    HttpRequest httpRequest5 = httpRequest;
                    while (true) {
                        break;
                    }
                    throw httpException;
                }
            }
        } else {
            HttpHost httpHost5 = target;
            HttpRequest httpRequest6 = httpRequest;
            throw new IllegalArgumentException("Request must not be null.");
        }
    }

    /* access modifiers changed from: protected */
    public RequestDirector createClientRequestDirector(HttpRequestExecutor requestExec2, ClientConnectionManager conman, ConnectionReuseStrategy reustrat, ConnectionKeepAliveStrategy kastrat, HttpRoutePlanner rouplan, HttpProcessor httpProcessor2, HttpRequestRetryHandler retryHandler2, RedirectHandler redirectHandler2, AuthenticationHandler targetAuthHandler2, AuthenticationHandler proxyAuthHandler2, UserTokenHandler stateHandler, HttpParams params) {
        DefaultRequestDirector defaultRequestDirector = new DefaultRequestDirector(requestExec2, conman, reustrat, kastrat, rouplan, httpProcessor2, retryHandler2, redirectHandler2, targetAuthHandler2, proxyAuthHandler2, stateHandler, params);
        return defaultRequestDirector;
    }

    /* access modifiers changed from: protected */
    public HttpParams determineParams(HttpRequest req) {
        return new ClientParamsStack(null, getParams(), req.getParams(), null);
    }

    public <T> T execute(HttpUriRequest request, ResponseHandler<? extends T> responseHandler) throws IOException, ClientProtocolException {
        return execute(request, responseHandler, (HttpContext) null);
    }

    public <T> T execute(HttpUriRequest request, ResponseHandler<? extends T> responseHandler, HttpContext context) throws IOException, ClientProtocolException {
        return execute(determineTarget(request), request, responseHandler, context);
    }

    public <T> T execute(HttpHost target, HttpRequest request, ResponseHandler<? extends T> responseHandler) throws IOException, ClientProtocolException {
        return execute(target, request, responseHandler, null);
    }

    public <T> T execute(HttpHost target, HttpRequest request, ResponseHandler<? extends T> responseHandler, HttpContext context) throws IOException, ClientProtocolException {
        if (responseHandler != null) {
            HttpResponse response = execute(target, request, context);
            try {
                T result = responseHandler.handleResponse(response);
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    entity.consumeContent();
                }
                return result;
            } catch (Throwable t2) {
                this.log.warn("Error consuming content after an exception.", t2);
            }
        } else {
            throw new IllegalArgumentException("Response handler must not be null.");
        }
        if (t instanceof Error) {
            throw ((Error) t);
        } else if (t instanceof RuntimeException) {
            throw ((RuntimeException) t);
        } else if (t instanceof IOException) {
            throw ((IOException) t);
        } else {
            throw new UndeclaredThrowableException(t);
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v2, resolved type: java.lang.Object[]} */
    /* JADX WARNING: Multi-variable type inference failed */
    private boolean checkRequestForMms(HttpRequest request) {
        HttpRequest httpRequest = request;
        if (httpRequest instanceof HttpPost) {
            try {
                HttpEntity entity = ((HttpPost) httpRequest).getEntity();
                if (!(entity == null || entity.getContentType() == null || !MMS_CONTENT_TYPE.equals(entity.getContentType().getValue()))) {
                    InputStream mmsPdu = entity.getContent();
                    int typeName = 0;
                    int typeValue = 0;
                    if (mmsPdu != null) {
                        typeName = mmsPdu.read();
                        typeValue = mmsPdu.read();
                    }
                    if (MMS_SEND_TYPE_NAME == typeName && MMS_SEND_TYPE_VALUE == typeValue) {
                        Class<?> clazz = Class.forName("com.huawei.hsm.permission.ConnectPermission");
                        if (clazz.getField("isControl").getBoolean(clazz)) {
                            byte[] pduBuf = new byte[MAX_PDU_PARSER_LENGTH];
                            mmsPdu.read(pduBuf);
                            boolean z = true;
                            Boolean isBlocked = (Boolean) clazz.getDeclaredMethod("isBlocked", new Class[]{byte[].class}).invoke(clazz.newInstance(), new Object[]{pduBuf});
                            if (isBlocked == null || !isBlocked.booleanValue()) {
                                z = false;
                            }
                            return z;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private BasicHttpResponse getFakeResponse() {
        return new BasicHttpResponse(new ProtocolVersion("HTTP", 100, FAKE_PROTOCOL_MINOR_VERSION), 404, FAKE_HTTP_RESPONSE_REASON);
    }
}
