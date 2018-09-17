package com.android.okhttp.internal.http;

import com.android.okhttp.Address;
import com.android.okhttp.ConnectionPool;
import com.android.okhttp.internal.Internal;
import com.android.okhttp.internal.RouteDatabase;
import com.android.okhttp.internal.Util;
import com.android.okhttp.internal.io.RealConnection;
import com.android.okhttp.okio.Sink;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLPeerUnverifiedException;

public final class StreamAllocation {
    public final Address address;
    private boolean canceled;
    private RealConnection connection;
    private final ConnectionPool connectionPool;
    private boolean released;
    private RouteSelector routeSelector;
    private HttpStream stream;

    public StreamAllocation(ConnectionPool connectionPool, Address address) {
        this.connectionPool = connectionPool;
        this.address = address;
    }

    public HttpStream newStream(int connectTimeout, int readTimeout, int writeTimeout, boolean connectionRetryEnabled, boolean doExtensiveHealthChecks) throws RouteException, IOException {
        try {
            HttpStream resultStream;
            RealConnection resultConnection = findHealthyConnection(connectTimeout, readTimeout, writeTimeout, connectionRetryEnabled, doExtensiveHealthChecks);
            if (resultConnection.framedConnection != null) {
                resultStream = new Http2xStream(this, resultConnection.framedConnection);
            } else {
                resultConnection.getSocket().setSoTimeout(readTimeout);
                resultConnection.source.timeout().timeout((long) readTimeout, TimeUnit.MILLISECONDS);
                resultConnection.sink.timeout().timeout((long) writeTimeout, TimeUnit.MILLISECONDS);
                resultStream = new Http1xStream(this, resultConnection.source, resultConnection.sink);
            }
            synchronized (this.connectionPool) {
                resultConnection.streamCount++;
                this.stream = resultStream;
            }
            return resultStream;
        } catch (IOException e) {
            throw new RouteException(e);
        }
    }

