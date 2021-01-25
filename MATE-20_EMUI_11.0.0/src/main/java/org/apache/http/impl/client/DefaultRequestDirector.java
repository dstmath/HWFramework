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
    private final Log log = LogFactory.getLog(getClass());
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

    /* JADX INFO: Multiple debug info for r6v6 'route'  org.apache.http.conn.routing.HttpRoute: [D('target' org.apache.http.HttpHost), D('route' org.apache.http.conn.routing.HttpRoute)] */
    /* JADX WARNING: Code restructure failed: missing block: B:100:0x0205, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:101:0x0206, code lost:
        r4 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:102:0x0208, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:103:0x0209, code lost:
        r3 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:104:0x020e, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:105:0x020f, code lost:
        r3 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:111:0x0232, code lost:
        if (r25.log.isInfoEnabled() != false) goto L_0x0234;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:112:0x0234, code lost:
        r5 = r25.log;
        r7 = new java.lang.StringBuilder();
        r22 = r9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:114:?, code lost:
        r7.append("I/O exception (");
        r7.append(r4.getClass().getName());
        r7.append(") caught when processing request: ");
        r7.append(r4.getMessage());
        r5.info(r7.toString());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:115:0x0261, code lost:
        r22 = r9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:117:0x0269, code lost:
        if (r25.log.isDebugEnabled() != false) goto L_0x026b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:118:0x026b, code lost:
        r25.log.debug(r4.getMessage(), r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:119:0x0274, code lost:
        r25.log.info("Retrying request");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:120:0x0280, code lost:
        if (r6.getHopCount() == 1) goto L_0x0282;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:121:0x0282, code lost:
        r25.log.debug("Reopening the direct connection.");
        r25.managedConn.open(r6, r28, r25.params);
        r4 = r4;
        r7 = r21;
        r9 = r22;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:122:0x0299, code lost:
        throw r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:124:0x029c, code lost:
        throw r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:168:0x0382, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:169:0x0383, code lost:
        r3 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:170:0x038a, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:171:0x038b, code lost:
        r3 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:172:0x0392, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:173:0x0393, code lost:
        r3 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x009f, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x00a0, code lost:
        r3 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x00a7, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x00a8, code lost:
        r3 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x00af, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x00b0, code lost:
        r3 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:81:0x0199, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:82:0x019a, code lost:
        r3 = r0;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:102:0x0208 A[ExcHandler: RuntimeException (r0v23 'e' java.lang.RuntimeException A[CUSTOM_DECLARE]), Splitter:B:75:0x018a] */
    /* JADX WARNING: Removed duplicated region for block: B:104:0x020e A[ExcHandler: HttpException (r0v22 'e' org.apache.http.HttpException A[CUSTOM_DECLARE]), Splitter:B:75:0x018a] */
    /* JADX WARNING: Removed duplicated region for block: B:110:0x022c  */
    /* JADX WARNING: Removed duplicated region for block: B:211:0x029a A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x009f A[ExcHandler: RuntimeException (r0v35 'e' java.lang.RuntimeException A[CUSTOM_DECLARE]), Splitter:B:11:0x004a] */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x00af A[ExcHandler: HttpException (r0v33 'e' org.apache.http.HttpException A[CUSTOM_DECLARE]), Splitter:B:11:0x004a] */
    @Override // org.apache.http.client.RequestDirector
    public HttpResponse execute(HttpHost target, HttpRequest request, HttpContext context) throws HttpException, IOException {
        HttpException ex;
        IOException ex2;
        RuntimeException ex3;
        HttpRoute origRoute;
        HttpRoute route;
        HttpHost target2;
        HttpHost proxy;
        HttpRequest orig = request;
        RequestWrapper origWrapper = wrapRequest(orig);
        origWrapper.setParams(this.params);
        HttpHost target3 = target;
        HttpRoute origRoute2 = determineRoute(target3, origWrapper, context);
        RoutedRequest roureq = new RoutedRequest(origWrapper, origRoute2);
        long timeout = ConnManagerParams.getTimeout(this.params);
        int execCount = 0;
        boolean reuse = false;
        HttpResponse response = null;
        boolean done = false;
        while (!done) {
            try {
                RequestWrapper wrapper = roureq.getRequest();
                HttpRoute route2 = roureq.getRoute();
                Object userToken = context.getAttribute(ClientContext.USER_TOKEN);
                try {
                    if (this.managedConn == null) {
                        try {
                            route = route2;
                            origRoute = origRoute2;
                            try {
                                ClientConnectionRequest connRequest = this.connManager.requestConnection(route, userToken);
                                if (orig instanceof AbortableHttpRequest) {
                                    ((AbortableHttpRequest) orig).setConnectionRequest(connRequest);
                                }
                                try {
                                    this.managedConn = connRequest.getConnection(timeout, TimeUnit.MILLISECONDS);
                                    if (HttpConnectionParams.isStaleCheckingEnabled(this.params)) {
                                        this.log.debug("Stale connection check");
                                        if (this.managedConn.isStale()) {
                                            this.log.debug("Stale connection detected");
                                            this.managedConn.close();
                                        }
                                    }
                                } catch (InterruptedException interrupted) {
                                    InterruptedIOException iox = new InterruptedIOException();
                                    iox.initCause(interrupted);
                                    throw iox;
                                }
                            } catch (IOException e) {
                            } catch (HttpException e2) {
                            } catch (RuntimeException e3) {
                            }
                        } catch (HttpException e4) {
                            ex = e4;
                            abortConnection();
                            throw ex;
                        } catch (IOException e5) {
                            ex2 = e5;
                            abortConnection();
                            throw ex2;
                        } catch (RuntimeException e6) {
                            ex3 = e6;
                            abortConnection();
                            throw ex3;
                        }
                    } else {
                        route = route2;
                        origRoute = origRoute2;
                    }
                } catch (HttpException e7) {
                    ex = e7;
                    abortConnection();
                    throw ex;
                } catch (IOException e8) {
                    ex2 = e8;
                    abortConnection();
                    throw ex2;
                } catch (RuntimeException e9) {
                    ex3 = e9;
                    abortConnection();
                    throw ex3;
                }
                try {
                    if (orig instanceof AbortableHttpRequest) {
                        ((AbortableHttpRequest) orig).setReleaseTrigger(this.managedConn);
                    }
                    if (!this.managedConn.isOpen()) {
                        this.managedConn.open(route, context, this.params);
                    } else {
                        this.managedConn.setSocketTimeout(HttpConnectionParams.getSoTimeout(this.params));
                    }
                    try {
                        establishRoute(route, context);
                        wrapper.resetHeaders();
                        rewriteRequestURI(wrapper, route);
                        target2 = (HttpHost) wrapper.getParams().getParameter(ClientPNames.VIRTUAL_HOST);
                        if (target2 == null) {
                            try {
                                target2 = route.getTargetHost();
                            } catch (HttpException e10) {
                                ex = e10;
                                abortConnection();
                                throw ex;
                            } catch (IOException e11) {
                                ex2 = e11;
                                abortConnection();
                                throw ex2;
                            } catch (RuntimeException e12) {
                                ex3 = e12;
                                abortConnection();
                                throw ex3;
                            }
                        }
                    } catch (TunnelRefusedException ex4) {
                        if (this.log.isDebugEnabled()) {
                            this.log.debug(ex4.getMessage());
                        }
                        response = ex4.getResponse();
                    }
                    try {
                        HttpHost proxy2 = route.getProxyHost();
                        try {
                            context.setAttribute(ExecutionContext.HTTP_TARGET_HOST, target2);
                            context.setAttribute(ExecutionContext.HTTP_PROXY_HOST, proxy2);
                        } catch (HttpException e13) {
                            ex = e13;
                            abortConnection();
                            throw ex;
                        } catch (IOException e14) {
                            ex2 = e14;
                            abortConnection();
                            throw ex2;
                        } catch (RuntimeException e15) {
                            ex3 = e15;
                            abortConnection();
                            throw ex3;
                        }
                        try {
                            context.setAttribute(ExecutionContext.HTTP_CONNECTION, this.managedConn);
                            context.setAttribute(ClientContext.TARGET_AUTH_STATE, this.targetAuthState);
                            context.setAttribute(ClientContext.PROXY_AUTH_STATE, this.proxyAuthState);
                            this.requestExec.preProcess(wrapper, this.httpProcessor, context);
                            context.setAttribute(ExecutionContext.HTTP_REQUEST, wrapper);
                            boolean retrying = true;
                            while (retrying) {
                                execCount++;
                                wrapper.incrementExecCount();
                                if (wrapper.getExecCount() > 1) {
                                    try {
                                        if (!wrapper.isRepeatable()) {
                                            throw new NonRepeatableRequestException("Cannot retry request with a non-repeatable request entity");
                                        }
                                    } catch (IOException e16) {
                                        proxy = proxy2;
                                        IOException ex5 = e16;
                                        this.log.debug("Closing the connection.");
                                        this.managedConn.close();
                                        if (!this.retryHandler.retryRequest(ex5, execCount, context)) {
                                        }
                                    } catch (HttpException e17) {
                                    } catch (RuntimeException e18) {
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
                                response = this.requestExec.execute(wrapper, this.managedConn, context);
                                retrying = false;
                                proxy2 = proxy;
                            }
                            response.setParams(this.params);
                            this.requestExec.postProcess(response, this.httpProcessor, context);
                            reuse = this.reuseStrategy.keepAlive(response, context);
                            if (reuse) {
                                this.managedConn.setIdleDuration(this.keepAliveStrategy.getKeepAliveDuration(response, context), TimeUnit.MILLISECONDS);
                            }
                            RoutedRequest followup = handleResponse(roureq, response, context);
                            if (followup == null) {
                                done = true;
                            } else {
                                if (reuse) {
                                    this.log.debug("Connection kept alive");
                                    HttpEntity entity = response.getEntity();
                                    if (entity != null) {
                                        entity.consumeContent();
                                    }
                                    this.managedConn.markReusable();
                                } else {
                                    this.managedConn.close();
                                }
                                if (!followup.getRoute().equals(roureq.getRoute())) {
                                    releaseConnection();
                                }
                                roureq = followup;
                            }
                            Object userToken2 = this.userTokenHandler.getUserToken(context);
                            context.setAttribute(ClientContext.USER_TOKEN, userToken2);
                            if (this.managedConn != null) {
                                this.managedConn.setState(userToken2);
                            }
                            origWrapper = origWrapper;
                            origRoute2 = origRoute;
                            target3 = target2;
                            orig = orig;
                            timeout = timeout;
                        } catch (HttpException e19) {
                            ex = e19;
                            abortConnection();
                            throw ex;
                        } catch (IOException e20) {
                            ex2 = e20;
                            abortConnection();
                            throw ex2;
                        } catch (RuntimeException e21) {
                            ex3 = e21;
                            abortConnection();
                            throw ex3;
                        }
                    } catch (HttpException e22) {
                        ex = e22;
                        abortConnection();
                        throw ex;
                    } catch (IOException e23) {
                        ex2 = e23;
                        abortConnection();
                        throw ex2;
                    } catch (RuntimeException e24) {
                        ex3 = e24;
                        abortConnection();
                        throw ex3;
                    }
                } catch (HttpException e25) {
                    ex = e25;
                    abortConnection();
                    throw ex;
                } catch (IOException e26) {
                    ex2 = e26;
                    abortConnection();
                    throw ex2;
                } catch (RuntimeException e27) {
                    ex3 = e27;
                    abortConnection();
                    throw ex3;
                }
            } catch (HttpException e28) {
                ex = e28;
                abortConnection();
                throw ex;
            } catch (IOException e29) {
                ex2 = e29;
                abortConnection();
                throw ex2;
            } catch (RuntimeException e30) {
                ex3 = e30;
                abortConnection();
                throw ex3;
            }
        }
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

    /* access modifiers changed from: protected */
    public HttpRoute determineRoute(HttpHost target, HttpRequest request, HttpContext context) throws HttpException {
        URI uri;
        if (target == null) {
            target = (HttpHost) request.getParams().getParameter(ClientPNames.DEFAULT_HOST);
        }
        if (target != null) {
            return this.routePlanner.determineRoute(target, request, context);
        }
        String scheme = null;
        String host = null;
        String path = null;
        if ((request instanceof HttpUriRequest) && (uri = ((HttpUriRequest) request).getURI()) != null) {
            scheme = uri.getScheme();
            host = uri.getHost();
            path = uri.getPath();
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
    /* JADX WARNING: Removed duplicated region for block: B:42:0x0120  */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x0142  */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x018e  */
    /* JADX WARNING: Removed duplicated region for block: B:62:0x01bc  */
    public boolean createTunnelToTarget(HttpRoute route, HttpContext context) throws HttpException, IOException {
        HttpHost target;
        HttpResponse response;
        HttpResponse response2;
        CredentialsProvider credsProvider;
        AuthenticationException ex;
        HttpHost proxy = route.getProxyHost();
        HttpHost target2 = route.getTargetHost();
        boolean done = false;
        HttpResponse response3 = null;
        while (true) {
            if (done) {
                break;
            }
            boolean done2 = true;
            if (!this.managedConn.isOpen()) {
                this.managedConn.open(route, context, this.params);
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
                } catch (AuthenticationException ex2) {
                    if (this.log.isErrorEnabled()) {
                        this.log.error("Proxy authentication error: " + ex2.getMessage());
                    }
                }
            }
            HttpResponse response4 = this.requestExec.execute(connect, this.managedConn, context);
            if (response4.getStatusLine().getStatusCode() >= 200) {
                CredentialsProvider credsProvider2 = (CredentialsProvider) context.getAttribute(ClientContext.CREDS_PROVIDER);
                if (credsProvider2 == null || !HttpClientParams.isAuthenticating(this.params)) {
                    response = response4;
                    target = target2;
                } else if (this.proxyAuthHandler.isAuthenticationRequested(response4, context)) {
                    this.log.debug("Proxy requested authentication");
                    try {
                        target = target2;
                        credsProvider = credsProvider2;
                        response2 = response4;
                        try {
                            processChallenges(this.proxyAuthHandler.getChallenges(response4, context), this.proxyAuthState, this.proxyAuthHandler, response4, context);
                        } catch (AuthenticationException e) {
                            ex = e;
                        }
                    } catch (AuthenticationException e2) {
                        ex = e2;
                        response2 = response4;
                        target = target2;
                        credsProvider = credsProvider2;
                        if (this.log.isWarnEnabled()) {
                            this.log.warn("Authentication error: " + ex.getMessage());
                            response3 = response2;
                            if (response3.getStatusLine().getStatusCode() <= 299) {
                            }
                        }
                        updateAuthState(this.proxyAuthState, proxy, credsProvider);
                        if (this.proxyAuthState.getCredentials() == null) {
                        }
                        response3 = response;
                        done = done2;
                        target2 = target;
                    }
                    updateAuthState(this.proxyAuthState, proxy, credsProvider);
                    if (this.proxyAuthState.getCredentials() == null) {
                        done2 = false;
                        response = response2;
                        if (this.reuseStrategy.keepAlive(response, context)) {
                            this.log.debug("Connection kept alive");
                            HttpEntity entity = response.getEntity();
                            if (entity != null) {
                                entity.consumeContent();
                            }
                        } else {
                            this.managedConn.close();
                        }
                    } else {
                        response = response2;
                    }
                } else {
                    response = response4;
                    target = target2;
                    this.proxyAuthState.setAuthScope(null);
                }
                response3 = response;
                done = done2;
                target2 = target;
            } else {
                throw new HttpException("Unexpected response to CONNECT request: " + response4.getStatusLine());
            }
        }
        if (response3.getStatusLine().getStatusCode() <= 299) {
            HttpEntity entity2 = response3.getEntity();
            if (entity2 != null) {
                response3.setEntity(new BufferedHttpEntity(entity2));
            }
            this.managedConn.close();
            throw new TunnelRefusedException("CONNECT refused by proxy: " + response3.getStatusLine(), response3);
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
    /* JADX WARNING: Removed duplicated region for block: B:38:0x0135 A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x0136 A[RETURN] */
    public RoutedRequest handleResponse(RoutedRequest roureq, HttpResponse response, HttpContext context) throws HttpException, IOException {
        HttpHost target;
        HttpHost target2;
        String str;
        AuthenticationException ex;
        HttpRoute route = roureq.getRoute();
        HttpHost proxy = route.getProxyHost();
        RequestWrapper request = roureq.getRequest();
        HttpParams params2 = request.getParams();
        if (!HttpClientParams.isRedirecting(params2) || !this.redirectHandler.isRedirectRequested(response, context)) {
            CredentialsProvider credsProvider = (CredentialsProvider) context.getAttribute(ClientContext.CREDS_PROVIDER);
            if (credsProvider == null || !HttpClientParams.isAuthenticating(params2)) {
                return null;
            }
            if (this.targetAuthHandler.isAuthenticationRequested(response, context)) {
                HttpHost target3 = (HttpHost) context.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
                if (target3 == null) {
                    target = route.getTargetHost();
                } else {
                    target = target3;
                }
                this.log.debug("Target requested authentication");
                try {
                    target2 = target;
                    str = "Authentication error: ";
                    try {
                        processChallenges(this.targetAuthHandler.getChallenges(response, context), this.targetAuthState, this.targetAuthHandler, response, context);
                    } catch (AuthenticationException e) {
                        ex = e;
                    }
                } catch (AuthenticationException e2) {
                    ex = e2;
                    target2 = target;
                    str = "Authentication error: ";
                    if (this.log.isWarnEnabled()) {
                        Log log2 = this.log;
                        log2.warn(str + ex.getMessage());
                        return null;
                    }
                    updateAuthState(this.targetAuthState, target2, credsProvider);
                    if (this.targetAuthState.getCredentials() == null) {
                    }
                }
                updateAuthState(this.targetAuthState, target2, credsProvider);
                if (this.targetAuthState.getCredentials() == null) {
                    return roureq;
                }
                return null;
            }
            this.targetAuthState.setAuthScope(null);
            if (this.proxyAuthHandler.isAuthenticationRequested(response, context)) {
                this.log.debug("Proxy requested authentication");
                try {
                    processChallenges(this.proxyAuthHandler.getChallenges(response, context), this.proxyAuthState, this.proxyAuthHandler, response, context);
                } catch (AuthenticationException ex2) {
                    if (this.log.isWarnEnabled()) {
                        Log log3 = this.log;
                        log3.warn("Authentication error: " + ex2.getMessage());
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
            return null;
        }
        int i = this.redirectCount;
        if (i < this.maxRedirects) {
            this.redirectCount = i + 1;
            URI uri = this.redirectHandler.getLocationURI(response, context);
            HttpHost newTarget = new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme());
            HttpGet redirect = new HttpGet(uri);
            redirect.setHeaders(request.getOriginal().getAllHeaders());
            RequestWrapper wrapper = new RequestWrapper(redirect);
            wrapper.setParams(params2);
            HttpRoute newRoute = determineRoute(newTarget, wrapper, context);
            RoutedRequest newRequest = new RoutedRequest(wrapper, newRoute);
            if (this.log.isDebugEnabled()) {
                Log log4 = this.log;
                log4.debug("Redirecting to '" + uri + "' via " + newRoute);
            }
            return newRequest;
        }
        throw new RedirectException("Maximum redirects (" + this.maxRedirects + ") exceeded");
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

    /* JADX INFO: Multiple debug info for r2v2 java.lang.Object: [D('cls' java.lang.Class<?>), D('policy' java.lang.Object)] */
    private static boolean isCleartextTrafficPermitted(String hostname) {
        Object policy;
        Method method;
        try {
            synchronized (DefaultRequestDirector.class) {
                if (cleartextTrafficPermittedMethod == null) {
                    Class<?> cls = Class.forName("android.security.NetworkSecurityPolicy");
                    networkSecurityPolicy = cls.getMethod("getInstance", new Class[0]).invoke(null, new Object[0]);
                    cleartextTrafficPermittedMethod = cls.getMethod("isCleartextTrafficPermitted", String.class);
                }
                policy = networkSecurityPolicy;
                method = cleartextTrafficPermittedMethod;
            }
            return ((Boolean) method.invoke(policy, hostname)).booleanValue();
        } catch (ReflectiveOperationException e) {
            return true;
        }
    }
}
