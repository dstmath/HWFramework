package android.util.apk;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.DigestException;

/* access modifiers changed from: package-private */
public class ByteBufferDataSource implements DataSource {
    private final ByteBuffer mBuf;

    ByteBufferDataSource(ByteBuffer buf) {
        this.mBuf = buf.slice();
    }

    @Override // android.util.apk.DataSource
    public long size() {
        return (long) this.mBuf.capacity();
    }

    @Override // android.util.apk.DataSource
    public void feedIntoDataDigester(DataDigester md, long offset, int size) throws IOException, DigestException {
        ByteBuffer region;
        synchronized (this.mBuf) {
            this.mBuf.position(0);
            this.mBuf.limit(((int) offset) + size);
            this.mBuf.position((int) offset);
            region = this.mBuf.slice();
        }
        md.consume(region);
    }
}
