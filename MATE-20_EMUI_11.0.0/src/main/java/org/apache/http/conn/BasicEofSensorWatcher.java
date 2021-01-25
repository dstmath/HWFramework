package org.apache.http.conn;

import java.io.IOException;
import java.io.InputStream;

@Deprecated
public class BasicEofSensorWatcher implements EofSensorWatcher {
    protected boolean attemptReuse;
    protected ManagedClientConnection managedConn;

    public BasicEofSensorWatcher(ManagedClientConnection conn, boolean reuse) {
        if (conn != null) {
            this.managedConn = conn;
            this.attemptReuse = reuse;
            return;
        }
        throw new IllegalArgumentException("Connection may not be null.");
    }

    /* JADX INFO: finally extract failed */
    @Override // org.apache.http.conn.EofSensorWatcher
    public boolean eofDetected(InputStream wrapped) throws IOException {
        try {
            if (this.attemptReuse) {
                wrapped.close();
                this.managedConn.markReusable();
            }
            this.managedConn.releaseConnection();
            return false;
        } catch (Throwable th) {
            this.managedConn.releaseConnection();
            throw th;
        }
    }

    /* JADX INFO: finally extract failed */
    @Override // org.apache.http.conn.EofSensorWatcher
    public boolean streamClosed(InputStream wrapped) throws IOException {
        try {
            if (this.attemptReuse) {
                wrapped.close();
                this.managedConn.markReusable();
            }
            this.managedConn.releaseConnection();
            return false;
        } catch (Throwable th) {
            this.managedConn.releaseConnection();
            throw th;
        }
    }

    @Override // org.apache.http.conn.EofSensorWatcher
    public boolean streamAbort(InputStream wrapped) throws IOException {
        this.managedConn.abortConnection();
        return false;
    }
}
