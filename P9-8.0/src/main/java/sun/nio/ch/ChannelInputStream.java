package sun.nio.ch;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.IllegalBlockingModeException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.SelectableChannel;

public class ChannelInputStream extends InputStream {
    private byte[] b1 = null;
    private ByteBuffer bb = null;
    private byte[] bs = null;
    protected final ReadableByteChannel ch;

    public static int read(ReadableByteChannel ch, ByteBuffer bb) throws IOException {
        if (!(ch instanceof SelectableChannel)) {
            return ch.read(bb);
        }
        int n;
        SelectableChannel sc = (SelectableChannel) ch;
        synchronized (sc.blockingLock()) {
            if (sc.isBlocking()) {
                n = ch.read(bb);
            } else {
                throw new IllegalBlockingModeException();
            }
        }
        return n;
    }

    public ChannelInputStream(ReadableByteChannel ch) {
        this.ch = ch;
    }

    public synchronized int read() throws IOException {
        if (this.b1 == null) {
            this.b1 = new byte[1];
        }
        if (read(this.b1) != 1) {
            return -1;
        }
        return this.b1[0] & 255;
    }

    public synchronized int read(byte[] bs, int off, int len) throws IOException {
        if (off >= 0) {
            if (off <= bs.length && len >= 0) {
                if (off + len <= bs.length && off + len >= 0) {
                    if (len == 0) {
                        return 0;
                    }
                    ByteBuffer bb;
                    if (this.bs == bs) {
                        bb = this.bb;
                    } else {
                        bb = ByteBuffer.wrap(bs);
                    }
                    bb.limit(Math.min(off + len, bb.capacity()));
                    bb.position(off);
                    this.bb = bb;
                    this.bs = bs;
                    return read(bb);
                }
            }
        }
        throw new IndexOutOfBoundsException();
    }

    protected int read(ByteBuffer bb) throws IOException {
        return read(this.ch, bb);
    }

    public int available() throws IOException {
        if (!(this.ch instanceof SeekableByteChannel)) {
            return 0;
        }
        SeekableByteChannel sbc = this.ch;
        long rem = Math.max(0, sbc.size() - sbc.position());
        return rem > 2147483647L ? Integer.MAX_VALUE : (int) rem;
    }

    public void close() throws IOException {
        this.ch.close();
    }
}
