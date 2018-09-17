package com.android.org.conscrypt;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public final class OpenSSLBIOSource {
    private OpenSSLBIOInputStream source;

    private static class ByteBufferInputStream extends InputStream {
        private final ByteBuffer source;

        public ByteBufferInputStream(ByteBuffer source) {
            this.source = source;
        }

        public int read() throws IOException {
            if (this.source.remaining() > 0) {
                return this.source.get();
            }
            return -1;
        }

        public int available() throws IOException {
            return this.source.limit() - this.source.position();
        }

        public int read(byte[] buffer) throws IOException {
            int originalPosition = this.source.position();
            this.source.get(buffer);
            return this.source.position() - originalPosition;
        }

        public int read(byte[] buffer, int byteOffset, int byteCount) throws IOException {
            int toRead = Math.min(this.source.remaining(), byteCount);
            int originalPosition = this.source.position();
            this.source.get(buffer, byteOffset, toRead);
            return this.source.position() - originalPosition;
        }

        public void reset() throws IOException {
            this.source.reset();
        }

        public long skip(long byteCount) throws IOException {
            int originalPosition = this.source.position();
            this.source.position((int) (((long) originalPosition) + byteCount));
            return (long) (this.source.position() - originalPosition);
        }
    }

    public static OpenSSLBIOSource wrap(ByteBuffer buffer) {
        return new OpenSSLBIOSource(new OpenSSLBIOInputStream(new ByteBufferInputStream(buffer), false));
    }

    public OpenSSLBIOSource(OpenSSLBIOInputStream source) {
        this.source = source;
    }

    public long getContext() {
        return this.source.getBioContext();
    }

    public synchronized void release() {
        if (this.source != null) {
            NativeCrypto.BIO_free_all(this.source.getBioContext());
            this.source = null;
        }
    }

    protected void finalize() throws Throwable {
        try {
            release();
        } finally {
            super.finalize();
        }
    }
}
