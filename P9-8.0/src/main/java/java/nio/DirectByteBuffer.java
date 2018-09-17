package java.nio;

import dalvik.system.VMRuntime;
import java.io.FileDescriptor;
import libcore.io.Memory;
import sun.misc.Cleaner;
import sun.nio.ch.DirectBuffer;

public class DirectByteBuffer extends MappedByteBuffer implements DirectBuffer {
    static final /* synthetic */ boolean -assertionsDisabled = (DirectByteBuffer.class.desiredAssertionStatus() ^ 1);
    final Cleaner cleaner;
    final MemoryRef memoryRef;

    static final class MemoryRef {
        long allocatedAddress;
        byte[] buffer;
        boolean isAccessible;
        boolean isFreed;
        final int offset;

        MemoryRef(int capacity) {
            VMRuntime runtime = VMRuntime.getRuntime();
            this.buffer = (byte[]) runtime.newNonMovableArray(Byte.TYPE, capacity + 7);
            this.allocatedAddress = runtime.addressOf(this.buffer);
            this.offset = (int) (((this.allocatedAddress + 7) & -8) - this.allocatedAddress);
            this.isAccessible = true;
            this.isFreed = false;
        }

        MemoryRef(long allocatedAddress) {
            this.buffer = null;
            this.allocatedAddress = allocatedAddress;
            this.offset = 0;
            this.isAccessible = true;
        }

        void free() {
            this.buffer = null;
            this.allocatedAddress = 0;
            this.isAccessible = false;
            this.isFreed = true;
        }
    }

    DirectByteBuffer(int capacity, MemoryRef memoryRef) {
        super(-1, 0, capacity, capacity, memoryRef.buffer, memoryRef.offset);
        this.memoryRef = memoryRef;
        this.address = memoryRef.allocatedAddress + ((long) memoryRef.offset);
        this.cleaner = null;
        this.isReadOnly = false;
    }

    private DirectByteBuffer(long addr, int cap) {
        super(-1, 0, cap, cap);
        this.memoryRef = new MemoryRef(addr);
        this.address = addr;
        this.cleaner = null;
    }

    public DirectByteBuffer(int cap, long addr, FileDescriptor fd, Runnable unmapper, boolean isReadOnly) {
        super(-1, 0, cap, cap, fd);
        this.isReadOnly = isReadOnly;
        this.memoryRef = new MemoryRef(addr);
        this.address = addr;
        this.cleaner = Cleaner.create(this.memoryRef, unmapper);
    }

    DirectByteBuffer(MemoryRef memoryRef, int mark, int pos, int lim, int cap, int off) {
        this(memoryRef, mark, pos, lim, cap, off, false);
    }

    DirectByteBuffer(MemoryRef memoryRef, int mark, int pos, int lim, int cap, int off, boolean isReadOnly) {
        super(mark, pos, lim, cap, memoryRef.buffer, off);
        this.isReadOnly = isReadOnly;
        this.memoryRef = memoryRef;
        this.address = memoryRef.allocatedAddress + ((long) off);
        this.cleaner = null;
    }

    public final Object attachment() {
        return this.memoryRef;
    }

    public final Cleaner cleaner() {
        return this.cleaner;
    }

    public final ByteBuffer slice() {
        if (this.memoryRef.isAccessible) {
            int pos = position();
            int lim = limit();
            if (-assertionsDisabled || pos <= lim) {
                int rem = pos <= lim ? lim - pos : 0;
                int off = pos + this.offset;
                if (-assertionsDisabled || off >= 0) {
                    return new DirectByteBuffer(this.memoryRef, -1, 0, rem, rem, off, this.isReadOnly);
                }
                throw new AssertionError();
            }
            throw new AssertionError();
        }
        throw new IllegalStateException("buffer is inaccessible");
    }

    public final ByteBuffer duplicate() {
        if (!this.memoryRef.isFreed) {
            return new DirectByteBuffer(this.memoryRef, markValue(), position(), limit(), capacity(), this.offset, this.isReadOnly);
        }
        throw new IllegalStateException("buffer has been freed");
    }

    public final ByteBuffer asReadOnlyBuffer() {
        if (!this.memoryRef.isFreed) {
            return new DirectByteBuffer(this.memoryRef, markValue(), position(), limit(), capacity(), this.offset, true);
        }
        throw new IllegalStateException("buffer has been freed");
    }

