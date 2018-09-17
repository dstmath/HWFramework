package com.android.org.bouncycastle.util.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class TeeInputStream extends InputStream {
    private final InputStream input;
    private final OutputStream output;

    public TeeInputStream(InputStream input, OutputStream output) {
        this.input = input;
        this.output = output;
    }

    public int read(byte[] buf) throws IOException {
        return read(buf, 0, buf.length);
    }

    public int read(byte[] buf, int off, int len) throws IOException {
        int i = this.input.read(buf, off, len);
        if (i > 0) {
            this.output.write(buf, off, i);
        }
        return i;
    }

    public int read() throws IOException {
        int i = this.input.read();
        if (i >= 0) {
            this.output.write(i);
        }
        return i;
    }

    public void close() throws IOException {
        this.input.close();
        this.output.close();
    }

    public OutputStream getOutputStream() {
        return this.output;
    }
}
