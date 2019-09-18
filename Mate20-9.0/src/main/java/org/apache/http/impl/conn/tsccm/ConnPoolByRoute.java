package org.apache.http.impl.conn.tsccm;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.conn.ClientConnectionOperator;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRoute;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.params.HttpParams;

@Deprecated
public class ConnPoolByRoute extends AbstractConnPool {
    private final ConnPerRoute connPerRoute;
    protected Queue<BasicPoolEntry> freeConnections;
    private final Log log = LogFactory.getLog((Class) getClass());
    protected final int maxTotalConnections;
    protected final ClientConnectionOperator operator;
    protected final Map<HttpRoute, RouteSpecificPool> routeToPool;
    protected Queue<WaitingThread> waitingThreads;

    public ConnPoolByRoute(ClientConnectionOperator operator2, HttpParams params) {
        if (operator2 != null) {
            this.operator = operator2;
            this.freeConnections = createFreeConnQueue();
            this.waitingThreads = createWaitingThreadQueue();
            this.routeToPool = createRouteToPoolMap();
            this.maxTotalConnections = ConnManagerParams.getMaxTotalConnections(params);
            this.connPerRoute = ConnManagerParams.getMaxConnectionsPerRoute(params);
            return;
        }
        throw new IllegalArgumentException("Connection operator may not be null");
    }

    /* access modifiers changed from: protected */
    public Queue<BasicPoolEntry> createFreeConnQueue() {
        return new LinkedList();
    }

    /* access modifiers changed from: protected */
    public Queue<WaitingThread> createWaitingThreadQueue() {
        return new LinkedList();
    }

    /* access modifiers changed from: protected */
    public Map<HttpRoute, RouteSpecificPool> createRouteToPoolMap() {
        return new HashMap();
    }

    /* access modifiers changed from: protected */
    public RouteSpecificPool newRouteSpecificPool(HttpRoute route) {
        return new RouteSpecificPool(route, this.connPerRoute.getMaxForRoute(route));
    }

    /* access modifiers changed from: protected */
    public WaitingThread newWaitingThread(Condition cond, RouteSpecificPool rospl) {
        return new WaitingThread(cond, rospl);
    }

    /* access modifiers changed from: protected */
    public RouteSpecificPool getRoutePool(HttpRoute route, boolean create) {
        this.poolLock.lock();
        try {
            RouteSpecificPool rospl = this.routeToPool.get(route);
            if (rospl == null && create) {
                rospl = newRouteSpecificPool(route);
                this.routeToPool.put(route, rospl);
            }
            return rospl;
        } finally {
            this.poolLock.unlock();
        }
    }

    public int getConnectionsInPool(HttpRoute route) {
        this.poolLock.lock();
        int i = 0;
        try {
            RouteSpecificPool rospl = getRoutePool(route, false);
            if (rospl != null) {
                i = rospl.getEntryCount();
            }
            return i;
        } finally {
            this.poolLock.unlock();
        }
    }

    public PoolEntryRequest requestPoolEntry(final HttpRoute route, final Object state) {
        final WaitingThreadAborter aborter = new WaitingThreadAborter();
        return new PoolEntryRequest() {
            public void abortRequest() {
                ConnPoolByRoute.this.poolLock.lock();
                try {
                    aborter.abort();
                } finally {
                    ConnPoolByRoute.this.poolLock.unlock();
                }
            }

            public BasicPoolEntry getPoolEntry(long timeout, TimeUnit tunit) throws InterruptedException, ConnectionPoolTimeoutException {
                return ConnPoolByRoute.this.getEntryBlocking(route, state, timeout, tunit, aborter);
            }
        };
    }

