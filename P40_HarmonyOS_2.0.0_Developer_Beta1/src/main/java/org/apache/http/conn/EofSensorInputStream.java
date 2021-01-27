package org.apache.http.conn;

import java.io.IOException;
import java.io.InputStream;

@Deprecated
public class EofSensorInputStream extends InputStream implements ConnectionReleaseTrigger {
    private EofSensorWatcher eofWatcher;
    private boolean selfClosed;
    protected InputStream wrappedStream;

    public EofSensorInputStream(InputStream in, EofSensorWatcher watcher) {
        if (in != null) {
            this.wrappedStream = in;
            this.selfClosed = false;
            this.eofWatcher = watcher;
            return;
        }
        throw new IllegalArgumentException("Wrapped stream may not be null.");
    }

    /* access modifiers changed from: protected */
    public boolean isReadAllowed() throws IOException {
        if (!this.selfClosed) {
            return this.wrappedStream != null;
        }
        throw new IOException("Attempted read on closed stream.");
    }

    @Override // java.io.InputStream
    public int read() throws IOException {
        if (!isReadAllowed()) {
            return -1;
        }
        try {
            int l = this.wrappedStream.read();
            checkEOF(l);
            return l;
        } catch (IOException ex) {
            checkAbort();
            throw ex;
        }
    }

    @Override // java.io.InputStream
    public int read(byte[] b, int off, int len) throws IOException {
        if (!isReadAllowed()) {
            return -1;
        }
        try {
            int l = this.wrappedStream.read(b, off, len);
            checkEOF(l);
            return l;
        } catch (IOException ex) {
            checkAbort();
            throw ex;
        }
    }

    @Override // java.io.InputStream
    public int read(byte[] b) throws IOException {
        if (!isReadAllowed()) {
            return -1;
        }
        try {
            int l = this.wrappedStream.read(b);
            checkEOF(l);
            return l;
        } catch (IOException ex) {
            checkAbort();
            throw ex;
        }
    }

    @Override // java.io.InputStream
    public int available() throws IOException {
        if (!isReadAllowed()) {
            return 0;
        }
        try {
            return this.wrappedStream.available();
        } catch (IOException ex) {
            checkAbort();
            throw ex;
        }
    }

    @Override // java.io.InputStream, java.io.Closeable, java.lang.AutoCloseable
    public void close() throws IOException {
        this.selfClosed = true;
        checkClose();
    }

    /* access modifiers changed from: protected */
    public void checkEOF(int eof) throws IOException {
        InputStream inputStream = this.wrappedStream;
        if (inputStream != null && eof < 0) {
            boolean scws = true;
            try {
                if (this.eofWatcher != null) {
                    scws = this.eofWatcher.eofDetected(inputStream);
                }
                if (scws) {
                    this.wrappedStream.close();
                }
            } finally {
                this.wrappedStream = null;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void checkClose() throws IOException {
        InputStream inputStream = this.wrappedStream;
        if (inputStream != null) {
            boolean scws = true;
            try {
                if (this.eofWatcher != null) {
                    scws = this.eofWatcher.streamClosed(inputStream);
                }
                if (scws) {
                    this.wrappedStream.close();
                }
            } finally {
                this.wrappedStream = null;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void checkAbort() throws IOException {
        InputStream inputStream = this.wrappedStream;
        if (inputStream != null) {
            boolean scws = true;
            try {
                if (this.eofWatcher != null) {
                    scws = this.eofWatcher.streamAbort(inputStream);
                }
                if (scws) {
                    this.wrappedStream.close();
                }
            } finally {
                this.wrappedStream = null;
            }
        }
    }

    @Override // org.apache.http.conn.ConnectionReleaseTrigger
    public void releaseConnection() throws IOException {
        close();
    }

    @Override // org.apache.http.conn.ConnectionReleaseTrigger
    public void abortConnection() throws IOException {
        this.selfClosed = true;
        checkAbort();
    }
}
