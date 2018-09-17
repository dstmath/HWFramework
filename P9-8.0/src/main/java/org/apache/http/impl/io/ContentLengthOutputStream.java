package org.apache.http.impl.io;

import java.io.IOException;
import java.io.OutputStream;
import org.apache.http.io.SessionOutputBuffer;

@Deprecated
public class ContentLengthOutputStream extends OutputStream {
    private boolean closed = false;
    private final long contentLength;
    private final SessionOutputBuffer out;
    private long total = 0;

    public ContentLengthOutputStream(SessionOutputBuffer out, long contentLength) {
        if (out == null) {
            throw new IllegalArgumentException("Session output buffer may not be null");
        } else if (contentLength < 0) {
            throw new IllegalArgumentException("Content length may not be negative");
        } else {
            this.out = out;
            this.contentLength = contentLength;
        }
    }

    public void close() throws IOException {
        if (!this.closed) {
            this.closed = true;
            this.out.flush();
        }
    }

    public void flush() throws IOException {
        this.out.flush();
    }

    public void write(byte[] b, int off, int len) throws IOException {
        if (this.closed) {
            throw new IOException("Attempted write to closed stream.");
        } else if (this.total < this.contentLength) {
            long max = this.contentLength - this.total;
            if (((long) len) > max) {
                len = (int) max;
            }
            this.out.write(b, off, len);
            this.total += (long) len;
        }
    }

    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    public void write(int b) throws IOException {
        if (this.closed) {
            throw new IOException("Attempted write to closed stream.");
        } else if (this.total < this.contentLength) {
            this.out.write(b);
            this.total++;
        }
    }
}
