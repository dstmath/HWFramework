package org.apache.http.conn;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.http.HttpEntity;
import org.apache.http.entity.HttpEntityWrapper;

@Deprecated
public class BasicManagedEntity extends HttpEntityWrapper implements ConnectionReleaseTrigger, EofSensorWatcher {
    protected final boolean attemptReuse;
    protected ManagedClientConnection managedConn;

    public BasicManagedEntity(HttpEntity entity, ManagedClientConnection conn, boolean reuse) {
        super(entity);
        if (conn != null) {
            this.managedConn = conn;
            this.attemptReuse = reuse;
            return;
        }
        throw new IllegalArgumentException("Connection may not be null.");
    }

    public boolean isRepeatable() {
        return false;
    }

    public InputStream getContent() throws IOException {
        return new EofSensorInputStream(this.wrappedEntity.getContent(), this);
    }

    public void consumeContent() throws IOException {
        if (this.managedConn != null) {
            try {
                if (this.attemptReuse) {
                    this.wrappedEntity.consumeContent();
                    this.managedConn.markReusable();
                }
            } finally {
                releaseManagedConnection();
            }
        }
    }

    public void writeTo(OutputStream outstream) throws IOException {
        super.writeTo(outstream);
        consumeContent();
    }

    public void releaseConnection() throws IOException {
        consumeContent();
    }

    public void abortConnection() throws IOException {
        if (this.managedConn != null) {
            try {
                this.managedConn.abortConnection();
            } finally {
                this.managedConn = null;
            }
        }
    }

    /* JADX INFO: finally extract failed */
    public boolean eofDetected(InputStream wrapped) throws IOException {
        try {
            if (this.attemptReuse && this.managedConn != null) {
                wrapped.close();
                this.managedConn.markReusable();
            }
            releaseManagedConnection();
            return false;
        } catch (Throwable th) {
            releaseManagedConnection();
            throw th;
        }
    }

    /* JADX INFO: finally extract failed */
    public boolean streamClosed(InputStream wrapped) throws IOException {
        try {
            if (this.attemptReuse && this.managedConn != null) {
                wrapped.close();
                this.managedConn.markReusable();
            }
            releaseManagedConnection();
            return false;
        } catch (Throwable th) {
            releaseManagedConnection();
            throw th;
        }
    }

    public boolean streamAbort(InputStream wrapped) throws IOException {
        if (this.managedConn != null) {
            this.managedConn.abortConnection();
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public void releaseManagedConnection() throws IOException {
        if (this.managedConn != null) {
            try {
                this.managedConn.releaseConnection();
            } finally {
                this.managedConn = null;
            }
        }
    }
}