    public final long address() {
        return this.address;
    }

    private long ix(int i) {
        return this.address + ((long) i);
    }

    private byte get(long a) {
        return Memory.peekByte(a);
    }

    public final byte get() {
        if (this.memoryRef.isAccessible) {
            return get(ix(nextGetIndex()));
        }
        throw new IllegalStateException("buffer is inaccessible");
    }

    public final byte get(int i) {
        if (this.memoryRef.isAccessible) {
            return get(ix(checkIndex(i)));
        }
        throw new IllegalStateException("buffer is inaccessible");
    }

    public ByteBuffer get(byte[] dst, int dstOffset, int length) {
        if (this.memoryRef.isAccessible) {
            Buffer.checkBounds(dstOffset, length, dst.length);
            int pos = position();
            int lim = limit();
            if (-assertionsDisabled || pos <= lim) {
                if (length > (pos <= lim ? lim - pos : 0)) {
                    throw new BufferUnderflowException();
                }
                Memory.peekByteArray(ix(pos), dst, dstOffset, length);
                this.position = pos + length;
                return this;
            }
            throw new AssertionError();
        }
        throw new IllegalStateException("buffer is inaccessible");
    }

    private ByteBuffer put(long a, byte x) {
        Memory.pokeByte(a, x);
        return this;
    }

    public final ByteBuffer put(byte x) {
        if (!this.memoryRef.isAccessible) {
            throw new IllegalStateException("buffer is inaccessible");
        } else if (this.isReadOnly) {
            throw new ReadOnlyBufferException();
        } else {
            put(ix(nextPutIndex()), x);
            return this;
        }
    }

    public final ByteBuffer put(int i, byte x) {
        if (!this.memoryRef.isAccessible) {
            throw new IllegalStateException("buffer is inaccessible");
        } else if (this.isReadOnly) {
            throw new ReadOnlyBufferException();
        } else {
            put(ix(checkIndex(i)), x);
            return this;
        }
    }

    public ByteBuffer put(byte[] src, int srcOffset, int length) {
        if (!this.memoryRef.isAccessible) {
            throw new IllegalStateException("buffer is inaccessible");
        } else if (this.isReadOnly) {
            throw new ReadOnlyBufferException();
        } else {
            Buffer.checkBounds(srcOffset, length, src.length);
            int pos = position();
            int lim = limit();
            if (-assertionsDisabled || pos <= lim) {
                if (length > (pos <= lim ? lim - pos : 0)) {
                    throw new BufferOverflowException();
                }
                Memory.pokeByteArray(ix(pos), src, srcOffset, length);
                this.position = pos + length;
                return this;
            }
            throw new AssertionError();
        }
    }

    public final ByteBuffer compact() {
        if (!this.memoryRef.isAccessible) {
            throw new IllegalStateException("buffer is inaccessible");
        } else if (this.isReadOnly) {
            throw new ReadOnlyBufferException();
        } else {
            int pos = position();
            int lim = limit();
            if (-assertionsDisabled || pos <= lim) {
                int rem = pos <= lim ? lim - pos : 0;
                System.arraycopy(this.hb, this.position + this.offset, this.hb, this.offset, remaining());
                position(rem);
                limit(capacity());
                discardMark();
                return this;
            }
            throw new AssertionError();
        }
    }

    public final boolean isDirect() {
        return true;
    }

    public final boolean isReadOnly() {
        return this.isReadOnly;
    }

    final byte _get(int i) {
        return get(i);
    }

    final void _put(int i, byte b) {
        put(i, b);
    }

    public final char getChar() {
        if (this.memoryRef.isAccessible) {
            int newPosition = this.position + 2;
            if (newPosition > limit()) {
                throw new BufferUnderflowException();
            }
            char x = (char) Memory.peekShort(ix(this.position), this.nativeByteOrder ^ 1);
            this.position = newPosition;
            return x;
        }
        throw new IllegalStateException("buffer is inaccessible");
    }

    public final char getChar(int i) {
        if (this.memoryRef.isAccessible) {
            checkIndex(i, 2);
            return (char) Memory.peekShort(ix(i), this.nativeByteOrder ^ 1);
        }
        throw new IllegalStateException("buffer is inaccessible");
    }

