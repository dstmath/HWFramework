package org.apache.http.impl.client;

import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.HttpHost;
import org.apache.http.HttpVersion;
import org.apache.http.auth.AuthSchemeRegistry;
import org.apache.http.client.AuthenticationHandler;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.RedirectHandler;
import org.apache.http.client.UserTokenHandler;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.client.protocol.RequestAddCookies;
import org.apache.http.client.protocol.RequestDefaultHeaders;
import org.apache.http.client.protocol.RequestProxyAuthentication;
import org.apache.http.client.protocol.RequestTargetAuthentication;
import org.apache.http.client.protocol.ResponseProcessCookies;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ClientConnectionManagerFactory;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.cookie.CookieSpecRegistry;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.auth.BasicSchemeFactory;
import org.apache.http.impl.auth.DigestSchemeFactory;
import org.apache.http.impl.conn.ProxySelectorRoutePlanner;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.impl.cookie.BestMatchSpecFactory;
import org.apache.http.impl.cookie.BrowserCompatSpecFactory;
import org.apache.http.impl.cookie.NetscapeDraftSpecFactory;
import org.apache.http.impl.cookie.RFC2109SpecFactory;
import org.apache.http.impl.cookie.RFC2965SpecFactory;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.BasicHttpProcessor;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestExecutor;
import org.apache.http.protocol.RequestConnControl;
import org.apache.http.protocol.RequestContent;
import org.apache.http.protocol.RequestExpectContinue;
import org.apache.http.protocol.RequestTargetHost;
import org.apache.http.protocol.RequestUserAgent;
import org.apache.http.util.VersionInfo;

@Deprecated
public class DefaultHttpClient extends AbstractHttpClient {
    public DefaultHttpClient(ClientConnectionManager conman, HttpParams params) {
        super(conman, params);
    }

    public DefaultHttpClient(HttpParams params) {
        super(null, params);
    }

    public DefaultHttpClient() {
        super(null, null);
    }

    /* access modifiers changed from: protected */
    @Override // org.apache.http.impl.client.AbstractHttpClient
    public HttpParams createHttpParams() {
        HttpParams params = new BasicHttpParams();
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(params, "ISO-8859-1");
        HttpProtocolParams.setUseExpectContinue(params, false);
        VersionInfo vi = VersionInfo.loadVersionInfo("org.apache.http.client", getClass().getClassLoader());
        String release = vi != null ? vi.getRelease() : VersionInfo.UNAVAILABLE;
        HttpProtocolParams.setUserAgent(params, "Apache-HttpClient/" + release + " (java 1.4)");
        return params;
    }

    /* access modifiers changed from: protected */
    @Override // org.apache.http.impl.client.AbstractHttpClient
    public HttpRequestExecutor createRequestExecutor() {
        return new HttpRequestExecutor();
    }

    /* access modifiers changed from: protected */
    @Override // org.apache.http.impl.client.AbstractHttpClient
    public ClientConnectionManager createClientConnectionManager() {
        String className;
        SchemeRegistry registry = new SchemeRegistry();
        registry.register(new Scheme(HttpHost.DEFAULT_SCHEME_NAME, PlainSocketFactory.getSocketFactory(), 80));
        registry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
        HttpParams params = getParams();
        ClientConnectionManagerFactory factory = (ClientConnectionManagerFactory) params.getParameter(ClientPNames.CONNECTION_MANAGER_FACTORY);
        if (factory == null && (className = (String) params.getParameter(ClientPNames.CONNECTION_MANAGER_FACTORY_CLASS_NAME)) != null) {
            try {
                factory = (ClientConnectionManagerFactory) Class.forName(className).newInstance();
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("Invalid class name: " + className);
            } catch (IllegalAccessException ex) {
                throw new IllegalAccessError(ex.getMessage());
            } catch (InstantiationException ex2) {
                throw new InstantiationError(ex2.getMessage());
            }
        }
        if (factory != null) {
            return factory.newInstance(params, registry);
        }
        return new SingleClientConnManager(getParams(), registry);
    }

    /* access modifiers changed from: protected */
    @Override // org.apache.http.impl.client.AbstractHttpClient
    public HttpContext createHttpContext() {
        HttpContext context = new BasicHttpContext();
        context.setAttribute(ClientContext.AUTHSCHEME_REGISTRY, getAuthSchemes());
        context.setAttribute(ClientContext.COOKIESPEC_REGISTRY, getCookieSpecs());
        context.setAttribute(ClientContext.COOKIE_STORE, getCookieStore());
        context.setAttribute(ClientContext.CREDS_PROVIDER, getCredentialsProvider());
        return context;
    }

