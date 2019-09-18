package java.nio;

import java.nio.DirectByteBuffer;
import libcore.io.Memory;

public abstract class ByteBuffer extends Buffer implements Comparable<ByteBuffer> {
    boolean bigEndian;
    final byte[] hb;
    boolean isReadOnly;
    boolean nativeByteOrder;
    final int offset;

    /* access modifiers changed from: package-private */
    public abstract byte _get(int i);

    /* access modifiers changed from: package-private */
    public abstract void _put(int i, byte b);

    public abstract CharBuffer asCharBuffer();

    public abstract DoubleBuffer asDoubleBuffer();

    public abstract FloatBuffer asFloatBuffer();

    public abstract IntBuffer asIntBuffer();

    public abstract LongBuffer asLongBuffer();

    public abstract ByteBuffer asReadOnlyBuffer();

    public abstract ShortBuffer asShortBuffer();

    public abstract ByteBuffer compact();

    public abstract ByteBuffer duplicate();

    public abstract byte get();

    public abstract byte get(int i);

    public abstract char getChar();

    public abstract char getChar(int i);

    public abstract double getDouble();

    public abstract double getDouble(int i);

    public abstract float getFloat();

    public abstract float getFloat(int i);

    public abstract int getInt();

    public abstract int getInt(int i);

    public abstract long getLong();

    public abstract long getLong(int i);

    public abstract short getShort();

    public abstract short getShort(int i);

    public abstract boolean isDirect();

    public abstract ByteBuffer put(byte b);

    public abstract ByteBuffer put(int i, byte b);

    public abstract ByteBuffer putChar(char c);

    public abstract ByteBuffer putChar(int i, char c);

    public abstract ByteBuffer putDouble(double d);

    public abstract ByteBuffer putDouble(int i, double d);

    public abstract ByteBuffer putFloat(float f);

    public abstract ByteBuffer putFloat(int i, float f);

    public abstract ByteBuffer putInt(int i);

    public abstract ByteBuffer putInt(int i, int i2);

    public abstract ByteBuffer putLong(int i, long j);

    public abstract ByteBuffer putLong(long j);

    public abstract ByteBuffer putShort(int i, short s);

    public abstract ByteBuffer putShort(short s);

    public abstract ByteBuffer slice();

    ByteBuffer(int mark, int pos, int lim, int cap, byte[] hb2, int offset2) {
        super(mark, pos, lim, cap, 0);
        boolean z = true;
        this.bigEndian = true;
        this.nativeByteOrder = Bits.byteOrder() != ByteOrder.BIG_ENDIAN ? false : z;
        this.hb = hb2;
        this.offset = offset2;
    }

    ByteBuffer(int mark, int pos, int lim, int cap) {
        this(mark, pos, lim, cap, null, 0);
    }

    public static ByteBuffer allocateDirect(int capacity) {
        if (capacity >= 0) {
            return new DirectByteBuffer(capacity, new DirectByteBuffer.MemoryRef(capacity));
        }
        throw new IllegalArgumentException("capacity < 0: " + capacity);
    }

    public static ByteBuffer allocate(int capacity) {
        if (capacity >= 0) {
            return new HeapByteBuffer(capacity, capacity);
        }
        throw new IllegalArgumentException();
    }

    public static ByteBuffer wrap(byte[] array, int offset2, int length) {
        try {
            return new HeapByteBuffer(array, offset2, length);
        } catch (IllegalArgumentException e) {
            throw new IndexOutOfBoundsException();
        }
    }

    public static ByteBuffer wrap(byte[] array) {
        return wrap(array, 0, array.length);
    }

    public ByteBuffer get(byte[] dst, int offset2, int length) {
        checkBounds(offset2, length, dst.length);
        if (length <= remaining()) {
            int end = offset2 + length;
            for (int i = offset2; i < end; i++) {
                dst[i] = get();
            }
            return this;
        }
        throw new BufferUnderflowException();
    }

