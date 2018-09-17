package com.huawei.okhttp3.internal.connection;

import com.huawei.okhttp3.Address;
import com.huawei.okhttp3.ConnectionPool;
import com.huawei.okhttp3.OkHttpClient;
import com.huawei.okhttp3.Request;
import com.huawei.okhttp3.Route;
import com.huawei.okhttp3.internal.Internal;
import com.huawei.okhttp3.internal.Util;
import com.huawei.okhttp3.internal.http.HttpCodec;
import com.huawei.okhttp3.internal.http1.Http1Codec;
import com.huawei.okhttp3.internal.http2.ConnectionShutdownException;
import com.huawei.okhttp3.internal.http2.ErrorCode;
import com.huawei.okhttp3.internal.http2.Http2Codec;
import com.huawei.okhttp3.internal.http2.StreamResetException;
import java.io.Closeable;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

public final class StreamAllocation {
    static final /* synthetic */ boolean -assertionsDisabled = (StreamAllocation.class.desiredAssertionStatus() ^ 1);
    private static final int HTTP2_CONNECTION_WAIT_TIME = 1000;
    public final Address address;
    private final Object callStackTrace;
    private boolean canceled;
    private HttpCodec codec;
    private RealConnection connection;
    private final ConnectionPool connectionPool;
    private boolean http2Indicator = false;
    private int refusedStreamCount;
    private boolean released;
    private Route route;
    private final RouteSelector routeSelector;

    public static final class StreamAllocationReference extends WeakReference<StreamAllocation> {
        public final Object callStackTrace;

        StreamAllocationReference(StreamAllocation referent, Object callStackTrace) {
            super(referent);
            this.callStackTrace = callStackTrace;
        }
    }

    public StreamAllocation(ConnectionPool connectionPool, Address address, Object callStackTrace) {
        this.connectionPool = connectionPool;
        this.address = address;
        this.routeSelector = new RouteSelector(address, routeDatabase());
        this.callStackTrace = callStackTrace;
    }

    public void newHttp2Connection(OkHttpClient client, Request request) throws IOException {
        int connectTimeout = client.connectTimeoutMillis();
        int readTimeout = client.readTimeoutMillis();
        int writeTimeout = client.writeTimeoutMillis();
        boolean connectionRetryEnabled = client.retryOnConnectionFailure();
        if (Integer.parseInt(request.header("Http2ConnectionIndex")) > this.connectionPool.http2ConnectionCount(this.address)) {
            Route selectedRoute = this.route;
            if (selectedRoute == null) {
                selectedRoute = this.routeSelector.next();
                this.route = selectedRoute;
            }
            RealConnection newConnection = new RealConnection(selectedRoute);
            synchronized (this.connectionPool) {
                Internal.instance.put(this.connectionPool, newConnection);
            }
            newConnection.connect(connectTimeout, readTimeout, writeTimeout, this.address.connectionSpecs(), connectionRetryEnabled);
            routeDatabase().connected(newConnection.route());
        }
    }

    public HttpCodec newStream(OkHttpClient client, boolean doExtensiveHealthChecks) {
        int connectTimeout = client.connectTimeoutMillis();
        int readTimeout = client.readTimeoutMillis();
        int writeTimeout = client.writeTimeoutMillis();
        try {
            HttpCodec resultCodec;
            RealConnection resultConnection = findHealthyConnection(client, connectTimeout, readTimeout, writeTimeout, client.retryOnConnectionFailure(), doExtensiveHealthChecks);
            if (resultConnection.http2Connection != null) {
                resultCodec = new Http2Codec(client, this, resultConnection.http2Connection);
            } else {
                resultConnection.socket().setSoTimeout(readTimeout);
                resultConnection.source.timeout().timeout((long) readTimeout, TimeUnit.MILLISECONDS);
                resultConnection.sink.timeout().timeout((long) writeTimeout, TimeUnit.MILLISECONDS);
                resultCodec = new Http1Codec(client, this, resultConnection.source, resultConnection.sink);
            }
            synchronized (this.connectionPool) {
                this.codec = resultCodec;
            }
            return resultCodec;
        } catch (IOException e) {
            throw new RouteException(e);
        }
    }

