package org.apache.http.impl.conn;

import java.io.IOException;
import org.apache.http.HttpHost;
import org.apache.http.conn.ClientConnectionOperator;
import org.apache.http.conn.OperatedClientConnection;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.routing.RouteTracker;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

@Deprecated
public abstract class AbstractPoolEntry {
    protected final ClientConnectionOperator connOperator;
    protected final OperatedClientConnection connection;
    protected volatile HttpRoute route;
    protected volatile Object state;
    protected volatile RouteTracker tracker;

    protected AbstractPoolEntry(ClientConnectionOperator connOperator, HttpRoute route) {
        if (connOperator == null) {
            throw new IllegalArgumentException("Connection operator may not be null");
        }
        this.connOperator = connOperator;
        this.connection = connOperator.createConnection();
        this.route = route;
        this.tracker = null;
    }

    public Object getState() {
        return this.state;
    }

    public void setState(Object state) {
        this.state = state;
    }

    public void open(HttpRoute route, HttpContext context, HttpParams params) throws IOException {
        if (route == null) {
            throw new IllegalArgumentException("Route must not be null.");
        } else if (params == null) {
            throw new IllegalArgumentException("Parameters must not be null.");
        } else if (this.tracker == null || !this.tracker.isConnected()) {
            this.tracker = new RouteTracker(route);
            HttpHost proxy = route.getProxyHost();
            this.connOperator.openConnection(this.connection, proxy != null ? proxy : route.getTargetHost(), route.getLocalAddress(), context, params);
            RouteTracker localTracker = this.tracker;
            if (localTracker == null) {
                throw new IOException("Request aborted");
            } else if (proxy == null) {
                localTracker.connectTarget(this.connection.isSecure());
            } else {
                localTracker.connectProxy(proxy, this.connection.isSecure());
            }
        } else {
            throw new IllegalStateException("Connection already open.");
        }
    }

    public void tunnelTarget(boolean secure, HttpParams params) throws IOException {
        if (params == null) {
            throw new IllegalArgumentException("Parameters must not be null.");
        } else if (this.tracker == null || (this.tracker.isConnected() ^ 1) != 0) {
            throw new IllegalStateException("Connection not open.");
        } else if (this.tracker.isTunnelled()) {
            throw new IllegalStateException("Connection is already tunnelled.");
        } else {
            this.connection.update(null, this.tracker.getTargetHost(), secure, params);
            this.tracker.tunnelTarget(secure);
        }
    }

    public void tunnelProxy(HttpHost next, boolean secure, HttpParams params) throws IOException {
        if (next == null) {
            throw new IllegalArgumentException("Next proxy must not be null.");
        } else if (params == null) {
            throw new IllegalArgumentException("Parameters must not be null.");
        } else if (this.tracker == null || (this.tracker.isConnected() ^ 1) != 0) {
            throw new IllegalStateException("Connection not open.");
        } else {
            this.connection.update(null, next, secure, params);
            this.tracker.tunnelProxy(next, secure);
        }
    }

    public void layerProtocol(HttpContext context, HttpParams params) throws IOException {
        if (params == null) {
            throw new IllegalArgumentException("Parameters must not be null.");
        } else if (this.tracker == null || (this.tracker.isConnected() ^ 1) != 0) {
            throw new IllegalStateException("Connection not open.");
        } else if (!this.tracker.isTunnelled()) {
            throw new IllegalStateException("Protocol layering without a tunnel not supported.");
        } else if (this.tracker.isLayered()) {
            throw new IllegalStateException("Multiple protocol layering not supported.");
        } else {
            this.connOperator.updateSecureConnection(this.connection, this.tracker.getTargetHost(), context, params);
            this.tracker.layerProtocol(this.connection.isSecure());
        }
    }

    protected void shutdownEntry() {
        this.tracker = null;
    }
}