    public ByteBuffer get(byte[] dst) {
        return get(dst, 0, dst.length);
    }

    /* JADX WARNING: Multi-variable type inference failed */
    public ByteBuffer put(ByteBuffer src) {
        if (!isAccessible()) {
            throw new IllegalStateException("buffer is inaccessible");
        } else if (src == this) {
            throw new IllegalArgumentException();
        } else if (!this.isReadOnly) {
            int n = src.remaining();
            if (n <= remaining()) {
                if (this.hb == null || src.hb == null) {
                    Object srcObject = src.isDirect() ? src : src.hb;
                    int srcOffset = src.position();
                    if (!src.isDirect()) {
                        srcOffset += src.offset;
                    }
                    Object dstObject = isDirect() ? this : this.hb;
                    int dstOffset = position();
                    if (!isDirect()) {
                        dstOffset += this.offset;
                    }
                    Memory.memmove(dstObject, dstOffset, srcObject, srcOffset, (long) n);
                } else {
                    System.arraycopy(src.hb, src.position() + src.offset, this.hb, position() + this.offset, n);
                }
                src.position(src.limit());
                position(position() + n);
                return this;
            }
            throw new BufferOverflowException();
        } else {
            throw new ReadOnlyBufferException();
        }
    }

    public ByteBuffer put(byte[] src, int offset2, int length) {
        checkBounds(offset2, length, src.length);
        if (length <= remaining()) {
            int end = offset2 + length;
            for (int i = offset2; i < end; i++) {
                put(src[i]);
            }
            return this;
        }
        throw new BufferOverflowException();
    }

    public final ByteBuffer put(byte[] src) {
        return put(src, 0, src.length);
    }

    public final boolean hasArray() {
        return this.hb != null && !this.isReadOnly;
    }

    public final byte[] array() {
        if (this.hb == null) {
            throw new UnsupportedOperationException();
        } else if (!this.isReadOnly) {
            return this.hb;
        } else {
            throw new ReadOnlyBufferException();
        }
    }

    public final int arrayOffset() {
        if (this.hb == null) {
            throw new UnsupportedOperationException();
        } else if (!this.isReadOnly) {
            return this.offset;
        } else {
            throw new ReadOnlyBufferException();
        }
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(getClass().getName());
        sb.append("[pos=");
        sb.append(position());
        sb.append(" lim=");
        sb.append(limit());
        sb.append(" cap=");
        sb.append(capacity());
        sb.append("]");
        return sb.toString();
    }

    public int hashCode() {
        int h = 1;
        int p = position();
        for (int i = limit() - 1; i >= p; i--) {
            h = (31 * h) + get(i);
        }
        return h;
    }

    public boolean equals(Object ob) {
        if (this == ob) {
            return true;
        }
        if (!(ob instanceof ByteBuffer)) {
            return false;
        }
        ByteBuffer that = (ByteBuffer) ob;
        if (remaining() != that.remaining()) {
            return false;
        }
        int p = position();
        int i = limit() - 1;
        int j = that.limit() - 1;
        while (i >= p) {
            if (!equals(get(i), that.get(j))) {
                return false;
            }
            i--;
            j--;
        }
        return true;
    }

    private static boolean equals(byte x, byte y) {
        return x == y;
    }

    public int compareTo(ByteBuffer that) {
        int n = position() + Math.min(remaining(), that.remaining());
        int i = position();
        int j = that.position();
        while (i < n) {
            int cmp = compare(get(i), that.get(j));
            if (cmp != 0) {
                return cmp;
            }
            i++;
            j++;
        }
        return remaining() - that.remaining();
    }

    private static int compare(byte x, byte y) {
        return Byte.compare(x, y);
    }

    public final ByteOrder order() {
        return this.bigEndian ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;
    }

    public final ByteBuffer order(ByteOrder bo) {
        boolean z = false;
        this.bigEndian = bo == ByteOrder.BIG_ENDIAN;
        if (this.bigEndian == (Bits.byteOrder() == ByteOrder.BIG_ENDIAN)) {
            z = true;
        }
        this.nativeByteOrder = z;
        return this;
    }

