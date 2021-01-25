package org.apache.http.conn.params;

import java.util.HashMap;
import java.util.Map;
import org.apache.http.conn.routing.HttpRoute;

@Deprecated
public final class ConnPerRouteBean implements ConnPerRoute {
    public static final int DEFAULT_MAX_CONNECTIONS_PER_ROUTE = 2;
    private int defaultMax;
    private final Map<HttpRoute, Integer> maxPerHostMap;

    public ConnPerRouteBean(int defaultMax2) {
        this.maxPerHostMap = new HashMap();
        setDefaultMaxPerRoute(defaultMax2);
    }

    public ConnPerRouteBean() {
        this(2);
    }

    public int getDefaultMax() {
        return this.defaultMax;
    }

    public void setDefaultMaxPerRoute(int max) {
        if (max >= 1) {
            this.defaultMax = max;
            return;
        }
        throw new IllegalArgumentException("The maximum must be greater than 0.");
    }

    public void setMaxForRoute(HttpRoute route, int max) {
        if (route == null) {
            throw new IllegalArgumentException("HTTP route may not be null.");
        } else if (max >= 1) {
            this.maxPerHostMap.put(route, Integer.valueOf(max));
        } else {
            throw new IllegalArgumentException("The maximum must be greater than 0.");
        }
    }

    @Override // org.apache.http.conn.params.ConnPerRoute
    public int getMaxForRoute(HttpRoute route) {
        if (route != null) {
            Integer max = this.maxPerHostMap.get(route);
            if (max != null) {
                return max.intValue();
            }
            return this.defaultMax;
        }
        throw new IllegalArgumentException("HTTP route may not be null.");
    }

    public void setMaxForRoutes(Map<HttpRoute, Integer> map) {
        if (map != null) {
            this.maxPerHostMap.clear();
            this.maxPerHostMap.putAll(map);
        }
    }
}
