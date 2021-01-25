package com.huawei.okhttp3.internal.connection;

import com.huawei.okhttp3.Address;
import com.huawei.okhttp3.Call;
import com.huawei.okhttp3.EventListener;
import com.huawei.okhttp3.Interceptor;
import com.huawei.okhttp3.OkHttpClient;
import com.huawei.okhttp3.Route;
import com.huawei.okhttp3.internal.Util;
import com.huawei.okhttp3.internal.connection.RouteSelector;
import com.huawei.okhttp3.internal.http.ExchangeCodec;
import java.io.IOException;
import java.net.Socket;
import java.util.List;

/* access modifiers changed from: package-private */
public final class ExchangeFinder {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private final Address address;
    private final Call call;
    private RealConnection connectingConnection;
    private final RealConnectionPool connectionPool;
    private final EventListener eventListener;
    private boolean hasStreamFailure;
    private Route nextRouteToTry;
    private RouteSelector.Selection routeSelection;
    private final RouteSelector routeSelector;
    private final Transmitter transmitter;

    ExchangeFinder(Transmitter transmitter2, RealConnectionPool connectionPool2, Address address2, Call call2, EventListener eventListener2) {
        this.transmitter = transmitter2;
        this.connectionPool = connectionPool2;
        this.address = address2;
        this.call = call2;
        this.eventListener = eventListener2;
        this.routeSelector = new RouteSelector(address2, connectionPool2.routeDatabase, call2, eventListener2);
    }

    public ExchangeCodec find(OkHttpClient client, Interceptor.Chain chain, boolean doExtensiveHealthChecks) {
        try {
            return findHealthyConnection(chain.connectTimeoutMillis(), chain.readTimeoutMillis(), chain.writeTimeoutMillis(), client.pingIntervalMillis(), client.retryOnConnectionFailure(), doExtensiveHealthChecks).newCodec(client, chain);
        } catch (RouteException e) {
            trackFailure();
            throw e;
        } catch (IOException e2) {
            trackFailure();
            throw new RouteException(e2);
        }
    }

    private RealConnection findHealthyConnection(int connectTimeout, int readTimeout, int writeTimeout, int pingIntervalMillis, boolean connectionRetryEnabled, boolean doExtensiveHealthChecks) throws IOException {
        while (true) {
            RealConnection candidate = findConnection(connectTimeout, readTimeout, writeTimeout, pingIntervalMillis, connectionRetryEnabled);
            synchronized (this.connectionPool) {
                if (candidate.successCount == 0) {
                    return candidate;
                }
            }
            if (candidate.isHealthy(doExtensiveHealthChecks)) {
                return candidate;
            }
            candidate.noNewExchanges();
        }
    }

    private RealConnection findConnection(int connectTimeout, int readTimeout, int writeTimeout, int pingIntervalMillis, boolean connectionRetryEnabled) throws IOException {
        RealConnection releasedConnection;
        Socket toClose;
        boolean newRouteSelection;
        RouteSelector.Selection selection;
        boolean foundPooledConnection = false;
        RealConnection result = null;
        Route selectedRoute = null;
        synchronized (this.connectionPool) {
            if (!this.transmitter.isCanceled()) {
                this.hasStreamFailure = false;
                releasedConnection = this.transmitter.connection;
                if (this.transmitter.connection == null || !this.transmitter.connection.noNewExchanges) {
                    toClose = null;
                } else {
                    toClose = this.transmitter.releaseConnectionNoEvents();
                }
                if (this.transmitter.connection != null) {
                    result = this.transmitter.connection;
                    releasedConnection = null;
                }
                if (result == null) {
                    if (this.connectionPool.transmitterAcquirePooledConnection(this.address, this.transmitter, null, false)) {
                        foundPooledConnection = true;
                        result = this.transmitter.connection;
                    } else if (this.nextRouteToTry != null) {
                        selectedRoute = this.nextRouteToTry;
                        this.nextRouteToTry = null;
                    } else if (retryCurrentRoute()) {
                        selectedRoute = this.transmitter.connection.route();
                    }
                }
            } else {
                throw new IOException("Canceled");
            }
        }
        Util.closeQuietly(toClose);
        if (releasedConnection != null) {
            this.eventListener.connectionReleased(this.call, releasedConnection);
        }
        if (foundPooledConnection) {
            this.eventListener.connectionAcquired(this.call, result);
        }
        if (result != null) {
            return result;
        }
        if (selectedRoute != null || ((selection = this.routeSelection) != null && selection.hasNext())) {
            newRouteSelection = false;
        } else {
            this.routeSelection = this.routeSelector.next();
            newRouteSelection = true;
        }
        List<Route> routes = null;
        synchronized (this.connectionPool) {
            if (!this.transmitter.isCanceled()) {
                if (newRouteSelection) {
                    routes = this.routeSelection.getAll();
                    if (this.connectionPool.transmitterAcquirePooledConnection(this.address, this.transmitter, routes, false)) {
                        foundPooledConnection = true;
                        result = this.transmitter.connection;
                    }
                }
                if (!foundPooledConnection) {
                    if (selectedRoute == null) {
                        selectedRoute = this.routeSelection.next();
                    }
                    result = new RealConnection(this.connectionPool, selectedRoute);
                    this.connectingConnection = result;
                }
            } else {
                throw new IOException("Canceled");
            }
        }
        if (foundPooledConnection) {
            this.eventListener.connectionAcquired(this.call, result);
            return result;
        }
        result.connect(connectTimeout, readTimeout, writeTimeout, pingIntervalMillis, connectionRetryEnabled, this.call, this.eventListener);
        this.connectionPool.routeDatabase.connected(result.route());
        Socket socket = null;
        synchronized (this.connectionPool) {
            this.connectingConnection = null;
            if (this.connectionPool.transmitterAcquirePooledConnection(this.address, this.transmitter, routes, true)) {
                result.noNewExchanges = true;
                socket = result.socket();
                result = this.transmitter.connection;
                this.nextRouteToTry = selectedRoute;
            } else {
                this.connectionPool.put(result);
                this.transmitter.acquireConnectionNoEvents(result);
            }
        }
        Util.closeQuietly(socket);
        this.eventListener.connectionAcquired(this.call, result);
        return result;
    }

    /* access modifiers changed from: package-private */
    public RealConnection connectingConnection() {
        return this.connectingConnection;
    }

    /* access modifiers changed from: package-private */
    public void trackFailure() {
        synchronized (this.connectionPool) {
            this.hasStreamFailure = true;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean hasStreamFailure() {
        boolean z;
        synchronized (this.connectionPool) {
            z = this.hasStreamFailure;
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    public boolean hasRouteToTry() {
        synchronized (this.connectionPool) {
            boolean z = true;
            if (this.nextRouteToTry != null) {
                return true;
            }
            if (retryCurrentRoute()) {
                this.nextRouteToTry = this.transmitter.connection.route();
                return true;
            }
            if ((this.routeSelection == null || !this.routeSelection.hasNext()) && !this.routeSelector.hasNext()) {
                z = false;
            }
            return z;
        }
    }

    private boolean retryCurrentRoute() {
        return this.transmitter.connection != null && this.transmitter.connection.routeFailureCount == 0 && Util.sameConnection(this.transmitter.connection.route().address().url(), this.address.url());
    }

    public void setAddressHeaderField(String headerHost) {
        this.address.setHeaderHost(headerHost);
    }
}
