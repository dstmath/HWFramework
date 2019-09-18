package com.android.org.conscrypt;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

final class OpenSSLBIOSource {
    private OpenSSLBIOInputStream source;

    private static class ByteBufferInputStream extends InputStream {
        private final ByteBuffer source;

        ByteBufferInputStream(ByteBuffer source2) {
            this.source = source2;
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
            long originalPosition = (long) this.source.position();
            this.source.position((int) (originalPosition + byteCount));
            return ((long) this.source.position()) - originalPosition;
        }
    }

    static OpenSSLBIOSource wrap(ByteBuffer buffer) {
        return new OpenSSLBIOSource(new OpenSSLBIOInputStream(new ByteBufferInputStream(buffer), false));
    }

    private OpenSSLBIOSource(OpenSSLBIOInputStream source2) {
        this.source = source2;
    }

    /* access modifiers changed from: package-private */
    public long getContext() {
        return this.source.getBioContext();
    }

    private synchronized void release() {
        if (this.source != null) {
            NativeCrypto.BIO_free_all(this.source.getBioContext());
            this.source = null;
        }
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        try {
            release();
        } finally {
            super.finalize();
        }
    }
}
