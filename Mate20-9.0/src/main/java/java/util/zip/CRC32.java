package java.util.zip;

import java.nio.ByteBuffer;
import sun.nio.ch.DirectBuffer;

public class CRC32 implements Checksum {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private int crc;

    private static native int update(int i, int i2);

    private static native int updateByteBuffer(int i, long j, int i2, int i3);

    private static native int updateBytes(int i, byte[] bArr, int i2, int i3);

    public void update(int b) {
        this.crc = update(this.crc, b);
    }

    public void update(byte[] b, int off, int len) {
        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || off > b.length - len) {
            throw new ArrayIndexOutOfBoundsException();
        } else {
            this.crc = updateBytes(this.crc, b, off, len);
        }
    }

    public void update(byte[] b) {
        this.crc = updateBytes(this.crc, b, 0, b.length);
    }

    public void update(ByteBuffer buffer) {
        int pos = buffer.position();
        int limit = buffer.limit();
        int rem = limit - pos;
        if (rem > 0) {
            if (buffer instanceof DirectBuffer) {
                this.crc = updateByteBuffer(this.crc, ((DirectBuffer) buffer).address(), pos, rem);
            } else if (buffer.hasArray()) {
                this.crc = updateBytes(this.crc, buffer.array(), buffer.arrayOffset() + pos, rem);
            } else {
                byte[] b = new byte[rem];
                buffer.get(b);
                this.crc = updateBytes(this.crc, b, 0, b.length);
            }
            buffer.position(limit);
        }
    }

    public void reset() {
        this.crc = 0;
    }

    public long getValue() {
        return ((long) this.crc) & 4294967295L;
    }
}
