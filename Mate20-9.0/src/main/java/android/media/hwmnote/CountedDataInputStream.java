package android.media.hwmnote;

import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

class CountedDataInputStream extends FilterInputStream {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static final int BYTE_ARRAY_SIZE = 8;
    private static final int INT_BYTE_COUNT = 4;
    private static final int SHORT_BYTE_COUNT = 2;
    private static final long UNSIGNED_DWORD = 4294967295L;
    private static final int UNSIGNED_WORD = 65535;
    private final byte[] mByteArray = new byte[8];
    private final ByteBuffer mByteBuffer = ByteBuffer.wrap(this.mByteArray);
    private int mCount = 0;

    protected CountedDataInputStream(InputStream in) {
        super(in);
    }

    public int getReadByteCount() {
        return this.mCount;
    }

    public int read(byte[] b) throws IOException {
        int r = this.in.read(b);
        this.mCount += r >= 0 ? r : 0;
        return r;
    }

    public int read(byte[] b, int off, int len) throws IOException {
        int r = this.in.read(b, off, len);
        this.mCount += r >= 0 ? r : 0;
        return r;
    }

    public int read() throws IOException {
        int r = this.in.read();
        this.mCount += r >= 0 ? 1 : 0;
        return r;
    }

    public long skip(long length) throws IOException {
        long skip = this.in.skip(length);
        this.mCount = (int) (((long) this.mCount) + skip);
        return skip;
    }

    public void skipOrThrow(long length) throws IOException {
        if (skip(length) != length) {
            throw new EOFException();
        }
    }

    public void skipTo(long target) throws IOException {
        skipOrThrow(target - ((long) this.mCount));
    }

    public void readOrThrow(byte[] b, int off, int len) throws IOException {
        if (read(b, off, len) != len) {
            throw new EOFException();
        }
    }

    public void readOrThrow(byte[] b) throws IOException {
        readOrThrow(b, 0, b.length);
    }

    public void setByteOrder(ByteOrder order) {
        this.mByteBuffer.order(order);
    }

    public ByteOrder getByteOrder() {
        return this.mByteBuffer.order();
    }

    public short readShort() throws IOException {
        readOrThrow(this.mByteArray, 0, 2);
        this.mByteBuffer.rewind();
        return this.mByteBuffer.getShort();
    }

    public int readUnsignedShort() throws IOException {
        return readShort() & 65535;
    }

    public int readInt() throws IOException {
        readOrThrow(this.mByteArray, 0, 4);
        this.mByteBuffer.rewind();
        return this.mByteBuffer.getInt();
    }

    public long readUnsignedInt() throws IOException {
        return ((long) readInt()) & UNSIGNED_DWORD;
    }

    public long readLong() throws IOException {
        readOrThrow(this.mByteArray, 0, 8);
        this.mByteBuffer.rewind();
        return this.mByteBuffer.getLong();
    }
}
