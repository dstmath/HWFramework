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

    public StreamAllocation(ConnectionPool connectionPool2, Address address2) {
        this.connectionPool = connectionPool2;
        this.address = address2;
    }

    public HttpStream newStream(int connectTimeout, int readTimeout, int writeTimeout, boolean connectionRetryEnabled, boolean doExtensiveHealthChecks) throws RouteException, IOException {
        HttpStream resultStream;
        try {
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

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0014, code lost:
        return r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0012, code lost:
        if (r0.isHealthy(r8) == false) goto L_0x0015;
     */
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
        while (true) {
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:24:0x003b, code lost:
        r8 = new com.android.okhttp.internal.io.RealConnection(r9.routeSelector.next());
        acquire(r8);
        r2 = r9.connectionPool;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x004c, code lost:
        monitor-enter(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:?, code lost:
        com.android.okhttp.internal.Internal.instance.put(r9.connectionPool, r8);
        r9.connection = r8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0058, code lost:
        if (r9.canceled != false) goto L_0x0075;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x005a, code lost:
        monitor-exit(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x005b, code lost:
        r8.connect(r10, r11, r12, r9.address.getConnectionSpecs(), r13);
        routeDatabase().connected(r8.getRoute());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0074, code lost:
        return r8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x007c, code lost:
        throw new java.io.IOException("Canceled");
     */
    private RealConnection findConnection(int connectTimeout, int readTimeout, int writeTimeout, boolean connectionRetryEnabled) throws IOException, RouteException {
        synchronized (this.connectionPool) {
            if (this.released) {
                throw new IllegalStateException("released");
            } else if (this.stream != null) {
                throw new IllegalStateException("stream != null");
            } else if (!this.canceled) {
                RealConnection allocatedConnection = this.connection;
                if (allocatedConnection != null && !allocatedConnection.noNewStreams) {
                    return allocatedConnection;
                }
                RealConnection pooledConnection = Internal.instance.get(this.connectionPool, this.address, this);
                if (pooledConnection != null) {
                    this.connection = pooledConnection;
                    return pooledConnection;
                } else if (this.routeSelector == null) {
                    this.routeSelector = new RouteSelector(this.address, routeDatabase());
                }
            } else {
                throw new IOException("Canceled");
            }
        }
    }

    public void streamFinished(HttpStream stream2) {
        synchronized (this.connectionPool) {
            if (stream2 != null) {
                if (stream2 == this.stream) {
                }
            }
            throw new IllegalStateException("expected " + this.stream + " but was " + stream2);
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

    private void deallocate(boolean noNewStreams, boolean released2, boolean streamFinished) {
        RealConnection connectionToClose = null;
        synchronized (this.connectionPool) {
            if (streamFinished) {
                try {
                    this.stream = null;
                } catch (Throwable th) {
                    while (true) {
                        throw th;
                    }
                }
            }
            if (released2) {
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

    public void acquire(RealConnection connection2) {
        connection2.allocations.add(new WeakReference(this));
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

    public boolean recover(RouteException e) {
        if (this.canceled) {
            return false;
        }
        if (this.connection != null) {
            connectionFailed(e.getLastConnectException());
        }
        if ((this.routeSelector == null || this.routeSelector.hasNext()) && isRecoverable(e)) {
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
        return (this.routeSelector == null || this.routeSelector.hasNext()) && isRecoverable(e) && (requestBodyOut == null || (requestBodyOut instanceof RetryableSink));
    }

    private boolean isRecoverable(IOException e) {
        if (!(e instanceof ProtocolException) && !(e instanceof InterruptedIOException)) {
            return true;
        }
        return false;
    }

    private boolean isRecoverable(RouteException e) {
        IOException ioe = e.getLastConnectException();
        if (ioe instanceof ProtocolException) {
            return false;
        }
        if (ioe instanceof InterruptedIOException) {
            return ioe instanceof SocketTimeoutException;
        }
        if ((!(ioe instanceof SSLHandshakeException) || !(ioe.getCause() instanceof CertificateException)) && !(ioe instanceof SSLPeerUnverifiedException)) {
            return true;
        }
        return false;
    }

    public String toString() {
        return this.address.toString();
    }
}
