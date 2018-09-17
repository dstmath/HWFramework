package org.apache.http.impl.client;

import java.io.IOException;
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

    public DefaultRequestDirector(HttpRequestExecutor requestExec, ClientConnectionManager conman, ConnectionReuseStrategy reustrat, ConnectionKeepAliveStrategy kastrat, HttpRoutePlanner rouplan, HttpProcessor httpProcessor, HttpRequestRetryHandler retryHandler, RedirectHandler redirectHandler, AuthenticationHandler targetAuthHandler, AuthenticationHandler proxyAuthHandler, UserTokenHandler userTokenHandler, HttpParams params) {
        if (requestExec == null) {
            throw new IllegalArgumentException("Request executor may not be null.");
        } else if (conman == null) {
            throw new IllegalArgumentException("Client connection manager may not be null.");
        } else if (reustrat == null) {
            throw new IllegalArgumentException("Connection reuse strategy may not be null.");
        } else if (kastrat == null) {
            throw new IllegalArgumentException("Connection keep alive strategy may not be null.");
        } else if (rouplan == null) {
            throw new IllegalArgumentException("Route planner may not be null.");
        } else if (httpProcessor == null) {
            throw new IllegalArgumentException("HTTP protocol processor may not be null.");
        } else if (retryHandler == null) {
            throw new IllegalArgumentException("HTTP request retry handler may not be null.");
        } else if (redirectHandler == null) {
            throw new IllegalArgumentException("Redirect handler may not be null.");
        } else if (targetAuthHandler == null) {
            throw new IllegalArgumentException("Target authentication handler may not be null.");
        } else if (proxyAuthHandler == null) {
            throw new IllegalArgumentException("Proxy authentication handler may not be null.");
        } else if (userTokenHandler == null) {
            throw new IllegalArgumentException("User token handler may not be null.");
        } else if (params == null) {
            throw new IllegalArgumentException("HTTP parameters may not be null");
        } else {
            this.requestExec = requestExec;
            this.connManager = conman;
            this.reuseStrategy = reustrat;
            this.keepAliveStrategy = kastrat;
            this.routePlanner = rouplan;
            this.httpProcessor = httpProcessor;
            this.retryHandler = retryHandler;
            this.redirectHandler = redirectHandler;
            this.targetAuthHandler = targetAuthHandler;
            this.proxyAuthHandler = proxyAuthHandler;
            this.userTokenHandler = userTokenHandler;
            this.params = params;
            this.managedConn = null;
            this.redirectCount = 0;
            this.maxRedirects = this.params.getIntParameter(ClientPNames.MAX_REDIRECTS, 100);
            this.targetAuthState = new AuthState();
            this.proxyAuthState = new AuthState();
        }
    }

    private RequestWrapper wrapRequest(HttpRequest request) throws ProtocolException {
        if (request instanceof HttpEntityEnclosingRequest) {
            return new EntityEnclosingRequestWrapper((HttpEntityEnclosingRequest) request);
        }
        return new RequestWrapper(request);
    }

    protected void rewriteRequestURI(RequestWrapper request, HttpRoute route) throws ProtocolException {
        try {
            URI uri = request.getURI();
            if (route.getProxyHost() == null || (route.isTunnelled() ^ 1) == 0) {
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

    /* JADX WARNING: Removed duplicated region for block: B:39:0x01c8 A:{Splitter: B:91:0x0355, ExcHandler: org.apache.http.HttpException (r12_0 'ex' org.apache.http.HttpException)} */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x01f5 A:{Splitter: B:2:0x0040, ExcHandler: java.lang.RuntimeException (r11_0 'ex' java.lang.RuntimeException)} */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x01c8 A:{Splitter: B:91:0x0355, ExcHandler: org.apache.http.HttpException (r12_0 'ex' org.apache.http.HttpException)} */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x01f5 A:{Splitter: B:2:0x0040, ExcHandler: java.lang.RuntimeException (r11_0 'ex' java.lang.RuntimeException)} */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Missing block: B:39:0x01c8, code:
            r12 = move-exception;
     */
    /* JADX WARNING: Missing block: B:40:0x01c9, code:
            abortConnection();
     */
    /* JADX WARNING: Missing block: B:41:0x01cc, code:
            throw r12;
     */
    /* JADX WARNING: Missing block: B:42:0x01cd, code:
            r17 = move-exception;
     */
    /* JADX WARNING: Missing block: B:44:?, code:
            r18 = new java.io.InterruptedIOException();
            r18.initCause(r17);
     */
    /* JADX WARNING: Missing block: B:45:0x01da, code:
            throw r18;
     */
    /* JADX WARNING: Missing block: B:46:0x01db, code:
            r10 = move-exception;
     */
    /* JADX WARNING: Missing block: B:47:0x01dc, code:
            abortConnection();
     */
    /* JADX WARNING: Missing block: B:48:0x01df, code:
            throw r10;
     */
    /* JADX WARNING: Missing block: B:51:0x01f5, code:
            r11 = move-exception;
     */
    /* JADX WARNING: Missing block: B:52:0x01f6, code:
            abortConnection();
     */
    /* JADX WARNING: Missing block: B:53:0x01f9, code:
            throw r11;
     */
    /* JADX WARNING: Missing block: B:77:0x0295, code:
            r10 = move-exception;
     */
    /* JADX WARNING: Missing block: B:79:?, code:
            r35.log.debug("Closing the connection.");
            r35.managedConn.close();
     */
    /* JADX WARNING: Missing block: B:80:0x02b9, code:
            if (r35.retryHandler.retryRequest(r10, r14, r38) != false) goto L_0x02bb;
     */
    /* JADX WARNING: Missing block: B:82:0x02c5, code:
            if (r35.log.isInfoEnabled() != false) goto L_0x02c7;
     */
    /* JADX WARNING: Missing block: B:83:0x02c7, code:
            r35.log.info("I/O exception (" + r10.getClass().getName() + ") caught when processing request: " + r10.getMessage());
     */
    /* JADX WARNING: Missing block: B:85:0x0305, code:
            if (r35.log.isDebugEnabled() != false) goto L_0x0307;
     */
    /* JADX WARNING: Missing block: B:86:0x0307, code:
            r35.log.debug(r10.getMessage(), r10);
     */
    /* JADX WARNING: Missing block: B:87:0x0318, code:
            r35.log.info("Retrying request");
     */
    /* JADX WARNING: Missing block: B:88:0x032e, code:
            if (r27.getHopCount() == 1) goto L_0x0330;
     */
    /* JADX WARNING: Missing block: B:89:0x0330, code:
            r35.log.debug("Reopening the direct connection.");
            r35.managedConn.open(r27, r38, r35.params);
     */
    /* JADX WARNING: Missing block: B:95:?, code:
            throw r10;
     */
    /* JADX WARNING: Missing block: B:96:0x0372, code:
            throw r10;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public HttpResponse execute(HttpHost target, HttpRequest request, HttpContext context) throws HttpException, IOException {
        HttpRequest orig = request;
        RequestWrapper origWrapper = wrapRequest(request);
        origWrapper.setParams(this.params);
        RoutedRequest routedRequest = new RoutedRequest(origWrapper, determineRoute(target, origWrapper, context));
        long timeout = ConnManagerParams.getTimeout(this.params);
        int execCount = 0;
        boolean reuse = false;
        HttpResponse response = null;
        boolean done = false;
        while (!done) {
            RequestWrapper wrapper;
            RoutedRequest roureq;
            HttpRoute route;
            Object userToken;
            try {
                wrapper = roureq.getRequest();
                route = roureq.getRoute();
                userToken = context.getAttribute(ClientContext.USER_TOKEN);
                if (this.managedConn == null) {
                    ClientConnectionRequest connRequest = this.connManager.requestConnection(route, userToken);
                    if (request instanceof AbortableHttpRequest) {
                        ((AbortableHttpRequest) request).setConnectionRequest(connRequest);
                    }
                    this.managedConn = connRequest.getConnection(timeout, TimeUnit.MILLISECONDS);
                    if (HttpConnectionParams.isStaleCheckingEnabled(this.params)) {
                        this.log.debug("Stale connection check");
                        if (this.managedConn.isStale()) {
                            this.log.debug("Stale connection detected");
                            this.managedConn.close();
                        }
                    }
                }
            } catch (IOException e) {
            } catch (HttpException ex) {
            } catch (RuntimeException ex2) {
            }
            if (request instanceof AbortableHttpRequest) {
                ((AbortableHttpRequest) request).setReleaseTrigger(this.managedConn);
            }
            if (this.managedConn.isOpen()) {
                this.managedConn.setSocketTimeout(HttpConnectionParams.getSoTimeout(this.params));
            } else {
                this.managedConn.open(route, context, this.params);
            }
            try {
                establishRoute(route, context);
                wrapper.resetHeaders();
                rewriteRequestURI(wrapper, route);
                target = (HttpHost) wrapper.getParams().getParameter(ClientPNames.VIRTUAL_HOST);
                if (target == null) {
                    target = route.getTargetHost();
                }
                HttpHost proxy = route.getProxyHost();
                context.setAttribute(ExecutionContext.HTTP_TARGET_HOST, target);
                context.setAttribute(ExecutionContext.HTTP_PROXY_HOST, proxy);
                context.setAttribute(ExecutionContext.HTTP_CONNECTION, this.managedConn);
                context.setAttribute(ClientContext.TARGET_AUTH_STATE, this.targetAuthState);
                context.setAttribute(ClientContext.PROXY_AUTH_STATE, this.proxyAuthState);
                this.requestExec.preProcess(wrapper, this.httpProcessor, context);
                context.setAttribute(ExecutionContext.HTTP_REQUEST, wrapper);
                boolean retrying = true;
                while (retrying) {
                    execCount++;
                    wrapper.incrementExecCount();
                    if (wrapper.getExecCount() <= 1 || (wrapper.isRepeatable() ^ 1) == 0) {
                        if (this.log.isDebugEnabled()) {
                            this.log.debug("Attempt " + execCount + " to execute request");
                        }
                        if (route.isSecure() || (isCleartextTrafficPermitted(route.getTargetHost().getHostName()) ^ 1) == 0) {
                            response = this.requestExec.execute(wrapper, this.managedConn, context);
                            retrying = false;
                        } else {
                            throw new IOException("Cleartext traffic not permitted: " + route.getTargetHost());
                        }
                    }
                    throw new NonRepeatableRequestException("Cannot retry request with a non-repeatable request entity");
                }
                response.setParams(this.params);
                this.requestExec.postProcess(response, this.httpProcessor, context);
                reuse = this.reuseStrategy.keepAlive(response, context);
                if (reuse) {
                    long duration = this.keepAliveStrategy.getKeepAliveDuration(response, context);
                    this.managedConn.setIdleDuration(duration, TimeUnit.MILLISECONDS);
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
                userToken = this.userTokenHandler.getUserToken(context);
                context.setAttribute(ClientContext.USER_TOKEN, userToken);
                if (this.managedConn != null) {
                    this.managedConn.setState(userToken);
                }
            } catch (TunnelRefusedException ex3) {
                if (this.log.isDebugEnabled()) {
                    this.log.debug(ex3.getMessage());
                }
                response = ex3.getResponse();
            }
        }
        if (response == null || response.getEntity() == null || (response.getEntity().isStreaming() ^ 1) != 0) {
            if (reuse) {
                this.managedConn.markReusable();
            }
            releaseConnection();
        } else {
            response.setEntity(new BasicManagedEntity(response.getEntity(), this.managedConn, reuse));
        }
        return response;
    }

    protected void releaseConnection() {
        try {
            this.managedConn.releaseConnection();
        } catch (IOException ignored) {
            this.log.debug("IOException releasing connection", ignored);
        }
        this.managedConn = null;
    }

    protected HttpRoute determineRoute(HttpHost target, HttpRequest request, HttpContext context) throws HttpException {
        if (target == null) {
            target = (HttpHost) request.getParams().getParameter(ClientPNames.DEFAULT_HOST);
        }
        if (target != null) {
            return this.routePlanner.determineRoute(target, request, context);
        }
        String scheme = null;
        String host = null;
        String path = null;
        if (request instanceof HttpUriRequest) {
            URI uri = ((HttpUriRequest) request).getURI();
            if (uri != null) {
                scheme = uri.getScheme();
                host = uri.getHost();
                path = uri.getPath();
            }
        }
        throw new IllegalStateException("Target host must not be null, or set in parameters. scheme=" + scheme + ", host=" + host + ", path=" + path);
    }

    protected void establishRoute(HttpRoute route, HttpContext context) throws HttpException, IOException {
        HttpRouteDirector rowdy = new BasicRouteDirector();
        int step;
        do {
            HttpRoute fact = this.managedConn.getRoute();
            step = rowdy.nextStep(route, fact);
            boolean secure;
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
                    secure = createTunnelToTarget(route, context);
                    this.log.debug("Tunnel to target created.");
                    this.managedConn.tunnelTarget(secure, this.params);
                    continue;
                case 4:
                    int hop = fact.getHopCount() - 1;
                    secure = createTunnelToProxy(route, hop, context);
                    this.log.debug("Tunnel to proxy created.");
                    this.managedConn.tunnelProxy(route.getHopTarget(hop), secure, this.params);
                    continue;
                case 5:
                    this.managedConn.layerProtocol(context, this.params);
                    continue;
                default:
                    throw new IllegalStateException("Unknown step indicator " + step + " from RouteDirector.");
            }
        } while (step > 0);
    }

    protected boolean createTunnelToTarget(HttpRoute route, HttpContext context) throws HttpException, IOException {
        HttpEntity entity;
        HttpHost proxy = route.getProxyHost();
        HttpHost target = route.getTargetHost();
        HttpResponse response = null;
        boolean done = false;
        while (!done) {
            done = true;
            if (!this.managedConn.isOpen()) {
                this.managedConn.open(route, context, this.params);
            }
            HttpRequest connect = createConnectRequest(route, context);
            String agent = HttpProtocolParams.getUserAgent(this.params);
            if (agent != null) {
                connect.addHeader(HTTP.USER_AGENT, agent);
            }
            connect.addHeader(HTTP.TARGET_HOST, target.toHostString());
            AuthScheme authScheme = this.proxyAuthState.getAuthScheme();
            AuthScope authScope = this.proxyAuthState.getAuthScope();
            Credentials creds = this.proxyAuthState.getCredentials();
            if (!(creds == null || (authScope == null && (authScheme.isConnectionBased() ^ 1) == 0))) {
                try {
                    connect.addHeader(authScheme.authenticate(creds, connect));
                } catch (AuthenticationException ex) {
                    if (this.log.isErrorEnabled()) {
                        this.log.error("Proxy authentication error: " + ex.getMessage());
                    }
                }
            }
            response = this.requestExec.execute(connect, this.managedConn, context);
            if (response.getStatusLine().getStatusCode() < 200) {
                throw new HttpException("Unexpected response to CONNECT request: " + response.getStatusLine());
            }
            CredentialsProvider credsProvider = (CredentialsProvider) context.getAttribute(ClientContext.CREDS_PROVIDER);
            if (credsProvider != null && HttpClientParams.isAuthenticating(this.params)) {
                if (this.proxyAuthHandler.isAuthenticationRequested(response, context)) {
                    this.log.debug("Proxy requested authentication");
                    try {
                        processChallenges(this.proxyAuthHandler.getChallenges(response, context), this.proxyAuthState, this.proxyAuthHandler, response, context);
                    } catch (AuthenticationException ex2) {
                        if (this.log.isWarnEnabled()) {
                            this.log.warn("Authentication error: " + ex2.getMessage());
                            break;
                        }
                    }
                    updateAuthState(this.proxyAuthState, proxy, credsProvider);
                    if (this.proxyAuthState.getCredentials() != null) {
                        done = false;
                        if (this.reuseStrategy.keepAlive(response, context)) {
                            this.log.debug("Connection kept alive");
                            entity = response.getEntity();
                            if (entity != null) {
                                entity.consumeContent();
                            }
                        } else {
                            this.managedConn.close();
                        }
                    }
                } else {
                    this.proxyAuthState.setAuthScope(null);
                }
            }
        }
        if (response.getStatusLine().getStatusCode() > 299) {
            entity = response.getEntity();
            if (entity != null) {
                response.setEntity(new BufferedHttpEntity(entity));
            }
            this.managedConn.close();
            throw new TunnelRefusedException("CONNECT refused by proxy: " + response.getStatusLine(), response);
        }
        this.managedConn.markReusable();
        return false;
    }

    protected boolean createTunnelToProxy(HttpRoute route, int hop, HttpContext context) throws HttpException, IOException {
        throw new UnsupportedOperationException("Proxy chains are not supported.");
    }

    protected HttpRequest createConnectRequest(HttpRoute route, HttpContext context) {
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

    protected RoutedRequest handleResponse(RoutedRequest roureq, HttpResponse response, HttpContext context) throws HttpException, IOException {
        HttpRoute route = roureq.getRoute();
        HttpHost proxy = route.getProxyHost();
        RequestWrapper request = roureq.getRequest();
        HttpParams params = request.getParams();
        if (!HttpClientParams.isRedirecting(params) || !this.redirectHandler.isRedirectRequested(response, context)) {
            CredentialsProvider credsProvider = (CredentialsProvider) context.getAttribute(ClientContext.CREDS_PROVIDER);
            if (credsProvider != null && HttpClientParams.isAuthenticating(params)) {
                if (this.targetAuthHandler.isAuthenticationRequested(response, context)) {
                    HttpHost target = (HttpHost) context.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
                    if (target == null) {
                        target = route.getTargetHost();
                    }
                    this.log.debug("Target requested authentication");
                    try {
                        processChallenges(this.targetAuthHandler.getChallenges(response, context), this.targetAuthState, this.targetAuthHandler, response, context);
                    } catch (AuthenticationException ex) {
                        if (this.log.isWarnEnabled()) {
                            this.log.warn("Authentication error: " + ex.getMessage());
                            return null;
                        }
                    }
                    updateAuthState(this.targetAuthState, target, credsProvider);
                    if (this.targetAuthState.getCredentials() != null) {
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
                            this.log.warn("Authentication error: " + ex2.getMessage());
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
            return null;
        } else if (this.redirectCount >= this.maxRedirects) {
            throw new RedirectException("Maximum redirects (" + this.maxRedirects + ") exceeded");
        } else {
            this.redirectCount++;
            URI uri = this.redirectHandler.getLocationURI(response, context);
            HttpHost newTarget = new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme());
            HttpGet httpGet = new HttpGet(uri);
            httpGet.setHeaders(request.getOriginal().getAllHeaders());
            RequestWrapper requestWrapper = new RequestWrapper(httpGet);
            requestWrapper.setParams(params);
            HttpRoute newRoute = determineRoute(newTarget, requestWrapper, context);
            RoutedRequest newRequest = new RoutedRequest(requestWrapper, newRoute);
            if (this.log.isDebugEnabled()) {
                this.log.debug("Redirecting to '" + uri + "' via " + newRoute);
            }
            return newRequest;
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
        Header challenge = (Header) challenges.get(id.toLowerCase(Locale.ENGLISH));
        if (challenge == null) {
            throw new AuthenticationException(id + " authorization challenge expected, but not found");
        }
        authScheme.processChallenge(challenge);
        this.log.debug("Authorization challenge processed");
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
                this.log.debug("Authentication scope: " + authScope);
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
        try {
            Object policy;
            Method method;
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
