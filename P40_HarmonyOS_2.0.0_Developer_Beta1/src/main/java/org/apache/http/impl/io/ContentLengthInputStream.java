package org.apache.http.impl.io;

import java.io.IOException;
import java.io.InputStream;
import org.apache.http.io.SessionInputBuffer;

@Deprecated
public class ContentLengthInputStream extends InputStream {
    private static final int BUFFER_SIZE = 2048;
    private boolean closed = false;
    private long contentLength;
    private SessionInputBuffer in = null;
    private long pos = 0;

    public ContentLengthInputStream(SessionInputBuffer in2, long contentLength2) {
        if (in2 == null) {
            throw new IllegalArgumentException("Input stream may not be null");
        } else if (contentLength2 >= 0) {
            this.in = in2;
            this.contentLength = contentLength2;
        } else {
            throw new IllegalArgumentException("Content length may not be negative");
        }
    }

    @Override // java.io.InputStream, java.io.Closeable, java.lang.AutoCloseable
    public void close() throws IOException {
        if (!this.closed) {
            try {
                do {
                } while (read(new byte[BUFFER_SIZE]) >= 0);
            } finally {
                this.closed = true;
            }
        }
    }

    @Override // java.io.InputStream
    public int read() throws IOException {
        if (!this.closed) {
            long j = this.pos;
            if (j >= this.contentLength) {
                return -1;
            }
            this.pos = j + 1;
            return this.in.read();
        }
        throw new IOException("Attempted read from closed stream.");
    }

    @Override // java.io.InputStream
    public int read(byte[] b, int off, int len) throws IOException {
        if (!this.closed) {
            long j = this.pos;
            long j2 = this.contentLength;
            if (j >= j2) {
                return -1;
            }
            if (((long) len) + j > j2) {
                len = (int) (j2 - j);
            }
            int count = this.in.read(b, off, len);
            this.pos += (long) count;
            return count;
        }
        throw new IOException("Attempted read from closed stream.");
    }

    @Override // java.io.InputStream
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override // java.io.InputStream
    public long skip(long n) throws IOException {
        int l;
        if (n <= 0) {
            return 0;
        }
        byte[] buffer = new byte[BUFFER_SIZE];
        long remaining = Math.min(n, this.contentLength - this.pos);
        long count = 0;
        while (remaining > 0 && (l = read(buffer, 0, (int) Math.min(2048L, remaining))) != -1) {
            count += (long) l;
            remaining -= (long) l;
        }
        this.pos += count;
        return count;
    }
}