    /* access modifiers changed from: protected */
    public BasicPoolEntry getEntryBlocking(HttpRoute route, Object state, long timeout, TimeUnit tunit, WaitingThreadAborter aborter) throws ConnectionPoolTimeoutException, InterruptedException {
        Date deadline;
        HttpRoute httpRoute = route;
        Object obj = state;
        long j = timeout;
        if (j > 0) {
            deadline = new Date(System.currentTimeMillis() + tunit.toMillis(j));
        } else {
            TimeUnit timeUnit = tunit;
            deadline = null;
        }
        BasicPoolEntry entry = null;
        this.poolLock.lock();
        try {
            RouteSpecificPool rospl = getRoutePool(httpRoute, true);
            WaitingThread waitingThread = null;
            while (true) {
                if (entry != null) {
                    break;
                } else if (!this.isShutDown) {
                    if (this.log.isDebugEnabled()) {
                        this.log.debug("Total connections kept alive: " + this.freeConnections.size());
                        this.log.debug("Total issued connections: " + this.issuedConnections.size());
                        this.log.debug("Total allocated connection: " + this.numConnections + " out of " + this.maxTotalConnections);
                    }
                    entry = getFreeEntry(rospl, obj);
                    if (entry != null) {
                        break;
                    }
                    boolean hasCapacity = rospl.getCapacity() > 0;
                    if (this.log.isDebugEnabled()) {
                        this.log.debug("Available capacity: " + rospl.getCapacity() + " out of " + rospl.getMaxEntries() + " [" + httpRoute + "][" + obj + "]");
                    }
                    if (hasCapacity && this.numConnections < this.maxTotalConnections) {
                        entry = createEntry(rospl, this.operator);
                    } else if (!hasCapacity || this.freeConnections.isEmpty()) {
                        if (this.log.isDebugEnabled()) {
                            this.log.debug("Need to wait for connection [" + httpRoute + "][" + obj + "]");
                        }
                        if (waitingThread == null) {
                            waitingThread = newWaitingThread(this.poolLock.newCondition(), rospl);
                            try {
                                aborter.setWaitingThread(waitingThread);
                            } catch (Throwable th) {
                                th = th;
                            }
                        } else {
                            WaitingThreadAborter waitingThreadAborter = aborter;
                        }
                        rospl.queueThread(waitingThread);
                        this.waitingThreads.add(waitingThread);
                        boolean success = waitingThread.await(deadline);
                        rospl.removeThread(waitingThread);
                        this.waitingThreads.remove(waitingThread);
                        if (!success && deadline != null) {
                            if (deadline.getTime() <= System.currentTimeMillis()) {
                                throw new ConnectionPoolTimeoutException("Timeout waiting for connection");
                            }
                        }
                    } else {
                        deleteLeastUsedEntry();
                        entry = createEntry(rospl, this.operator);
                    }
                    WaitingThreadAborter waitingThreadAborter2 = aborter;
                } else {
                    WaitingThreadAborter waitingThreadAborter3 = aborter;
                    throw new IllegalStateException("Connection pool shut down.");
                }
            }
            WaitingThreadAborter waitingThreadAborter4 = aborter;
            this.poolLock.unlock();
            return entry;
        } catch (Throwable th2) {
            th = th2;
            WaitingThreadAborter waitingThreadAborter5 = aborter;
            this.poolLock.unlock();
            throw th;
        }
    }

    public void freeEntry(BasicPoolEntry entry, boolean reusable, long validDuration, TimeUnit timeUnit) {
        HttpRoute route = entry.getPlannedRoute();
        if (this.log.isDebugEnabled()) {
            this.log.debug("Freeing connection [" + route + "][" + entry.getState() + "]");
        }
        this.poolLock.lock();
        try {
            if (this.isShutDown) {
                closeConnection(entry.getConnection());
                return;
            }
            this.issuedConnections.remove(entry.getWeakRef());
            RouteSpecificPool rospl = getRoutePool(route, true);
            if (reusable) {
                rospl.freeEntry(entry);
                this.freeConnections.add(entry);
                this.idleConnHandler.add(entry.getConnection(), validDuration, timeUnit);
            } else {
                rospl.dropEntry();
                this.numConnections--;
            }
            notifyWaitingThread(rospl);
            this.poolLock.unlock();
        } finally {
            this.poolLock.unlock();
        }
    }

    /* access modifiers changed from: protected */
    public BasicPoolEntry getFreeEntry(RouteSpecificPool rospl, Object state) {
        BasicPoolEntry entry = null;
        this.poolLock.lock();
        boolean done = false;
        while (!done) {
            try {
                entry = rospl.allocEntry(state);
                if (entry != null) {
                    if (this.log.isDebugEnabled()) {
                        this.log.debug("Getting free connection [" + rospl.getRoute() + "][" + state + "]");
                    }
                    this.freeConnections.remove(entry);
                    if (!this.idleConnHandler.remove(entry.getConnection())) {
                        if (this.log.isDebugEnabled()) {
                            this.log.debug("Closing expired free connection [" + rospl.getRoute() + "][" + state + "]");
                        }
                        closeConnection(entry.getConnection());
                        rospl.dropEntry();
                        this.numConnections--;
                    } else {
                        this.issuedConnections.add(entry.getWeakRef());
                        done = true;
                    }
                } else {
                    done = true;
                    if (this.log.isDebugEnabled()) {
                        this.log.debug("No free connections [" + rospl.getRoute() + "][" + state + "]");
                    }
                }
            } catch (Throwable th) {
                this.poolLock.unlock();
                throw th;
            }
        }
        this.poolLock.unlock();
        return entry;
    }

