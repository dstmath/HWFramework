package org.apache.http.impl.conn;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.protocol.HttpContext;

@Deprecated
public class ProxySelectorRoutePlanner implements HttpRoutePlanner {
    private static final /* synthetic */ int[] -java-net-Proxy$TypeSwitchesValues = null;
    protected ProxySelector proxySelector;
    protected SchemeRegistry schemeRegistry;

    private static /* synthetic */ int[] -getjava-net-Proxy$TypeSwitchesValues() {
        if (-java-net-Proxy$TypeSwitchesValues != null) {
            return -java-net-Proxy$TypeSwitchesValues;
        }
        int[] iArr = new int[Type.values().length];
        try {
            iArr[Type.DIRECT.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[Type.HTTP.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[Type.SOCKS.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        -java-net-Proxy$TypeSwitchesValues = iArr;
        return iArr;
    }

    public ProxySelectorRoutePlanner(SchemeRegistry schreg, ProxySelector prosel) {
        if (schreg == null) {
            throw new IllegalArgumentException("SchemeRegistry must not be null.");
        }
        this.schemeRegistry = schreg;
        this.proxySelector = prosel;
    }

    public ProxySelector getProxySelector() {
        return this.proxySelector;
    }

    public void setProxySelector(ProxySelector prosel) {
        this.proxySelector = prosel;
    }

    public HttpRoute determineRoute(HttpHost target, HttpRequest request, HttpContext context) throws HttpException {
        if (request == null) {
            throw new IllegalStateException("Request must not be null.");
        }
        HttpRoute route = ConnRouteParams.getForcedRoute(request.getParams());
        if (route != null) {
            return route;
        }
        if (target == null) {
            throw new IllegalStateException("Target host must not be null.");
        }
        InetAddress local = ConnRouteParams.getLocalAddress(request.getParams());
        HttpHost proxy = (HttpHost) request.getParams().getParameter(ConnRoutePNames.DEFAULT_PROXY);
        if (proxy == null) {
            proxy = determineProxy(target, request, context);
        } else if (ConnRouteParams.NO_HOST.equals(proxy)) {
            proxy = null;
        }
        boolean secure = this.schemeRegistry.getScheme(target.getSchemeName()).isLayered();
        if (proxy == null) {
            route = new HttpRoute(target, local, secure);
        } else {
            route = new HttpRoute(target, local, proxy, secure);
        }
        return route;
    }

    protected HttpHost determineProxy(HttpHost target, HttpRequest request, HttpContext context) throws HttpException {
        ProxySelector psel = this.proxySelector;
        if (psel == null) {
            psel = ProxySelector.getDefault();
        }
        if (psel == null) {
            return null;
        }
        try {
            Proxy p = chooseProxy(psel.select(new URI(target.toURI())), target, request, context);
            HttpHost httpHost = null;
            if (p.type() == Type.HTTP) {
                if (p.address() instanceof InetSocketAddress) {
                    InetSocketAddress isa = (InetSocketAddress) p.address();
                    httpHost = new HttpHost(getHost(isa), isa.getPort());
                } else {
                    throw new HttpException("Unable to handle non-Inet proxy address: " + p.address());
                }
            }
            return httpHost;
        } catch (URISyntaxException usx) {
            throw new HttpException("Cannot convert host to URI: " + target, usx);
        }
    }

    protected String getHost(InetSocketAddress isa) {
        return isa.isUnresolved() ? isa.getHostName() : isa.getAddress().getHostAddress();
    }

    protected Proxy chooseProxy(List<Proxy> proxies, HttpHost target, HttpRequest request, HttpContext context) {
        if (proxies == null || proxies.isEmpty()) {
            throw new IllegalArgumentException("Proxy list must not be empty.");
        }
        Proxy result = null;
        int i = 0;
        while (result == null && i < proxies.size()) {
            Proxy p = (Proxy) proxies.get(i);
            switch (-getjava-net-Proxy$TypeSwitchesValues()[p.type().ordinal()]) {
                case 1:
                case 2:
                    result = p;
                    break;
                default:
                    break;
            }
            i++;
        }
        if (result == null) {
            return Proxy.NO_PROXY;
        }
        return result;
    }
}
