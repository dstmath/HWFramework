package com.huawei.okhttp3.internal.connection;

import com.huawei.okhttp3.Address;
import com.huawei.okhttp3.Call;
import com.huawei.okhttp3.Connection;
import com.huawei.okhttp3.ConnectionPool;
import com.huawei.okhttp3.EventListener;
import com.huawei.okhttp3.Interceptor;
import com.huawei.okhttp3.OkHttpClient;
import com.huawei.okhttp3.Request;
import com.huawei.okhttp3.Route;
import com.huawei.okhttp3.internal.Internal;
import com.huawei.okhttp3.internal.Util;
import com.huawei.okhttp3.internal.connection.RouteSelector;
import com.huawei.okhttp3.internal.http.HttpCodec;
import com.huawei.okhttp3.internal.http2.ConnectionShutdownException;
import com.huawei.okhttp3.internal.http2.ErrorCode;
import com.huawei.okhttp3.internal.http2.StreamResetException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.Socket;

public final class StreamAllocation {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    public final Address address;
    public final Call call;
    private final Object callStackTrace;
    private boolean canceled;
    private HttpCodec codec;
    private RealConnection connection;
    private final int connectionAttemptDelay;
    private final ConnectionPool connectionPool;
    public final EventListener eventListener;
    private int refusedStreamCount;
    private boolean released;
    private boolean reportedAcquired;
    private Route route;
    private RouteSelector.Selection routeSelection;
    private final RouteSelector routeSelector;

    public static final class StreamAllocationReference extends WeakReference<StreamAllocation> {
        public final Object callStackTrace;

        StreamAllocationReference(StreamAllocation referent, Object callStackTrace2) {
            super(referent);
            this.callStackTrace = callStackTrace2;
        }
    }

    public StreamAllocation(ConnectionPool connectionPool2, Address address2, Call call2, EventListener eventListener2, Object callStackTrace2, int connectionAttemptDelay2) {
        this.connectionPool = connectionPool2;
        this.address = address2;
        this.routeSelector = new RouteSelector(address2, routeDatabase(), call2, eventListener2);
        this.call = call2;
        this.eventListener = eventListener2;
        this.callStackTrace = callStackTrace2;
        this.connectionAttemptDelay = connectionAttemptDelay2;
    }

    public StreamAllocation(ConnectionPool connectionPool2, Address address2, Call call2, EventListener eventListener2, Object callStackTrace2) {
        this.connectionPool = connectionPool2;
        this.address = address2;
        this.call = call2;
        this.eventListener = eventListener2;
        this.routeSelector = new RouteSelector(address2, routeDatabase(), call2, eventListener2);
        this.callStackTrace = callStackTrace2;
        this.connectionAttemptDelay = 0;
    }

    public void newHttp2Connection(OkHttpClient client, Request request) throws IOException {
        RealConnection result;
        int connectTimeout = client.connectTimeoutMillis();
        int readTimeout = client.readTimeoutMillis();
        int writeTimeout = client.writeTimeoutMillis();
        int pingIntervalMillis = client.pingIntervalMillis();
        boolean connectionRetryEnabled = client.retryOnConnectionFailure();
        if (request.isCreateConnectionRequest()) {
            int http2ConnectionIndex = Integer.parseInt(request.header("Http2ConnectionIndex"));
            int connectCount = this.connectionPool.http2ConnectionCount(this.address);
            if (http2ConnectionIndex > connectCount) {
                if (this.route == null && (this.routeSelection == null || !this.routeSelection.hasNext())) {
                    this.routeSelection = this.routeSelector.next();
                }
                synchronized (this.connectionPool) {
                    try {
                        Route selectedRoute = this.routeSelection.next();
                        try {
                            this.route = selectedRoute;
                            result = new RealConnection(this.connectionPool, selectedRoute);
                        } catch (Throwable th) {
                            th = th;
                            int i = connectCount;
                            int i2 = http2ConnectionIndex;
                            int i3 = connectTimeout;
                            Route route2 = selectedRoute;
                            while (true) {
                                try {
                                    break;
                                } catch (Throwable th2) {
                                    th = th2;
                                }
                            }
                            throw th;
                        }
                        try {
                            if (this.routeSelection.concurrentConnectEnabled()) {
                                try {
                                    result.prepareConcurrentConnect(this.routeSelection.concurrentInetSocketAddresses(), this.connectionAttemptDelay);
                                    result.setRouteSelection(this.routeSelection);
                                } catch (Throwable th3) {
                                    th = th3;
                                    RealConnection realConnection = result;
                                    Route route3 = selectedRoute;
                                    int i4 = connectCount;
                                    int i5 = http2ConnectionIndex;
                                    int i6 = connectTimeout;
                                }
                            }
                            int i7 = connectTimeout;
                            RealConnection result2 = result;
                            Route selectedRoute2 = selectedRoute;
                            int i8 = connectCount;
                            int i9 = http2ConnectionIndex;
                            result.connect(connectTimeout, readTimeout, writeTimeout, pingIntervalMillis, connectionRetryEnabled, this.call, this.eventListener);
                            this.routeSelection.connected(result2.route());
                            result2.keepaliveTimestampNs = System.nanoTime();
                            synchronized (this.connectionPool) {
                                Internal.instance.put(this.connectionPool, result2);
                            }
                            RealConnection realConnection2 = result2;
                            Route route4 = selectedRoute2;
                        } catch (Throwable th4) {
                            th = th4;
                            int i10 = connectCount;
                            int i11 = http2ConnectionIndex;
                            int i12 = connectTimeout;
                            RealConnection realConnection3 = result;
                            Route route5 = selectedRoute;
                            while (true) {
                                break;
                            }
                            throw th;
                        }
                    } catch (Throwable th5) {
                        th = th5;
                        int i13 = connectCount;
                        int i14 = http2ConnectionIndex;
                        int i15 = connectTimeout;
                        while (true) {
                            break;
                        }
                        throw th;
                    }
                }
            } else {
                int i16 = http2ConnectionIndex;
                int i17 = connectTimeout;
            }
        } else {
            Request request2 = request;
            int i18 = connectTimeout;
            throw new IllegalArgumentException("a normal Request without http2ConnectionIndex");
        }
    }