    /* access modifiers changed from: protected */
    @Override // org.apache.http.impl.client.AbstractHttpClient
    public ConnectionReuseStrategy createConnectionReuseStrategy() {
        return new DefaultConnectionReuseStrategy();
    }

    /* access modifiers changed from: protected */
    @Override // org.apache.http.impl.client.AbstractHttpClient
    public ConnectionKeepAliveStrategy createConnectionKeepAliveStrategy() {
        return new DefaultConnectionKeepAliveStrategy();
    }

    /* access modifiers changed from: protected */
    @Override // org.apache.http.impl.client.AbstractHttpClient
    public AuthSchemeRegistry createAuthSchemeRegistry() {
        AuthSchemeRegistry registry = new AuthSchemeRegistry();
        registry.register("Basic", new BasicSchemeFactory());
        registry.register("Digest", new DigestSchemeFactory());
        return registry;
    }

    /* access modifiers changed from: protected */
    @Override // org.apache.http.impl.client.AbstractHttpClient
    public CookieSpecRegistry createCookieSpecRegistry() {
        CookieSpecRegistry registry = new CookieSpecRegistry();
        registry.register(CookiePolicy.BEST_MATCH, new BestMatchSpecFactory());
        registry.register(CookiePolicy.BROWSER_COMPATIBILITY, new BrowserCompatSpecFactory());
        registry.register(CookiePolicy.NETSCAPE, new NetscapeDraftSpecFactory());
        registry.register(CookiePolicy.RFC_2109, new RFC2109SpecFactory());
        registry.register(CookiePolicy.RFC_2965, new RFC2965SpecFactory());
        return registry;
    }

    /* access modifiers changed from: protected */
    @Override // org.apache.http.impl.client.AbstractHttpClient
    public BasicHttpProcessor createHttpProcessor() {
        BasicHttpProcessor httpproc = new BasicHttpProcessor();
        httpproc.addInterceptor(new RequestDefaultHeaders());
        httpproc.addInterceptor(new RequestContent());
        httpproc.addInterceptor(new RequestTargetHost());
        httpproc.addInterceptor(new RequestConnControl());
        httpproc.addInterceptor(new RequestUserAgent());
        httpproc.addInterceptor(new RequestExpectContinue());
        httpproc.addInterceptor(new RequestAddCookies());
        httpproc.addInterceptor(new ResponseProcessCookies());
        httpproc.addInterceptor(new RequestTargetAuthentication());
        httpproc.addInterceptor(new RequestProxyAuthentication());
        return httpproc;
    }

    /* access modifiers changed from: protected */
    @Override // org.apache.http.impl.client.AbstractHttpClient
    public HttpRequestRetryHandler createHttpRequestRetryHandler() {
        return new DefaultHttpRequestRetryHandler();
    }

    /* access modifiers changed from: protected */
    @Override // org.apache.http.impl.client.AbstractHttpClient
    public RedirectHandler createRedirectHandler() {
        return new DefaultRedirectHandler();
    }

    /* access modifiers changed from: protected */
    @Override // org.apache.http.impl.client.AbstractHttpClient
    public AuthenticationHandler createTargetAuthenticationHandler() {
        return new DefaultTargetAuthenticationHandler();
    }

    /* access modifiers changed from: protected */
    @Override // org.apache.http.impl.client.AbstractHttpClient
    public AuthenticationHandler createProxyAuthenticationHandler() {
        return new DefaultProxyAuthenticationHandler();
    }

    /* access modifiers changed from: protected */
    @Override // org.apache.http.impl.client.AbstractHttpClient
    public CookieStore createCookieStore() {
        return new BasicCookieStore();
    }

    /* access modifiers changed from: protected */
    @Override // org.apache.http.impl.client.AbstractHttpClient
    public CredentialsProvider createCredentialsProvider() {
        return new BasicCredentialsProvider();
    }

    /* access modifiers changed from: protected */
    @Override // org.apache.http.impl.client.AbstractHttpClient
    public HttpRoutePlanner createHttpRoutePlanner() {
        return new ProxySelectorRoutePlanner(getConnectionManager().getSchemeRegistry(), null);
    }

    /* access modifiers changed from: protected */
    @Override // org.apache.http.impl.client.AbstractHttpClient
    public UserTokenHandler createUserTokenHandler() {
        return new DefaultUserTokenHandler();
    }
}