    /* access modifiers changed from: package-private */
    public char getCharUnchecked(int index) {
        throw new UnsupportedOperationException();
    }

    /* access modifiers changed from: package-private */
    public void getUnchecked(int pos, char[] dst, int dstOffset, int length) {
        throw new UnsupportedOperationException();
    }

    /* access modifiers changed from: package-private */
    public void putCharUnchecked(int index, char value) {
        throw new UnsupportedOperationException();
    }

    /* access modifiers changed from: package-private */
    public void putUnchecked(int pos, char[] dst, int srcOffset, int length) {
        throw new UnsupportedOperationException();
    }

    /* access modifiers changed from: package-private */
    public short getShortUnchecked(int index) {
        throw new UnsupportedOperationException();
    }

    /* access modifiers changed from: package-private */
    public void getUnchecked(int pos, short[] dst, int dstOffset, int length) {
        throw new UnsupportedOperationException();
    }

    /* access modifiers changed from: package-private */
    public void putShortUnchecked(int index, short value) {
        throw new UnsupportedOperationException();
    }

    /* access modifiers changed from: package-private */
    public void putUnchecked(int pos, short[] dst, int srcOffset, int length) {
        throw new UnsupportedOperationException();
    }

    /* access modifiers changed from: package-private */
    public int getIntUnchecked(int index) {
        throw new UnsupportedOperationException();
    }

    /* access modifiers changed from: package-private */
    public void getUnchecked(int pos, int[] dst, int dstOffset, int length) {
        throw new UnsupportedOperationException();
    }

    /* access modifiers changed from: package-private */
    public void putIntUnchecked(int index, int value) {
        throw new UnsupportedOperationException();
    }

    /* access modifiers changed from: package-private */
    public void putUnchecked(int pos, int[] dst, int srcOffset, int length) {
        throw new UnsupportedOperationException();
    }

    /* access modifiers changed from: package-private */
    public long getLongUnchecked(int index) {
        throw new UnsupportedOperationException();
    }

    /* access modifiers changed from: package-private */
    public void getUnchecked(int pos, long[] dst, int dstOffset, int length) {
        throw new UnsupportedOperationException();
    }

    /* access modifiers changed from: package-private */
    public void putLongUnchecked(int index, long value) {
        throw new UnsupportedOperationException();
    }

    /* access modifiers changed from: package-private */
    public void putUnchecked(int pos, long[] dst, int srcOffset, int length) {
        throw new UnsupportedOperationException();
    }

    /* access modifiers changed from: package-private */
    public float getFloatUnchecked(int index) {
        throw new UnsupportedOperationException();
    }

    /* access modifiers changed from: package-private */
    public void getUnchecked(int pos, float[] dst, int dstOffset, int length) {
        throw new UnsupportedOperationException();
    }

    /* access modifiers changed from: package-private */
    public void putFloatUnchecked(int index, float value) {
        throw new UnsupportedOperationException();
    }

    /* access modifiers changed from: package-private */
    public void putUnchecked(int pos, float[] dst, int srcOffset, int length) {
        throw new UnsupportedOperationException();
    }

    /* access modifiers changed from: package-private */
    public double getDoubleUnchecked(int index) {
        throw new UnsupportedOperationException();
    }

    /* access modifiers changed from: package-private */
    public void getUnchecked(int pos, double[] dst, int dstOffset, int length) {
        throw new UnsupportedOperationException();
    }

    /* access modifiers changed from: package-private */
    public void putDoubleUnchecked(int index, double value) {
        throw new UnsupportedOperationException();
    }

    /* access modifiers changed from: package-private */
    public void putUnchecked(int pos, double[] dst, int srcOffset, int length) {
        throw new UnsupportedOperationException();
    }

    public boolean isAccessible() {
        return true;
    }

    public void setAccessible(boolean value) {
        throw new UnsupportedOperationException();
    }
}
