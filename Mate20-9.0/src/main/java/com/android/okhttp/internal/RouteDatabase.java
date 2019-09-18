package com.android.okhttp.internal;

import com.android.okhttp.Route;
import java.util.LinkedHashSet;
import java.util.Set;

public final class RouteDatabase {
    private final Set<Route> failedRoutes = new LinkedHashSet();

    public synchronized void failed(Route failedRoute) {
        this.failedRoutes.add(failedRoute);
    }

    public synchronized void connected(Route route) {
        this.failedRoutes.remove(route);
    }

    public synchronized boolean shouldPostpone(Route route) {
        return this.failedRoutes.contains(route);
    }

    public synchronized int failedRoutesCount() {
        return this.failedRoutes.size();
    }
}
