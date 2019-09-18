package com.android.org.conscrypt;

import java.io.ByteArrayOutputStream;

final class OpenSSLBIOSink {
    private final ByteArrayOutputStream buffer;
    private final long ctx;
    private int position;

    static OpenSSLBIOSink create() {
        return new OpenSSLBIOSink(new ByteArrayOutputStream());
    }

    private OpenSSLBIOSink(ByteArrayOutputStream buffer2) {
        this.ctx = NativeCrypto.create_BIO_OutputStream(buffer2);
        this.buffer = buffer2;
    }

    /* access modifiers changed from: package-private */
    public int available() {
        return this.buffer.size() - this.position;
    }

    /* access modifiers changed from: package-private */
    public void reset() {
        this.buffer.reset();
        this.position = 0;
    }

    /* access modifiers changed from: package-private */
    public long skip(long byteCount) {
        int maxLength = Math.min(available(), (int) byteCount);
        this.position += maxLength;
        if (this.position == this.buffer.size()) {
            reset();
        }
        return (long) maxLength;
    }

    /* access modifiers changed from: package-private */
    public long getContext() {
        return this.ctx;
    }

    /* access modifiers changed from: package-private */
    public byte[] toByteArray() {
        return this.buffer.toByteArray();
    }

    /* access modifiers changed from: package-private */
    public int position() {
        return this.position;
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        try {
            NativeCrypto.BIO_free_all(this.ctx);
        } finally {
            super.finalize();
        }
    }
}
