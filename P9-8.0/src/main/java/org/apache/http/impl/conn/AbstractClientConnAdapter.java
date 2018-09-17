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

    protected void detach() {
        this.wrappedConnection = null;
        this.connManager = null;
        this.duration = Long.MAX_VALUE;
    }

    protected OperatedClientConnection getWrappedConnection() {
        return this.wrappedConnection;
    }

    protected ClientConnectionManager getManager() {
        return this.connManager;
    }

    protected final void assertNotAborted() throws InterruptedIOException {
        if (this.aborted) {
            throw new InterruptedIOException("Connection has been shut down.");
        }
    }

    protected final void assertValid(OperatedClientConnection wrappedConn) {
        if (wrappedConn == null) {
            throw new IllegalStateException("No wrapped connection.");
        }
    }

    public boolean isOpen() {
        OperatedClientConnection conn = getWrappedConnection();
        if (conn == null) {
            return false;
        }
        return conn.isOpen();
    }

    public boolean isStale() {
        if (this.aborted) {
            return true;
        }
        OperatedClientConnection conn = getWrappedConnection();
        if (conn == null) {
            return true;
        }
        return conn.isStale();
    }

    public void setSocketTimeout(int timeout) {
        OperatedClientConnection conn = getWrappedConnection();
        assertValid(conn);
        conn.setSocketTimeout(timeout);
    }

    public int getSocketTimeout() {
        OperatedClientConnection conn = getWrappedConnection();
        assertValid(conn);
        return conn.getSocketTimeout();
    }

    public HttpConnectionMetrics getMetrics() {
        OperatedClientConnection conn = getWrappedConnection();
        assertValid(conn);
        return conn.getMetrics();
    }

    public void flush() throws IOException {
        assertNotAborted();
        OperatedClientConnection conn = getWrappedConnection();
        assertValid(conn);
        conn.flush();
    }

    public boolean isResponseAvailable(int timeout) throws IOException {
        assertNotAborted();
        OperatedClientConnection conn = getWrappedConnection();
        assertValid(conn);
        return conn.isResponseAvailable(timeout);
    }

    public void receiveResponseEntity(HttpResponse response) throws HttpException, IOException {
        assertNotAborted();
        OperatedClientConnection conn = getWrappedConnection();
        assertValid(conn);
        unmarkReusable();
        conn.receiveResponseEntity(response);
    }

    public HttpResponse receiveResponseHeader() throws HttpException, IOException {
        assertNotAborted();
        OperatedClientConnection conn = getWrappedConnection();
        assertValid(conn);
        unmarkReusable();
        return conn.receiveResponseHeader();
    }

    public void sendRequestEntity(HttpEntityEnclosingRequest request) throws HttpException, IOException {
        assertNotAborted();
        OperatedClientConnection conn = getWrappedConnection();
        assertValid(conn);
        unmarkReusable();
        conn.sendRequestEntity(request);
    }

    public void sendRequestHeader(HttpRequest request) throws HttpException, IOException {
        assertNotAborted();
        OperatedClientConnection conn = getWrappedConnection();
        assertValid(conn);
        unmarkReusable();
        conn.sendRequestHeader(request);
    }

    public InetAddress getLocalAddress() {
        OperatedClientConnection conn = getWrappedConnection();
        assertValid(conn);
        return conn.getLocalAddress();
    }

    public int getLocalPort() {
        OperatedClientConnection conn = getWrappedConnection();
        assertValid(conn);
        return conn.getLocalPort();
    }

    public InetAddress getRemoteAddress() {
        OperatedClientConnection conn = getWrappedConnection();
        assertValid(conn);
        return conn.getRemoteAddress();
    }

    public int getRemotePort() {
        OperatedClientConnection conn = getWrappedConnection();
        assertValid(conn);
        return conn.getRemotePort();
    }

    public boolean isSecure() {
        OperatedClientConnection conn = getWrappedConnection();
        assertValid(conn);
        return conn.isSecure();
    }

    public SSLSession getSSLSession() {
        OperatedClientConnection conn = getWrappedConnection();
        assertValid(conn);
        if (!isOpen()) {
            return null;
        }
        SSLSession result = null;
        Socket sock = conn.getSocket();
        if (sock instanceof SSLSocket) {
            result = ((SSLSocket) sock).getSession();
        }
        return result;
    }

    public void markReusable() {
        this.markedReusable = true;
    }

    public void unmarkReusable() {
        this.markedReusable = false;
    }

    public boolean isMarkedReusable() {
        return this.markedReusable;
    }

    public void setIdleDuration(long duration, TimeUnit unit) {
        if (duration > 0) {
            this.duration = unit.toMillis(duration);
        } else {
            this.duration = -1;
        }
    }

    public void releaseConnection() {
        if (this.connManager != null) {
            this.connManager.releaseConnection(this, this.duration, TimeUnit.MILLISECONDS);
        }
    }

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
