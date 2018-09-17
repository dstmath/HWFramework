package com.android.org.conscrypt;

import java.io.ByteArrayOutputStream;

public final class OpenSSLBIOSink {
    private final ByteArrayOutputStream buffer;
    private final long ctx;
    private int position;

    public static OpenSSLBIOSink create() {
        return new OpenSSLBIOSink(new ByteArrayOutputStream());
    }

    private OpenSSLBIOSink(ByteArrayOutputStream buffer) {
        this.ctx = NativeCrypto.create_BIO_OutputStream(buffer);
        this.buffer = buffer;
    }

    public int available() {
        return this.buffer.size() - this.position;
    }

    public void reset() {
        this.buffer.reset();
        this.position = 0;
    }

    public long skip(long byteCount) {
        int maxLength = Math.min(available(), (int) byteCount);
        this.position += maxLength;
        if (this.position == this.buffer.size()) {
            reset();
        }
        return (long) maxLength;
    }

    public long getContext() {
        return this.ctx;
    }

    public byte[] toByteArray() {
        return this.buffer.toByteArray();
    }

    public int position() {
        return this.position;
    }

    protected void finalize() throws Throwable {
        try {
            NativeCrypto.BIO_free_all(this.ctx);
        } finally {
            super.finalize();
        }
    }
}
