package org.apache.http.impl.conn;

import android.net.TrafficStats;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.TimeUnit;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ClientConnectionOperator;
import org.apache.http.conn.ClientConnectionRequest;
import org.apache.http.conn.ManagedClientConnection;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.routing.RouteTracker;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.params.HttpParams;

@Deprecated
public class SingleClientConnManager implements ClientConnectionManager {
    public static final String MISUSE_MESSAGE = "Invalid use of SingleClientConnManager: connection still allocated.\nMake sure to release the connection before allocating another one.";
    protected boolean alwaysShutDown;
    protected ClientConnectionOperator connOperator;
    protected long connectionExpiresTime;
    protected volatile boolean isShutDown;
    protected long lastReleaseTime;
    private final Log log = LogFactory.getLog((Class) getClass());
    protected ConnAdapter managedConn;
    protected SchemeRegistry schemeRegistry;
    protected PoolEntry uniquePoolEntry;

    protected class ConnAdapter extends AbstractPooledConnAdapter {
        protected ConnAdapter(PoolEntry entry, HttpRoute route) {
            super(SingleClientConnManager.this, entry);
            markReusable();
            entry.route = route;
        }
    }

    protected class PoolEntry extends AbstractPoolEntry {
        protected PoolEntry() {
            super(SingleClientConnManager.this.connOperator, null);
        }

        /* access modifiers changed from: protected */
        public void close() throws IOException {
            shutdownEntry();
            if (this.connection.isOpen()) {
                this.connection.close();
            }
        }

        /* access modifiers changed from: protected */
        public void shutdown() throws IOException {
            shutdownEntry();
            if (this.connection.isOpen()) {
                this.connection.shutdown();
            }
        }
    }

    public SingleClientConnManager(HttpParams params, SchemeRegistry schreg) {
        if (schreg != null) {
            this.schemeRegistry = schreg;
            this.connOperator = createConnectionOperator(schreg);
            this.uniquePoolEntry = new PoolEntry();
            this.managedConn = null;
            this.lastReleaseTime = -1;
            this.alwaysShutDown = false;
            this.isShutDown = false;
            return;
        }
        throw new IllegalArgumentException("Scheme registry must not be null.");
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        shutdown();
        super.finalize();
    }

    public SchemeRegistry getSchemeRegistry() {
        return this.schemeRegistry;
    }

    /* access modifiers changed from: protected */
    public ClientConnectionOperator createConnectionOperator(SchemeRegistry schreg) {
        return new DefaultClientConnectionOperator(schreg);
    }

    /* access modifiers changed from: protected */
    public final void assertStillUp() throws IllegalStateException {
        if (this.isShutDown) {
            throw new IllegalStateException("Manager is shut down.");
        }
    }

    public final ClientConnectionRequest requestConnection(final HttpRoute route, final Object state) {
        return new ClientConnectionRequest() {
            public void abortRequest() {
            }

            public ManagedClientConnection getConnection(long timeout, TimeUnit tunit) {
                return SingleClientConnManager.this.getConnection(route, state);
            }
        };
    }

