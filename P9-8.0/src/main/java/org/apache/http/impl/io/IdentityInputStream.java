package org.apache.http.impl.io;

import java.io.IOException;
import java.io.InputStream;
import org.apache.http.io.SessionInputBuffer;

@Deprecated
public class IdentityInputStream extends InputStream {
    private boolean closed = false;
    private final SessionInputBuffer in;

    public IdentityInputStream(SessionInputBuffer in) {
        if (in == null) {
            throw new IllegalArgumentException("Session input buffer may not be null");
        }
        this.in = in;
    }

    public int available() throws IOException {
        if (this.closed || !this.in.isDataAvailable(10)) {
            return 0;
        }
        return 1;
    }

    public void close() throws IOException {
        this.closed = true;
    }

    public int read() throws IOException {
        if (this.closed) {
            return -1;
        }
        return this.in.read();
    }

    public int read(byte[] b, int off, int len) throws IOException {
        if (this.closed) {
            return -1;
        }
        return this.in.read(b, off, len);
    }
}
