package org.apache.http.impl.conn;

import java.net.InetAddress;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.protocol.HttpContext;

@Deprecated
public class DefaultHttpRoutePlanner implements HttpRoutePlanner {
    protected SchemeRegistry schemeRegistry;

    public DefaultHttpRoutePlanner(SchemeRegistry schreg) {
        if (schreg != null) {
            this.schemeRegistry = schreg;
            return;
        }
        throw new IllegalArgumentException("SchemeRegistry must not be null.");
    }

    public HttpRoute determineRoute(HttpHost target, HttpRequest request, HttpContext context) throws HttpException {
        HttpRoute route;
        if (request != null) {
            HttpRoute route2 = ConnRouteParams.getForcedRoute(request.getParams());
            if (route2 != null) {
                return route2;
            }
            if (target != null) {
                InetAddress local = ConnRouteParams.getLocalAddress(request.getParams());
                HttpHost proxy = ConnRouteParams.getDefaultProxy(request.getParams());
                boolean secure = this.schemeRegistry.getScheme(target.getSchemeName()).isLayered();
                if (proxy == null) {
                    route = new HttpRoute(target, local, secure);
                } else {
                    route = new HttpRoute(target, local, proxy, secure);
                }
                return route;
            }
            throw new IllegalStateException("Target host must not be null.");
        }
        throw new IllegalStateException("Request must not be null.");
    }
}
