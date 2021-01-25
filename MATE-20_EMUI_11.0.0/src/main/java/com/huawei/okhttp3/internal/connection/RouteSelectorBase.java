package com.huawei.okhttp3.internal.connection;

import com.huawei.okhttp3.Call;
import com.huawei.okhttp3.Route;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class RouteSelectorBase {
    private final List<InetAddress> additionalInetAddresses;
    protected List<InetSocketAddress> inetSocketAddresses = Collections.emptyList();
    private boolean isConcurrentConnectEnabled;
    protected List<Proxy> proxies = Collections.emptyList();

    public RouteSelectorBase(Call call) {
        if (call != null) {
            this.isConcurrentConnectEnabled = call.request().concurrentConnectEnabled();
            this.additionalInetAddresses = call.request().additionalInetAddresses();
            return;
        }
        this.isConcurrentConnectEnabled = false;
        this.additionalInetAddresses = null;
    }

    /* access modifiers changed from: package-private */
    public void concurrentConnectProxy() {
        if (!this.isConcurrentConnectEnabled) {
            return;
        }
        if (this.proxies.size() > 1 || (this.proxies.size() == 1 && this.proxies.get(0).type() != Proxy.Type.DIRECT)) {
            this.isConcurrentConnectEnabled = false;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean concurrentConnectEnabled() {
        return this.isConcurrentConnectEnabled;
    }

    /* access modifiers changed from: package-private */
    public void prepareConcurrentConnectAddresses(int socketPort) {
        if (this.isConcurrentConnectEnabled) {
            List<InetAddress> list = this.additionalInetAddresses;
            if (list != null) {
                for (int i = list.size() - 1; i >= 0; i--) {
                    InetSocketAddress socketAddress = new InetSocketAddress(this.additionalInetAddresses.get(i), socketPort);
                    if (this.inetSocketAddresses.contains(socketAddress)) {
                        this.inetSocketAddresses.remove(socketAddress);
                    }
                    this.inetSocketAddresses.add(0, socketAddress);
                }
            }
            if (this.inetSocketAddresses.size() == 1) {
                this.isConcurrentConnectEnabled = false;
            }
        }
    }

    public List<InetAddress> additionalInetAddresses() {
        return this.additionalInetAddresses;
    }

    public static class SelectionBase {
        private InetSocketAddress connectedTcpAddress = null;
        private boolean isConcurrentConnectEnabled;
        private final RouteDatabase routeDatabase;
        protected List<Route> routes;

        SelectionBase(List<Route> routes2, boolean isConcurrentConnectEnabled2, RouteDatabase routeDatabase2) {
            this.routes = routes2;
            this.isConcurrentConnectEnabled = isConcurrentConnectEnabled2;
            this.routeDatabase = routeDatabase2;
        }

        SelectionBase(List<Route> routes2) {
            this.routes = routes2;
            this.isConcurrentConnectEnabled = false;
            this.routeDatabase = null;
        }

        public boolean concurrentConnectEnabled() {
            return this.isConcurrentConnectEnabled;
        }

        public ArrayList<InetSocketAddress> concurrentInetSocketAddresses() {
            if (this.routes == null) {
                return null;
            }
            ArrayList<InetSocketAddress> inetSocketAddresses = new ArrayList<>();
            for (Route route : this.routes) {
                if (route != null) {
                    inetSocketAddresses.add(route.socketAddress());
                }
            }
            return inetSocketAddresses;
        }

        private Route removeRoute(InetSocketAddress socketAddress) {
            List<Route> list = this.routes;
            if (list == null) {
                return null;
            }
            Iterator<Route> routeIterator = list.iterator();
            while (routeIterator.hasNext()) {
                Route route = routeIterator.next();
                if (route != null && route.socketAddress().equals(socketAddress)) {
                    routeIterator.remove();
                    return route;
                }
            }
            return null;
        }

        public void connectFailed() {
            InetSocketAddress inetSocketAddress;
            if (this.isConcurrentConnectEnabled && (inetSocketAddress = this.connectedTcpAddress) != null) {
                Route route = removeRoute(inetSocketAddress);
                if (route != null) {
                    this.routeDatabase.failed(route);
                }
                this.connectedTcpAddress = null;
            }
        }

        public void connected(Route connectedRoute) {
            if (!this.isConcurrentConnectEnabled) {
                this.routeDatabase.connected(connectedRoute);
                return;
            }
            InetSocketAddress inetSocketAddress = this.connectedTcpAddress;
            if (inetSocketAddress != null) {
                Route route = removeRoute(inetSocketAddress);
                if (route != null) {
                    this.routeDatabase.connected(route);
                }
                this.connectedTcpAddress = null;
            }
        }

        public void setConnectedTcpAddress(InetSocketAddress connectedTcpAddress2) {
            if (this.isConcurrentConnectEnabled && connectedTcpAddress2 != null) {
                this.connectedTcpAddress = connectedTcpAddress2;
            }
        }

        public void setFailedTcpAddresses(ArrayList<InetSocketAddress> failedAddresses) {
            if (this.isConcurrentConnectEnabled && failedAddresses != null) {
                Iterator<InetSocketAddress> it = failedAddresses.iterator();
                while (it.hasNext()) {
                    Route route = removeRoute(it.next());
                    if (route != null) {
                        this.routeDatabase.failed(route);
                    }
                }
            }
        }
    }
}