    public ManagedClientConnection getConnection(HttpRoute route, Object state) {
        if (route != null) {
            assertStillUp();
            if (this.log.isDebugEnabled()) {
                Log log2 = this.log;
                log2.debug("Get connection for route " + route);
            }
            if (this.managedConn != null) {
                revokeConnection();
            }
            boolean recreate = false;
            boolean shutdown = false;
            closeExpiredConnections();
            if (this.uniquePoolEntry.connection.isOpen()) {
                RouteTracker tracker = this.uniquePoolEntry.tracker;
                shutdown = tracker == null || !tracker.toRoute().equals(route);
            } else {
                recreate = true;
            }
            if (shutdown) {
                recreate = true;
                try {
                    this.uniquePoolEntry.shutdown();
                } catch (IOException iox) {
                    this.log.debug("Problem shutting down connection.", iox);
                }
            }
            if (recreate) {
                this.uniquePoolEntry = new PoolEntry();
            }
            try {
                Socket socket = this.uniquePoolEntry.connection.getSocket();
                if (socket != null) {
                    TrafficStats.tagSocket(socket);
                }
            } catch (IOException iox2) {
                this.log.debug("Problem tagging socket.", iox2);
            }
            this.managedConn = new ConnAdapter(this.uniquePoolEntry, route);
            return this.managedConn;
        }
        throw new IllegalArgumentException("Route may not be null.");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0082, code lost:
        if (r12 > 0) goto L_0x00a6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x00a4, code lost:
        if (r12 <= 0) goto L_0x00b0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x00a6, code lost:
        r10.connectionExpiresTime = r14.toMillis(r12) + r10.lastReleaseTime;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x00b0, code lost:
        r10.connectionExpiresTime = Long.MAX_VALUE;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x00b3, code lost:
        return;
     */
    public void releaseConnection(ManagedClientConnection conn, long validDuration, TimeUnit timeUnit) {
        assertStillUp();
        if (conn instanceof ConnAdapter) {
            if (this.log.isDebugEnabled()) {
                Log log2 = this.log;
                log2.debug("Releasing connection " + conn);
            }
            ConnAdapter sca = (ConnAdapter) conn;
            if (sca.poolEntry != null) {
                ClientConnectionManager manager = sca.getManager();
                if (manager == null || manager == this) {
                    try {
                        Socket socket = this.uniquePoolEntry.connection.getSocket();
                        if (socket != null) {
                            TrafficStats.untagSocket(socket);
                        }
                        if (sca.isOpen() && (this.alwaysShutDown || !sca.isMarkedReusable())) {
                            if (this.log.isDebugEnabled()) {
                                this.log.debug("Released connection open but not reusable.");
                            }
                            sca.shutdown();
                        }
                        sca.detach();
                        this.managedConn = null;
                        this.lastReleaseTime = System.currentTimeMillis();
                    } catch (IOException iox) {
                        if (this.log.isDebugEnabled()) {
                            this.log.debug("Exception shutting down released connection.", iox);
                        }
                        sca.detach();
                        this.managedConn = null;
                        this.lastReleaseTime = System.currentTimeMillis();
                    } catch (Throwable th) {
                        sca.detach();
                        this.managedConn = null;
                        this.lastReleaseTime = System.currentTimeMillis();
                        if (validDuration > 0) {
                            this.connectionExpiresTime = timeUnit.toMillis(validDuration) + this.lastReleaseTime;
                        } else {
                            this.connectionExpiresTime = Long.MAX_VALUE;
                        }
                        throw th;
                    }
                } else {
                    throw new IllegalArgumentException("Connection not obtained from this manager.");
                }
            }
        } else {
            throw new IllegalArgumentException("Connection class mismatch, connection not obtained from this manager.");
        }
    }

    public void closeExpiredConnections() {
        if (System.currentTimeMillis() >= this.connectionExpiresTime) {
            closeIdleConnections(0, TimeUnit.MILLISECONDS);
        }
    }

    public void closeIdleConnections(long idletime, TimeUnit tunit) {
        assertStillUp();
        if (tunit == null) {
            throw new IllegalArgumentException("Time unit must not be null.");
        } else if (this.managedConn == null && this.uniquePoolEntry.connection.isOpen()) {
            if (this.lastReleaseTime <= System.currentTimeMillis() - tunit.toMillis(idletime)) {
                try {
                    this.uniquePoolEntry.close();
                } catch (IOException iox) {
                    this.log.debug("Problem closing idle connection.", iox);
                }
            }
        }
    }

    public void shutdown() {
        this.isShutDown = true;
        if (this.managedConn != null) {
            this.managedConn.detach();
        }
        try {
            if (this.uniquePoolEntry != null) {
                this.uniquePoolEntry.shutdown();
            }
        } catch (IOException iox) {
            this.log.debug("Problem while shutting down manager.", iox);
        } catch (Throwable th) {
            this.uniquePoolEntry = null;
            throw th;
        }
        this.uniquePoolEntry = null;
    }

    /* access modifiers changed from: protected */
    public void revokeConnection() {
        if (this.managedConn != null) {
            this.log.warn(MISUSE_MESSAGE);
            this.managedConn.detach();
            try {
                this.uniquePoolEntry.shutdown();
            } catch (IOException iox) {
                this.log.debug("Problem while shutting down connection.", iox);
            }
        }
    }
}