    char getCharUnchecked(int i) {
        if (this.memoryRef.isAccessible) {
            return (char) Memory.peekShort(ix(i), this.nativeByteOrder ^ 1);
        }
        throw new IllegalStateException("buffer is inaccessible");
    }

    void getUnchecked(int pos, char[] dst, int dstOffset, int length) {
        if (this.memoryRef.isAccessible) {
            Memory.peekCharArray(ix(pos), dst, dstOffset, length, this.nativeByteOrder ^ 1);
            return;
        }
        throw new IllegalStateException("buffer is inaccessible");
    }

    private ByteBuffer putChar(long a, char x) {
        Memory.pokeShort(a, (short) x, this.nativeByteOrder ^ 1);
        return this;
    }

    public final ByteBuffer putChar(char x) {
        if (!this.memoryRef.isAccessible) {
            throw new IllegalStateException("buffer is inaccessible");
        } else if (this.isReadOnly) {
            throw new ReadOnlyBufferException();
        } else {
            putChar(ix(nextPutIndex(2)), x);
            return this;
        }
    }

    public final ByteBuffer putChar(int i, char x) {
        if (!this.memoryRef.isAccessible) {
            throw new IllegalStateException("buffer is inaccessible");
        } else if (this.isReadOnly) {
            throw new ReadOnlyBufferException();
        } else {
            putChar(ix(checkIndex(i, 2)), x);
            return this;
        }
    }

    void putCharUnchecked(int i, char x) {
        if (this.memoryRef.isAccessible) {
            putChar(ix(i), x);
            return;
        }
        throw new IllegalStateException("buffer is inaccessible");
    }

    void putUnchecked(int pos, char[] src, int srcOffset, int length) {
        if (this.memoryRef.isAccessible) {
            Memory.pokeCharArray(ix(pos), src, srcOffset, length, this.nativeByteOrder ^ 1);
            return;
        }
        throw new IllegalStateException("buffer is inaccessible");
    }

    public final CharBuffer asCharBuffer() {
        if (this.memoryRef.isFreed) {
            throw new IllegalStateException("buffer has been freed");
        }
        int off = position();
        int lim = limit();
        if (-assertionsDisabled || off <= lim) {
            int size = (off <= lim ? lim - off : 0) >> 1;
            return new ByteBufferAsCharBuffer(this, -1, 0, size, size, off, order());
        }
        throw new AssertionError();
    }

    private short getShort(long a) {
        return Memory.peekShort(a, this.nativeByteOrder ^ 1);
    }

    public final short getShort() {
        if (this.memoryRef.isAccessible) {
            return getShort(ix(nextGetIndex(2)));
        }
        throw new IllegalStateException("buffer is inaccessible");
    }

    public final short getShort(int i) {
        if (this.memoryRef.isAccessible) {
            return getShort(ix(checkIndex(i, 2)));
        }
        throw new IllegalStateException("buffer is inaccessible");
    }

    short getShortUnchecked(int i) {
        if (this.memoryRef.isAccessible) {
            return getShort(ix(i));
        }
        throw new IllegalStateException("buffer is inaccessible");
    }

    void getUnchecked(int pos, short[] dst, int dstOffset, int length) {
        if (this.memoryRef.isAccessible) {
            Memory.peekShortArray(ix(pos), dst, dstOffset, length, this.nativeByteOrder ^ 1);
            return;
        }
        throw new IllegalStateException("buffer is inaccessible");
    }

    private ByteBuffer putShort(long a, short x) {
        Memory.pokeShort(a, x, this.nativeByteOrder ^ 1);
        return this;
    }

    public final ByteBuffer putShort(short x) {
        if (!this.memoryRef.isAccessible) {
            throw new IllegalStateException("buffer is inaccessible");
        } else if (this.isReadOnly) {
            throw new ReadOnlyBufferException();
        } else {
            putShort(ix(nextPutIndex(2)), x);
            return this;
        }
    }

    public final ByteBuffer putShort(int i, short x) {
        if (!this.memoryRef.isAccessible) {
            throw new IllegalStateException("buffer is inaccessible");
        } else if (this.isReadOnly) {
            throw new ReadOnlyBufferException();
        } else {
            putShort(ix(checkIndex(i, 2)), x);
            return this;
        }
    }

