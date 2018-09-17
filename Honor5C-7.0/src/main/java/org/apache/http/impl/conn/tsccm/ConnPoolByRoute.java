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
    private final Log log;
    protected final int maxTotalConnections;
    protected final ClientConnectionOperator operator;
    protected final Map<HttpRoute, RouteSpecificPool> routeToPool;
    protected Queue<WaitingThread> waitingThreads;

    /* renamed from: org.apache.http.impl.conn.tsccm.ConnPoolByRoute.1 */
    class AnonymousClass1 implements PoolEntryRequest {
        final /* synthetic */ WaitingThreadAborter val$aborter;
        final /* synthetic */ HttpRoute val$route;
        final /* synthetic */ Object val$state;

        AnonymousClass1(WaitingThreadAborter val$aborter, HttpRoute val$route, Object val$state) {
            this.val$aborter = val$aborter;
            this.val$route = val$route;
            this.val$state = val$state;
        }

        public void abortRequest() {
            ConnPoolByRoute.this.poolLock.lock();
            try {
                this.val$aborter.abort();
            } finally {
                ConnPoolByRoute.this.poolLock.unlock();
            }
        }

        public BasicPoolEntry getPoolEntry(long timeout, TimeUnit tunit) throws InterruptedException, ConnectionPoolTimeoutException {
            return ConnPoolByRoute.this.getEntryBlocking(this.val$route, this.val$state, timeout, tunit, this.val$aborter);
        }
    }

    public ConnPoolByRoute(ClientConnectionOperator operator, HttpParams params) {
        this.log = LogFactory.getLog(getClass());
        if (operator == null) {
            throw new IllegalArgumentException("Connection operator may not be null");
        }
        this.operator = operator;
        this.freeConnections = createFreeConnQueue();
        this.waitingThreads = createWaitingThreadQueue();
        this.routeToPool = createRouteToPoolMap();
        this.maxTotalConnections = ConnManagerParams.getMaxTotalConnections(params);
        this.connPerRoute = ConnManagerParams.getMaxConnectionsPerRoute(params);
    }

    protected Queue<BasicPoolEntry> createFreeConnQueue() {
        return new LinkedList();
    }

    protected Queue<WaitingThread> createWaitingThreadQueue() {
        return new LinkedList();
    }

    protected Map<HttpRoute, RouteSpecificPool> createRouteToPoolMap() {
        return new HashMap();
    }

    protected RouteSpecificPool newRouteSpecificPool(HttpRoute route) {
        return new RouteSpecificPool(route, this.connPerRoute.getMaxForRoute(route));
    }

    protected WaitingThread newWaitingThread(Condition cond, RouteSpecificPool rospl) {
        return new WaitingThread(cond, rospl);
    }

    protected RouteSpecificPool getRoutePool(HttpRoute route, boolean create) {
        this.poolLock.lock();
        try {
            RouteSpecificPool rospl = (RouteSpecificPool) this.routeToPool.get(route);
            if (rospl == null && create) {
                rospl = newRouteSpecificPool(route);
                this.routeToPool.put(route, rospl);
            }
            this.poolLock.unlock();
            return rospl;
        } catch (Throwable th) {
            this.poolLock.unlock();
        }
    }

    public int getConnectionsInPool(HttpRoute route) {
        int i = 0;
        this.poolLock.lock();
        try {
            RouteSpecificPool rospl = getRoutePool(route, false);
            if (rospl != null) {
                i = rospl.getEntryCount();
            }
            this.poolLock.unlock();
            return i;
        } catch (Throwable th) {
            this.poolLock.unlock();
        }
    }

    public PoolEntryRequest requestPoolEntry(HttpRoute route, Object state) {
        return new AnonymousClass1(new WaitingThreadAborter(), route, state);
    }

    protected BasicPoolEntry getEntryBlocking(HttpRoute route, Object state, long timeout, TimeUnit tunit, WaitingThreadAborter aborter) throws ConnectionPoolTimeoutException, InterruptedException {
        Date date = null;
        if (timeout > 0) {
            date = new Date(System.currentTimeMillis() + tunit.toMillis(timeout));
        }
        BasicPoolEntry entry = null;
        this.poolLock.lock();
        RouteSpecificPool rospl = getRoutePool(route, true);
        WaitingThread waitingThread = null;
        while (entry == null) {
            if (this.isShutDown) {
                throw new IllegalStateException("Connection pool shut down.");
            }
            if (this.log.isDebugEnabled()) {
                this.log.debug("Total connections kept alive: " + this.freeConnections.size());
                this.log.debug("Total issued connections: " + this.issuedConnections.size());
                this.log.debug("Total allocated connection: " + this.numConnections + " out of " + this.maxTotalConnections);
            }
            entry = getFreeEntry(rospl, state);
            if (entry != null) {
                break;
            }
            boolean hasCapacity = rospl.getCapacity() > 0;
            if (this.log.isDebugEnabled()) {
                this.log.debug("Available capacity: " + rospl.getCapacity() + " out of " + rospl.getMaxEntries() + " [" + route + "][" + state + "]");
            }
            if (hasCapacity && this.numConnections < this.maxTotalConnections) {
                entry = createEntry(rospl, this.operator);
            } else if (!hasCapacity || this.freeConnections.isEmpty()) {
                if (this.log.isDebugEnabled()) {
                    this.log.debug("Need to wait for connection [" + route + "][" + state + "]");
                }
                if (waitingThread == null) {
                    waitingThread = newWaitingThread(this.poolLock.newCondition(), rospl);
                    aborter.setWaitingThread(waitingThread);
                }
                try {
                    rospl.queueThread(waitingThread);
                    this.waitingThreads.add(waitingThread);
                    boolean success = waitingThread.await(date);
                    rospl.removeThread(waitingThread);
                    this.waitingThreads.remove(waitingThread);
                    if (!(success || date == null || date.getTime() > System.currentTimeMillis())) {
                        throw new ConnectionPoolTimeoutException("Timeout waiting for connection");
                    }
                } catch (Throwable th) {
                    this.poolLock.unlock();
                }
            } else {
                deleteLeastUsedEntry();
                entry = createEntry(rospl, this.operator);
            }
        }
        this.poolLock.unlock();
        return entry;
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

    protected BasicPoolEntry getFreeEntry(RouteSpecificPool rospl, Object state) {
        BasicPoolEntry entry = null;
        this.poolLock.lock();
        boolean done = false;
        while (!done) {
            entry = rospl.allocEntry(state);
            if (entry != null) {
                if (this.log.isDebugEnabled()) {
                    this.log.debug("Getting free connection [" + rospl.getRoute() + "][" + state + "]");
                }
                this.freeConnections.remove(entry);
                if (this.idleConnHandler.remove(entry.getConnection())) {
                    try {
                        this.issuedConnections.add(entry.getWeakRef());
                        done = true;
                    } catch (Throwable th) {
                        this.poolLock.unlock();
                    }
                } else {
                    if (this.log.isDebugEnabled()) {
                        this.log.debug("Closing expired free connection [" + rospl.getRoute() + "][" + state + "]");
                    }
                    closeConnection(entry.getConnection());
                    rospl.dropEntry();
                    this.numConnections--;
                }
            } else {
                done = true;
                if (this.log.isDebugEnabled()) {
                    this.log.debug("No free connections [" + rospl.getRoute() + "][" + state + "]");
                }
            }
        }
        this.poolLock.unlock();
        return entry;
    }

    protected BasicPoolEntry createEntry(RouteSpecificPool rospl, ClientConnectionOperator op) {
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

    protected void deleteEntry(BasicPoolEntry entry) {
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

    protected void deleteLeastUsedEntry() {
        try {
            this.poolLock.lock();
            BasicPoolEntry entry = (BasicPoolEntry) this.freeConnections.remove();
            if (entry != null) {
                deleteEntry(entry);
            } else if (this.log.isDebugEnabled()) {
                this.log.debug("No free connection to delete.");
            }
            this.poolLock.unlock();
        } catch (Throwable th) {
            this.poolLock.unlock();
        }
    }

    protected void handleLostEntry(HttpRoute route) {
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

    protected void notifyWaitingThread(RouteSpecificPool rospl) {
        WaitingThread waitingThread = null;
        this.poolLock.lock();
        if (rospl != null) {
            try {
                if (rospl.hasThread()) {
                    if (this.log.isDebugEnabled()) {
                        this.log.debug("Notifying thread waiting on pool [" + rospl.getRoute() + "]");
                    }
                    waitingThread = rospl.nextThread();
                    if (waitingThread != null) {
                        waitingThread.wakeup();
                    }
                    this.poolLock.unlock();
                }
            } catch (Throwable th) {
                this.poolLock.unlock();
            }
        }
        if (!this.waitingThreads.isEmpty()) {
            if (this.log.isDebugEnabled()) {
                this.log.debug("Notifying thread waiting on any pool");
            }
            waitingThread = (WaitingThread) this.waitingThreads.remove();
        } else if (this.log.isDebugEnabled()) {
            this.log.debug("Notifying no-one, there are no waiting threads");
        }
        if (waitingThread != null) {
            waitingThread.wakeup();
        }
        this.poolLock.unlock();
    }

    public void deleteClosedConnections() {
        this.poolLock.lock();
        try {
            Iterator<BasicPoolEntry> iter = this.freeConnections.iterator();
            while (iter.hasNext()) {
                BasicPoolEntry entry = (BasicPoolEntry) iter.next();
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
                BasicPoolEntry entry = (BasicPoolEntry) ibpe.next();
                ibpe.remove();
                closeConnection(entry.getConnection());
            }
            Iterator<WaitingThread> iwth = this.waitingThreads.iterator();
            while (iwth.hasNext()) {
                WaitingThread waiter = (WaitingThread) iwth.next();
                iwth.remove();
                waiter.wakeup();
            }
            this.routeToPool.clear();
        } finally {
            this.poolLock.unlock();
        }
    }
}
