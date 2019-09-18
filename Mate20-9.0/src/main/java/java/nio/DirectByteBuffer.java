package java.nio;

import dalvik.system.VMRuntime;
import java.io.FileDescriptor;
import libcore.io.Memory;
import sun.misc.Cleaner;
import sun.nio.ch.DirectBuffer;

public class DirectByteBuffer extends MappedByteBuffer implements DirectBuffer {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    final Cleaner cleaner;
    final MemoryRef memoryRef;

    static final class MemoryRef {
        long allocatedAddress;
        byte[] buffer;
        boolean isAccessible;
        boolean isFreed;
        final int offset;
        final Object originalBufferObject;

        MemoryRef(int capacity) {
            VMRuntime runtime = VMRuntime.getRuntime();
            this.buffer = (byte[]) runtime.newNonMovableArray(Byte.TYPE, capacity + 7);
            this.allocatedAddress = runtime.addressOf(this.buffer);
            this.offset = (int) (((this.allocatedAddress + 7) & -8) - this.allocatedAddress);
            this.isAccessible = true;
            this.isFreed = false;
            this.originalBufferObject = null;
        }

        MemoryRef(long allocatedAddress2, Object originalBufferObject2) {
            this.buffer = null;
            this.allocatedAddress = allocatedAddress2;
            this.offset = 0;
            this.originalBufferObject = originalBufferObject2;
            this.isAccessible = true;
        }

        /* access modifiers changed from: package-private */
        public void free() {
            this.buffer = null;
            this.allocatedAddress = 0;
            this.isAccessible = false;
            this.isFreed = true;
        }
    }

    DirectByteBuffer(int capacity, MemoryRef memoryRef2) {
        super(-1, 0, capacity, capacity, memoryRef2.buffer, memoryRef2.offset);
        this.memoryRef = memoryRef2;
        this.address = memoryRef2.allocatedAddress + ((long) memoryRef2.offset);
        this.cleaner = null;
        this.isReadOnly = false;
    }

    private DirectByteBuffer(long addr, int cap) {
        super(-1, 0, cap, cap);
        this.memoryRef = new MemoryRef(addr, this);
        this.address = addr;
        this.cleaner = null;
    }

    public DirectByteBuffer(int cap, long addr, FileDescriptor fd, Runnable unmapper, boolean isReadOnly) {
        super(-1, 0, cap, cap, fd);
        this.isReadOnly = isReadOnly;
        this.memoryRef = new MemoryRef(addr, null);
        this.address = addr;
        this.cleaner = Cleaner.create(this.memoryRef, unmapper);
    }

    DirectByteBuffer(MemoryRef memoryRef2, int mark, int pos, int lim, int cap, int off) {
        this(memoryRef2, mark, pos, lim, cap, off, false);
    }

