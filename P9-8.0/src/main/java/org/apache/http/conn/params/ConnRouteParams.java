package org.apache.http.conn.params;

import java.net.InetAddress;
import org.apache.http.HttpHost;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.params.HttpParams;

@Deprecated
public class ConnRouteParams implements ConnRoutePNames {
    public static final HttpHost NO_HOST = new HttpHost("127.0.0.255", 0, "no-host");
    public static final HttpRoute NO_ROUTE = new HttpRoute(NO_HOST);

    private ConnRouteParams() {
    }

    public static HttpHost getDefaultProxy(HttpParams params) {
        if (params == null) {
            throw new IllegalArgumentException("Parameters must not be null.");
        }
        HttpHost proxy = (HttpHost) params.getParameter(ConnRoutePNames.DEFAULT_PROXY);
        if (proxy == null || !NO_HOST.equals(proxy)) {
            return proxy;
        }
        return null;
    }

    public static void setDefaultProxy(HttpParams params, HttpHost proxy) {
        if (params == null) {
            throw new IllegalArgumentException("Parameters must not be null.");
        }
        params.setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
    }

    public static HttpRoute getForcedRoute(HttpParams params) {
        if (params == null) {
            throw new IllegalArgumentException("Parameters must not be null.");
        }
        HttpRoute route = (HttpRoute) params.getParameter(ConnRoutePNames.FORCED_ROUTE);
        if (route == null || !NO_ROUTE.equals(route)) {
            return route;
        }
        return null;
    }

    public static void setForcedRoute(HttpParams params, HttpRoute route) {
        if (params == null) {
            throw new IllegalArgumentException("Parameters must not be null.");
        }
        params.setParameter(ConnRoutePNames.FORCED_ROUTE, route);
    }

    public static InetAddress getLocalAddress(HttpParams params) {
        if (params != null) {
            return (InetAddress) params.getParameter(ConnRoutePNames.LOCAL_ADDRESS);
        }
        throw new IllegalArgumentException("Parameters must not be null.");
    }

    public static void setLocalAddress(HttpParams params, InetAddress local) {
        if (params == null) {
            throw new IllegalArgumentException("Parameters must not be null.");
        }
        params.setParameter(ConnRoutePNames.LOCAL_ADDRESS, local);
    }
}
