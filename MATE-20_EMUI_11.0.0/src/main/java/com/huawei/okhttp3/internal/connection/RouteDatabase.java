package com.huawei.okhttp3.internal.connection;

import com.huawei.okhttp3.Route;
import java.util.LinkedHashSet;
import java.util.Set;

/* access modifiers changed from: package-private */
public final class RouteDatabase {
    private final Set<Route> failedRoutes = new LinkedHashSet();

    RouteDatabase() {
    }

    public synchronized void failed(Route failedRoute) {
        this.failedRoutes.add(failedRoute);
    }

    public synchronized void connected(Route route) {
        this.failedRoutes.remove(route);
    }

    public synchronized boolean shouldPostpone(Route route) {
        return this.failedRoutes.contains(route);
    }
}