    void putShortUnchecked(int i, short x) {
        if (this.memoryRef.isAccessible) {
            putShort(ix(i), x);
            return;
        }
        throw new IllegalStateException("buffer is inaccessible");
    }

    void putUnchecked(int pos, short[] src, int srcOffset, int length) {
        if (this.memoryRef.isAccessible) {
            Memory.pokeShortArray(ix(pos), src, srcOffset, length, this.nativeByteOrder ^ 1);
            return;
        }
        throw new IllegalStateException("buffer is inaccessible");
    }

    public final ShortBuffer asShortBuffer() {
        if (this.memoryRef.isFreed) {
            throw new IllegalStateException("buffer has been freed");
        }
        int off = position();
        int lim = limit();
        if (-assertionsDisabled || off <= lim) {
            int size = (off <= lim ? lim - off : 0) >> 1;
            return new ByteBufferAsShortBuffer(this, -1, 0, size, size, off, order());
        }
        throw new AssertionError();
    }

    private int getInt(long a) {
        return Memory.peekInt(a, this.nativeByteOrder ^ 1);
    }

    public int getInt() {
        if (this.memoryRef.isAccessible) {
            return getInt(ix(nextGetIndex(4)));
        }
        throw new IllegalStateException("buffer is inaccessible");
    }

    public int getInt(int i) {
        if (this.memoryRef.isAccessible) {
            return getInt(ix(checkIndex(i, 4)));
        }
        throw new IllegalStateException("buffer is inaccessible");
    }

    final int getIntUnchecked(int i) {
        if (this.memoryRef.isAccessible) {
            return getInt(ix(i));
        }
        throw new IllegalStateException("buffer is inaccessible");
    }

    final void getUnchecked(int pos, int[] dst, int dstOffset, int length) {
        if (this.memoryRef.isAccessible) {
            Memory.peekIntArray(ix(pos), dst, dstOffset, length, this.nativeByteOrder ^ 1);
            return;
        }
        throw new IllegalStateException("buffer is inaccessible");
    }

    private ByteBuffer putInt(long a, int x) {
        Memory.pokeInt(a, x, this.nativeByteOrder ^ 1);
        return this;
    }

    public final ByteBuffer putInt(int x) {
        if (!this.memoryRef.isAccessible) {
            throw new IllegalStateException("buffer is inaccessible");
        } else if (this.isReadOnly) {
            throw new ReadOnlyBufferException();
        } else {
            putInt(ix(nextPutIndex(4)), x);
            return this;
        }
    }

    public final ByteBuffer putInt(int i, int x) {
        if (!this.memoryRef.isAccessible) {
            throw new IllegalStateException("buffer is inaccessible");
        } else if (this.isReadOnly) {
            throw new ReadOnlyBufferException();
        } else {
            putInt(ix(checkIndex(i, 4)), x);
            return this;
        }
    }

    final void putIntUnchecked(int i, int x) {
        if (this.memoryRef.isAccessible) {
            putInt(ix(i), x);
            return;
        }
        throw new IllegalStateException("buffer is inaccessible");
    }

    final void putUnchecked(int pos, int[] src, int srcOffset, int length) {
        if (this.memoryRef.isAccessible) {
            Memory.pokeIntArray(ix(pos), src, srcOffset, length, this.nativeByteOrder ^ 1);
            return;
        }
        throw new IllegalStateException("buffer is inaccessible");
    }

    public final IntBuffer asIntBuffer() {
        if (this.memoryRef.isFreed) {
            throw new IllegalStateException("buffer has been freed");
        }
        int off = position();
        int lim = limit();
        if (-assertionsDisabled || off <= lim) {
            int size = (off <= lim ? lim - off : 0) >> 2;
            return new ByteBufferAsIntBuffer(this, -1, 0, size, size, off, order());
        }
        throw new AssertionError();
    }

    private long getLong(long a) {
        return Memory.peekLong(a, this.nativeByteOrder ^ 1);
    }

    public final long getLong() {
        if (this.memoryRef.isAccessible) {
            return getLong(ix(nextGetIndex(8)));
        }
        throw new IllegalStateException("buffer is inaccessible");
    }

    public final long getLong(int i) {
        if (this.memoryRef.isAccessible) {
            return getLong(ix(checkIndex(i, 8)));
        }
        throw new IllegalStateException("buffer is inaccessible");
    }

