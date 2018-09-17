package com.huawei.okhttp3;

import com.huawei.okhttp3.internal.Util;
import com.huawei.okhttp3.internal.connection.RealConnection;
import com.huawei.okhttp3.internal.connection.RouteDatabase;
import com.huawei.okhttp3.internal.connection.StreamAllocation;
import com.huawei.okhttp3.internal.connection.StreamAllocation.StreamAllocationReference;
import com.huawei.okhttp3.internal.platform.Platform;
import java.io.Closeable;
import java.lang.ref.Reference;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class ConnectionPool {
    static final /* synthetic */ boolean -assertionsDisabled = (ConnectionPool.class.desiredAssertionStatus() ^ 1);
    private static final Executor executor = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60, TimeUnit.SECONDS, new SynchronousQueue(), Util.threadFactory("OkHttp ConnectionPool", true));
    private final Runnable cleanupRunnable;
    boolean cleanupRunning;
    private final Deque<RealConnection> connections;
    public final Object h2AvailableLock;
    public boolean h2ConnectionIsCreating;
    private final Deque<Http2Host> http2Hosts;
    private final long keepAliveDurationNs;
    private final Deque<Http2ConnectionEventListener> listenerList;
    private final int maxIdleConnections;
    final RouteDatabase routeDatabase;

    public interface Http2ConnectionEventListener {
        void onEvicted(String str, int i, String str2);
    }

    public ConnectionPool() {
        this(5, 5, TimeUnit.MINUTES);
    }

    public ConnectionPool(int maxIdleConnections, long keepAliveDuration, TimeUnit timeUnit) {
        this.cleanupRunnable = new Runnable() {
            public void run() {
                while (true) {
                    long waitNanos = ConnectionPool.this.cleanup(System.nanoTime());
                    if (waitNanos != -1) {
                        if (waitNanos > 0) {
                            long waitMillis = waitNanos / 1000000;
                            waitNanos -= waitMillis * 1000000;
                            synchronized (ConnectionPool.this) {
                                try {
                                    ConnectionPool.this.wait(waitMillis, (int) waitNanos);
                                } catch (InterruptedException e) {
                                }
                            }
                        }
                    } else {
                        return;
                    }
                }
            }
        };
        this.connections = new ArrayDeque();
        this.routeDatabase = new RouteDatabase();
        this.http2Hosts = new ArrayDeque();
        this.listenerList = new ArrayDeque();
        this.h2AvailableLock = new Object();
        this.h2ConnectionIsCreating = false;
        this.maxIdleConnections = maxIdleConnections;
        this.keepAliveDurationNs = timeUnit.toNanos(keepAliveDuration);
        if (keepAliveDuration <= 0) {
            throw new IllegalArgumentException("keepAliveDuration <= 0: " + keepAliveDuration);
        }
    }

    public synchronized int idleConnectionCount() {
        int total;
        total = 0;
        for (RealConnection connection : this.connections) {
            if (connection.allocations.isEmpty()) {
                total++;
            }
        }
        return total;
    }

    public synchronized int connectionCount() {
        return this.connections.size();
    }

    RealConnection get(Address address, StreamAllocation streamAllocation) {
        if (-assertionsDisabled || Thread.holdsLock(this)) {
            RealConnection h2Connection = getHttp2Connection(address);
            if (h2Connection != null) {
                streamAllocation.acquire(h2Connection);
                return h2Connection;
            }
            for (RealConnection connection : this.connections) {
                if (connection.isEligible(address)) {
                    streamAllocation.acquire(connection);
                    return connection;
                }
            }
            return null;
        }
        throw new AssertionError();
    }

    Closeable deduplicate(Address address, StreamAllocation streamAllocation, int connectionReserved) {
        if (-assertionsDisabled || Thread.holdsLock(this)) {
            int index = 0;
            for (RealConnection connection : this.connections) {
                if (connection.isEligible(address) && connection.isMultiplexed() && connection != streamAllocation.connection()) {
                    index++;
                    if (index == connectionReserved) {
                        return streamAllocation.releaseAndAcquire(connection);
                    }
                }
            }
            return null;
        }
        throw new AssertionError();
    }

    void put(RealConnection connection) {
        if (-assertionsDisabled || Thread.holdsLock(this)) {
            if (!this.cleanupRunning) {
                this.cleanupRunning = true;
                executor.execute(this.cleanupRunnable);
            }
            this.connections.add(connection);
            if (connection.http2Connection != null) {
                addHttp2ConnectionHost(connection);
                return;
            }
            return;
        }
        throw new AssertionError();
    }

    boolean connectionBecameIdle(RealConnection connection) {
        if (!-assertionsDisabled && !Thread.holdsLock(this)) {
            throw new AssertionError();
        } else if (connection.noNewStreams || this.maxIdleConnections == 0) {
            this.connections.remove(connection);
            removeHttp2ConnectionHost(connection);
            return true;
        } else {
            notifyAll();
            return false;
        }
    }

    public void evictAll() {
        List<okhttp3.internal.connection.RealConnection> evictedConnections = new ArrayList();
        synchronized (this) {
            Iterator<okhttp3.internal.connection.RealConnection> i = this.connections.iterator();
            while (i.hasNext()) {
                RealConnection connection = (RealConnection) i.next();
                if (connection.allocations.isEmpty()) {
                    connection.noNewStreams = true;
                    evictedConnections.add(connection);
                    i.remove();
                    removeHttp2ConnectionHost(connection);
                }
            }
        }
        Iterator connection$iterator = evictedConnections.iterator();
        while (connection$iterator.hasNext()) {
            Util.closeQuietly(((RealConnection) connection$iterator.next()).socket());
        }
    }

    long cleanup(long now) {
        int inUseConnectionCount = 0;
        int idleConnectionCount = 0;
        RealConnection longestIdleConnection = null;
        long longestIdleDurationNs = Long.MIN_VALUE;
        synchronized (this) {
            for (RealConnection connection : this.connections) {
                if (pruneAndGetAllocationCount(connection, now) > 0) {
                    inUseConnectionCount++;
                } else if (connection.http2Connection == null || now - connection.keepaliveTimestampNs >= RealConnection.maxReserveDurationNs) {
                    idleConnectionCount++;
                    long idleDurationNs = now - connection.idleAtNanos;
                    if (idleDurationNs > longestIdleDurationNs) {
                        longestIdleDurationNs = idleDurationNs;
                        longestIdleConnection = connection;
                    }
                } else {
                    inUseConnectionCount++;
                }
            }
            long j;
            if (longestIdleDurationNs >= this.keepAliveDurationNs || idleConnectionCount > this.maxIdleConnections) {
                this.connections.remove(longestIdleConnection);
                removeHttp2ConnectionHost(longestIdleConnection);
                Util.closeQuietly(longestIdleConnection.socket());
                return 0;
            } else if (idleConnectionCount > 0) {
                j = this.keepAliveDurationNs - longestIdleDurationNs;
                return j;
            } else if (inUseConnectionCount > 0) {
                j = this.keepAliveDurationNs;
                return j;
            } else {
                this.cleanupRunning = false;
                return -1;
            }
        }
    }

    private int pruneAndGetAllocationCount(RealConnection connection, long now) {
        List<Reference<okhttp3.internal.connection.StreamAllocation>> references = connection.allocations;
        int i = 0;
        while (i < references.size()) {
            Reference<okhttp3.internal.connection.StreamAllocation> reference = (Reference) references.get(i);
            if (reference.get() != null) {
                i++;
            } else {
                StreamAllocationReference streamAllocRef = (StreamAllocationReference) reference;
                Platform.get().logCloseableLeak("A connection to " + connection.route().address().url() + " was leaked. Did you forget to close a response body?", streamAllocRef.callStackTrace);
                references.remove(i);
                connection.noNewStreams = true;
                if (references.isEmpty()) {
                    connection.idleAtNanos = now - this.keepAliveDurationNs;
                    return 0;
                }
            }
        }
        return references.size();
    }

    private Http2Host getHttp2Host(Address address) {
        for (Http2Host h : this.http2Hosts) {
            if (address.equals(h.address())) {
                return h;
            }
        }
        return null;
    }

    private RealConnection getHttp2Connection(Address address) {
        Http2Host http2Host = getHttp2Host(address);
        if (http2Host != null) {
            return http2Host.getAvailableConnection();
        }
        return null;
    }

    public synchronized void addHttp2ConnectionHost(RealConnection connection) {
        Http2Host http2Host = getHttp2Host(connection.route().address());
        if (http2Host == null) {
            http2Host = new Http2Host(connection.route().address());
            this.http2Hosts.push(http2Host);
        }
        http2Host.addConnection(connection);
    }

    private void removeHttp2ConnectionHost(RealConnection connection) {
        if (connection.http2Connection != null) {
            Http2Host http2Host = getHttp2Host(connection.route().address());
            if (http2Host != null) {
                http2Host.removeConnection(connection);
                if (http2Host.isEmpty()) {
                    this.http2Hosts.remove(http2Host);
                    onHttp2ConnectionEvictedEvent(connection.route().address());
                }
            }
        }
    }

    public synchronized int http2ConnectionCount(Address address) {
        int count;
        count = 0;
        for (RealConnection connection : this.connections) {
            if (!(!address.equals(connection.route().address) || (connection.noNewStreams ^ 1) == 0 || connection.http2Connection == null)) {
                if (connection.successCount == 0 || connection.isHealthy(true)) {
                    count++;
                }
            }
        }
        return count;
    }

    public int http2ConnectionCount(String hostName, int port, String scheme) {
        int count = 0;
        for (RealConnection connection : this.connections) {
            if (connection.http2Connection != null && hostName.equals(connection.route().address.url().host()) && port == connection.route().address.url().port() && scheme.equals(connection.route().address.url().scheme()) && (connection.noNewStreams ^ 1) != 0) {
                if (connection.successCount == 0 || connection.isHealthy(true)) {
                    count++;
                }
            }
        }
        return count;
    }

    public boolean keepHttp2ConnectionAlive(String hostName, int port, String scheme) {
        for (RealConnection connection : this.connections) {
            if (connection.http2Connection != null && hostName.equals(connection.route().address.url().host()) && port == connection.route().address.url().port() && scheme.equals(connection.route().address.url().scheme()) && (connection.noNewStreams ^ 1) != 0 && connection.isHealthy(true)) {
                connection.keepaliveTimestampNs = System.nanoTime();
                return true;
            }
        }
        return false;
    }

    private void onHttp2ConnectionEvictedEvent(Address address) {
        HttpUrl url = address.url();
        for (Http2ConnectionEventListener listener : this.listenerList) {
            listener.onEvicted(url.host(), url.port(), url.scheme());
        }
    }

    public synchronized void addHttp2Listener(Http2ConnectionEventListener listener) {
        if (listener != null) {
            if (!this.listenerList.contains(listener)) {
                this.listenerList.add(listener);
            }
        }
    }

    public synchronized void removeHttp2Listener(Http2ConnectionEventListener listener) {
        this.listenerList.remove(listener);
    }
}
