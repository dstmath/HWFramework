package com.huawei.okhttp3.internal.connection;

import com.huawei.android.telephony.SignalStrengthEx;
import com.huawei.okhttp3.Address;
import com.huawei.okhttp3.ConnectionPoolBase;
import com.huawei.okhttp3.Route;
import com.huawei.okhttp3.internal.Util;
import com.huawei.okhttp3.internal.connection.Transmitter;
import com.huawei.okhttp3.internal.platform.Platform;
import java.io.IOException;
import java.lang.ref.Reference;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;

public final class RealConnectionPool extends ConnectionPoolBase {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static final Executor executor = new ThreadPoolExecutor(0, (int) SignalStrengthEx.INVALID, 60, TimeUnit.SECONDS, new SynchronousQueue(), Util.threadFactory("OkHttp ConnectionPool", true));
    private final Runnable cleanupRunnable = new Runnable() {
        /* class com.huawei.okhttp3.internal.connection.$$Lambda$RealConnectionPool$YYoFKFzTMGlC5tlfbQ0P33U0aGs */

        @Override // java.lang.Runnable
        public final void run() {
            RealConnectionPool.this.lambda$new$0$RealConnectionPool();
        }
    };
    boolean cleanupRunning;
    private final long keepAliveDurationNs;
    private final int maxIdleConnections;
    final RouteDatabase routeDatabase = new RouteDatabase();

    public /* synthetic */ void lambda$new$0$RealConnectionPool() {
        while (true) {
            long waitNanos = cleanup(System.nanoTime());
            if (waitNanos != -1) {
                if (waitNanos > 0) {
                    long waitMillis = waitNanos / 1000000;
                    long waitNanos2 = waitNanos - (1000000 * waitMillis);
                    synchronized (this) {
                        try {
                            wait(waitMillis, (int) waitNanos2);
                        } catch (InterruptedException e) {
                        }
                    }
                }
            } else {
                return;
            }
        }
    }

    public RealConnectionPool(int maxIdleConnections2, long keepAliveDuration, TimeUnit timeUnit) {
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
            if (connection.transmitters.isEmpty()) {
                total++;
            }
        }
        return total;
    }

    public synchronized int connectionCount() {
        return this.connections.size();
    }

    /* access modifiers changed from: package-private */
    public boolean transmitterAcquirePooledConnection(Address address, Transmitter transmitter, @Nullable List<Route> routes, boolean requireMultiplexed) {
        for (RealConnection connection : this.connections) {
            if ((!requireMultiplexed || connection.isMultiplexed()) && connection.isEligible(address, routes)) {
                transmitter.acquireConnectionNoEvents(connection);
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void put(RealConnection connection) {
        if (!this.cleanupRunning) {
            this.cleanupRunning = true;
            executor.execute(this.cleanupRunnable);
        }
        this.connections.add(connection);
    }

    /* access modifiers changed from: package-private */
    public boolean connectionBecameIdle(RealConnection connection) {
        if (connection.noNewExchanges || this.maxIdleConnections == 0) {
            this.connections.remove(connection);
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
                if (connection.transmitters.isEmpty()) {
                    connection.noNewExchanges = true;
                    evictedConnections.add(connection);
                    i.remove();
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
                        return this.keepAliveDurationNs - longestIdleDurationNs;
                    } else if (inUseConnectionCount > 0) {
                        return this.keepAliveDurationNs;
                    } else {
                        this.cleanupRunning = false;
                        return -1;
                    }
                }
            }
            this.connections.remove(longestIdleConnection);
            Util.closeQuietly(longestIdleConnection.socket());
            return 0;
        }
    }

    private int pruneAndGetAllocationCount(RealConnection connection, long now) {
        List<Reference<Transmitter>> references = connection.transmitters;
        int i = 0;
        while (i < references.size()) {
            Reference<Transmitter> reference = references.get(i);
            if (reference.get() != null) {
                i++;
            } else {
                Platform.get().logCloseableLeak("A connection to " + connection.route().address().url() + " was leaked. Did you forget to close a response body?", ((Transmitter.TransmitterReference) reference).callStackTrace);
                references.remove(i);
                connection.noNewExchanges = true;
                if (references.isEmpty()) {
                    connection.idleAtNanos = now - this.keepAliveDurationNs;
                    return 0;
                }
            }
        }
        return references.size();
    }

    public void connectFailed(Route failedRoute, IOException failure) {
        if (failedRoute.proxy().type() != Proxy.Type.DIRECT) {
            Address address = failedRoute.address();
            address.proxySelector().connectFailed(address.url().uri(), failedRoute.proxy().address(), failure);
        }
        this.routeDatabase.failed(failedRoute);
    }
}
