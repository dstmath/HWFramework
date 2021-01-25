package com.huawei.okhttp3.internal.connection;

import com.huawei.okhttp3.Address;
import com.huawei.okhttp3.Call;
import com.huawei.okhttp3.EventListener;
import com.huawei.okhttp3.HttpUrl;
import com.huawei.okhttp3.Route;
import com.huawei.okhttp3.internal.Util;
import com.huawei.okhttp3.internal.connection.RouteSelectorBase;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

public final class RouteSelector extends RouteSelectorBase {
    private final Address address;
    private final Call call;
    private final EventListener eventListener;
    private int nextProxyIndex;
    private final List<Route> postponedRoutes = new ArrayList();
    private final RouteDatabase routeDatabase;

    RouteSelector(Address address2, RouteDatabase routeDatabase2, Call call2, EventListener eventListener2) {
        super(call2);
        this.address = address2;
        this.routeDatabase = routeDatabase2;
        this.call = call2;
        this.eventListener = eventListener2;
        resetNextProxy(address2.url(), address2.proxy());
        concurrentConnectProxy();
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
                    Route route = new Route(this.address, proxy, (InetSocketAddress) this.inetSocketAddresses.get(i));
                    if (this.routeDatabase.shouldPostpone(route)) {
                        this.postponedRoutes.add(route);
                    } else {
                        routes.add(route);
                    }
                }
                if (!routes.isEmpty()) {
                    break;
                }
            }
            if (concurrentConnectEnabled() || routes.isEmpty()) {
                routes.addAll(this.postponedRoutes);
                this.postponedRoutes.clear();
            }
            return new Selection(routes, concurrentConnectEnabled(), this.routeDatabase);
        }
        throw new NoSuchElementException();
    }

    private void resetNextProxy(HttpUrl url, Proxy proxy) {
        List list;
        if (proxy != null) {
            this.proxies = Collections.singletonList(proxy);
        } else {
            List<Proxy> proxiesOrNull = this.address.proxySelector().select(url.uri());
            if (proxiesOrNull == null || proxiesOrNull.isEmpty()) {
                list = Util.immutableList(Proxy.NO_PROXY);
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
            List list = this.proxies;
            int i = this.nextProxyIndex;
            this.nextProxyIndex = i + 1;
            Proxy result = (Proxy) list.get(i);
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
        } else if (!concurrentConnectEnabled() || additionalInetAddresses().isEmpty()) {
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

    static String getHostString(InetSocketAddress socketAddress) {
        InetAddress address2 = socketAddress.getAddress();
        if (address2 == null) {
            return socketAddress.getHostName();
        }
        return address2.getHostAddress();
    }

    public static final class Selection extends RouteSelectorBase.SelectionBase {
        private int nextRouteIndex = 0;

        Selection(List<Route> routes) {
            super(routes);
            this.routes = routes;
        }

        Selection(List<Route> routes, boolean concurrentConnectEnabled, RouteDatabase routeDatabase) {
            super(routes, concurrentConnectEnabled, routeDatabase);
            this.routes = routes;
        }

        public boolean hasNext() {
            return concurrentConnectEnabled() ? this.routes.size() > 0 : this.nextRouteIndex < this.routes.size();
        }

        public Route next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            } else if (concurrentConnectEnabled()) {
                return (Route) this.routes.get(0);
            } else {
                List list = this.routes;
                int i = this.nextRouteIndex;
                this.nextRouteIndex = i + 1;
                return (Route) list.get(i);
            }
        }

        public List<Route> getAll() {
            return new ArrayList(this.routes);
        }
    }
}
