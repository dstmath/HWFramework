package org.apache.http.impl.conn.tsccm;

import android.net.TrafficStats;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.TimeUnit;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ClientConnectionOperator;
import org.apache.http.conn.ClientConnectionRequest;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.apache.http.conn.ManagedClientConnection;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.conn.DefaultClientConnectionOperator;
import org.apache.http.params.HttpParams;

@Deprecated
public class ThreadSafeClientConnManager implements ClientConnectionManager {
    protected ClientConnectionOperator connOperator;
    protected final AbstractConnPool connectionPool;
    private final Log log = LogFactory.getLog(getClass());
    protected SchemeRegistry schemeRegistry;

    public ThreadSafeClientConnManager(HttpParams params, SchemeRegistry schreg) {
        if (params != null) {
            this.schemeRegistry = schreg;
            this.connOperator = createConnectionOperator(schreg);
            this.connectionPool = createConnectionPool(params);
            return;
        }
        throw new IllegalArgumentException("HTTP parameters may not be null");
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        shutdown();
        super.finalize();
    }

    /* access modifiers changed from: protected */
    public AbstractConnPool createConnectionPool(HttpParams params) {
        AbstractConnPool acp = new ConnPoolByRoute(this.connOperator, params);
        if (1 != 0) {
            acp.enableConnectionGC();
        }
        return acp;
    }

    /* access modifiers changed from: protected */
    public ClientConnectionOperator createConnectionOperator(SchemeRegistry schreg) {
        return new DefaultClientConnectionOperator(schreg);
    }

    @Override // org.apache.http.conn.ClientConnectionManager
    public SchemeRegistry getSchemeRegistry() {
        return this.schemeRegistry;
    }

    @Override // org.apache.http.conn.ClientConnectionManager
    public ClientConnectionRequest requestConnection(final HttpRoute route, Object state) {
        final PoolEntryRequest poolRequest = this.connectionPool.requestPoolEntry(route, state);
        return new ClientConnectionRequest() {
            /* class org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager.AnonymousClass1 */

            @Override // org.apache.http.conn.ClientConnectionRequest
            public void abortRequest() {
                poolRequest.abortRequest();
            }

            @Override // org.apache.http.conn.ClientConnectionRequest
            public ManagedClientConnection getConnection(long timeout, TimeUnit tunit) throws InterruptedException, ConnectionPoolTimeoutException {
                if (route != null) {
                    if (ThreadSafeClientConnManager.this.log.isDebugEnabled()) {
                        Log log = ThreadSafeClientConnManager.this.log;
                        log.debug("ThreadSafeClientConnManager.getConnection: " + route + ", timeout = " + timeout);
                    }
                    BasicPoolEntry entry = poolRequest.getPoolEntry(timeout, tunit);
                    try {
                        Socket socket = entry.getConnection().getSocket();
                        if (socket != null) {
                            TrafficStats.tagSocket(socket);
                        }
                    } catch (IOException iox) {
                        ThreadSafeClientConnManager.this.log.debug("Problem tagging socket.", iox);
                    }
                    return new BasicPooledConnAdapter(ThreadSafeClientConnManager.this, entry);
                }
                throw new IllegalArgumentException("Route may not be null.");
            }
        };
    }

    @Override // org.apache.http.conn.ClientConnectionManager
    public void releaseConnection(ManagedClientConnection conn, long validDuration, TimeUnit timeUnit) {
        boolean reusable;
        BasicPoolEntry entry;
        if (conn instanceof BasicPooledConnAdapter) {
            BasicPooledConnAdapter hca = (BasicPooledConnAdapter) conn;
            if (hca.getPoolEntry() == null || hca.getManager() == this) {
                try {
                    Socket socket = ((BasicPoolEntry) hca.getPoolEntry()).getConnection().getSocket();
                    if (socket != null) {
                        TrafficStats.untagSocket(socket);
                    }
                    if (hca.isOpen() && !hca.isMarkedReusable()) {
                        if (this.log.isDebugEnabled()) {
                            this.log.debug("Released connection open but not marked reusable.");
                        }
                        hca.shutdown();
                    }
                    entry = (BasicPoolEntry) hca.getPoolEntry();
                    reusable = hca.isMarkedReusable();
                    hca.detach();
                    if (entry == null) {
                        return;
                    }
                } catch (IOException iox) {
                    if (this.log.isDebugEnabled()) {
                        this.log.debug("Exception shutting down released connection.", iox);
                    }
                    entry = (BasicPoolEntry) hca.getPoolEntry();
                    reusable = hca.isMarkedReusable();
                    hca.detach();
                    if (entry == null) {
                        return;
                    }
                } catch (Throwable th) {
                    BasicPoolEntry entry2 = (BasicPoolEntry) hca.getPoolEntry();
                    boolean reusable2 = hca.isMarkedReusable();
                    hca.detach();
                    if (entry2 != null) {
                        this.connectionPool.freeEntry(entry2, reusable2, validDuration, timeUnit);
                    }
                    throw th;
                }
                this.connectionPool.freeEntry(entry, reusable, validDuration, timeUnit);
                return;
            }
            throw new IllegalArgumentException("Connection not obtained from this manager.");
        }
        throw new IllegalArgumentException("Connection class mismatch, connection not obtained from this manager.");
    }

    @Override // org.apache.http.conn.ClientConnectionManager
    public void shutdown() {
        this.connectionPool.shutdown();
    }

    public int getConnectionsInPool(HttpRoute route) {
        return ((ConnPoolByRoute) this.connectionPool).getConnectionsInPool(route);
    }

    public int getConnectionsInPool() {
        int i;
        synchronized (this.connectionPool) {
            i = this.connectionPool.numConnections;
        }
        return i;
    }

    @Override // org.apache.http.conn.ClientConnectionManager
    public void closeIdleConnections(long idleTimeout, TimeUnit tunit) {
        this.connectionPool.closeIdleConnections(idleTimeout, tunit);
        this.connectionPool.deleteClosedConnections();
    }

    @Override // org.apache.http.conn.ClientConnectionManager
    public void closeExpiredConnections() {
        this.connectionPool.closeExpiredConnections();
        this.connectionPool.deleteClosedConnections();
    }
}
