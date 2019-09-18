package com.huawei.okhttp3;

import com.huawei.okhttp3.internal.Util;
import com.huawei.okhttp3.internal.connection.RealConnection;
import com.huawei.okhttp3.internal.connection.RouteDatabase;
import com.huawei.okhttp3.internal.connection.StreamAllocation;
import com.huawei.okhttp3.internal.platform.Platform;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;

public final class ConnectionPool {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static final Executor executor;
    private final Runnable cleanupRunnable;
    boolean cleanupRunning;
    private final Deque<RealConnection> connections;
    private final Deque<Http2Host> http2Hosts;
    private final long keepAliveDurationNs;
    private final Deque<WeakReference<Http2ConnectionEventListener>> listenerWrList;
    private final int maxIdleConnections;
    final RouteDatabase routeDatabase;

    public interface Http2ConnectionEventListener {
        void onEvicted(String str, int i, String str2);
    }

    static {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60, TimeUnit.SECONDS, new SynchronousQueue(), Util.threadFactory("OkHttp ConnectionPool", true));
        executor = threadPoolExecutor;
    }

    public ConnectionPool() {
        this(5, 5, TimeUnit.MINUTES);
    }

    public ConnectionPool(int maxIdleConnections2, long keepAliveDuration, TimeUnit timeUnit) {
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
        this.http2Hosts = new ArrayDeque();
        this.listenerWrList = new ArrayDeque();
        this.maxIdleConnections = maxIdleConnections2;
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

    /* access modifiers changed from: package-private */
    @Nullable
    public RealConnection get(Address address, StreamAllocation streamAllocation, Route route) {
        RealConnection h2Connection = getHttp2Connection(address);
        if (h2Connection != null) {
            streamAllocation.acquire(h2Connection, true);
            return h2Connection;
        }
        for (RealConnection connection : this.connections) {
            if (connection.isEligible(address, route)) {
                streamAllocation.acquire(connection, true);
                return connection;
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    @Nullable
    public Socket deduplicate(Address address, StreamAllocation streamAllocation, int connectionReserved) {
        int index = 0;
        for (RealConnection connection : this.connections) {
            if (connection.isEligible(address, null) && connection.isMultiplexed() && connection != streamAllocation.connection()) {
                index++;
                if (index == connectionReserved) {
                    return streamAllocation.releaseAndAcquire(connection);
                }
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public void put(RealConnection connection) {
        if (!this.cleanupRunning) {
            this.cleanupRunning = true;
            executor.execute(this.cleanupRunnable);
        }
        this.connections.add(connection);
        if (connection.isMultiplexed()) {
            addHttp2ConnectionHost(connection);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean connectionBecameIdle(RealConnection connection) {
        if (connection.noNewStreams || this.maxIdleConnections == 0) {
            this.connections.remove(connection);
            removeHttp2ConnectionHost(connection);
            return true;
        }
        notifyAll();
        return false;
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
                    removeHttp2ConnectionHost(connection);
                }
            }
        }
        for (RealConnection connection2 : evictedConnections) {
            Util.closeQuietly(connection2.socket());
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
                } else if (!connection.isMultiplexed() || now - connection.keepaliveTimestampNs >= 1000000000) {
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
            if (longestIdleDurationNs < this.keepAliveDurationNs) {
                if (idleConnectionCount <= this.maxIdleConnections) {
                    if (idleConnectionCount > 0) {
                        long j = this.keepAliveDurationNs - longestIdleDurationNs;
                        return j;
                    } else if (inUseConnectionCount > 0) {
                        long j2 = this.keepAliveDurationNs;
                        return j2;
                    } else {
                        this.cleanupRunning = false;
                        return -1;
                    }
                }
            }
            this.connections.remove(longestIdleConnection);
            removeHttp2ConnectionHost(longestIdleConnection);
            Util.closeQuietly(longestIdleConnection.socket());
            return 0;
        }
    }

    private int pruneAndGetAllocationCount(RealConnection connection, long now) {
        List<Reference<StreamAllocation>> references = connection.allocations;
        int i = 0;
        while (i < references.size()) {
            Reference<StreamAllocation> reference = references.get(i);
            if (reference.get() != null) {
                i++;
            } else {
                Platform.get().logCloseableLeak("A connection to " + connection.route().address().url() + " was leaked. Did you forget to close a response body?", ((StreamAllocation.StreamAllocationReference) reference).callStackTrace);
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
        if (connection != null && connection.isMultiplexed()) {
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
            if (address.equals(connection.route().address) && !connection.noNewStreams && connection.isMultiplexed()) {
                if (connection.successCount == 0 || connection.isHealthy(true)) {
                    count++;
                }
            }
        }
        return count;
    }

    public synchronized int http2ConnectionCount(String hostName, int port, String scheme) {
        int count;
        count = 0;
        for (RealConnection connection : this.connections) {
            if (connection.isMultiplexed() && hostName.equals(connection.route().address.url().host()) && port == connection.route().address.url().port() && scheme.equals(connection.route().address.url().scheme()) && !connection.noNewStreams) {
                if (connection.successCount == 0 || connection.isHealthy(true)) {
                    count++;
                }
            }
        }
        return count;
    }

    public synchronized boolean keepHttp2ConnectionAlive(String hostName, int port, String scheme) {
        for (RealConnection connection : this.connections) {
            if (connection.isMultiplexed() && hostName.equals(connection.route().address.url().host()) && port == connection.route().address.url().port() && scheme.equals(connection.route().address.url().scheme()) && !connection.noNewStreams && connection.isHealthy(true)) {
                connection.keepaliveTimestampNs = System.nanoTime();
                return true;
            }
        }
        return false;
    }

    private void onHttp2ConnectionEvictedEvent(Address address) {
        HttpUrl url = address.url();
        Iterator<WeakReference<Http2ConnectionEventListener>> itWr = this.listenerWrList.iterator();
        while (itWr.hasNext()) {
            Http2ConnectionEventListener listenerInList = (Http2ConnectionEventListener) itWr.next().get();
            if (listenerInList != null) {
                listenerInList.onEvicted(url.host(), url.port(), url.scheme());
            } else {
                itWr.remove();
            }
        }
    }

    public synchronized void addHttp2Listener(Http2ConnectionEventListener listener) {
        if (listener != null) {
            this.listenerWrList.add(new WeakReference<>(listener));
        }
    }

    public synchronized void removeHttp2Listener(Http2ConnectionEventListener listener) {
        Iterator<WeakReference<Http2ConnectionEventListener>> itWr = this.listenerWrList.iterator();
        while (itWr.hasNext()) {
            Http2ConnectionEventListener listenerInList = (Http2ConnectionEventListener) itWr.next().get();
            if (listenerInList == null || listener == listenerInList) {
                itWr.remove();
            }
        }
    }
}
