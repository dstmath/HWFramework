package java.nio;

import libcore.io.Memory;

final class HeapByteBuffer extends ByteBuffer {
    HeapByteBuffer(int cap, int lim) {
        this(cap, lim, false);
    }

    private HeapByteBuffer(int cap, int lim, boolean isReadOnly) {
        super(-1, 0, lim, cap, new byte[cap], 0);
        this.isReadOnly = isReadOnly;
    }

    HeapByteBuffer(byte[] buf, int off, int len) {
        this(buf, off, len, false);
    }

    private HeapByteBuffer(byte[] buf, int off, int len, boolean isReadOnly) {
        super(-1, off, off + len, buf.length, buf, 0);
        this.isReadOnly = isReadOnly;
    }

    private HeapByteBuffer(byte[] buf, int mark, int pos, int lim, int cap, int off, boolean isReadOnly) {
        super(mark, pos, lim, cap, buf, off);
        this.isReadOnly = isReadOnly;
    }

    public ByteBuffer slice() {
        return new HeapByteBuffer(this.hb, -1, 0, remaining(), remaining(), position() + this.offset, this.isReadOnly);
    }

    public ByteBuffer duplicate() {
        return new HeapByteBuffer(this.hb, markValue(), position(), limit(), capacity(), this.offset, this.isReadOnly);
    }

    public ByteBuffer asReadOnlyBuffer() {
        return new HeapByteBuffer(this.hb, markValue(), position(), limit(), capacity(), this.offset, true);
    }

    protected int ix(int i) {
        return this.offset + i;
    }

    public byte get() {
        return this.hb[ix(nextGetIndex())];
    }

    public byte get(int i) {
        return this.hb[ix(checkIndex(i))];
    }

    public ByteBuffer get(byte[] dst, int offset, int length) {
        Buffer.checkBounds(offset, length, dst.length);
        if (length > remaining()) {
            throw new BufferUnderflowException();
        }
        System.arraycopy(this.hb, ix(position()), dst, offset, length);
        position(position() + length);
        return this;
    }

    public boolean isDirect() {
        return false;
    }

    public boolean isReadOnly() {
        return this.isReadOnly;
    }

    public ByteBuffer put(byte x) {
        if (this.isReadOnly) {
            throw new ReadOnlyBufferException();
        }
        this.hb[ix(nextPutIndex())] = x;
        return this;
    }

    public ByteBuffer put(int i, byte x) {
        if (this.isReadOnly) {
            throw new ReadOnlyBufferException();
        }
        this.hb[ix(checkIndex(i))] = x;
        return this;
    }

    public ByteBuffer put(byte[] src, int offset, int length) {
        if (this.isReadOnly) {
            throw new ReadOnlyBufferException();
        }
        Buffer.checkBounds(offset, length, src.length);
        if (length > remaining()) {
            throw new BufferOverflowException();
        }
        System.arraycopy(src, offset, this.hb, ix(position()), length);
        position(position() + length);
        return this;
    }

    public ByteBuffer compact() {
        if (this.isReadOnly) {
            throw new ReadOnlyBufferException();
        }
        System.arraycopy(this.hb, ix(position()), this.hb, ix(0), remaining());
        position(remaining());
        limit(capacity());
        discardMark();
        return this;
    }

    byte _get(int i) {
        return this.hb[i];
    }

    void _put(int i, byte b) {
        if (this.isReadOnly) {
            throw new ReadOnlyBufferException();
        }
        this.hb[i] = b;
    }

    public char getChar() {
        return Bits.getChar(this, ix(nextGetIndex(2)), this.bigEndian);
    }

    public char getChar(int i) {
        return Bits.getChar(this, ix(checkIndex(i, 2)), this.bigEndian);
    }

    char getCharUnchecked(int i) {
        return Bits.getChar(this, ix(i), this.bigEndian);
    }

    void getUnchecked(int pos, char[] dst, int dstOffset, int length) {
        Memory.unsafeBulkGet(dst, dstOffset, length * 2, this.hb, ix(pos), 2, this.nativeByteOrder ^ 1);
    }

    public ByteBuffer putChar(char x) {
        if (this.isReadOnly) {
            throw new ReadOnlyBufferException();
        }
        Bits.putChar(this, ix(nextPutIndex(2)), x, this.bigEndian);
        return this;
    }

