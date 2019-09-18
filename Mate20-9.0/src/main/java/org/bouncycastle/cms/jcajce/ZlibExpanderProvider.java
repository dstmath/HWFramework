package org.bouncycastle.cms.jcajce;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.InflaterInputStream;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.operator.InputExpander;
import org.bouncycastle.operator.InputExpanderProvider;
import org.bouncycastle.util.io.StreamOverflowException;

public class ZlibExpanderProvider implements InputExpanderProvider {
    /* access modifiers changed from: private */
    public final long limit;

    private static class LimitedInputStream extends FilterInputStream {
        private long remaining;

        public LimitedInputStream(InputStream inputStream, long j) {
            super(inputStream);
            this.remaining = j;
        }

        /* JADX WARNING: Code restructure failed: missing block: B:5:0x0019, code lost:
            if (r4 >= 0) goto L_0x001b;
         */
        public int read() throws IOException {
            if (this.remaining >= 0) {
                int read = this.in.read();
                if (read >= 0) {
                    long j = this.remaining - 1;
                    this.remaining = j;
                }
                return read;
            }
            throw new StreamOverflowException("expanded byte limit exceeded");
        }

        public int read(byte[] bArr, int i, int i2) throws IOException {
            if (i2 < 1) {
                return super.read(bArr, i, i2);
            }
            if (this.remaining < 1) {
                read();
                return -1;
            }
            if (this.remaining <= ((long) i2)) {
                i2 = (int) this.remaining;
            }
            int read = this.in.read(bArr, i, i2);
            if (read > 0) {
                this.remaining -= (long) read;
            }
            return read;
        }
    }

    public ZlibExpanderProvider() {
        this.limit = -1;
    }

    public ZlibExpanderProvider(long j) {
        this.limit = j;
    }

    public InputExpander get(final AlgorithmIdentifier algorithmIdentifier) {
        return new InputExpander() {
            public AlgorithmIdentifier getAlgorithmIdentifier() {
                return algorithmIdentifier;
            }

            public InputStream getInputStream(InputStream inputStream) {
                InflaterInputStream inflaterInputStream = new InflaterInputStream(inputStream);
                return ZlibExpanderProvider.this.limit >= 0 ? new LimitedInputStream(inflaterInputStream, ZlibExpanderProvider.this.limit) : inflaterInputStream;
            }
        };
    }
}
