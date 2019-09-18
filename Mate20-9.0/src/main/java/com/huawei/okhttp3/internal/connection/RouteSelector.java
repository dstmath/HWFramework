package com.huawei.okhttp3.internal.connection;

import com.huawei.okhttp3.Address;
import com.huawei.okhttp3.Call;
import com.huawei.okhttp3.EventListener;
import com.huawei.okhttp3.HttpUrl;
import com.huawei.okhttp3.Route;
import com.huawei.okhttp3.internal.Util;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public final class RouteSelector {
    private final List<InetAddress> additionalInetAddresses;
    private final Address address;
    private final Call call;
    private boolean concurrentConnectEnabled;
    private final EventListener eventListener;
    private List<InetSocketAddress> inetSocketAddresses = Collections.emptyList();
    private int nextProxyIndex;
    private final List<Route> postponedRoutes = new ArrayList();
    private List<Proxy> proxies = Collections.emptyList();
    private final RouteDatabase routeDatabase;

    public static final class Selection {
        private boolean concurrentConnectEnabled;
        private InetSocketAddress connectedTcpAddress = null;
        private int nextRouteIndex = 0;
        private final RouteDatabase routeDatabase;
        private final List<Route> routes;

        Selection(List<Route> routes2, boolean concurrentConnectEnabled2, RouteDatabase routeDatabase2) {
            this.routes = routes2;
            this.concurrentConnectEnabled = concurrentConnectEnabled2;
            this.routeDatabase = routeDatabase2;
        }

        Selection(List<Route> routes2) {
            this.routes = routes2;
            this.concurrentConnectEnabled = false;
            this.routeDatabase = null;
        }

        public boolean hasNext() {
            boolean z = false;
            if (this.concurrentConnectEnabled) {
                if (this.routes.size() > 0) {
                    z = true;
                }
                return z;
            }
            if (this.nextRouteIndex < this.routes.size()) {
                z = true;
            }
            return z;
        }

        public Route next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            } else if (this.concurrentConnectEnabled) {
                return this.routes.get(0);
            } else {
                List<Route> list = this.routes;
                int i = this.nextRouteIndex;
                this.nextRouteIndex = i + 1;
                return list.get(i);
            }
        }

        public List<Route> getAll() {
            return new ArrayList(this.routes);
        }

        public boolean concurrentConnectEnabled() {
            return this.concurrentConnectEnabled;
        }

        public ArrayList<InetSocketAddress> concurrentInetSocketAddresses() {
            ArrayList<InetSocketAddress> inetSocketAddresses = new ArrayList<>();
            for (Route route : this.routes) {
                inetSocketAddresses.add(route.socketAddress());
            }
            return inetSocketAddresses;
        }

        private Route removeRoute(InetSocketAddress socketAddress) {
            Iterator<Route> routeIterator = this.routes.iterator();
            while (routeIterator.hasNext()) {
                Route route = routeIterator.next();
                if (route.socketAddress().equals(socketAddress)) {
                    Route targetRoute = route;
                    routeIterator.remove();
                    return targetRoute;
                }
            }
            return null;
        }

        public void connectFailed() {
            if (this.concurrentConnectEnabled && this.connectedTcpAddress != null) {
                Route route = removeRoute(this.connectedTcpAddress);
                if (route != null) {
                    this.routeDatabase.failed(route);
                }
                this.connectedTcpAddress = null;
            }
        }

        public void connected(Route connectedRoute) {
            if (!this.concurrentConnectEnabled) {
                this.routeDatabase.connected(connectedRoute);
            } else if (this.connectedTcpAddress != null) {
                Route route = removeRoute(this.connectedTcpAddress);
                if (route != null) {
                    this.routeDatabase.connected(route);
                }
                this.connectedTcpAddress = null;
            }
        }

        public void setConnectedTcpAddress(InetSocketAddress connectedTcpAddress2) {
            if (this.concurrentConnectEnabled) {
                this.connectedTcpAddress = connectedTcpAddress2;
            }
        }

        public void setFailedTcpAddresses(ArrayList<InetSocketAddress> failedAddresses) {
            if (this.concurrentConnectEnabled) {
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

    public RouteSelector(Address address2, RouteDatabase routeDatabase2, Call call2, EventListener eventListener2) {
        this.address = address2;
        this.routeDatabase = routeDatabase2;
        this.call = call2;
        this.eventListener = eventListener2;
        this.concurrentConnectEnabled = call2.request().concurrentConnectEnabled();
        this.additionalInetAddresses = call2.request().additionalInetAddresses();
        resetNextProxy(address2.url(), address2.proxy());
        if (!this.concurrentConnectEnabled) {
            return;
        }
        if (this.proxies.size() > 1 || (this.proxies.size() == 1 && this.proxies.get(0).type() != Proxy.Type.DIRECT)) {
            this.concurrentConnectEnabled = false;
        }
    }

    public boolean hasNext() {
        return hasNextProxy() || !this.postponedRoutes.isEmpty();
    }

    public Selection next() throws IOException {
        if (hasNext()) {
            List<Route> routes = new ArrayList<>();
            while (hasNextProxy()) {
                Proxy proxy = nextProxy();
                int size = this.inetSocketAddresses.size();
                for (int i = 0; i < size; i++) {
                    Route route = new Route(this.address, proxy, this.inetSocketAddresses.get(i));
                    if (this.routeDatabase.shouldPostpone(route)) {
                        this.postponedRoutes.add(route);
                    } else {
                        routes.add(route);
                    }
                }
                if (routes.isEmpty() == 0) {
                    break;
                }
            }
            if (this.concurrentConnectEnabled || routes.isEmpty()) {
                routes.addAll(this.postponedRoutes);
                this.postponedRoutes.clear();
            }
            return new Selection(routes, this.concurrentConnectEnabled, this.routeDatabase);
        }
        throw new NoSuchElementException();
    }

    public void connectFailed(Route failedRoute, IOException failure) {
        if (!(failedRoute.proxy().type() == Proxy.Type.DIRECT || this.address.proxySelector() == null)) {
            this.address.proxySelector().connectFailed(this.address.url().uri(), failedRoute.proxy().address(), failure);
        }
        if (!this.concurrentConnectEnabled) {
            this.routeDatabase.failed(failedRoute);
        }
    }

    private void resetNextProxy(HttpUrl url, Proxy proxy) {
        List<Proxy> list;
        if (proxy != null) {
            this.proxies = Collections.singletonList(proxy);
        } else {
            List<Proxy> proxiesOrNull = this.address.proxySelector().select(url.uri());
            if (proxiesOrNull == null || proxiesOrNull.isEmpty()) {
                list = Util.immutableList((T[]) new Proxy[]{Proxy.NO_PROXY});
            } else {
                list = Util.immutableList(proxiesOrNull);
            }
            this.proxies = list;
        }
        this.nextProxyIndex = 0;
    }

    private boolean hasNextProxy() {
        return this.nextProxyIndex < this.proxies.size();
    }

    private Proxy nextProxy() throws IOException {
        if (hasNextProxy()) {
            List<Proxy> list = this.proxies;
            int i = this.nextProxyIndex;
            this.nextProxyIndex = i + 1;
            Proxy result = list.get(i);
            resetNextInetSocketAddress(result);
            return result;
        }
        throw new SocketException("No route to " + this.address.url().host() + "; exhausted proxy configurations: " + this.proxies);
    }

    private void resetNextInetSocketAddress(Proxy proxy) throws IOException {
        String socketHost;
        int socketPort;
        this.inetSocketAddresses = new ArrayList();
        if (proxy.type() == Proxy.Type.DIRECT || proxy.type() == Proxy.Type.SOCKS) {
            socketHost = this.address.url().host();
            socketPort = this.address.url().port();
        } else {
            SocketAddress proxyAddress = proxy.address();
            if (proxyAddress instanceof InetSocketAddress) {
                InetSocketAddress proxySocketAddress = (InetSocketAddress) proxyAddress;
                socketHost = getHostString(proxySocketAddress);
                socketPort = proxySocketAddress.getPort();
            } else {
                throw new IllegalArgumentException("Proxy.address() is not an InetSocketAddress: " + proxyAddress.getClass());
            }
        }
        if (socketPort < 1 || socketPort > 65535) {
            throw new SocketException("No route to " + socketHost + ":" + socketPort + "; port is out of range");
        }
        if (proxy.type() == Proxy.Type.SOCKS) {
            this.inetSocketAddresses.add(InetSocketAddress.createUnresolved(socketHost, socketPort));
        } else {
            this.eventListener.dnsStart(this.call, socketHost);
            List<InetAddress> addresses = this.address.dns().lookup(socketHost);
            if (!addresses.isEmpty()) {
                this.eventListener.dnsEnd(this.call, socketHost, addresses);
                int size = addresses.size();
                for (int i = 0; i < size; i++) {
                    this.inetSocketAddresses.add(new InetSocketAddress(addresses.get(i), socketPort));
                }
            } else {
                throw new UnknownHostException(this.address.dns() + " returned no addresses for " + socketHost);
            }
        }
        prepareConcurrentConnectAddresses(socketPort);
    }

    private void prepareConcurrentConnectAddresses(int socketPort) {
        if (this.concurrentConnectEnabled) {
            if (this.additionalInetAddresses != null) {
                for (int i = this.additionalInetAddresses.size() - 1; i >= 0; i--) {
                    InetSocketAddress socketAddress = new InetSocketAddress(this.additionalInetAddresses.get(i), socketPort);
                    if (this.inetSocketAddresses.contains(socketAddress)) {
                        this.inetSocketAddresses.remove(socketAddress);
                    }
                    this.inetSocketAddresses.add(0, socketAddress);
                }
            }
            if (this.inetSocketAddresses.size() == 1) {
                this.concurrentConnectEnabled = false;
            }
        }
    }

    static String getHostString(InetSocketAddress socketAddress) {
        InetAddress address2 = socketAddress.getAddress();
        if (address2 == null) {
            return socketAddress.getHostName();
        }
        return address2.getHostAddress();
    }
}
