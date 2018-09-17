package org.apache.http.impl.conn.tsccm;

import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.apache.http.conn.OperatedClientConnection;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.conn.IdleConnectionHandler;

@Deprecated
public abstract class AbstractConnPool implements RefQueueHandler {
    protected IdleConnectionHandler idleConnHandler = new IdleConnectionHandler();
    protected volatile boolean isShutDown;
    protected Set<BasicPoolEntryRef> issuedConnections = new HashSet();
    private final Log log = LogFactory.getLog(getClass());
    protected int numConnections;
    protected final Lock poolLock = new ReentrantLock(false);
    protected ReferenceQueue<Object> refQueue;
    private RefQueueWorker refWorker;

    public abstract void deleteClosedConnections();

    public abstract void freeEntry(BasicPoolEntry basicPoolEntry, boolean z, long j, TimeUnit timeUnit);

    protected abstract void handleLostEntry(HttpRoute httpRoute);

    public abstract PoolEntryRequest requestPoolEntry(HttpRoute httpRoute, Object obj);

    protected AbstractConnPool() {
    }

    public void enableConnectionGC() throws IllegalStateException {
        if (this.refQueue != null) {
            throw new IllegalStateException("Connection GC already enabled.");
        }
        this.poolLock.lock();
        try {
            if (this.numConnections > 0) {
                throw new IllegalStateException("Pool already in use.");
            }
            this.refQueue = new ReferenceQueue();
            this.refWorker = new RefQueueWorker(this.refQueue, this);
            Thread t = new Thread(this.refWorker);
            t.setDaemon(true);
            t.setName("RefQueueWorker@" + this);
            t.start();
        } finally {
            this.poolLock.unlock();
        }
    }

    public final BasicPoolEntry getEntry(HttpRoute route, Object state, long timeout, TimeUnit tunit) throws ConnectionPoolTimeoutException, InterruptedException {
        return requestPoolEntry(route, state).getPoolEntry(timeout, tunit);
    }

    public void handleReference(Reference ref) {
        this.poolLock.lock();
        try {
            if ((ref instanceof BasicPoolEntryRef) && this.issuedConnections.remove(ref)) {
                HttpRoute route = ((BasicPoolEntryRef) ref).getRoute();
                if (this.log.isDebugEnabled()) {
                    this.log.debug("Connection garbage collected. " + route);
                }
                handleLostEntry(route);
            }
            this.poolLock.unlock();
        } catch (Throwable th) {
            this.poolLock.unlock();
        }
    }

    public void closeIdleConnections(long idletime, TimeUnit tunit) {
        if (tunit == null) {
            throw new IllegalArgumentException("Time unit must not be null.");
        }
        this.poolLock.lock();
        try {
            this.idleConnHandler.closeIdleConnections(tunit.toMillis(idletime));
        } finally {
            this.poolLock.unlock();
        }
    }

    public void closeExpiredConnections() {
        this.poolLock.lock();
        try {
            this.idleConnHandler.closeExpiredConnections();
        } finally {
            this.poolLock.unlock();
        }
    }

    public void shutdown() {
        this.poolLock.lock();
        try {
            if (!this.isShutDown) {
                if (this.refWorker != null) {
                    this.refWorker.shutdown();
                }
                Iterator<BasicPoolEntryRef> iter = this.issuedConnections.iterator();
                while (iter.hasNext()) {
                    BasicPoolEntryRef per = (BasicPoolEntryRef) iter.next();
                    iter.remove();
                    BasicPoolEntry entry = (BasicPoolEntry) per.get();
                    if (entry != null) {
                        closeConnection(entry.getConnection());
                    }
                }
                this.idleConnHandler.removeAll();
                this.isShutDown = true;
                this.poolLock.unlock();
            }
        } finally {
            this.poolLock.unlock();
        }
    }

    protected void closeConnection(OperatedClientConnection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (IOException ex) {
                this.log.debug("I/O error closing connection", ex);
            }
        }
    }
}
