package org.apache.http.impl.client;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthState;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.MalformedChallengeException;
import org.apache.http.client.AuthenticationHandler;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.NonRepeatableRequestException;
import org.apache.http.client.RedirectException;
import org.apache.http.client.RedirectHandler;
import org.apache.http.client.RequestDirector;
import org.apache.http.client.UserTokenHandler;
import org.apache.http.client.methods.AbortableHttpRequest;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.conn.BasicManagedEntity;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ClientConnectionRequest;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.ManagedClientConnection;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.routing.BasicRouteDirector;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.routing.HttpRouteDirector;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpRequestExecutor;

@Deprecated
public class DefaultRequestDirector implements RequestDirector {
    private static Method cleartextTrafficPermittedMethod;
    private static Object networkSecurityPolicy;
    protected final ClientConnectionManager connManager;
    protected final HttpProcessor httpProcessor;
    protected final ConnectionKeepAliveStrategy keepAliveStrategy;
    private final Log log = LogFactory.getLog((Class) getClass());
    protected ManagedClientConnection managedConn;
    private int maxRedirects;
    protected final HttpParams params;
    private final AuthenticationHandler proxyAuthHandler;
    private final AuthState proxyAuthState;
    private int redirectCount;
    protected final RedirectHandler redirectHandler;
    protected final HttpRequestExecutor requestExec;
    protected final HttpRequestRetryHandler retryHandler;
    protected final ConnectionReuseStrategy reuseStrategy;
    protected final HttpRoutePlanner routePlanner;
    private final AuthenticationHandler targetAuthHandler;
    private final AuthState targetAuthState;
    private final UserTokenHandler userTokenHandler;

    public DefaultRequestDirector(HttpRequestExecutor requestExec2, ClientConnectionManager conman, ConnectionReuseStrategy reustrat, ConnectionKeepAliveStrategy kastrat, HttpRoutePlanner rouplan, HttpProcessor httpProcessor2, HttpRequestRetryHandler retryHandler2, RedirectHandler redirectHandler2, AuthenticationHandler targetAuthHandler2, AuthenticationHandler proxyAuthHandler2, UserTokenHandler userTokenHandler2, HttpParams params2) {
        if (requestExec2 == null) {
            throw new IllegalArgumentException("Request executor may not be null.");
        } else if (conman == null) {
            throw new IllegalArgumentException("Client connection manager may not be null.");
        } else if (reustrat == null) {
            throw new IllegalArgumentException("Connection reuse strategy may not be null.");
        } else if (kastrat == null) {
            throw new IllegalArgumentException("Connection keep alive strategy may not be null.");
        } else if (rouplan == null) {
            throw new IllegalArgumentException("Route planner may not be null.");
        } else if (httpProcessor2 == null) {
            throw new IllegalArgumentException("HTTP protocol processor may not be null.");
        } else if (retryHandler2 == null) {
            throw new IllegalArgumentException("HTTP request retry handler may not be null.");
        } else if (redirectHandler2 == null) {
            throw new IllegalArgumentException("Redirect handler may not be null.");
        } else if (targetAuthHandler2 == null) {
            throw new IllegalArgumentException("Target authentication handler may not be null.");
        } else if (proxyAuthHandler2 == null) {
            throw new IllegalArgumentException("Proxy authentication handler may not be null.");
        } else if (userTokenHandler2 == null) {
            throw new IllegalArgumentException("User token handler may not be null.");
        } else if (params2 != null) {
            this.requestExec = requestExec2;
            this.connManager = conman;
            this.reuseStrategy = reustrat;
            this.keepAliveStrategy = kastrat;
            this.routePlanner = rouplan;
            this.httpProcessor = httpProcessor2;
            this.retryHandler = retryHandler2;
            this.redirectHandler = redirectHandler2;
            this.targetAuthHandler = targetAuthHandler2;
            this.proxyAuthHandler = proxyAuthHandler2;
            this.userTokenHandler = userTokenHandler2;
            this.params = params2;
            this.managedConn = null;
            this.redirectCount = 0;
            this.maxRedirects = this.params.getIntParameter(ClientPNames.MAX_REDIRECTS, 100);
            this.targetAuthState = new AuthState();
            this.proxyAuthState = new AuthState();
        } else {
            throw new IllegalArgumentException("HTTP parameters may not be null");
        }
    }

