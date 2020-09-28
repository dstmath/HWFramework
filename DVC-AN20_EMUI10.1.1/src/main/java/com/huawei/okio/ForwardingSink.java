package com.huawei.okio;

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

    @Override // com.huawei.okio.Sink
    public void write(Buffer source, long byteCount) throws IOException {
        this.delegate.write(source, byteCount);
    }

    @Override // com.huawei.okio.Sink, java.io.Flushable
    public void flush() throws IOException {
        this.delegate.flush();
    }

    @Override // com.huawei.okio.Sink
    public Timeout timeout() {
        return this.delegate.timeout();
    }

    @Override // java.io.Closeable, com.huawei.okio.Sink, java.lang.AutoCloseable
    public void close() throws IOException {
        this.delegate.close();
    }

    public String toString() {
        return getClass().getSimpleName() + "(" + this.delegate.toString() + ")";
    }
}