    public ByteBuffer putChar(int i, char x) {
        if (this.isReadOnly) {
            throw new ReadOnlyBufferException();
        }
        Bits.putChar(this, ix(checkIndex(i, 2)), x, this.bigEndian);
        return this;
    }

    void putCharUnchecked(int i, char x) {
        Bits.putChar(this, ix(i), x, this.bigEndian);
    }

    void putUnchecked(int pos, char[] src, int srcOffset, int length) {
        Memory.unsafeBulkPut(this.hb, ix(pos), length * 2, src, srcOffset, 2, this.nativeByteOrder ^ 1);
    }

    public CharBuffer asCharBuffer() {
        int size = remaining() >> 1;
        return new ByteBufferAsCharBuffer(this, -1, 0, size, size, position(), order());
    }

    public short getShort() {
        return Bits.getShort(this, ix(nextGetIndex(2)), this.bigEndian);
    }

    public short getShort(int i) {
        return Bits.getShort(this, ix(checkIndex(i, 2)), this.bigEndian);
    }

    short getShortUnchecked(int i) {
        return Bits.getShort(this, ix(i), this.bigEndian);
    }

    void getUnchecked(int pos, short[] dst, int dstOffset, int length) {
        Memory.unsafeBulkGet(dst, dstOffset, length * 2, this.hb, ix(pos), 2, this.nativeByteOrder ^ 1);
    }

    public ByteBuffer putShort(short x) {
        if (this.isReadOnly) {
            throw new ReadOnlyBufferException();
        }
        Bits.putShort(this, ix(nextPutIndex(2)), x, this.bigEndian);
        return this;
    }

    public ByteBuffer putShort(int i, short x) {
        if (this.isReadOnly) {
            throw new ReadOnlyBufferException();
        }
        Bits.putShort(this, ix(checkIndex(i, 2)), x, this.bigEndian);
        return this;
    }

    void putShortUnchecked(int i, short x) {
        Bits.putShort(this, ix(i), x, this.bigEndian);
    }

    void putUnchecked(int pos, short[] src, int srcOffset, int length) {
        Memory.unsafeBulkPut(this.hb, ix(pos), length * 2, src, srcOffset, 2, this.nativeByteOrder ^ 1);
    }

    public ShortBuffer asShortBuffer() {
        int size = remaining() >> 1;
        return new ByteBufferAsShortBuffer(this, -1, 0, size, size, position(), order());
    }

    public int getInt() {
        return Bits.getInt(this, ix(nextGetIndex(4)), this.bigEndian);
    }

    public int getInt(int i) {
        return Bits.getInt(this, ix(checkIndex(i, 4)), this.bigEndian);
    }

    int getIntUnchecked(int i) {
        return Bits.getInt(this, ix(i), this.bigEndian);
    }

    void getUnchecked(int pos, int[] dst, int dstOffset, int length) {
        Memory.unsafeBulkGet(dst, dstOffset, length * 4, this.hb, ix(pos), 4, this.nativeByteOrder ^ 1);
    }

    public ByteBuffer putInt(int x) {
        if (this.isReadOnly) {
            throw new ReadOnlyBufferException();
        }
        Bits.putInt(this, ix(nextPutIndex(4)), x, this.bigEndian);
        return this;
    }

    public ByteBuffer putInt(int i, int x) {
        if (this.isReadOnly) {
            throw new ReadOnlyBufferException();
        }
        Bits.putInt(this, ix(checkIndex(i, 4)), x, this.bigEndian);
        return this;
    }

    void putIntUnchecked(int i, int x) {
        Bits.putInt(this, ix(i), x, this.bigEndian);
    }

    void putUnchecked(int pos, int[] src, int srcOffset, int length) {
        Memory.unsafeBulkPut(this.hb, ix(pos), length * 4, src, srcOffset, 4, this.nativeByteOrder ^ 1);
    }

    public IntBuffer asIntBuffer() {
        int size = remaining() >> 2;
        return new ByteBufferAsIntBuffer(this, -1, 0, size, size, position(), order());
    }

    public long getLong() {
        return Bits.getLong(this, ix(nextGetIndex(8)), this.bigEndian);
    }

    public long getLong(int i) {
        return Bits.getLong(this, ix(checkIndex(i, 8)), this.bigEndian);
    }

    long getLongUnchecked(int i) {
        return Bits.getLong(this, ix(i), this.bigEndian);
    }

    void getUnchecked(int pos, long[] dst, int dstOffset, int length) {
        Memory.unsafeBulkGet(dst, dstOffset, length * 8, this.hb, ix(pos), 8, this.nativeByteOrder ^ 1);
    }