    final long getLongUnchecked(int i) {
        if (this.memoryRef.isAccessible) {
            return getLong(ix(i));
        }
        throw new IllegalStateException("buffer is inaccessible");
    }

    final void getUnchecked(int pos, long[] dst, int dstOffset, int length) {
        if (this.memoryRef.isAccessible) {
            Memory.peekLongArray(ix(pos), dst, dstOffset, length, this.nativeByteOrder ^ 1);
            return;
        }
        throw new IllegalStateException("buffer is inaccessible");
    }

    private ByteBuffer putLong(long a, long x) {
        Memory.pokeLong(a, x, this.nativeByteOrder ^ 1);
        return this;
    }

    public final ByteBuffer putLong(long x) {
        if (!this.memoryRef.isAccessible) {
            throw new IllegalStateException("buffer is inaccessible");
        } else if (this.isReadOnly) {
            throw new ReadOnlyBufferException();
        } else {
            putLong(ix(nextPutIndex(8)), x);
            return this;
        }
    }

    public final ByteBuffer putLong(int i, long x) {
        if (!this.memoryRef.isAccessible) {
            throw new IllegalStateException("buffer is inaccessible");
        } else if (this.isReadOnly) {
            throw new ReadOnlyBufferException();
        } else {
            putLong(ix(checkIndex(i, 8)), x);
            return this;
        }
    }

    final void putLongUnchecked(int i, long x) {
        if (this.memoryRef.isAccessible) {
            putLong(ix(i), x);
            return;
        }
        throw new IllegalStateException("buffer is inaccessible");
    }

    final void putUnchecked(int pos, long[] src, int srcOffset, int length) {
        if (this.memoryRef.isAccessible) {
            Memory.pokeLongArray(ix(pos), src, srcOffset, length, this.nativeByteOrder ^ 1);
            return;
        }
        throw new IllegalStateException("buffer is inaccessible");
    }

    public final LongBuffer asLongBuffer() {
        if (this.memoryRef.isFreed) {
            throw new IllegalStateException("buffer has been freed");
        }
        int off = position();
        int lim = limit();
        if (-assertionsDisabled || off <= lim) {
            int size = (off <= lim ? lim - off : 0) >> 3;
            return new ByteBufferAsLongBuffer(this, -1, 0, size, size, off, order());
        }
        throw new AssertionError();
    }

    private float getFloat(long a) {
        return Float.intBitsToFloat(Memory.peekInt(a, this.nativeByteOrder ^ 1));
    }

    public final float getFloat() {
        if (this.memoryRef.isAccessible) {
            return getFloat(ix(nextGetIndex(4)));
        }
        throw new IllegalStateException("buffer is inaccessible");
    }

    public final float getFloat(int i) {
        if (this.memoryRef.isAccessible) {
            return getFloat(ix(checkIndex(i, 4)));
        }
        throw new IllegalStateException("buffer is inaccessible");
    }

    final float getFloatUnchecked(int i) {
        if (this.memoryRef.isAccessible) {
            return getFloat(ix(i));
        }
        throw new IllegalStateException("buffer is inaccessible");
    }

    final void getUnchecked(int pos, float[] dst, int dstOffset, int length) {
        if (this.memoryRef.isAccessible) {
            Memory.peekFloatArray(ix(pos), dst, dstOffset, length, this.nativeByteOrder ^ 1);
            return;
        }
        throw new IllegalStateException("buffer is inaccessible");
    }

    private ByteBuffer putFloat(long a, float x) {
        Memory.pokeInt(a, Float.floatToRawIntBits(x), this.nativeByteOrder ^ 1);
        return this;
    }

    public final ByteBuffer putFloat(float x) {
        if (!this.memoryRef.isAccessible) {
            throw new IllegalStateException("buffer is inaccessible");
        } else if (this.isReadOnly) {
            throw new ReadOnlyBufferException();
        } else {
            putFloat(ix(nextPutIndex(4)), x);
            return this;
        }
    }

    public final ByteBuffer putFloat(int i, float x) {
        if (!this.memoryRef.isAccessible) {
            throw new IllegalStateException("buffer is inaccessible");
        } else if (this.isReadOnly) {
            throw new ReadOnlyBufferException();
        } else {
            putFloat(ix(checkIndex(i, 4)), x);
            return this;
        }
    }