    DirectByteBuffer(MemoryRef memoryRef2, int mark, int pos, int lim, int cap, int off, boolean isReadOnly) {
        super(mark, pos, lim, cap, memoryRef2.buffer, off);
        this.isReadOnly = isReadOnly;
        this.memoryRef = memoryRef2;
        this.address = memoryRef2.allocatedAddress + ((long) off);
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
            int rem = pos <= lim ? lim - pos : 0;
            DirectByteBuffer directByteBuffer = new DirectByteBuffer(this.memoryRef, -1, 0, rem, rem, this.offset + pos, this.isReadOnly);
            return directByteBuffer;
        }
        throw new IllegalStateException("buffer is inaccessible");
    }

    public final ByteBuffer duplicate() {
        if (!this.memoryRef.isFreed) {
            DirectByteBuffer directByteBuffer = new DirectByteBuffer(this.memoryRef, markValue(), position(), limit(), capacity(), this.offset, this.isReadOnly);
            return directByteBuffer;
        }
        throw new IllegalStateException("buffer has been freed");
    }

    public final ByteBuffer asReadOnlyBuffer() {
        if (!this.memoryRef.isFreed) {
            DirectByteBuffer directByteBuffer = new DirectByteBuffer(this.memoryRef, markValue(), position(), limit(), capacity(), this.offset, true);
            return directByteBuffer;
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
            checkBounds(dstOffset, length, dst.length);
            int pos = position();
            int lim = limit();
            if (length <= (pos <= lim ? lim - pos : 0)) {
                Memory.peekByteArray(ix(pos), dst, dstOffset, length);
                this.position = pos + length;
                return this;
            }
            throw new BufferUnderflowException();
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
        } else if (!this.isReadOnly) {
            put(ix(nextPutIndex()), x);
            return this;
        } else {
            throw new ReadOnlyBufferException();
        }
    }

    public final ByteBuffer put(int i, byte x) {
        if (!this.memoryRef.isAccessible) {
            throw new IllegalStateException("buffer is inaccessible");
        } else if (!this.isReadOnly) {
            put(ix(checkIndex(i)), x);
            return this;
        } else {
            throw new ReadOnlyBufferException();
        }
    }

    public ByteBuffer put(byte[] src, int srcOffset, int length) {
        if (!this.memoryRef.isAccessible) {
            throw new IllegalStateException("buffer is inaccessible");
        } else if (!this.isReadOnly) {
            checkBounds(srcOffset, length, src.length);
            int pos = position();
            int lim = limit();
            if (length <= (pos <= lim ? lim - pos : 0)) {
                Memory.pokeByteArray(ix(pos), src, srcOffset, length);
                this.position = pos + length;
                return this;
            }
            throw new BufferOverflowException();
        } else {
            throw new ReadOnlyBufferException();
        }
    }

    public final ByteBuffer compact() {
        if (!this.memoryRef.isAccessible) {
            throw new IllegalStateException("buffer is inaccessible");
        } else if (!this.isReadOnly) {
            int pos = position();
            int lim = limit();
            int rem = pos <= lim ? lim - pos : 0;
            System.arraycopy(this.hb, this.position + this.offset, this.hb, this.offset, remaining());
            position(rem);
            limit(capacity());
            discardMark();
            return this;
        } else {
            throw new ReadOnlyBufferException();
        }
    }

    public final boolean isDirect() {
        return true;
    }

    public final boolean isReadOnly() {
        return this.isReadOnly;
    }

    /* access modifiers changed from: package-private */
    public final byte _get(int i) {
        return get(i);
    }

    /* access modifiers changed from: package-private */
    public final void _put(int i, byte b) {
        put(i, b);
    }

    public final char getChar() {
        if (this.memoryRef.isAccessible) {
            int newPosition = this.position + 2;
            if (newPosition <= limit()) {
                char x = (char) Memory.peekShort(ix(this.position), !this.nativeByteOrder);
                this.position = newPosition;
                return x;
            }
            throw new BufferUnderflowException();
        }
        throw new IllegalStateException("buffer is inaccessible");
    }

    public final char getChar(int i) {
        if (this.memoryRef.isAccessible) {
            checkIndex(i, 2);
            return (char) Memory.peekShort(ix(i), !this.nativeByteOrder);
        }
        throw new IllegalStateException("buffer is inaccessible");
    }

    /* access modifiers changed from: package-private */
    public char getCharUnchecked(int i) {
        if (this.memoryRef.isAccessible) {
            return (char) Memory.peekShort(ix(i), !this.nativeByteOrder);
        }
        throw new IllegalStateException("buffer is inaccessible");
    }

    /* access modifiers changed from: package-private */
    public void getUnchecked(int pos, char[] dst, int dstOffset, int length) {
        if (this.memoryRef.isAccessible) {
            Memory.peekCharArray(ix(pos), dst, dstOffset, length, !this.nativeByteOrder);
            return;
        }
        throw new IllegalStateException("buffer is inaccessible");
    }

    private ByteBuffer putChar(long a, char x) {
        Memory.pokeShort(a, (short) x, !this.nativeByteOrder);
        return this;
    }

    public final ByteBuffer putChar(char x) {
        if (!this.memoryRef.isAccessible) {
            throw new IllegalStateException("buffer is inaccessible");
        } else if (!this.isReadOnly) {
            putChar(ix(nextPutIndex(2)), x);
            return this;
        } else {
            throw new ReadOnlyBufferException();
        }
    }

    public final ByteBuffer putChar(int i, char x) {
        if (!this.memoryRef.isAccessible) {
            throw new IllegalStateException("buffer is inaccessible");
        } else if (!this.isReadOnly) {
            putChar(ix(checkIndex(i, 2)), x);
            return this;
        } else {
            throw new ReadOnlyBufferException();
        }
    }

    /* access modifiers changed from: package-private */
    public void putCharUnchecked(int i, char x) {
        if (this.memoryRef.isAccessible) {
            putChar(ix(i), x);
            return;
        }
        throw new IllegalStateException("buffer is inaccessible");
    }

    /* access modifiers changed from: package-private */
    public void putUnchecked(int pos, char[] src, int srcOffset, int length) {
        if (this.memoryRef.isAccessible) {
            Memory.pokeCharArray(ix(pos), src, srcOffset, length, !this.nativeByteOrder);
            return;
        }
        throw new IllegalStateException("buffer is inaccessible");
    }

    public final CharBuffer asCharBuffer() {
        if (!this.memoryRef.isFreed) {
            int off = position();
            int lim = limit();
            int size = (off <= lim ? lim - off : 0) >> 1;
            ByteBufferAsCharBuffer byteBufferAsCharBuffer = new ByteBufferAsCharBuffer(this, -1, 0, size, size, off, order());
            return byteBufferAsCharBuffer;
        }
        throw new IllegalStateException("buffer has been freed");
    }

    private short getShort(long a) {
        return Memory.peekShort(a, !this.nativeByteOrder);
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

    /* access modifiers changed from: package-private */
    public short getShortUnchecked(int i) {
        if (this.memoryRef.isAccessible) {
            return getShort(ix(i));
        }
        throw new IllegalStateException("buffer is inaccessible");
    }

    /* access modifiers changed from: package-private */
    public void getUnchecked(int pos, short[] dst, int dstOffset, int length) {
        if (this.memoryRef.isAccessible) {
            Memory.peekShortArray(ix(pos), dst, dstOffset, length, !this.nativeByteOrder);
            return;
        }
        throw new IllegalStateException("buffer is inaccessible");
    }

    private ByteBuffer putShort(long a, short x) {
        Memory.pokeShort(a, x, !this.nativeByteOrder);
        return this;
    }

    public final ByteBuffer putShort(short x) {
        if (!this.memoryRef.isAccessible) {
            throw new IllegalStateException("buffer is inaccessible");
        } else if (!this.isReadOnly) {
            putShort(ix(nextPutIndex(2)), x);
            return this;
        } else {
            throw new ReadOnlyBufferException();
        }
    }

    public final ByteBuffer putShort(int i, short x) {
        if (!this.memoryRef.isAccessible) {
            throw new IllegalStateException("buffer is inaccessible");
        } else if (!this.isReadOnly) {
            putShort(ix(checkIndex(i, 2)), x);
            return this;
        } else {
            throw new ReadOnlyBufferException();
        }
    }

    /* access modifiers changed from: package-private */
    public void putShortUnchecked(int i, short x) {
        if (this.memoryRef.isAccessible) {
            putShort(ix(i), x);
            return;
        }
        throw new IllegalStateException("buffer is inaccessible");
    }

    /* access modifiers changed from: package-private */
    public void putUnchecked(int pos, short[] src, int srcOffset, int length) {
        if (this.memoryRef.isAccessible) {
            Memory.pokeShortArray(ix(pos), src, srcOffset, length, !this.nativeByteOrder);
            return;
        }
        throw new IllegalStateException("buffer is inaccessible");
    }

    public final ShortBuffer asShortBuffer() {
        if (!this.memoryRef.isFreed) {
            int off = position();
            int lim = limit();
            int size = (off <= lim ? lim - off : 0) >> 1;
            ByteBufferAsShortBuffer byteBufferAsShortBuffer = new ByteBufferAsShortBuffer(this, -1, 0, size, size, off, order());
            return byteBufferAsShortBuffer;
        }
        throw new IllegalStateException("buffer has been freed");
    }

    private int getInt(long a) {
        return Memory.peekInt(a, !this.nativeByteOrder);
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

    /* access modifiers changed from: package-private */
    public final int getIntUnchecked(int i) {
        if (this.memoryRef.isAccessible) {
            return getInt(ix(i));
        }
        throw new IllegalStateException("buffer is inaccessible");
    }

    /* access modifiers changed from: package-private */
    public final void getUnchecked(int pos, int[] dst, int dstOffset, int length) {
        if (this.memoryRef.isAccessible) {
            Memory.peekIntArray(ix(pos), dst, dstOffset, length, !this.nativeByteOrder);
            return;
        }
        throw new IllegalStateException("buffer is inaccessible");
    }

    private ByteBuffer putInt(long a, int x) {
        Memory.pokeInt(a, x, !this.nativeByteOrder);
        return this;
    }

    public final ByteBuffer putInt(int x) {
        if (!this.memoryRef.isAccessible) {
            throw new IllegalStateException("buffer is inaccessible");
        } else if (!this.isReadOnly) {
            putInt(ix(nextPutIndex(4)), x);
            return this;
        } else {
            throw new ReadOnlyBufferException();
        }
    }

    public final ByteBuffer putInt(int i, int x) {
        if (!this.memoryRef.isAccessible) {
            throw new IllegalStateException("buffer is inaccessible");
        } else if (!this.isReadOnly) {
            putInt(ix(checkIndex(i, 4)), x);
            return this;
        } else {
            throw new ReadOnlyBufferException();
        }
    }

    /* access modifiers changed from: package-private */
    public final void putIntUnchecked(int i, int x) {
        if (this.memoryRef.isAccessible) {
            putInt(ix(i), x);
            return;
        }
        throw new IllegalStateException("buffer is inaccessible");
    }

    /* access modifiers changed from: package-private */
    public final void putUnchecked(int pos, int[] src, int srcOffset, int length) {
        if (this.memoryRef.isAccessible) {
            Memory.pokeIntArray(ix(pos), src, srcOffset, length, !this.nativeByteOrder);
            return;
        }
        throw new IllegalStateException("buffer is inaccessible");
    }

    public final IntBuffer asIntBuffer() {
        if (!this.memoryRef.isFreed) {
            int off = position();
            int lim = limit();
            int size = (off <= lim ? lim - off : 0) >> 2;
            ByteBufferAsIntBuffer byteBufferAsIntBuffer = new ByteBufferAsIntBuffer(this, -1, 0, size, size, off, order());
            return byteBufferAsIntBuffer;
        }
        throw new IllegalStateException("buffer has been freed");
    }

    private long getLong(long a) {
        return Memory.peekLong(a, !this.nativeByteOrder);
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

    /* access modifiers changed from: package-private */
    public final long getLongUnchecked(int i) {
        if (this.memoryRef.isAccessible) {
            return getLong(ix(i));
        }
        throw new IllegalStateException("buffer is inaccessible");
    }

    /* access modifiers changed from: package-private */
    public final void getUnchecked(int pos, long[] dst, int dstOffset, int length) {
        if (this.memoryRef.isAccessible) {
            Memory.peekLongArray(ix(pos), dst, dstOffset, length, !this.nativeByteOrder);
            return;
        }
        throw new IllegalStateException("buffer is inaccessible");
    }

    private ByteBuffer putLong(long a, long x) {
        Memory.pokeLong(a, x, !this.nativeByteOrder);
        return this;
    }

    public final ByteBuffer putLong(long x) {
        if (!this.memoryRef.isAccessible) {
            throw new IllegalStateException("buffer is inaccessible");
        } else if (!this.isReadOnly) {
            putLong(ix(nextPutIndex(8)), x);
            return this;
        } else {
            throw new ReadOnlyBufferException();
        }
    }

    public final ByteBuffer putLong(int i, long x) {
        if (!this.memoryRef.isAccessible) {
            throw new IllegalStateException("buffer is inaccessible");
        } else if (!this.isReadOnly) {
            putLong(ix(checkIndex(i, 8)), x);
            return this;
        } else {
            throw new ReadOnlyBufferException();
        }
    }

    /* access modifiers changed from: package-private */
    public final void putLongUnchecked(int i, long x) {
        if (this.memoryRef.isAccessible) {
            putLong(ix(i), x);
            return;
        }
        throw new IllegalStateException("buffer is inaccessible");
    }

    /* access modifiers changed from: package-private */
    public final void putUnchecked(int pos, long[] src, int srcOffset, int length) {
        if (this.memoryRef.isAccessible) {
            Memory.pokeLongArray(ix(pos), src, srcOffset, length, !this.nativeByteOrder);
            return;
        }
        throw new IllegalStateException("buffer is inaccessible");
    }

    public final LongBuffer asLongBuffer() {
        if (!this.memoryRef.isFreed) {
            int off = position();
            int lim = limit();
            int size = (off <= lim ? lim - off : 0) >> 3;
            ByteBufferAsLongBuffer byteBufferAsLongBuffer = new ByteBufferAsLongBuffer(this, -1, 0, size, size, off, order());
            return byteBufferAsLongBuffer;
        }
        throw new IllegalStateException("buffer has been freed");
    }

    private float getFloat(long a) {
        return Float.intBitsToFloat(Memory.peekInt(a, !this.nativeByteOrder));
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

    /* access modifiers changed from: package-private */
    public final float getFloatUnchecked(int i) {
        if (this.memoryRef.isAccessible) {
            return getFloat(ix(i));
        }
        throw new IllegalStateException("buffer is inaccessible");
    }

    /* access modifiers changed from: package-private */
    public final void getUnchecked(int pos, float[] dst, int dstOffset, int length) {
        if (this.memoryRef.isAccessible) {
            Memory.peekFloatArray(ix(pos), dst, dstOffset, length, !this.nativeByteOrder);
            return;
        }
        throw new IllegalStateException("buffer is inaccessible");
    }

    private ByteBuffer putFloat(long a, float x) {
        Memory.pokeInt(a, Float.floatToRawIntBits(x), !this.nativeByteOrder);
        return this;
    }

    public final ByteBuffer putFloat(float x) {
        if (!this.memoryRef.isAccessible) {
            throw new IllegalStateException("buffer is inaccessible");
        } else if (!this.isReadOnly) {
            putFloat(ix(nextPutIndex(4)), x);
            return this;
        } else {
            throw new ReadOnlyBufferException();
        }
    }

    public final ByteBuffer putFloat(int i, float x) {
        if (!this.memoryRef.isAccessible) {
            throw new IllegalStateException("buffer is inaccessible");
        } else if (!this.isReadOnly) {
            putFloat(ix(checkIndex(i, 4)), x);
            return this;
        } else {
            throw new ReadOnlyBufferException();
        }
    }

    /* access modifiers changed from: package-private */
    public final void putFloatUnchecked(int i, float x) {
        if (this.memoryRef.isAccessible) {
            putFloat(ix(i), x);
            return;
        }
        throw new IllegalStateException("buffer is inaccessible");
    }

    /* access modifiers changed from: package-private */
    public final void putUnchecked(int pos, float[] src, int srcOffset, int length) {
        if (this.memoryRef.isAccessible) {
            Memory.pokeFloatArray(ix(pos), src, srcOffset, length, !this.nativeByteOrder);
            return;
        }
        throw new IllegalStateException("buffer is inaccessible");
    }

    public final FloatBuffer asFloatBuffer() {
        if (!this.memoryRef.isFreed) {
            int off = position();
            int lim = limit();
            int size = (off <= lim ? lim - off : 0) >> 2;
            ByteBufferAsFloatBuffer byteBufferAsFloatBuffer = new ByteBufferAsFloatBuffer(this, -1, 0, size, size, off, order());
            return byteBufferAsFloatBuffer;
        }
        throw new IllegalStateException("buffer has been freed");
    }

    private double getDouble(long a) {
        return Double.longBitsToDouble(Memory.peekLong(a, !this.nativeByteOrder));
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

    /* access modifiers changed from: package-private */
    public final double getDoubleUnchecked(int i) {
        if (this.memoryRef.isAccessible) {
            return getDouble(ix(i));
        }
        throw new IllegalStateException("buffer is inaccessible");
    }

    /* access modifiers changed from: package-private */
    public final void getUnchecked(int pos, double[] dst, int dstOffset, int length) {
        if (this.memoryRef.isAccessible) {
            Memory.peekDoubleArray(ix(pos), dst, dstOffset, length, !this.nativeByteOrder);
            return;
        }
        throw new IllegalStateException("buffer is inaccessible");
    }

    private ByteBuffer putDouble(long a, double x) {
        Memory.pokeLong(a, Double.doubleToRawLongBits(x), !this.nativeByteOrder);
        return this;
    }

    public final ByteBuffer putDouble(double x) {
        if (!this.memoryRef.isAccessible) {
            throw new IllegalStateException("buffer is inaccessible");
        } else if (!this.isReadOnly) {
            putDouble(ix(nextPutIndex(8)), x);
            return this;
        } else {
            throw new ReadOnlyBufferException();
        }
    }

    public final ByteBuffer putDouble(int i, double x) {
        if (!this.memoryRef.isAccessible) {
            throw new IllegalStateException("buffer is inaccessible");
        } else if (!this.isReadOnly) {
            putDouble(ix(checkIndex(i, 8)), x);
            return this;
        } else {
            throw new ReadOnlyBufferException();
        }
    }

    /* access modifiers changed from: package-private */
    public final void putDoubleUnchecked(int i, double x) {
        if (this.memoryRef.isAccessible) {
            putDouble(ix(i), x);
            return;
        }
        throw new IllegalStateException("buffer is inaccessible");
    }

    /* access modifiers changed from: package-private */
    public final void putUnchecked(int pos, double[] src, int srcOffset, int length) {
        if (this.memoryRef.isAccessible) {
            Memory.pokeDoubleArray(ix(pos), src, srcOffset, length, !this.nativeByteOrder);
            return;
        }
        throw new IllegalStateException("buffer is inaccessible");
    }

    public final DoubleBuffer asDoubleBuffer() {
        if (!this.memoryRef.isFreed) {
            int off = position();
            int lim = limit();
            int size = (off <= lim ? lim - off : 0) >> 3;
            ByteBufferAsDoubleBuffer byteBufferAsDoubleBuffer = new ByteBufferAsDoubleBuffer(this, -1, 0, size, size, off, order());
            return byteBufferAsDoubleBuffer;
        }
        throw new IllegalStateException("buffer has been freed");
    }

    public final boolean isAccessible() {
        return this.memoryRef.isAccessible;
    }

    public final void setAccessible(boolean value) {
        this.memoryRef.isAccessible = value;
    }
}
