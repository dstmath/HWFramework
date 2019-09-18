package com.android.okhttp;

import com.android.okhttp.internal.RouteDatabase;
import com.android.okhttp.internal.Util;
import com.android.okhttp.internal.http.StreamAllocation;
import com.android.okhttp.internal.io.RealConnection;
import java.lang.ref.Reference;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class ConnectionPool {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static final long DEFAULT_KEEP_ALIVE_DURATION_MS = 300000;
    private static final ConnectionPool systemDefault;
    private Runnable cleanupRunnable;
    private final Deque<RealConnection> connections;
    private final Executor executor;
    private final long keepAliveDurationNs;
    private final int maxIdleConnections;
    final RouteDatabase routeDatabase;

    static {
        long keepAliveDurationMs;
        String keepAlive = System.getProperty("http.keepAlive");
        String keepAliveDuration = System.getProperty("http.keepAliveDuration");
        String maxIdleConnections2 = System.getProperty("http.maxConnections");
        if (keepAliveDuration != null) {
            keepAliveDurationMs = Long.parseLong(keepAliveDuration);
        } else {
            keepAliveDurationMs = DEFAULT_KEEP_ALIVE_DURATION_MS;
        }
        if (keepAlive != null && !Boolean.parseBoolean(keepAlive)) {
            systemDefault = new ConnectionPool(0, keepAliveDurationMs);
        } else if (maxIdleConnections2 != null) {
            systemDefault = new ConnectionPool(Integer.parseInt(maxIdleConnections2), keepAliveDurationMs);
        } else {
            systemDefault = new ConnectionPool(5, keepAliveDurationMs);
        }
    }

    public ConnectionPool(int maxIdleConnections2, long keepAliveDurationMs) {
        this(maxIdleConnections2, keepAliveDurationMs, TimeUnit.MILLISECONDS);
    }

    public ConnectionPool(int maxIdleConnections2, long keepAliveDuration, TimeUnit timeUnit) {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(0, 1, 60, TimeUnit.SECONDS, new LinkedBlockingQueue(), Util.threadFactory("OkHttp ConnectionPool", true));
        this.executor = threadPoolExecutor;
        this.cleanupRunnable = new Runnable() {
            public void run() {
                while (true) {
                    long waitNanos = ConnectionPool.this.cleanup(System.nanoTime());
                    if (waitNanos != -1) {
                        if (waitNanos > 0) {
                            long waitMillis = waitNanos / 1000000;
                            long waitNanos2 = waitNanos - (1000000 * waitMillis);
                            synchronized (ConnectionPool.this) {
                                try {
                                    ConnectionPool.this.wait(waitMillis, (int) waitNanos2);
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
        this.maxIdleConnections = maxIdleConnections2;
        this.keepAliveDurationNs = timeUnit.toNanos(keepAliveDuration);
        if (keepAliveDuration <= 0) {
            throw new IllegalArgumentException("keepAliveDuration <= 0: " + keepAliveDuration);
        }
    }

    public static ConnectionPool getDefault() {
        return systemDefault;
    }

    public synchronized int getIdleConnectionCount() {
        int total;
        total = 0;
        for (RealConnection connection : this.connections) {
            if (connection.allocations.isEmpty()) {
                total++;
            }
        }
        return total;
    }

    public synchronized int getConnectionCount() {
        return this.connections.size();
    }

    @Deprecated
    public synchronized int getSpdyConnectionCount() {
        return getMultiplexedConnectionCount();
    }

    public synchronized int getMultiplexedConnectionCount() {
        int total;
        total = 0;
        for (RealConnection connection : this.connections) {
            if (connection.isMultiplexed()) {
                total++;
            }
        }
        return total;
    }

    public synchronized int getHttpConnectionCount() {
        return this.connections.size() - getMultiplexedConnectionCount();
    }

    /* access modifiers changed from: package-private */
    public RealConnection get(Address address, StreamAllocation streamAllocation) {
        for (RealConnection connection : this.connections) {
            if (connection.allocations.size() < connection.allocationLimit() && address.equals(connection.getRoute().address) && !connection.noNewStreams) {
                streamAllocation.acquire(connection);
                return connection;
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public void put(RealConnection connection) {
        if (this.connections.isEmpty()) {
            this.executor.execute(this.cleanupRunnable);
        }
        this.connections.add(connection);
    }

    /* access modifiers changed from: package-private */
    public boolean connectionBecameIdle(RealConnection connection) {
        if (connection.noNewStreams || this.maxIdleConnections == 0) {
            this.connections.remove(connection);
            return true;
        }
        notifyAll();
        return $assertionsDisabled;
    }

    public void evictAll() {
        List<RealConnection> evictedConnections = new ArrayList<>();
        synchronized (this) {
            Iterator<RealConnection> i = this.connections.iterator();
            while (i.hasNext()) {
                RealConnection connection = i.next();
                if (connection.allocations.isEmpty()) {
                    connection.noNewStreams = true;
                    evictedConnections.add(connection);
                    i.remove();
                }
            }
        }
        for (RealConnection connection2 : evictedConnections) {
            Util.closeQuietly(connection2.getSocket());
        }
    }

    /* access modifiers changed from: package-private */
    public long cleanup(long now) {
        int inUseConnectionCount = 0;
        int idleConnectionCount = 0;
        RealConnection longestIdleConnection = null;
        long longestIdleDurationNs = Long.MIN_VALUE;
        synchronized (this) {
            for (RealConnection connection : this.connections) {
                if (pruneAndGetAllocationCount(connection, now) > 0) {
                    inUseConnectionCount++;
                } else {
                    idleConnectionCount++;
                    long idleDurationNs = now - connection.idleAtNanos;
                    if (idleDurationNs > longestIdleDurationNs) {
                        longestIdleDurationNs = idleDurationNs;
                        longestIdleConnection = connection;
                    }
                }
            }
            if (longestIdleDurationNs < this.keepAliveDurationNs) {
                if (idleConnectionCount <= this.maxIdleConnections) {
                    if (idleConnectionCount > 0) {
                        long j = this.keepAliveDurationNs - longestIdleDurationNs;
                        return j;
                    } else if (inUseConnectionCount <= 0) {
                        return -1;
                    } else {
                        long j2 = this.keepAliveDurationNs;
                        return j2;
                    }
                }
            }
            this.connections.remove(longestIdleConnection);
            Util.closeQuietly(longestIdleConnection.getSocket());
            return 0;
        }
    }

    private int pruneAndGetAllocationCount(RealConnection connection, long now) {
        List<Reference<StreamAllocation>> references = connection.allocations;
        int i = 0;
        while (i < references.size()) {
            if (references.get(i).get() != null) {
                i++;
            } else {
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

    /* access modifiers changed from: package-private */
    public void setCleanupRunnableForTest(Runnable cleanupRunnable2) {
        this.cleanupRunnable = cleanupRunnable2;
    }
}
