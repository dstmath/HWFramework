package com.android.org.bouncycastle.util.io;

import java.io.IOException;
import java.io.OutputStream;

public abstract class SimpleOutputStream extends OutputStream {
    public void close() {
    }

    public void flush() {
    }

    public void write(int b) throws IOException {
        write(new byte[]{(byte) b}, 0, 1);
    }
}
