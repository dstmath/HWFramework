package java.util.zip;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class CheckedInputStream extends FilterInputStream {
    private Checksum cksum;

    public CheckedInputStream(InputStream in, Checksum cksum2) {
        super(in);
        this.cksum = cksum2;
    }

    public int read() throws IOException {
        int b = this.in.read();
        if (b != -1) {
            this.cksum.update(b);
        }
        return b;
    }

    public int read(byte[] buf, int off, int len) throws IOException {
        int len2 = this.in.read(buf, off, len);
        if (len2 != -1) {
            this.cksum.update(buf, off, len2);
        }
        return len2;
    }

    public long skip(long n) throws IOException {
        byte[] buf = new byte[512];
        long total = 0;
        while (total < n) {
            long len = n - total;
            long len2 = (long) read(buf, 0, len < ((long) buf.length) ? (int) len : buf.length);
            if (len2 == -1) {
                return total;
            }
            total += len2;
        }
        return total;
    }

    public Checksum getChecksum() {
        return this.cksum;
    }
}
