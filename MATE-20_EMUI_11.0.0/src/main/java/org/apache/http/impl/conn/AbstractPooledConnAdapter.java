package org.apache.http.impl.conn;

import java.io.IOException;
import org.apache.http.HttpHost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.OperatedClientConnection;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

@Deprecated
public abstract class AbstractPooledConnAdapter extends AbstractClientConnAdapter {
    protected volatile AbstractPoolEntry poolEntry;

    protected AbstractPooledConnAdapter(ClientConnectionManager manager, AbstractPoolEntry entry) {
        super(manager, entry.connection);
        this.poolEntry = entry;
    }

    /* access modifiers changed from: protected */
    public final void assertAttached() {
        if (this.poolEntry == null) {
            throw new IllegalStateException("Adapter is detached.");
        }
    }

    /* access modifiers changed from: protected */
    @Override // org.apache.http.impl.conn.AbstractClientConnAdapter
    public void detach() {
        super.detach();
        this.poolEntry = null;
    }

    @Override // org.apache.http.conn.ManagedClientConnection
    public HttpRoute getRoute() {
        assertAttached();
        if (this.poolEntry.tracker == null) {
            return null;
        }
        return this.poolEntry.tracker.toRoute();
    }

    @Override // org.apache.http.conn.ManagedClientConnection
    public void open(HttpRoute route, HttpContext context, HttpParams params) throws IOException {
        assertAttached();
        this.poolEntry.open(route, context, params);
    }

    @Override // org.apache.http.conn.ManagedClientConnection
    public void tunnelTarget(boolean secure, HttpParams params) throws IOException {
        assertAttached();
        this.poolEntry.tunnelTarget(secure, params);
    }

    @Override // org.apache.http.conn.ManagedClientConnection
    public void tunnelProxy(HttpHost next, boolean secure, HttpParams params) throws IOException {
        assertAttached();
        this.poolEntry.tunnelProxy(next, secure, params);
    }

    @Override // org.apache.http.conn.ManagedClientConnection
    public void layerProtocol(HttpContext context, HttpParams params) throws IOException {
        assertAttached();
        this.poolEntry.layerProtocol(context, params);
    }

    @Override // org.apache.http.HttpConnection
    public void close() throws IOException {
        if (this.poolEntry != null) {
            this.poolEntry.shutdownEntry();
        }
        OperatedClientConnection conn = getWrappedConnection();
        if (conn != null) {
            conn.close();
        }
    }

    @Override // org.apache.http.HttpConnection
    public void shutdown() throws IOException {
        if (this.poolEntry != null) {
            this.poolEntry.shutdownEntry();
        }
        OperatedClientConnection conn = getWrappedConnection();
        if (conn != null) {
            conn.shutdown();
        }
    }

    @Override // org.apache.http.conn.ManagedClientConnection
    public Object getState() {
        assertAttached();
        return this.poolEntry.getState();
    }

    @Override // org.apache.http.conn.ManagedClientConnection
    public void setState(Object state) {
        assertAttached();
        this.poolEntry.setState(state);
    }
}
