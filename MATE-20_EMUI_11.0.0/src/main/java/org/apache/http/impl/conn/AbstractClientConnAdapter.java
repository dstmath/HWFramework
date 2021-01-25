package org.apache.http.impl.conn;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import org.apache.http.HttpConnectionMetrics;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ManagedClientConnection;
import org.apache.http.conn.OperatedClientConnection;

@Deprecated
public abstract class AbstractClientConnAdapter implements ManagedClientConnection {
    private volatile boolean aborted;
    private volatile ClientConnectionManager connManager;
    private volatile long duration;
    private final Thread executionThread = Thread.currentThread();
    private volatile boolean markedReusable;
    private volatile OperatedClientConnection wrappedConnection;

    protected AbstractClientConnAdapter(ClientConnectionManager mgr, OperatedClientConnection conn) {
        this.connManager = mgr;
        this.wrappedConnection = conn;
        this.markedReusable = false;
        this.aborted = false;
        this.duration = Long.MAX_VALUE;
    }

    /* access modifiers changed from: protected */
    public void detach() {
        this.wrappedConnection = null;
        this.connManager = null;
        this.duration = Long.MAX_VALUE;
    }

    /* access modifiers changed from: protected */
    public OperatedClientConnection getWrappedConnection() {
        return this.wrappedConnection;
    }

    /* access modifiers changed from: protected */
    public ClientConnectionManager getManager() {
        return this.connManager;
    }

    /* access modifiers changed from: protected */
    public final void assertNotAborted() throws InterruptedIOException {
        if (this.aborted) {
            throw new InterruptedIOException("Connection has been shut down.");
        }
    }

    /* access modifiers changed from: protected */
    public final void assertValid(OperatedClientConnection wrappedConn) {
        if (wrappedConn == null) {
            throw new IllegalStateException("No wrapped connection.");
        }
    }

    @Override // org.apache.http.HttpConnection
    public boolean isOpen() {
        OperatedClientConnection conn = getWrappedConnection();
        if (conn == null) {
            return false;
        }
        return conn.isOpen();
    }

    @Override // org.apache.http.HttpConnection
    public boolean isStale() {
        OperatedClientConnection conn;
        if (!this.aborted && (conn = getWrappedConnection()) != null) {
            return conn.isStale();
        }
        return true;
    }

    @Override // org.apache.http.HttpConnection
    public void setSocketTimeout(int timeout) {
        OperatedClientConnection conn = getWrappedConnection();
        assertValid(conn);
        conn.setSocketTimeout(timeout);
    }

    @Override // org.apache.http.HttpConnection
    public int getSocketTimeout() {
        OperatedClientConnection conn = getWrappedConnection();
        assertValid(conn);
        return conn.getSocketTimeout();
    }

    @Override // org.apache.http.HttpConnection
    public HttpConnectionMetrics getMetrics() {
        OperatedClientConnection conn = getWrappedConnection();
        assertValid(conn);
        return conn.getMetrics();
    }

    @Override // org.apache.http.HttpClientConnection
    public void flush() throws IOException {
        assertNotAborted();
        OperatedClientConnection conn = getWrappedConnection();
        assertValid(conn);
        conn.flush();
    }

    @Override // org.apache.http.HttpClientConnection
    public boolean isResponseAvailable(int timeout) throws IOException {
        assertNotAborted();
        OperatedClientConnection conn = getWrappedConnection();
        assertValid(conn);
        return conn.isResponseAvailable(timeout);
    }

    @Override // org.apache.http.HttpClientConnection
    public void receiveResponseEntity(HttpResponse response) throws HttpException, IOException {
        assertNotAborted();
        OperatedClientConnection conn = getWrappedConnection();
        assertValid(conn);
        unmarkReusable();
        conn.receiveResponseEntity(response);
    }

    @Override // org.apache.http.HttpClientConnection
    public HttpResponse receiveResponseHeader() throws HttpException, IOException {
        assertNotAborted();
        OperatedClientConnection conn = getWrappedConnection();
        assertValid(conn);
        unmarkReusable();
        return conn.receiveResponseHeader();
    }

    @Override // org.apache.http.HttpClientConnection
    public void sendRequestEntity(HttpEntityEnclosingRequest request) throws HttpException, IOException {
        assertNotAborted();
        OperatedClientConnection conn = getWrappedConnection();
        assertValid(conn);
        unmarkReusable();
        conn.sendRequestEntity(request);
    }

    @Override // org.apache.http.HttpClientConnection
    public void sendRequestHeader(HttpRequest request) throws HttpException, IOException {
        assertNotAborted();
        OperatedClientConnection conn = getWrappedConnection();
        assertValid(conn);
        unmarkReusable();
        conn.sendRequestHeader(request);
    }

    @Override // org.apache.http.HttpInetConnection
    public InetAddress getLocalAddress() {
        OperatedClientConnection conn = getWrappedConnection();
        assertValid(conn);
        return conn.getLocalAddress();
    }

    @Override // org.apache.http.HttpInetConnection
    public int getLocalPort() {
        OperatedClientConnection conn = getWrappedConnection();
        assertValid(conn);
        return conn.getLocalPort();
    }

    @Override // org.apache.http.HttpInetConnection
    public InetAddress getRemoteAddress() {
        OperatedClientConnection conn = getWrappedConnection();
        assertValid(conn);
        return conn.getRemoteAddress();
    }

    @Override // org.apache.http.HttpInetConnection
    public int getRemotePort() {
        OperatedClientConnection conn = getWrappedConnection();
        assertValid(conn);
        return conn.getRemotePort();
    }

    @Override // org.apache.http.conn.ManagedClientConnection
    public boolean isSecure() {
        OperatedClientConnection conn = getWrappedConnection();
        assertValid(conn);
        return conn.isSecure();
    }

    @Override // org.apache.http.conn.ManagedClientConnection
    public SSLSession getSSLSession() {
        OperatedClientConnection conn = getWrappedConnection();
        assertValid(conn);
        if (!isOpen()) {
            return null;
        }
        Socket sock = conn.getSocket();
        if (sock instanceof SSLSocket) {
            return ((SSLSocket) sock).getSession();
        }
        return null;
    }

    @Override // org.apache.http.conn.ManagedClientConnection
    public void markReusable() {
        this.markedReusable = true;
    }

    @Override // org.apache.http.conn.ManagedClientConnection
    public void unmarkReusable() {
        this.markedReusable = false;
    }

    @Override // org.apache.http.conn.ManagedClientConnection
    public boolean isMarkedReusable() {
        return this.markedReusable;
    }

    @Override // org.apache.http.conn.ManagedClientConnection
    public void setIdleDuration(long duration2, TimeUnit unit) {
        if (duration2 > 0) {
            this.duration = unit.toMillis(duration2);
        } else {
            this.duration = -1;
        }
    }

    @Override // org.apache.http.conn.ConnectionReleaseTrigger
    public void releaseConnection() {
        if (this.connManager != null) {
            this.connManager.releaseConnection(this, this.duration, TimeUnit.MILLISECONDS);
        }
    }

    @Override // org.apache.http.conn.ConnectionReleaseTrigger
    public void abortConnection() {
        if (!this.aborted) {
            this.aborted = true;
            unmarkReusable();
            try {
                shutdown();
            } catch (IOException e) {
            }
            if (this.executionThread.equals(Thread.currentThread())) {
                releaseConnection();
            }
        }
    }
}