    final void putFloatUnchecked(int i, float x) {
        if (this.memoryRef.isAccessible) {
            putFloat(ix(i), x);
            return;
        }
        throw new IllegalStateException("buffer is inaccessible");
    }

    final void putUnchecked(int pos, float[] src, int srcOffset, int length) {
        if (this.memoryRef.isAccessible) {
            Memory.pokeFloatArray(ix(pos), src, srcOffset, length, this.nativeByteOrder ^ 1);
            return;
        }
        throw new IllegalStateException("buffer is inaccessible");
    }

    public final FloatBuffer asFloatBuffer() {
        if (this.memoryRef.isFreed) {
            throw new IllegalStateException("buffer has been freed");
        }
        int off = position();
        int lim = limit();
        if (-assertionsDisabled || off <= lim) {
            int size = (off <= lim ? lim - off : 0) >> 2;
            return new ByteBufferAsFloatBuffer(this, -1, 0, size, size, off, order());
        }
        throw new AssertionError();
    }

    private double getDouble(long a) {
        return Double.longBitsToDouble(Memory.peekLong(a, this.nativeByteOrder ^ 1));
    }

    public final double getDouble() {
        if (this.memoryRef.isAccessible) {
            return getDouble(ix(nextGetIndex(8)));
        }
        throw new IllegalStateException("buffer is inaccessible");
    }

    public final double getDouble(int i) {
        if (this.memoryRef.isAccessible) {
            return getDouble(ix(checkIndex(i, 8)));
        }
        throw new IllegalStateException("buffer is inaccessible");
    }

    final double getDoubleUnchecked(int i) {
        if (this.memoryRef.isAccessible) {
            return getDouble(ix(i));
        }
        throw new IllegalStateException("buffer is inaccessible");
    }

    final void getUnchecked(int pos, double[] dst, int dstOffset, int length) {
        if (this.memoryRef.isAccessible) {
            Memory.peekDoubleArray(ix(pos), dst, dstOffset, length, this.nativeByteOrder ^ 1);
            return;
        }
        throw new IllegalStateException("buffer is inaccessible");
    }

    private ByteBuffer putDouble(long a, double x) {
        Memory.pokeLong(a, Double.doubleToRawLongBits(x), this.nativeByteOrder ^ 1);
        return this;
    }

    public final ByteBuffer putDouble(double x) {
        if (!this.memoryRef.isAccessible) {
            throw new IllegalStateException("buffer is inaccessible");
        } else if (this.isReadOnly) {
            throw new ReadOnlyBufferException();
        } else {
            putDouble(ix(nextPutIndex(8)), x);
            return this;
        }
    }

    public final ByteBuffer putDouble(int i, double x) {
        if (!this.memoryRef.isAccessible) {
            throw new IllegalStateException("buffer is inaccessible");
        } else if (this.isReadOnly) {
            throw new ReadOnlyBufferException();
        } else {
            putDouble(ix(checkIndex(i, 8)), x);
            return this;
        }
    }

    final void putDoubleUnchecked(int i, double x) {
        if (this.memoryRef.isAccessible) {
            putDouble(ix(i), x);
            return;
        }
        throw new IllegalStateException("buffer is inaccessible");
    }

    final void putUnchecked(int pos, double[] src, int srcOffset, int length) {
        if (this.memoryRef.isAccessible) {
            Memory.pokeDoubleArray(ix(pos), src, srcOffset, length, this.nativeByteOrder ^ 1);
            return;
        }
        throw new IllegalStateException("buffer is inaccessible");
    }

    public final DoubleBuffer asDoubleBuffer() {
        if (this.memoryRef.isFreed) {
            throw new IllegalStateException("buffer has been freed");
        }
        int off = position();
        int lim = limit();
        if (-assertionsDisabled || off <= lim) {
            int size = (off <= lim ? lim - off : 0) >> 3;
            return new ByteBufferAsDoubleBuffer(this, -1, 0, size, size, off, order());
        }
        throw new AssertionError();
    }

    public final boolean isAccessible() {
        return this.memoryRef.isAccessible;
    }

    public final void setAccessible(boolean value) {
        this.memoryRef.isAccessible = value;
    }
}