    public HttpCodec newStream(OkHttpClient client, Interceptor.Chain chain, boolean doExtensiveHealthChecks) {
        try {
            HttpCodec resultCodec = findHealthyConnection(client, chain.connectTimeoutMillis(), chain.readTimeoutMillis(), chain.writeTimeoutMillis(), client.pingIntervalMillis(), client.retryOnConnectionFailure(), doExtensiveHealthChecks).newCodec(client, chain, this);
            synchronized (this.connectionPool) {
                this.codec = resultCodec;
            }
            return resultCodec;
        } catch (IOException e) {
            throw new RouteException(e);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0018, code lost:
        return r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0012, code lost:
        if (r0.isHealthy(r10) != false) goto L_0x0018;
     */
    private RealConnection findHealthyConnection(OkHttpClient client, int connectTimeout, int readTimeout, int writeTimeout, int pingIntervalMillis, boolean connectionRetryEnabled, boolean doExtensiveHealthChecks) throws IOException {
        while (true) {
            RealConnection candidate = findConnection(client, connectTimeout, readTimeout, writeTimeout, pingIntervalMillis, connectionRetryEnabled);
            synchronized (this.connectionPool) {
                if (candidate.successCount == 0) {
                    return candidate;
                }
            }
            noNewStreams();
        }
        while (true) {
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0040, code lost:
        r7 = r0;
        com.huawei.okhttp3.internal.Util.closeQuietly(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0044, code lost:
        if (r7 == null) goto L_0x004d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0046, code lost:
        r1.eventListener.connectionReleased(r1.call, r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x004d, code lost:
        if (r2 == false) goto L_0x0056;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x004f, code lost:
        r1.eventListener.connectionAcquired(r1.call, r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0056, code lost:
        if (r3 == null) goto L_0x0059;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0058, code lost:
        return r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0059, code lost:
        r0 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x005a, code lost:
        if (r4 != null) goto L_0x0071;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x005e, code lost:
        if (r1.routeSelection == null) goto L_0x0068;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0066, code lost:
        if (r1.routeSelection.hasNext() != false) goto L_0x0071;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0068, code lost:
        r0 = true;
        r1.routeSelection = r1.routeSelector.next();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x0071, code lost:
        r8 = r0;
        r9 = r1.connectionPool;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x0074, code lost:
        monitor-enter(r9);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x0077, code lost:
        if (r1.canceled != false) goto L_0x013c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x0079, code lost:
        if (r8 == false) goto L_0x00a5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x007b, code lost:
        r0 = r1.routeSelection.getAll();
        r5 = 0;
        r10 = r0.size();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x0086, code lost:
        if (r5 >= r10) goto L_0x00a5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x0088, code lost:
        r11 = r0.get(r5);
        com.huawei.okhttp3.internal.Internal.instance.get(r1.connectionPool, r1.address, r1, r11);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x0099, code lost:
        if (r1.connection == null) goto L_0x00a2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x009b, code lost:
        r2 = true;
        r3 = r1.connection;
        r1.route = r11;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x00a2, code lost:
        r5 = r5 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x00a5, code lost:
        if (r2 != false) goto L_0x00d8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x00a7, code lost:
        if (r4 != null) goto L_0x00b0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x00a9, code lost:
        r4 = r1.routeSelection.next();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x00b0, code lost:
        r1.route = r4;
        r1.refusedStreamCount = 0;
        r3 = new com.huawei.okhttp3.internal.connection.RealConnection(r1.connectionPool, r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x00c3, code lost:
        if (r1.routeSelection.concurrentConnectEnabled() == false) goto L_0x00d5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x00c5, code lost:
        r3.prepareConcurrentConnect(r1.routeSelection.concurrentInetSocketAddresses(), r1.connectionAttemptDelay);
        r3.setRouteSelection(r1.routeSelection);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x00d5, code lost:
        acquire(r3, false);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x00d8, code lost:
        monitor-exit(r9);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x00d9, code lost:
        if (r2 == false) goto L_0x00e3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x00db, code lost:
        r1.eventListener.connectionAcquired(r1.call, r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x00e2, code lost:
        return r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x00e3, code lost:
        r3.connect(r20, r21, r22, r23, r24, r1.call, r1.eventListener);
        r1.routeSelection.connected(r3.route());
        r5 = null;
        r10 = r1.connectionPool;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x0105, code lost:
        monitor-enter(r10);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:?, code lost:
        r1.reportedAcquired = true;
        com.huawei.okhttp3.internal.Internal.instance.put(r1.connectionPool, r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x0114, code lost:
        if (r3.isMultiplexed() == false) goto L_0x0127;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:67:?, code lost:
        r5 = com.huawei.okhttp3.internal.Internal.instance.deduplicate(r19, r1.connectionPool, r1.address, r1);
        r3 = r1.connection;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:68:0x0127, code lost:
        r12 = r19;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:69:0x0129, code lost:
        monitor-exit(r10);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:70:0x012a, code lost:
        com.huawei.okhttp3.internal.Util.closeQuietly(r5);
        r1.eventListener.connectionAcquired(r1.call, r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:71:0x0134, code lost:
        return r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:72:0x0135, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:73:0x0136, code lost:
        r12 = r19;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:75:?, code lost:
        monitor-exit(r10);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:76:0x0139, code lost:
        throw r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:77:0x013a, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:78:0x013c, code lost:
        r12 = r19;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:81:0x0145, code lost:
        throw new java.io.IOException("Canceled");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:82:0x0146, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:83:0x0147, code lost:
        r12 = r19;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:84:0x0149, code lost:
        monitor-exit(r9);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:85:0x014a, code lost:
        throw r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:86:0x014b, code lost:
        r0 = th;
     */
    private RealConnection findConnection(OkHttpClient client, int connectTimeout, int readTimeout, int writeTimeout, int pingIntervalMillis, boolean connectionRetryEnabled) throws IOException {
        boolean foundPooledConnection = false;
        RealConnection result = null;
        Route selectedRoute = null;
        synchronized (this.connectionPool) {
            try {
                if (this.released) {
                    OkHttpClient okHttpClient = client;
                    throw new IllegalStateException("released");
                } else if (this.codec != null) {
                    OkHttpClient okHttpClient2 = client;
                    throw new IllegalStateException("codec != null");
                } else if (!this.canceled) {
                    Connection releasedConnection = this.connection;
                    Socket toClose = releaseIfNoNewStreams();
                    if (this.connection != null) {
                        result = this.connection;
                        releasedConnection = null;
                    }
                    if (!this.reportedAcquired) {
                        releasedConnection = null;
                    }
                    if (result == null) {
                        Internal.instance.get(this.connectionPool, this.address, this, null);
                        if (this.connection != null) {
                            foundPooledConnection = true;
                            result = this.connection;
                        } else {
                            selectedRoute = this.route;
                        }
                    }
                } else {
                    OkHttpClient okHttpClient3 = client;
                    throw new IOException("Canceled");
                }
            } catch (Throwable th) {
                th = th;
                throw th;
            }
        }
    }

    private Socket releaseIfNoNewStreams() {
        RealConnection allocatedConnection = this.connection;
        if (allocatedConnection == null || !allocatedConnection.noNewStreams) {
            return null;
        }
        return deallocate(false, false, true);
    }

    public void streamFinished(boolean noNewStreams, HttpCodec codec2, long bytesRead, IOException e) {
        Connection releasedConnection;
        Socket socket;
        boolean callEnd;
        this.eventListener.responseBodyEnd(this.call, bytesRead);
        synchronized (this.connectionPool) {
            if (codec2 != null) {
                if (codec2 == this.codec) {
                    if (!noNewStreams) {
                        this.connection.successCount++;
                    }
                    releasedConnection = this.connection;
                    socket = deallocate(noNewStreams, false, true);
                    if (this.connection != null) {
                        releasedConnection = null;
                    }
                    callEnd = this.released;
                }
            }
            throw new IllegalStateException("expected " + this.codec + " but was " + codec2);
        }
        Connection releasedConnection2 = releasedConnection;
        Util.closeQuietly(socket);
        if (releasedConnection2 != null) {
            this.eventListener.connectionReleased(this.call, releasedConnection2);
        }
        if (e != null) {
            this.eventListener.callFailed(this.call, e);
        } else if (callEnd) {
            this.eventListener.callEnd(this.call);
        }
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

    public Route route() {
        return this.route;
    }

    public synchronized RealConnection connection() {
        return this.connection;
    }

    public void release() {
        Connection releasedConnection;
        Socket socket;
        synchronized (this.connectionPool) {
            releasedConnection = this.connection;
            socket = deallocate(false, true, false);
            if (this.connection != null) {
                releasedConnection = null;
            }
        }
        Connection releasedConnection2 = releasedConnection;
        Util.closeQuietly(socket);
        if (releasedConnection2 != null) {
            this.eventListener.connectionReleased(this.call, releasedConnection2);
            this.eventListener.callEnd(this.call);
        }
    }

    public void noNewStreams() {
        Connection releasedConnection;
        Socket socket;
        synchronized (this.connectionPool) {
            releasedConnection = this.connection;
            socket = deallocate(true, false, false);
            if (this.connection != null) {
                releasedConnection = null;
            }
        }
        Connection releasedConnection2 = releasedConnection;
        Util.closeQuietly(socket);
        if (releasedConnection2 != null) {
            this.eventListener.connectionReleased(this.call, releasedConnection2);
        }
    }

    private Socket deallocate(boolean noNewStreams, boolean released2, boolean streamFinished) {
        if (streamFinished) {
            this.codec = null;
        }
        if (released2) {
            this.released = true;
        }
        Socket socket = null;
        if (this.connection != null) {
            if (noNewStreams) {
                this.connection.noNewStreams = true;
            }
            if (this.codec == null && (this.released || this.connection.noNewStreams)) {
                release(this.connection);
                if (this.connection.allocations.isEmpty()) {
                    this.connection.idleAtNanos = System.nanoTime();
                    if (Internal.instance.connectionBecameIdle(this.connectionPool, this.connection)) {
                        socket = this.connection.socket();
                    }
                }
                this.connection = null;
            }
        }
        return socket;
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
        Connection releasedConnection;
        Socket socket;
        boolean noNewStreams = false;
        synchronized (this.connectionPool) {
            if (e instanceof StreamResetException) {
                ErrorCode errorCode = ((StreamResetException) e).errorCode;
                if (errorCode == ErrorCode.REFUSED_STREAM) {
                    this.refusedStreamCount++;
                    if (this.refusedStreamCount > 1) {
                        noNewStreams = true;
                        this.route = null;
                    }
                } else if (errorCode != ErrorCode.CANCEL) {
                    noNewStreams = true;
                    this.route = null;
                }
            } else if (this.connection != null && (!this.connection.isMultiplexed() || (e instanceof ConnectionShutdownException))) {
                noNewStreams = true;
                if (this.connection.successCount == 0) {
                    if (!(this.route == null || e == null)) {
                        this.routeSelector.connectFailed(this.route, e);
                        if (this.routeSelection != null) {
                            this.routeSelection.connectFailed();
                        }
                    }
                    this.route = null;
                }
            }
            releasedConnection = this.connection;
            socket = deallocate(noNewStreams, false, true);
            if (this.connection != null || !this.reportedAcquired) {
                releasedConnection = null;
            }
        }
        Connection releasedConnection2 = releasedConnection;
        Util.closeQuietly(socket);
        if (releasedConnection2 != null) {
            this.eventListener.connectionReleased(this.call, releasedConnection2);
        }
    }

    public void acquire(RealConnection connection2, boolean reportedAcquired2) {
        if (this.connection == null || this.connection.noNewStreams) {
            this.connection = connection2;
            this.reportedAcquired = reportedAcquired2;
            connection2.allocations.add(new StreamAllocationReference(this, this.callStackTrace));
            return;
        }
        throw new IllegalStateException();
    }

    private void release(RealConnection connection2) {
        int size = connection2.allocations.size();
        for (int i = 0; i < size; i++) {
            if (connection2.allocations.get(i).get() == this) {
                connection2.allocations.remove(i);
                return;
            }
        }
        throw new IllegalStateException();
    }

    public Socket releaseAndAcquire(RealConnection newConnection) {
        if (this.codec == null && this.connection.allocations.size() == 1) {
            Socket socket = deallocate(true, false, false);
            this.connection = newConnection;
            newConnection.allocations.add(this.connection.allocations.get(0));
            return socket;
        }
        throw new IllegalStateException();
    }

    public boolean hasMoreRoutes() {
        return this.route != null || (this.routeSelection != null && this.routeSelection.hasNext()) || this.routeSelector.hasNext();
    }

    public String toString() {
        RealConnection connection2 = connection();
        return connection2 != null ? connection2.toString() : this.address.toString();
    }
}