    /* JADX WARNING: Missing block: B:9:0x0012, code:
            if (r0.isHealthy(r9) != false) goto L_0x001b;
     */
    /* JADX WARNING: Missing block: B:14:0x001b, code:
            return r0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private RealConnection findHealthyConnection(OkHttpClient client, int connectTimeout, int readTimeout, int writeTimeout, boolean connectionRetryEnabled, boolean doExtensiveHealthChecks) throws IOException {
        while (true) {
            RealConnection candidate = findConnection(client, connectTimeout, readTimeout, writeTimeout, connectionRetryEnabled);
            synchronized (this.connectionPool) {
                if (candidate.successCount == 0) {
                    return candidate;
                }
            }
            noNewStreams();
        }
    }

    /* JADX WARNING: Missing block: B:36:0x004e, code:
            if (r13.http2Indicator == false) goto L_0x0081;
     */
    /* JADX WARNING: Missing block: B:38:0x0058, code:
            if (r13.connectionPool.http2ConnectionCount(r13.address) != 0) goto L_0x0081;
     */
    /* JADX WARNING: Missing block: B:39:0x005a, code:
            r11 = false;
            r2 = r13.connectionPool.h2AvailableLock;
     */
    /* JADX WARNING: Missing block: B:40:0x005f, code:
            monitor-enter(r2);
     */
    /* JADX WARNING: Missing block: B:43:0x0064, code:
            if (r13.connectionPool.h2ConnectionIsCreating == false) goto L_0x0078;
     */
    /* JADX WARNING: Missing block: B:45:?, code:
            r13.connectionPool.h2AvailableLock.wait(1000);
     */
    /* JADX WARNING: Missing block: B:46:0x006f, code:
            r11 = true;
     */
    /* JADX WARNING: Missing block: B:52:?, code:
            r13.connectionPool.h2ConnectionIsCreating = true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private RealConnection findConnection(OkHttpClient client, int connectTimeout, int readTimeout, int writeTimeout, boolean connectionRetryEnabled) throws IOException {
        Route selectedRoute;
        synchronized (this.connectionPool) {
            if (this.released) {
                throw new IllegalStateException("released");
            } else if (this.codec != null) {
                throw new IllegalStateException("codec != null");
            } else if (this.canceled) {
                throw new IOException("Canceled");
            } else {
                RealConnection allocatedConnection = this.connection;
                if (allocatedConnection == null || (allocatedConnection.noNewStreams ^ 1) == 0) {
                    RealConnection pooledConnection = Internal.instance.get(this.connectionPool, this.address, this);
                    if (pooledConnection != null) {
                        this.connection = pooledConnection;
                        return pooledConnection;
                    }
                    selectedRoute = this.route;
                } else {
                    return allocatedConnection;
                }
            }
        }
        RealConnection newConnection;
        if (reFindConnection) {
            return findConnection(client, connectTimeout, readTimeout, writeTimeout, connectionRetryEnabled);
        }
        if (selectedRoute == null) {
            selectedRoute = this.routeSelector.next();
        }
        synchronized (this.connectionPool) {
            this.route = selectedRoute;
            this.refusedStreamCount = 0;
            newConnection = new RealConnection(selectedRoute);
            acquire(newConnection);
        }
        if (this.canceled) {
            if (this.http2Indicator) {
                synchronized (this.connectionPool.h2AvailableLock) {
                    this.connectionPool.h2ConnectionIsCreating = false;
                    this.connectionPool.h2AvailableLock.notifyAll();
                }
            }
            throw new IOException("Canceled");
        }
        Closeable closeable = null;
        try {
            newConnection.connect(connectTimeout, readTimeout, writeTimeout, this.address.connectionSpecs(), connectionRetryEnabled);
            routeDatabase().connected(newConnection.route());
            synchronized (this.connectionPool) {
                Internal.instance.put(this.connectionPool, newConnection);
                if (newConnection.http2Connection != null) {
                    closeable = Internal.instance.deduplicate(client, this.connectionPool, this.address, this);
                    newConnection = this.connection;
                }
            }
            if (this.http2Indicator) {
                synchronized (this.connectionPool.h2AvailableLock) {
                    this.connectionPool.h2ConnectionIsCreating = false;
                    this.connectionPool.h2AvailableLock.notifyAll();
                }
            }
            Util.closeQuietly(closeable);
            return newConnection;
        } catch (Exception e) {
            try {
                throw e;
            } catch (Throwable th) {
                if (this.http2Indicator) {
                    synchronized (this.connectionPool.h2AvailableLock) {
                        this.connectionPool.h2ConnectionIsCreating = false;
                        this.connectionPool.h2AvailableLock.notifyAll();
                    }
                }
            }
        }
    }

    public void streamFinished(boolean noNewStreams, HttpCodec codec) {
        Closeable closeable;
        synchronized (this.connectionPool) {
            if (codec != null) {
                if (codec == this.codec) {
                    if (!noNewStreams) {
                        RealConnection realConnection = this.connection;
                        realConnection.successCount++;
                    }
                    closeable = deallocate(noNewStreams, false, true);
                }
            }
            throw new IllegalStateException("expected " + this.codec + " but was " + codec);
        }
        Util.closeQuietly(closeable);
    }

    public HttpCodec codec() {
        HttpCodec httpCodec;
        synchronized (this.connectionPool) {
            httpCodec = this.codec;
        }
        return httpCodec;
    }

    private RouteDatabase routeDatabase() {
        return Internal.instance.routeDatabase(this.connectionPool);
    }

    public synchronized RealConnection connection() {
        return this.connection;
    }

    public void release() {
        Closeable closeable;
        synchronized (this.connectionPool) {
            closeable = deallocate(false, true, false);
        }
        Util.closeQuietly(closeable);
    }

    public void noNewStreams() {
        Closeable closeable;
        synchronized (this.connectionPool) {
            closeable = deallocate(true, false, false);
        }
        Util.closeQuietly(closeable);
    }

    private Closeable deallocate(boolean noNewStreams, boolean released, boolean streamFinished) {
        if (-assertionsDisabled || Thread.holdsLock(this.connectionPool)) {
            Closeable closeable = null;
            if (streamFinished) {
                this.codec = null;
            }
            if (released) {
                this.released = true;
            }
            if (this.connection != null) {
                if (noNewStreams) {
                    this.connection.noNewStreams = true;
                }
                if (this.codec == null && (this.released || this.connection.noNewStreams)) {
                    release(this.connection);
                    if (this.connection.allocations.isEmpty()) {
                        this.connection.idleAtNanos = System.nanoTime();
                        if (Internal.instance.connectionBecameIdle(this.connectionPool, this.connection)) {
                            closeable = this.connection.socket();
                        }
                    }
                    this.connection = null;
                }
            }
            return closeable;
        }
        throw new AssertionError();
    }

    public void cancel() {
        HttpCodec codecToCancel;
        RealConnection connectionToCancel;
        synchronized (this.connectionPool) {
            this.canceled = true;
            codecToCancel = this.codec;
            connectionToCancel = this.connection;
        }
        if (codecToCancel != null) {
            codecToCancel.cancel();
        } else if (connectionToCancel != null) {
            connectionToCancel.cancel();
        }
    }

    public void streamFailed(IOException e) {
        Closeable closeable;
        boolean noNewStreams = false;
        synchronized (this.connectionPool) {
            if (e instanceof StreamResetException) {
                StreamResetException streamResetException = (StreamResetException) e;
                if (streamResetException.errorCode == ErrorCode.REFUSED_STREAM) {
                    this.refusedStreamCount++;
                }
                if (streamResetException.errorCode != ErrorCode.REFUSED_STREAM || this.refusedStreamCount > 1) {
                    noNewStreams = true;
                    this.route = null;
                }
            } else if (this.connection != null && (!this.connection.isMultiplexed() || (e instanceof ConnectionShutdownException))) {
                noNewStreams = true;
                if (this.connection.successCount == 0) {
                    if (!(this.route == null || e == null)) {
                        this.routeSelector.connectFailed(this.route, e);
                    }
                    this.route = null;
                }
            }
            closeable = deallocate(noNewStreams, false, true);
        }
        Util.closeQuietly(closeable);
    }

    public void acquire(RealConnection connection) {
        if (!-assertionsDisabled && !Thread.holdsLock(this.connectionPool)) {
            throw new AssertionError();
        } else if (this.connection == null || (this.connection.noNewStreams ^ 1) == 0) {
            this.connection = connection;
            connection.allocations.add(new StreamAllocationReference(this, this.callStackTrace));
        } else {
            throw new IllegalStateException();
        }
    }

    private void release(RealConnection connection) {
        int size = connection.allocations.size();
        for (int i = 0; i < size; i++) {
            if (((Reference) connection.allocations.get(i)).get() == this) {
                connection.allocations.remove(i);
                return;
            }
        }
        throw new IllegalStateException();
    }

    public Closeable releaseAndAcquire(RealConnection newConnection) {
        if (!-assertionsDisabled && !Thread.holdsLock(this.connectionPool)) {
            throw new AssertionError();
        } else if (this.codec == null && this.connection.allocations.size() == 1) {
            Reference<okhttp3.internal.connection.StreamAllocation> onlyAllocation = (Reference) this.connection.allocations.get(0);
            Closeable closeable = deallocate(true, false, false);
            this.connection = newConnection;
            newConnection.allocations.add(onlyAllocation);
            return closeable;
        } else {
            throw new IllegalStateException();
        }
    }

    public boolean hasMoreRoutes() {
        return this.route == null ? this.routeSelector.hasNext() : true;
    }

    public void setHttp2Indicator() {
        this.http2Indicator = true;
    }

    public String toString() {
        return this.address.toString();
    }
}