    private RequestWrapper wrapRequest(HttpRequest request) throws ProtocolException {
        if (request instanceof HttpEntityEnclosingRequest) {
            return new EntityEnclosingRequestWrapper((HttpEntityEnclosingRequest) request);
        }
        return new RequestWrapper(request);
    }

    /* access modifiers changed from: protected */
    public void rewriteRequestURI(RequestWrapper request, HttpRoute route) throws ProtocolException {
        try {
            URI uri = request.getURI();
            if (route.getProxyHost() == null || route.isTunnelled()) {
                if (uri.isAbsolute()) {
                    request.setURI(URIUtils.rewriteURI(uri, null));
                }
            } else if (!uri.isAbsolute()) {
                request.setURI(URIUtils.rewriteURI(uri, route.getTargetHost()));
            }
        } catch (URISyntaxException ex) {
            throw new ProtocolException("Invalid URI: " + request.getRequestLine().getUri(), ex);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:103:0x01d8, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:104:0x01d9, code lost:
        r22 = r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:109:0x01f5, code lost:
        if (r1.log.isInfoEnabled() != false) goto L_0x01f7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:110:0x01f7, code lost:
        r3 = r1.log;
        r4 = new java.lang.StringBuilder();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:111:0x01fe, code lost:
        r23 = r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:113:?, code lost:
        r4.append("I/O exception (");
        r4.append(r0.getClass().getName());
        r4.append(") caught when processing request: ");
        r4.append(r0.getMessage());
        r3.info(r4.toString());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:114:0x0224, code lost:
        r23 = r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:116:0x022c, code lost:
        if (r1.log.isDebugEnabled() != false) goto L_0x022e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:117:0x022e, code lost:
        r1.log.debug(r0.getMessage(), r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:118:0x0237, code lost:
        r1.log.info("Retrying request");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:119:0x0243, code lost:
        if (r15.getHopCount() == 1) goto L_0x0245;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:120:0x0245, code lost:
        r1.log.debug("Reopening the direct connection.");
        r1.managedConn.open(r15, r2, r1.params);
        r3 = 1;
        r0 = r16;
        r4 = r22;
        r5 = r23;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:121:0x025d, code lost:
        throw r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:122:0x025e, code lost:
        r23 = r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:123:0x0260, code lost:
        throw r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:124:0x0261, code lost:
        r22 = r4;
        r23 = r5;
        r11.setParams(r1.params);
        r1.requestExec.postProcess(r11, r1.httpProcessor, r2);
        r10 = r1.reuseStrategy.keepAlive(r11, r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:125:0x0278, code lost:
        if (r10 == false) goto L_0x0287;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:126:0x027a, code lost:
        r1.managedConn.setIdleDuration(r1.keepAliveStrategy.getKeepAliveDuration(r11, r2), java.util.concurrent.TimeUnit.MILLISECONDS);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:127:0x0287, code lost:
        r0 = handleResponse(r6, r11, r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:128:0x028b, code lost:
        if (r0 != null) goto L_0x0290;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:129:0x028d, code lost:
        r12 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:130:0x0290, code lost:
        if (r10 == false) goto L_0x02a8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:131:0x0292, code lost:
        r1.log.debug("Connection kept alive");
        r3 = r11.getEntity();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:132:0x029d, code lost:
        if (r3 == null) goto L_0x02a2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:133:0x029f, code lost:
        r3.consumeContent();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:134:0x02a2, code lost:
        r1.managedConn.markReusable();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:135:0x02a8, code lost:
        r1.managedConn.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:137:0x02b9, code lost:
        if (r0.getRoute().equals(r6.getRoute()) != false) goto L_0x02be;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:138:0x02bb, code lost:
        releaseConnection();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:139:0x02be, code lost:
        r6 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:140:0x02c0, code lost:
        r3 = r1.userTokenHandler.getUserToken(r2);
        r2.setAttribute(org.apache.http.client.protocol.ClientContext.USER_TOKEN, r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:141:0x02cd, code lost:
        if (r1.managedConn == null) goto L_0x02d4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:142:0x02cf, code lost:
        r1.managedConn.setState(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:143:0x02d4, code lost:
        r4 = r17;
        r3 = r21;
        r5 = r23;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:175:0x035f, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:177:0x0361, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:179:0x0363, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x009a, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x009b, code lost:
        r21 = r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x009d, code lost:
        r23 = r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x00a8, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x00a9, code lost:
        r21 = r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x00ab, code lost:
        r23 = r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:82:0x0165, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:83:0x0168, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:84:0x016b, code lost:
        r0 = e;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:108:0x01ef A[Catch:{ HttpException -> 0x02e7, IOException -> 0x02e2, RuntimeException -> 0x02dd }] */
    /* JADX WARNING: Removed duplicated region for block: B:195:0x025e A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x009a A[ExcHandler: RuntimeException (e java.lang.RuntimeException), Splitter:B:8:0x0044] */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x00a8 A[ExcHandler: HttpException (e org.apache.http.HttpException), Splitter:B:8:0x0044] */
    /* JADX WARNING: Removed duplicated region for block: B:82:0x0165 A[ExcHandler: RuntimeException (e java.lang.RuntimeException), Splitter:B:76:0x0156] */
    /* JADX WARNING: Removed duplicated region for block: B:84:0x016b A[ExcHandler: HttpException (e org.apache.http.HttpException), Splitter:B:76:0x0156] */
    public HttpResponse execute(HttpHost target, HttpRequest request, HttpContext context) throws HttpException, IOException {
        RequestWrapper origWrapper;
        HttpHost proxy;
        Object userToken;
        ClientConnectionRequest connRequest;
        HttpContext httpContext = context;
        HttpRequest orig = request;
        RequestWrapper origWrapper2 = wrapRequest(orig);
        origWrapper2.setParams(this.params);
        HttpHost httpHost = target;
        HttpRoute origRoute = determineRoute(httpHost, origWrapper2, httpContext);
        RoutedRequest roureq = new RoutedRequest(origWrapper2, origRoute);
        long timeout = ConnManagerParams.getTimeout(this.params);
        boolean reuse = false;
        HttpResponse response = null;
        boolean done = false;
        int execCount = 0;
        HttpHost httpHost2 = httpHost;
        while (!done) {
            try {
                RequestWrapper wrapper = roureq.getRequest();
                HttpRoute route = roureq.getRoute();
                Object userToken2 = httpContext.getAttribute(ClientContext.USER_TOKEN);
                if (this.managedConn == null) {
                    try {
                        origWrapper = origWrapper2;
                        userToken = userToken2;
                    } catch (HttpException e) {
                        ex = e;
                        RequestWrapper requestWrapper = origWrapper2;
                        HttpRequest httpRequest = orig;
                        HttpRoute httpRoute = origRoute;
                        abortConnection();
                        throw ex;
                    } catch (IOException e2) {
                        ex = e2;
                        RequestWrapper requestWrapper2 = origWrapper2;
                        HttpRequest httpRequest2 = orig;
                        HttpRoute httpRoute2 = origRoute;
                        abortConnection();
                        throw ex;
                    } catch (RuntimeException e3) {
                        ex = e3;
                        RequestWrapper requestWrapper3 = origWrapper2;
                        HttpRequest httpRequest3 = orig;
                        HttpRoute httpRoute3 = origRoute;
                        abortConnection();
                        throw ex;
                    }
                    try {
                        ClientConnectionRequest connRequest2 = this.connManager.requestConnection(route, userToken);
                        if (orig instanceof AbortableHttpRequest) {
                            Object obj = userToken;
                            connRequest = connRequest2;
                            ((AbortableHttpRequest) orig).setConnectionRequest(connRequest);
                        } else {
                            connRequest = connRequest2;
                        }
                        this.managedConn = connRequest.getConnection(timeout, TimeUnit.MILLISECONDS);
                        if (HttpConnectionParams.isStaleCheckingEnabled(this.params)) {
                            ClientConnectionRequest clientConnectionRequest = connRequest;
                            this.log.debug("Stale connection check");
                            if (this.managedConn.isStale()) {
                                this.log.debug("Stale connection detected");
                                this.managedConn.close();
                            }
                        }
                    } catch (InterruptedException interrupted) {
                        ClientConnectionRequest clientConnectionRequest2 = connRequest;
                        InterruptedIOException iox = new InterruptedIOException();
                        iox.initCause(interrupted);
                        throw iox;
                    } catch (HttpException e4) {
                    } catch (IOException e5) {
                        ex = e5;
                        HttpRequest httpRequest4 = orig;
                        abortConnection();
                        throw ex;
                    } catch (RuntimeException e6) {
                    }
                } else {
                    origWrapper = origWrapper2;
                    Object obj2 = userToken2;
                }
                try {
                    if (orig instanceof AbortableHttpRequest) {
                        ((AbortableHttpRequest) orig).setReleaseTrigger(this.managedConn);
                    }
                    if (!this.managedConn.isOpen()) {
                        this.managedConn.open(route, httpContext, this.params);
                    } else {
                        this.managedConn.setSocketTimeout(HttpConnectionParams.getSoTimeout(this.params));
                    }
                    try {
                        establishRoute(route, httpContext);
                        wrapper.resetHeaders();
                        rewriteRequestURI(wrapper, route);
                        HttpHost target2 = (HttpHost) wrapper.getParams().getParameter(ClientPNames.VIRTUAL_HOST);
                        if (target2 == null) {
                            target2 = route.getTargetHost();
                        }
                        HttpHost proxy2 = route.getProxyHost();
                        httpContext.setAttribute(ExecutionContext.HTTP_TARGET_HOST, target2);
                        httpContext.setAttribute(ExecutionContext.HTTP_PROXY_HOST, proxy2);
                        HttpRequest orig2 = orig;
                        try {
                            httpContext.setAttribute(ExecutionContext.HTTP_CONNECTION, this.managedConn);
                            httpContext.setAttribute(ClientContext.TARGET_AUTH_STATE, this.targetAuthState);
                            httpContext.setAttribute(ClientContext.PROXY_AUTH_STATE, this.proxyAuthState);
                            this.requestExec.preProcess(wrapper, this.httpProcessor, httpContext);
                            httpContext.setAttribute(ExecutionContext.HTTP_REQUEST, wrapper);
                            int i = 1;
                            boolean retrying = true;
                            while (true) {
                                boolean retrying2 = retrying;
                                if (!retrying2) {
                                    break;
                                }
                                execCount++;
                                wrapper.incrementExecCount();
                                if (wrapper.getExecCount() > i) {
                                    try {
                                        if (!wrapper.isRepeatable()) {
                                            throw new NonRepeatableRequestException("Cannot retry request with a non-repeatable request entity");
                                        }
                                    } catch (IOException e7) {
                                        ex = e7;
                                        this.log.debug("Closing the connection.");
                                        this.managedConn.close();
                                        if (!this.retryHandler.retryRequest(ex, execCount, httpContext)) {
                                        }
                                    } catch (HttpException e8) {
                                    } catch (RuntimeException e9) {
                                    }
                                }
                                if (this.log.isDebugEnabled()) {
                                    Log log2 = this.log;
                                    StringBuilder sb = new StringBuilder();
                                    proxy = proxy2;
                                    sb.append("Attempt ");
                                    sb.append(execCount);
                                    sb.append(" to execute request");
                                    log2.debug(sb.toString());
                                } else {
                                    proxy = proxy2;
                                }
                                if (!route.isSecure()) {
                                    if (!isCleartextTrafficPermitted(route.getTargetHost().getHostName())) {
                                        throw new IOException("Cleartext traffic not permitted: " + route.getTargetHost());
                                    }
                                }
                                response = this.requestExec.execute(wrapper, this.managedConn, httpContext);
                                retrying = false;
                                proxy2 = proxy;
                                i = 1;
                            }
                        } catch (HttpException e10) {
                            ex = e10;
                            HttpRoute httpRoute4 = origRoute;
                            abortConnection();
                            throw ex;
                        } catch (IOException e11) {
                            ex = e11;
                            HttpRoute httpRoute5 = origRoute;
                            abortConnection();
                            throw ex;
                        } catch (RuntimeException e12) {
                            ex = e12;
                            HttpRoute httpRoute6 = origRoute;
                            abortConnection();
                            throw ex;
                        }
                    } catch (TunnelRefusedException ex) {
                        HttpRequest httpRequest5 = orig;
                        HttpRoute httpRoute7 = origRoute;
                        TunnelRefusedException tunnelRefusedException = ex;
                        if (this.log.isDebugEnabled()) {
                            this.log.debug(ex.getMessage());
                        }
                        response = ex.getResponse();
                    }
                } catch (HttpException e13) {
                    ex = e13;
                    HttpRequest httpRequest6 = orig;
                    HttpRoute httpRoute8 = origRoute;
                    abortConnection();
                    throw ex;
                } catch (IOException e14) {
                    ex = e14;
                    HttpRequest httpRequest7 = orig;
                    HttpRoute httpRoute9 = origRoute;
                    abortConnection();
                    throw ex;
                } catch (RuntimeException e15) {
                    ex = e15;
                    HttpRequest httpRequest8 = orig;
                    HttpRoute httpRoute10 = origRoute;
                    abortConnection();
                    throw ex;
                }
            } catch (HttpException e16) {
                ex = e16;
                HttpRequest httpRequest9 = orig;
                RequestWrapper requestWrapper4 = origWrapper2;
                HttpRoute httpRoute11 = origRoute;
                abortConnection();
                throw ex;
            } catch (IOException e17) {
                ex = e17;
                HttpRequest httpRequest10 = orig;
                RequestWrapper requestWrapper5 = origWrapper2;
                HttpRoute httpRoute12 = origRoute;
                abortConnection();
                throw ex;
            } catch (RuntimeException e18) {
                ex = e18;
                HttpRequest httpRequest11 = orig;
                RequestWrapper requestWrapper6 = origWrapper2;
                HttpRoute httpRoute13 = origRoute;
                abortConnection();
                throw ex;
            }
        }
        RequestWrapper requestWrapper7 = origWrapper2;
        HttpRoute httpRoute14 = origRoute;
        if (!(response == null || response.getEntity() == null)) {
            if (response.getEntity().isStreaming()) {
                response.setEntity(new BasicManagedEntity(response.getEntity(), this.managedConn, reuse));
                return response;
            }
        }
        if (reuse) {
            this.managedConn.markReusable();
        }
        releaseConnection();
        return response;
    }

    /* access modifiers changed from: protected */
    public void releaseConnection() {
        try {
            this.managedConn.releaseConnection();
        } catch (IOException ignored) {
            this.log.debug("IOException releasing connection", ignored);
        }
        this.managedConn = null;
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v6, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v2, resolved type: org.apache.http.HttpHost} */
    /* access modifiers changed from: protected */
    /* JADX WARNING: Multi-variable type inference failed */
    public HttpRoute determineRoute(HttpHost target, HttpRequest request, HttpContext context) throws HttpException {
        if (target == null) {
            target = request.getParams().getParameter(ClientPNames.DEFAULT_HOST);
        }
        if (target != null) {
            return this.routePlanner.determineRoute(target, request, context);
        }
        String scheme = null;
        String host = null;
        String path = null;
        if (request instanceof HttpUriRequest) {
            URI uri = ((HttpUriRequest) request).getURI();
            URI uri2 = uri;
            if (uri != null) {
                scheme = uri2.getScheme();
                host = uri2.getHost();
                path = uri2.getPath();
            }
        }
        throw new IllegalStateException("Target host must not be null, or set in parameters. scheme=" + scheme + ", host=" + host + ", path=" + path);
    }

    /* access modifiers changed from: protected */
    public void establishRoute(HttpRoute route, HttpContext context) throws HttpException, IOException {
        int step;
        HttpRouteDirector rowdy = new BasicRouteDirector();
        do {
            HttpRoute fact = this.managedConn.getRoute();
            step = rowdy.nextStep(route, fact);
            switch (step) {
                case -1:
                    throw new IllegalStateException("Unable to establish route.\nplanned = " + route + "\ncurrent = " + fact);
                case 0:
                    break;
                case 1:
                case 2:
                    this.managedConn.open(route, context, this.params);
                    continue;
                case 3:
                    boolean secure = createTunnelToTarget(route, context);
                    this.log.debug("Tunnel to target created.");
                    this.managedConn.tunnelTarget(secure, this.params);
                    continue;
                case 4:
                    int hop = fact.getHopCount() - 1;
                    boolean secure2 = createTunnelToProxy(route, hop, context);
                    this.log.debug("Tunnel to proxy created.");
                    this.managedConn.tunnelProxy(route.getHopTarget(hop), secure2, this.params);
                    continue;
                case 5:
                    this.managedConn.layerProtocol(context, this.params);
                    continue;
                default:
                    throw new IllegalStateException("Unknown step indicator " + step + " from RouteDirector.");
            }
        } while (step > 0);
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x011c  */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x013e  */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x018d  */
    /* JADX WARNING: Removed duplicated region for block: B:62:0x01bb  */
    public boolean createTunnelToTarget(HttpRoute route, HttpContext context) throws HttpException, IOException {
        HttpHost target;
        HttpResponse response;
        CredentialsProvider credsProvider;
        HttpContext httpContext = context;
        HttpHost proxy = route.getProxyHost();
        HttpHost target2 = route.getTargetHost();
        HttpResponse response2 = null;
        boolean done = false;
        while (true) {
            if (done) {
                HttpRoute httpRoute = route;
                HttpHost httpHost = target2;
                boolean z = done;
                break;
            }
            boolean done2 = true;
            if (!this.managedConn.isOpen()) {
                this.managedConn.open(route, httpContext, this.params);
            } else {
                HttpRoute httpRoute2 = route;
            }
            HttpRequest connect = createConnectRequest(route, context);
            String agent = HttpProtocolParams.getUserAgent(this.params);
            if (agent != null) {
                connect.addHeader(HTTP.USER_AGENT, agent);
            }
            connect.addHeader(HTTP.TARGET_HOST, target2.toHostString());
            AuthScheme authScheme = this.proxyAuthState.getAuthScheme();
            AuthScope authScope = this.proxyAuthState.getAuthScope();
            Credentials creds = this.proxyAuthState.getCredentials();
            if (creds != null && (authScope != null || !authScheme.isConnectionBased())) {
                try {
                    connect.addHeader(authScheme.authenticate(creds, connect));
                } catch (AuthenticationException ex) {
                    if (this.log.isErrorEnabled()) {
                        this.log.error("Proxy authentication error: " + ex.getMessage());
                    }
                }
            }
            HttpResponse response3 = this.requestExec.execute(connect, this.managedConn, httpContext);
            int status = response3.getStatusLine().getStatusCode();
            if (status >= 200) {
                CredentialsProvider credsProvider2 = (CredentialsProvider) httpContext.getAttribute(ClientContext.CREDS_PROVIDER);
                if (credsProvider2 == null || !HttpClientParams.isAuthenticating(this.params)) {
                    response2 = response3;
                    target = target2;
                } else if (this.proxyAuthHandler.isAuthenticationRequested(response3, httpContext)) {
                    this.log.debug("Proxy requested authentication");
                    try {
                        target = target2;
                        credsProvider = credsProvider2;
                        int i = status;
                        response = response3;
                        Credentials credentials = creds;
                        AuthScheme authScheme2 = authScheme;
                        try {
                            processChallenges(this.proxyAuthHandler.getChallenges(response3, httpContext), this.proxyAuthState, this.proxyAuthHandler, response, httpContext);
                        } catch (AuthenticationException e) {
                            ex = e;
                        }
                    } catch (AuthenticationException e2) {
                        ex = e2;
                        int i2 = status;
                        response = response3;
                        Credentials credentials2 = creds;
                        AuthScheme authScheme3 = authScheme;
                        target = target2;
                        credsProvider = credsProvider2;
                        if (this.log.isWarnEnabled()) {
                            this.log.warn("Authentication error: " + ex.getMessage());
                            response2 = response;
                            if (response2.getStatusLine().getStatusCode() <= 299) {
                            }
                        }
                        updateAuthState(this.proxyAuthState, proxy, credsProvider);
                        if (this.proxyAuthState.getCredentials() == null) {
                        }
                        done = done2;
                        target2 = target;
                    }
                    updateAuthState(this.proxyAuthState, proxy, credsProvider);
                    if (this.proxyAuthState.getCredentials() == null) {
                        done2 = false;
                        response2 = response;
                        if (this.reuseStrategy.keepAlive(response2, httpContext)) {
                            this.log.debug("Connection kept alive");
                            HttpEntity entity = response2.getEntity();
                            if (entity != null) {
                                entity.consumeContent();
                            }
                        } else {
                            this.managedConn.close();
                        }
                    } else {
                        response2 = response;
                    }
                } else {
                    Credentials credentials3 = creds;
                    AuthScheme authScheme4 = authScheme;
                    target = target2;
                    CredentialsProvider credentialsProvider = credsProvider2;
                    response2 = response3;
                    this.proxyAuthState.setAuthScope(null);
                }
                done = done2;
                target2 = target;
            } else {
                int i3 = status;
                Credentials credentials4 = creds;
                AuthScheme authScheme5 = authScheme;
                HttpHost httpHost2 = target2;
                throw new HttpException("Unexpected response to CONNECT request: " + response3.getStatusLine());
            }
        }
        if (response2.getStatusLine().getStatusCode() <= 299) {
            HttpEntity entity2 = response2.getEntity();
            if (entity2 != null) {
                response2.setEntity(new BufferedHttpEntity(entity2));
            }
            this.managedConn.close();
            throw new TunnelRefusedException("CONNECT refused by proxy: " + response2.getStatusLine(), response2);
        }
        this.managedConn.markReusable();
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean createTunnelToProxy(HttpRoute route, int hop, HttpContext context) throws HttpException, IOException {
        throw new UnsupportedOperationException("Proxy chains are not supported.");
    }

    /* access modifiers changed from: protected */
    public HttpRequest createConnectRequest(HttpRoute route, HttpContext context) {
        HttpHost target = route.getTargetHost();
        String host = target.getHostName();
        int port = target.getPort();
        if (port < 0) {
            port = this.connManager.getSchemeRegistry().getScheme(target.getSchemeName()).getDefaultPort();
        }
        StringBuilder buffer = new StringBuilder(host.length() + 6);
        buffer.append(host);
        buffer.append(':');
        buffer.append(Integer.toString(port));
        return new BasicHttpRequest("CONNECT", buffer.toString(), HttpProtocolParams.getVersion(this.params));
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x0134 A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x0135 A[RETURN] */
    public RoutedRequest handleResponse(RoutedRequest roureq, HttpResponse response, HttpContext context) throws HttpException, IOException {
        RoutedRequest routedRequest;
        RoutedRequest routedRequest2;
        HttpHost target;
        HttpResponse httpResponse = response;
        HttpContext httpContext = context;
        HttpRoute route = roureq.getRoute();
        HttpHost proxy = route.getProxyHost();
        RequestWrapper request = roureq.getRequest();
        HttpParams params2 = request.getParams();
        if (!HttpClientParams.isRedirecting(params2) || !this.redirectHandler.isRedirectRequested(httpResponse, httpContext)) {
            CredentialsProvider credsProvider = (CredentialsProvider) httpContext.getAttribute(ClientContext.CREDS_PROVIDER);
            if (credsProvider == null || !HttpClientParams.isAuthenticating(params2)) {
                RequestWrapper requestWrapper = request;
                routedRequest = null;
            } else if (this.targetAuthHandler.isAuthenticationRequested(httpResponse, httpContext)) {
                HttpHost target2 = (HttpHost) httpContext.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
                if (target2 == null) {
                    target2 = route.getTargetHost();
                }
                HttpHost target3 = target2;
                this.log.debug("Target requested authentication");
                try {
                    HttpRoute httpRoute = route;
                    target = target3;
                    RequestWrapper requestWrapper2 = request;
                    routedRequest2 = null;
                    try {
                        processChallenges(this.targetAuthHandler.getChallenges(httpResponse, httpContext), this.targetAuthState, this.targetAuthHandler, httpResponse, httpContext);
                    } catch (AuthenticationException e) {
                        ex = e;
                    }
                } catch (AuthenticationException e2) {
                    ex = e2;
                    HttpRoute httpRoute2 = route;
                    RequestWrapper requestWrapper3 = request;
                    target = target3;
                    routedRequest2 = null;
                    if (this.log.isWarnEnabled()) {
                        this.log.warn("Authentication error: " + ex.getMessage());
                        return routedRequest2;
                    }
                    updateAuthState(this.targetAuthState, target, credsProvider);
                    if (this.targetAuthState.getCredentials() == null) {
                    }
                }
                updateAuthState(this.targetAuthState, target, credsProvider);
                if (this.targetAuthState.getCredentials() == null) {
                    return roureq;
                }
                return routedRequest2;
            } else {
                RequestWrapper requestWrapper4 = request;
                routedRequest = null;
                this.targetAuthState.setAuthScope(null);
                if (this.proxyAuthHandler.isAuthenticationRequested(httpResponse, httpContext)) {
                    this.log.debug("Proxy requested authentication");
                    try {
                        processChallenges(this.proxyAuthHandler.getChallenges(httpResponse, httpContext), this.proxyAuthState, this.proxyAuthHandler, httpResponse, httpContext);
                    } catch (AuthenticationException ex) {
                        if (this.log.isWarnEnabled()) {
                            this.log.warn("Authentication error: " + ex.getMessage());
                            return null;
                        }
                    }
                    updateAuthState(this.proxyAuthState, proxy, credsProvider);
                    if (this.proxyAuthState.getCredentials() != null) {
                        return roureq;
                    }
                    return null;
                }
                this.proxyAuthState.setAuthScope(null);
            }
            return routedRequest;
        } else if (this.redirectCount < this.maxRedirects) {
            this.redirectCount++;
            URI uri = this.redirectHandler.getLocationURI(httpResponse, httpContext);
            HttpHost newTarget = new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme());
            HttpGet redirect = new HttpGet(uri);
            redirect.setHeaders(request.getOriginal().getAllHeaders());
            RequestWrapper wrapper = new RequestWrapper(redirect);
            wrapper.setParams(params2);
            HttpRoute newRoute = determineRoute(newTarget, wrapper, httpContext);
            RoutedRequest newRequest = new RoutedRequest(wrapper, newRoute);
            if (this.log.isDebugEnabled()) {
                Log log2 = this.log;
                HttpHost httpHost = newTarget;
                StringBuilder sb = new StringBuilder();
                HttpGet httpGet = redirect;
                sb.append("Redirecting to '");
                sb.append(uri);
                sb.append("' via ");
                sb.append(newRoute);
                log2.debug(sb.toString());
            } else {
                HttpGet httpGet2 = redirect;
            }
            return newRequest;
        } else {
            throw new RedirectException("Maximum redirects (" + this.maxRedirects + ") exceeded");
        }
    }

    private void abortConnection() {
        ManagedClientConnection mcc = this.managedConn;
        if (mcc != null) {
            this.managedConn = null;
            try {
                mcc.abortConnection();
            } catch (IOException ex) {
                if (this.log.isDebugEnabled()) {
                    this.log.debug(ex.getMessage(), ex);
                }
            }
            try {
                mcc.releaseConnection();
            } catch (IOException ignored) {
                this.log.debug("Error releasing connection", ignored);
            }
        }
    }

    private void processChallenges(Map<String, Header> challenges, AuthState authState, AuthenticationHandler authHandler, HttpResponse response, HttpContext context) throws MalformedChallengeException, AuthenticationException {
        AuthScheme authScheme = authState.getAuthScheme();
        if (authScheme == null) {
            authScheme = authHandler.selectScheme(challenges, response, context);
            authState.setAuthScheme(authScheme);
        }
        String id = authScheme.getSchemeName();
        Header challenge = challenges.get(id.toLowerCase(Locale.ENGLISH));
        if (challenge != null) {
            authScheme.processChallenge(challenge);
            this.log.debug("Authorization challenge processed");
            return;
        }
        throw new AuthenticationException(id + " authorization challenge expected, but not found");
    }

    private void updateAuthState(AuthState authState, HttpHost host, CredentialsProvider credsProvider) {
        if (authState.isValid()) {
            String hostname = host.getHostName();
            int port = host.getPort();
            if (port < 0) {
                port = this.connManager.getSchemeRegistry().getScheme(host).getDefaultPort();
            }
            AuthScheme authScheme = authState.getAuthScheme();
            AuthScope authScope = new AuthScope(hostname, port, authScheme.getRealm(), authScheme.getSchemeName());
            if (this.log.isDebugEnabled()) {
                Log log2 = this.log;
                log2.debug("Authentication scope: " + authScope);
            }
            Credentials creds = authState.getCredentials();
            if (creds == null) {
                creds = credsProvider.getCredentials(authScope);
                if (this.log.isDebugEnabled()) {
                    if (creds != null) {
                        this.log.debug("Found credentials");
                    } else {
                        this.log.debug("Credentials not found");
                    }
                }
            } else if (authScheme.isComplete()) {
                this.log.debug("Authentication failed");
                creds = null;
            }
            authState.setAuthScope(authScope);
            authState.setCredentials(creds);
        }
    }

    private static boolean isCleartextTrafficPermitted(String hostname) {
        Object policy;
        Method method;
        try {
            synchronized (DefaultRequestDirector.class) {
                if (cleartextTrafficPermittedMethod == null) {
                    Class<?> cls = Class.forName("android.security.NetworkSecurityPolicy");
                    networkSecurityPolicy = cls.getMethod("getInstance", new Class[0]).invoke(null, new Object[0]);
                    cleartextTrafficPermittedMethod = cls.getMethod("isCleartextTrafficPermitted", new Class[]{String.class});
                }
                policy = networkSecurityPolicy;
                method = cleartextTrafficPermittedMethod;
            }
            return ((Boolean) method.invoke(policy, new Object[]{hostname})).booleanValue();
        } catch (ReflectiveOperationException e) {
            return true;
        }
    }
}
