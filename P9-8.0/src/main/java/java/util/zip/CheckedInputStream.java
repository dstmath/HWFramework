package java.util.zip;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class CheckedInputStream extends FilterInputStream {
    private Checksum cksum;

    public CheckedInputStream(InputStream in, Checksum cksum) {
        super(in);
        this.cksum = cksum;
    }

    public int read() throws IOException {
        int b = this.in.read();
        if (b != -1) {
            this.cksum.update(b);
        }
        return b;
    }

    public int read(byte[] buf, int off, int len) throws IOException {
        len = this.in.read(buf, off, len);
        if (len != -1) {
            this.cksum.update(buf, off, len);
        }
        return len;
    }

    public long skip(long n) throws IOException {
        byte[] buf = new byte[512];
        long total = 0;
        while (total < n) {
            long len = n - total;
            len = (long) read(buf, 0, len < ((long) buf.length) ? (int) len : buf.length);
            if (len == -1) {
                return total;
            }
            total += len;
        }
        return total;
    }

    public Checksum getChecksum() {
        return this.cksum;
    }
}