    public ByteBuffer putLong(long x) {
        if (this.isReadOnly) {
            throw new ReadOnlyBufferException();
        }
        Bits.putLong(this, ix(nextPutIndex(8)), x, this.bigEndian);
        return this;
    }

    public ByteBuffer putLong(int i, long x) {
        if (this.isReadOnly) {
            throw new ReadOnlyBufferException();
        }
        Bits.putLong(this, ix(checkIndex(i, 8)), x, this.bigEndian);
        return this;
    }

    void putLongUnchecked(int i, long x) {
        Bits.putLong(this, ix(i), x, this.bigEndian);
    }

    void putUnchecked(int pos, long[] src, int srcOffset, int length) {
        Memory.unsafeBulkPut(this.hb, ix(pos), length * 8, src, srcOffset, 8, this.nativeByteOrder ^ 1);
    }

    public LongBuffer asLongBuffer() {
        int size = remaining() >> 3;
        return new ByteBufferAsLongBuffer(this, -1, 0, size, size, position(), order());
    }

    public float getFloat() {
        return Bits.getFloat(this, ix(nextGetIndex(4)), this.bigEndian);
    }

    public float getFloat(int i) {
        return Bits.getFloat(this, ix(checkIndex(i, 4)), this.bigEndian);
    }

    float getFloatUnchecked(int i) {
        return Bits.getFloat(this, ix(i), this.bigEndian);
    }

    void getUnchecked(int pos, float[] dst, int dstOffset, int length) {
        Memory.unsafeBulkGet(dst, dstOffset, length * 4, this.hb, ix(pos), 4, this.nativeByteOrder ^ 1);
    }

    public ByteBuffer putFloat(float x) {
        if (this.isReadOnly) {
            throw new ReadOnlyBufferException();
        }
        Bits.putFloat(this, ix(nextPutIndex(4)), x, this.bigEndian);
        return this;
    }

    public ByteBuffer putFloat(int i, float x) {
        if (this.isReadOnly) {
            throw new ReadOnlyBufferException();
        }
        Bits.putFloat(this, ix(checkIndex(i, 4)), x, this.bigEndian);
        return this;
    }

    void putFloatUnchecked(int i, float x) {
        Bits.putFloat(this, ix(i), x, this.bigEndian);
    }

    void putUnchecked(int pos, float[] src, int srcOffset, int length) {
        Memory.unsafeBulkPut(this.hb, ix(pos), length * 4, src, srcOffset, 4, this.nativeByteOrder ^ 1);
    }

    public FloatBuffer asFloatBuffer() {
        int size = remaining() >> 2;
        return new ByteBufferAsFloatBuffer(this, -1, 0, size, size, position(), order());
    }

    public double getDouble() {
        return Bits.getDouble(this, ix(nextGetIndex(8)), this.bigEndian);
    }

    public double getDouble(int i) {
        return Bits.getDouble(this, ix(checkIndex(i, 8)), this.bigEndian);
    }

    double getDoubleUnchecked(int i) {
        return Bits.getDouble(this, ix(i), this.bigEndian);
    }

    void getUnchecked(int pos, double[] dst, int dstOffset, int length) {
        Memory.unsafeBulkGet(dst, dstOffset, length * 8, this.hb, ix(pos), 8, this.nativeByteOrder ^ 1);
    }

    public ByteBuffer putDouble(double x) {
        if (this.isReadOnly) {
            throw new ReadOnlyBufferException();
        }
        Bits.putDouble(this, ix(nextPutIndex(8)), x, this.bigEndian);
        return this;
    }

    public ByteBuffer putDouble(int i, double x) {
        if (this.isReadOnly) {
            throw new ReadOnlyBufferException();
        }
        Bits.putDouble(this, ix(checkIndex(i, 8)), x, this.bigEndian);
        return this;
    }

    void putDoubleUnchecked(int i, double x) {
        Bits.putDouble(this, ix(i), x, this.bigEndian);
    }

    void putUnchecked(int pos, double[] src, int srcOffset, int length) {
        Memory.unsafeBulkPut(this.hb, ix(pos), length * 8, src, srcOffset, 8, this.nativeByteOrder ^ 1);
    }

    public DoubleBuffer asDoubleBuffer() {
        int size = remaining() >> 3;
        return new ByteBufferAsDoubleBuffer(this, -1, 0, size, size, position(), order());
    }
}