    /* JADX WARNING: Missing block: B:9:0x0012, code:
            if (r0.isHealthy(r8) == false) goto L_0x0018;
     */
    /* JADX WARNING: Missing block: B:10:0x0014, code:
            return r0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private RealConnection findHealthyConnection(int connectTimeout, int readTimeout, int writeTimeout, boolean connectionRetryEnabled, boolean doExtensiveHealthChecks) throws IOException, RouteException {
        while (true) {
            RealConnection candidate = findConnection(connectTimeout, readTimeout, writeTimeout, connectionRetryEnabled);
            synchronized (this.connectionPool) {
                if (candidate.streamCount == 0) {
                    return candidate;
                }
            }
            connectionFailed();
        }
    }

    /* JADX WARNING: Missing block: B:37:0x005b, code:
            r0 = new com.android.okhttp.internal.io.RealConnection(r9.routeSelector.next());
            acquire(r0);
            r2 = r9.connectionPool;
     */
    /* JADX WARNING: Missing block: B:38:0x006b, code:
            monitor-enter(r2);
     */
    /* JADX WARNING: Missing block: B:40:?, code:
            com.android.okhttp.internal.Internal.instance.put(r9.connectionPool, r0);
            r9.connection = r0;
     */
    /* JADX WARNING: Missing block: B:41:0x0077, code:
            if (r9.canceled == false) goto L_0x0085;
     */
    /* JADX WARNING: Missing block: B:43:0x0081, code:
            throw new java.io.IOException("Canceled");
     */
    /* JADX WARNING: Missing block: B:47:0x0085, code:
            monitor-exit(r2);
     */
    /* JADX WARNING: Missing block: B:48:0x0086, code:
            r0.connect(r10, r11, r12, r9.address.getConnectionSpecs(), r13);
            routeDatabase().connected(r0.getRoute());
     */
    /* JADX WARNING: Missing block: B:49:0x009e, code:
            return r0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private RealConnection findConnection(int connectTimeout, int readTimeout, int writeTimeout, boolean connectionRetryEnabled) throws IOException, RouteException {
        synchronized (this.connectionPool) {
            if (this.released) {
                throw new IllegalStateException("released");
            } else if (this.stream != null) {
                throw new IllegalStateException("stream != null");
            } else if (this.canceled) {
                throw new IOException("Canceled");
            } else {
                RealConnection allocatedConnection = this.connection;
                if (allocatedConnection == null || (allocatedConnection.noNewStreams ^ 1) == 0) {
                    RealConnection pooledConnection = Internal.instance.get(this.connectionPool, this.address, this);
                    if (pooledConnection != null) {
                        this.connection = pooledConnection;
                        return pooledConnection;
                    } else if (this.routeSelector == null) {
                        this.routeSelector = new RouteSelector(this.address, routeDatabase());
                    }
                } else {
                    return allocatedConnection;
                }
            }
        }
    }

    public void streamFinished(HttpStream stream) {
        synchronized (this.connectionPool) {
            if (stream != null) {
                if (stream == this.stream) {
                }
            }
            throw new IllegalStateException("expected " + this.stream + " but was " + stream);
        }
        deallocate(false, false, true);
    }

    public HttpStream stream() {
        HttpStream httpStream;
        synchronized (this.connectionPool) {
            httpStream = this.stream;
        }
        return httpStream;
    }

    private RouteDatabase routeDatabase() {
        return Internal.instance.routeDatabase(this.connectionPool);
    }

    public synchronized RealConnection connection() {
        return this.connection;
    }

    public void release() {
        deallocate(false, true, false);
    }

    public void noNewStreams() {
        deallocate(true, false, false);
    }

    private void deallocate(boolean noNewStreams, boolean released, boolean streamFinished) {
        RealConnection connectionToClose = null;
        synchronized (this.connectionPool) {
            if (streamFinished) {
                this.stream = null;
            }
            if (released) {
                this.released = true;
            }
            if (this.connection != null) {
                if (noNewStreams) {
                    this.connection.noNewStreams = true;
                }
                if (this.stream == null && (this.released || this.connection.noNewStreams)) {
                    release(this.connection);
                    if (this.connection.streamCount > 0) {
                        this.routeSelector = null;
                    }
                    if (this.connection.allocations.isEmpty()) {
                        this.connection.idleAtNanos = System.nanoTime();
                        if (Internal.instance.connectionBecameIdle(this.connectionPool, this.connection)) {
                            connectionToClose = this.connection;
                        }
                    }
                    this.connection = null;
                }
            }
        }
        if (connectionToClose != null) {
            Util.closeQuietly(connectionToClose.getSocket());
        }
    }

    public void cancel() {
        HttpStream streamToCancel;
        RealConnection connectionToCancel;
        synchronized (this.connectionPool) {
            this.canceled = true;
            streamToCancel = this.stream;
            connectionToCancel = this.connection;
        }
        if (streamToCancel != null) {
            streamToCancel.cancel();
        } else if (connectionToCancel != null) {
            connectionToCancel.cancel();
        }
    }

    private void connectionFailed(IOException e) {
        synchronized (this.connectionPool) {
            if (this.routeSelector != null) {
                if (this.connection.streamCount == 0) {
                    this.routeSelector.connectFailed(this.connection.getRoute(), e);
                } else {
                    this.routeSelector = null;
                }
            }
        }
        connectionFailed();
    }

    public void connectionFailed() {
        deallocate(true, false, true);
    }

    public void acquire(RealConnection connection) {
        connection.allocations.add(new WeakReference(this));
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

    public boolean recover(RouteException e) {
        if (this.canceled) {
            return false;
        }
        if (this.connection != null) {
            connectionFailed(e.getLastConnectException());
        }
        if ((this.routeSelector == null || (this.routeSelector.hasNext() ^ 1) == 0) && (isRecoverable(e) ^ 1) == 0) {
            return true;
        }
        return false;
    }

    public boolean recover(IOException e, Sink requestBodyOut) {
        if (this.connection != null) {
            int streamCount = this.connection.streamCount;
            connectionFailed(e);
            if (streamCount == 1) {
                return false;
            }
        }
        return (this.routeSelector == null || (this.routeSelector.hasNext() ^ 1) == 0) && (isRecoverable(e) ^ 1) == 0 && ((requestBodyOut != null ? requestBodyOut instanceof RetryableSink : 1) ^ 1) == 0;
    }

    private boolean isRecoverable(IOException e) {
        if ((e instanceof ProtocolException) || (e instanceof InterruptedIOException)) {
            return false;
        }
        return true;
    }

    private boolean isRecoverable(RouteException e) {
        IOException ioe = e.getLastConnectException();
        if (ioe instanceof ProtocolException) {
            return false;
        }
        if (ioe instanceof InterruptedIOException) {
            return ioe instanceof SocketTimeoutException;
        }
        if (((ioe instanceof SSLHandshakeException) && (ioe.getCause() instanceof CertificateException)) || (ioe instanceof SSLPeerUnverifiedException)) {
            return false;
        }
        return true;
    }

    public String toString() {
        return this.address.toString();
    }
}
