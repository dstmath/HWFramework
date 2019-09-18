package com.android.okhttp.okio;

import java.io.IOException;

public abstract class ForwardingSink implements Sink {
    private final Sink delegate;

    public ForwardingSink(Sink delegate2) {
        if (delegate2 != null) {
            this.delegate = delegate2;
            return;
        }
        throw new IllegalArgumentException("delegate == null");
    }

    public final Sink delegate() {
        return this.delegate;
    }

    public void write(Buffer source, long byteCount) throws IOException {
        this.delegate.write(source, byteCount);
    }

    public void flush() throws IOException {
        this.delegate.flush();
    }

    public Timeout timeout() {
        return this.delegate.timeout();
    }

    public void close() throws IOException {
        this.delegate.close();
    }

    public String toString() {
        return getClass().getSimpleName() + "(" + this.delegate.toString() + ")";
    }
}
