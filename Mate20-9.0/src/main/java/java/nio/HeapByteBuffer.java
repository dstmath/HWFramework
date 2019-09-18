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
        HeapByteBuffer heapByteBuffer = new HeapByteBuffer(this.hb, -1, 0, remaining(), remaining(), position() + this.offset, this.isReadOnly);
        return heapByteBuffer;
    }

    public ByteBuffer duplicate() {
        HeapByteBuffer heapByteBuffer = new HeapByteBuffer(this.hb, markValue(), position(), limit(), capacity(), this.offset, this.isReadOnly);
        return heapByteBuffer;
    }

    public ByteBuffer asReadOnlyBuffer() {
        HeapByteBuffer heapByteBuffer = new HeapByteBuffer(this.hb, markValue(), position(), limit(), capacity(), this.offset, true);
        return heapByteBuffer;
    }

    /* access modifiers changed from: protected */
    public int ix(int i) {
        return this.offset + i;
    }

    public byte get() {
        return this.hb[ix(nextGetIndex())];
    }

    public byte get(int i) {
        return this.hb[ix(checkIndex(i))];
    }

    public ByteBuffer get(byte[] dst, int offset, int length) {
        checkBounds(offset, length, dst.length);
        if (length <= remaining()) {
            System.arraycopy(this.hb, ix(position()), dst, offset, length);
            position(position() + length);
            return this;
        }
        throw new BufferUnderflowException();
    }

    public boolean isDirect() {
        return false;
    }

    public boolean isReadOnly() {
        return this.isReadOnly;
    }

    public ByteBuffer put(byte x) {
        if (!this.isReadOnly) {
            this.hb[ix(nextPutIndex())] = x;
            return this;
        }
        throw new ReadOnlyBufferException();
    }

    public ByteBuffer put(int i, byte x) {
        if (!this.isReadOnly) {
            this.hb[ix(checkIndex(i))] = x;
            return this;
        }
        throw new ReadOnlyBufferException();
    }

    public ByteBuffer put(byte[] src, int offset, int length) {
        if (!this.isReadOnly) {
            checkBounds(offset, length, src.length);
            if (length <= remaining()) {
                System.arraycopy(src, offset, this.hb, ix(position()), length);
                position(position() + length);
                return this;
            }
            throw new BufferOverflowException();
        }
        throw new ReadOnlyBufferException();
    }

    public ByteBuffer compact() {
        if (!this.isReadOnly) {
            System.arraycopy(this.hb, ix(position()), this.hb, ix(0), remaining());
            position(remaining());
            limit(capacity());
            discardMark();
            return this;
        }
        throw new ReadOnlyBufferException();
    }

    /* access modifiers changed from: package-private */
    public byte _get(int i) {
        return this.hb[i];
    }

    /* access modifiers changed from: package-private */
    public void _put(int i, byte b) {
        if (!this.isReadOnly) {
            this.hb[i] = b;
            return;
        }
        throw new ReadOnlyBufferException();
    }

    public char getChar() {
        return Bits.getChar(this, ix(nextGetIndex(2)), this.bigEndian);
    }

    public char getChar(int i) {
        return Bits.getChar(this, ix(checkIndex(i, 2)), this.bigEndian);
    }

    /* access modifiers changed from: package-private */
    public char getCharUnchecked(int i) {
        return Bits.getChar(this, ix(i), this.bigEndian);
    }

    /* access modifiers changed from: package-private */
    public void getUnchecked(int pos, char[] dst, int dstOffset, int length) {
        char[] cArr = dst;
        int i = dstOffset;
        Memory.unsafeBulkGet(cArr, i, length * 2, this.hb, ix(pos), 2, !this.nativeByteOrder);
    }

    public ByteBuffer putChar(char x) {
        if (!this.isReadOnly) {
            Bits.putChar(this, ix(nextPutIndex(2)), x, this.bigEndian);
            return this;
        }
        throw new ReadOnlyBufferException();
    }

    public ByteBuffer putChar(int i, char x) {
        if (!this.isReadOnly) {
            Bits.putChar(this, ix(checkIndex(i, 2)), x, this.bigEndian);
            return this;
        }
        throw new ReadOnlyBufferException();
    }

    /* access modifiers changed from: package-private */
    public void putCharUnchecked(int i, char x) {
        Bits.putChar(this, ix(i), x, this.bigEndian);
    }

    /* access modifiers changed from: package-private */
    public void putUnchecked(int pos, char[] src, int srcOffset, int length) {
        Memory.unsafeBulkPut(this.hb, ix(pos), length * 2, src, srcOffset, 2, !this.nativeByteOrder);
    }

    public CharBuffer asCharBuffer() {
        int size = remaining() >> 1;
        ByteBufferAsCharBuffer byteBufferAsCharBuffer = new ByteBufferAsCharBuffer(this, -1, 0, size, size, position(), order());
        return byteBufferAsCharBuffer;
    }

    public short getShort() {
        return Bits.getShort(this, ix(nextGetIndex(2)), this.bigEndian);
    }

    public short getShort(int i) {
        return Bits.getShort(this, ix(checkIndex(i, 2)), this.bigEndian);
    }

    /* access modifiers changed from: package-private */
    public short getShortUnchecked(int i) {
        return Bits.getShort(this, ix(i), this.bigEndian);
    }

    /* access modifiers changed from: package-private */
    public void getUnchecked(int pos, short[] dst, int dstOffset, int length) {
        short[] sArr = dst;
        int i = dstOffset;
        Memory.unsafeBulkGet(sArr, i, length * 2, this.hb, ix(pos), 2, !this.nativeByteOrder);
    }

    public ByteBuffer putShort(short x) {
        if (!this.isReadOnly) {
            Bits.putShort(this, ix(nextPutIndex(2)), x, this.bigEndian);
            return this;
        }
        throw new ReadOnlyBufferException();
    }

    public ByteBuffer putShort(int i, short x) {
        if (!this.isReadOnly) {
            Bits.putShort(this, ix(checkIndex(i, 2)), x, this.bigEndian);
            return this;
        }
        throw new ReadOnlyBufferException();
    }

    /* access modifiers changed from: package-private */
    public void putShortUnchecked(int i, short x) {
        Bits.putShort(this, ix(i), x, this.bigEndian);
    }

    /* access modifiers changed from: package-private */
    public void putUnchecked(int pos, short[] src, int srcOffset, int length) {
        Memory.unsafeBulkPut(this.hb, ix(pos), length * 2, src, srcOffset, 2, !this.nativeByteOrder);
    }

    public ShortBuffer asShortBuffer() {
        int size = remaining() >> 1;
        ByteBufferAsShortBuffer byteBufferAsShortBuffer = new ByteBufferAsShortBuffer(this, -1, 0, size, size, position(), order());
        return byteBufferAsShortBuffer;
    }

    public int getInt() {
        return Bits.getInt(this, ix(nextGetIndex(4)), this.bigEndian);
    }

    public int getInt(int i) {
        return Bits.getInt(this, ix(checkIndex(i, 4)), this.bigEndian);
    }

    /* access modifiers changed from: package-private */
    public int getIntUnchecked(int i) {
        return Bits.getInt(this, ix(i), this.bigEndian);
    }

    /* access modifiers changed from: package-private */
    public void getUnchecked(int pos, int[] dst, int dstOffset, int length) {
        int[] iArr = dst;
        int i = dstOffset;
        Memory.unsafeBulkGet(iArr, i, length * 4, this.hb, ix(pos), 4, !this.nativeByteOrder);
    }

    public ByteBuffer putInt(int x) {
        if (!this.isReadOnly) {
            Bits.putInt(this, ix(nextPutIndex(4)), x, this.bigEndian);
            return this;
        }
        throw new ReadOnlyBufferException();
    }

    public ByteBuffer putInt(int i, int x) {
        if (!this.isReadOnly) {
            Bits.putInt(this, ix(checkIndex(i, 4)), x, this.bigEndian);
            return this;
        }
        throw new ReadOnlyBufferException();
    }

    /* access modifiers changed from: package-private */
    public void putIntUnchecked(int i, int x) {
        Bits.putInt(this, ix(i), x, this.bigEndian);
    }

    /* access modifiers changed from: package-private */
    public void putUnchecked(int pos, int[] src, int srcOffset, int length) {
        Memory.unsafeBulkPut(this.hb, ix(pos), length * 4, src, srcOffset, 4, !this.nativeByteOrder);
    }

    public IntBuffer asIntBuffer() {
        int size = remaining() >> 2;
        ByteBufferAsIntBuffer byteBufferAsIntBuffer = new ByteBufferAsIntBuffer(this, -1, 0, size, size, position(), order());
        return byteBufferAsIntBuffer;
    }

    public long getLong() {
        return Bits.getLong(this, ix(nextGetIndex(8)), this.bigEndian);
    }

    public long getLong(int i) {
        return Bits.getLong(this, ix(checkIndex(i, 8)), this.bigEndian);
    }

    /* access modifiers changed from: package-private */
    public long getLongUnchecked(int i) {
        return Bits.getLong(this, ix(i), this.bigEndian);
    }

    /* access modifiers changed from: package-private */
    public void getUnchecked(int pos, long[] dst, int dstOffset, int length) {
        long[] jArr = dst;
        int i = dstOffset;
        Memory.unsafeBulkGet(jArr, i, length * 8, this.hb, ix(pos), 8, !this.nativeByteOrder);
    }

    public ByteBuffer putLong(long x) {
        if (!this.isReadOnly) {
            Bits.putLong(this, ix(nextPutIndex(8)), x, this.bigEndian);
            return this;
        }
        throw new ReadOnlyBufferException();
    }

    public ByteBuffer putLong(int i, long x) {
        if (!this.isReadOnly) {
            Bits.putLong(this, ix(checkIndex(i, 8)), x, this.bigEndian);
            return this;
        }
        throw new ReadOnlyBufferException();
    }

    /* access modifiers changed from: package-private */
    public void putLongUnchecked(int i, long x) {
        Bits.putLong(this, ix(i), x, this.bigEndian);
    }

    /* access modifiers changed from: package-private */
    public void putUnchecked(int pos, long[] src, int srcOffset, int length) {
        Memory.unsafeBulkPut(this.hb, ix(pos), length * 8, src, srcOffset, 8, !this.nativeByteOrder);
    }

    public LongBuffer asLongBuffer() {
        int size = remaining() >> 3;
        ByteBufferAsLongBuffer byteBufferAsLongBuffer = new ByteBufferAsLongBuffer(this, -1, 0, size, size, position(), order());
        return byteBufferAsLongBuffer;
    }

    public float getFloat() {
        return Bits.getFloat(this, ix(nextGetIndex(4)), this.bigEndian);
    }

    public float getFloat(int i) {
        return Bits.getFloat(this, ix(checkIndex(i, 4)), this.bigEndian);
    }

    /* access modifiers changed from: package-private */
    public float getFloatUnchecked(int i) {
        return Bits.getFloat(this, ix(i), this.bigEndian);
    }

    /* access modifiers changed from: package-private */
    public void getUnchecked(int pos, float[] dst, int dstOffset, int length) {
        float[] fArr = dst;
        int i = dstOffset;
        Memory.unsafeBulkGet(fArr, i, length * 4, this.hb, ix(pos), 4, !this.nativeByteOrder);
    }

    public ByteBuffer putFloat(float x) {
        if (!this.isReadOnly) {
            Bits.putFloat(this, ix(nextPutIndex(4)), x, this.bigEndian);
            return this;
        }
        throw new ReadOnlyBufferException();
    }

    public ByteBuffer putFloat(int i, float x) {
        if (!this.isReadOnly) {
            Bits.putFloat(this, ix(checkIndex(i, 4)), x, this.bigEndian);
            return this;
        }
        throw new ReadOnlyBufferException();
    }

    /* access modifiers changed from: package-private */
    public void putFloatUnchecked(int i, float x) {
        Bits.putFloat(this, ix(i), x, this.bigEndian);
    }

    /* access modifiers changed from: package-private */
    public void putUnchecked(int pos, float[] src, int srcOffset, int length) {
        Memory.unsafeBulkPut(this.hb, ix(pos), length * 4, src, srcOffset, 4, !this.nativeByteOrder);
    }

    public FloatBuffer asFloatBuffer() {
        int size = remaining() >> 2;
        ByteBufferAsFloatBuffer byteBufferAsFloatBuffer = new ByteBufferAsFloatBuffer(this, -1, 0, size, size, position(), order());
        return byteBufferAsFloatBuffer;
    }

    public double getDouble() {
        return Bits.getDouble(this, ix(nextGetIndex(8)), this.bigEndian);
    }

    public double getDouble(int i) {
        return Bits.getDouble(this, ix(checkIndex(i, 8)), this.bigEndian);
    }

    /* access modifiers changed from: package-private */
    public double getDoubleUnchecked(int i) {
        return Bits.getDouble(this, ix(i), this.bigEndian);
    }

    /* access modifiers changed from: package-private */
    public void getUnchecked(int pos, double[] dst, int dstOffset, int length) {
        double[] dArr = dst;
        int i = dstOffset;
        Memory.unsafeBulkGet(dArr, i, length * 8, this.hb, ix(pos), 8, !this.nativeByteOrder);
    }

    public ByteBuffer putDouble(double x) {
        if (!this.isReadOnly) {
            Bits.putDouble(this, ix(nextPutIndex(8)), x, this.bigEndian);
            return this;
        }
        throw new ReadOnlyBufferException();
    }

    public ByteBuffer putDouble(int i, double x) {
        if (!this.isReadOnly) {
            Bits.putDouble(this, ix(checkIndex(i, 8)), x, this.bigEndian);
            return this;
        }
        throw new ReadOnlyBufferException();
    }

    /* access modifiers changed from: package-private */
    public void putDoubleUnchecked(int i, double x) {
        Bits.putDouble(this, ix(i), x, this.bigEndian);
    }

    /* access modifiers changed from: package-private */
    public void putUnchecked(int pos, double[] src, int srcOffset, int length) {
        Memory.unsafeBulkPut(this.hb, ix(pos), length * 8, src, srcOffset, 8, !this.nativeByteOrder);
    }

    public DoubleBuffer asDoubleBuffer() {
        int size = remaining() >> 3;
        ByteBufferAsDoubleBuffer byteBufferAsDoubleBuffer = new ByteBufferAsDoubleBuffer(this, -1, 0, size, size, position(), order());
        return byteBufferAsDoubleBuffer;
    }
}