    /* access modifiers changed from: protected */
    public BasicPoolEntry createEntry(RouteSpecificPool rospl, ClientConnectionOperator op) {
        if (this.log.isDebugEnabled()) {
            this.log.debug("Creating new connection [" + rospl.getRoute() + "]");
        }
        BasicPoolEntry entry = new BasicPoolEntry(op, rospl.getRoute(), this.refQueue);
        this.poolLock.lock();
        try {
            rospl.createdEntry(entry);
            this.numConnections++;
            this.issuedConnections.add(entry.getWeakRef());
            return entry;
        } finally {
            this.poolLock.unlock();
        }
    }

    /* access modifiers changed from: protected */
    public void deleteEntry(BasicPoolEntry entry) {
        HttpRoute route = entry.getPlannedRoute();
        if (this.log.isDebugEnabled()) {
            this.log.debug("Deleting connection [" + route + "][" + entry.getState() + "]");
        }
        this.poolLock.lock();
        try {
            closeConnection(entry.getConnection());
            RouteSpecificPool rospl = getRoutePool(route, true);
            rospl.deleteEntry(entry);
            this.numConnections--;
            if (rospl.isUnused()) {
                this.routeToPool.remove(route);
            }
            this.idleConnHandler.remove(entry.getConnection());
        } finally {
            this.poolLock.unlock();
        }
    }

    /* access modifiers changed from: protected */
    public void deleteLeastUsedEntry() {
        try {
            this.poolLock.lock();
            BasicPoolEntry entry = this.freeConnections.remove();
            if (entry != null) {
                deleteEntry(entry);
            } else if (this.log.isDebugEnabled()) {
                this.log.debug("No free connection to delete.");
            }
        } finally {
            this.poolLock.unlock();
        }
    }

    /* access modifiers changed from: protected */
    public void handleLostEntry(HttpRoute route) {
        this.poolLock.lock();
        try {
            RouteSpecificPool rospl = getRoutePool(route, true);
            rospl.dropEntry();
            if (rospl.isUnused()) {
                this.routeToPool.remove(route);
            }
            this.numConnections--;
            notifyWaitingThread(rospl);
        } finally {
            this.poolLock.unlock();
        }
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x006f A[Catch:{ all -> 0x003b }] */
    public void notifyWaitingThread(RouteSpecificPool rospl) {
        WaitingThread waitingThread = null;
        this.poolLock.lock();
        if (rospl != null) {
            try {
                if (rospl.hasThread()) {
                    if (this.log.isDebugEnabled()) {
                        Log log2 = this.log;
                        log2.debug("Notifying thread waiting on pool [" + rospl.getRoute() + "]");
                    }
                    waitingThread = rospl.nextThread();
                    if (waitingThread != null) {
                        waitingThread.wakeup();
                    }
                    this.poolLock.unlock();
                }
            } catch (Throwable th) {
                this.poolLock.unlock();
                throw th;
            }
        }
        if (!this.waitingThreads.isEmpty()) {
            if (this.log.isDebugEnabled()) {
                this.log.debug("Notifying thread waiting on any pool");
            }
            waitingThread = this.waitingThreads.remove();
        } else if (this.log.isDebugEnabled()) {
            this.log.debug("Notifying no-one, there are no waiting threads");
        }
        if (waitingThread != null) {
        }
        this.poolLock.unlock();
    }

    public void deleteClosedConnections() {
        this.poolLock.lock();
        try {
            Iterator<BasicPoolEntry> iter = this.freeConnections.iterator();
            while (iter.hasNext()) {
                BasicPoolEntry entry = iter.next();
                if (!entry.getConnection().isOpen()) {
                    iter.remove();
                    deleteEntry(entry);
                }
            }
        } finally {
            this.poolLock.unlock();
        }
    }

    public void shutdown() {
        this.poolLock.lock();
        try {
            super.shutdown();
            Iterator<BasicPoolEntry> ibpe = this.freeConnections.iterator();
            while (ibpe.hasNext()) {
                ibpe.remove();
                closeConnection(ibpe.next().getConnection());
            }
            Iterator<WaitingThread> iwth = this.waitingThreads.iterator();
            while (iwth.hasNext()) {
                iwth.remove();
                iwth.next().wakeup();
            }
            this.routeToPool.clear();
        } finally {
            this.poolLock.unlock();
        }
    }
}
