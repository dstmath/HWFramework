package java.nio;

import dalvik.system.VMRuntime;
import java.io.FileDescriptor;
import libcore.io.Memory;
import sun.misc.Cleaner;
import sun.nio.ch.DirectBuffer;

public class DirectByteBuffer extends MappedByteBuffer implements DirectBuffer {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    final Cleaner cleaner;
    final MemoryRef memoryRef;

    static class MemoryRef {
        long allocatedAddress;
        byte[] buffer;
        boolean isAccessible;
        final int offset;

        MemoryRef(int capacity) {
            VMRuntime runtime = VMRuntime.getRuntime();
            this.buffer = (byte[]) runtime.newNonMovableArray(Byte.TYPE, capacity + 7);
            this.allocatedAddress = runtime.addressOf(this.buffer);
            this.offset = (int) (((this.allocatedAddress + 7) & -8) - this.allocatedAddress);
            this.isAccessible = true;
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
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.nio.DirectByteBuffer.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.nio.DirectByteBuffer.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: java.nio.DirectByteBuffer.<clinit>():void");
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

    public Object attachment() {
        return this.memoryRef;
    }

    public Cleaner cleaner() {
        return this.cleaner;
    }

    public ByteBuffer slice() {
        int i = 1;
        if (this.memoryRef.isAccessible) {
            int pos = position();
            int lim = limit();
            if (!-assertionsDisabled) {
                if ((pos <= lim ? 1 : 0) == 0) {
                    throw new AssertionError();
                }
            }
            int rem = pos <= lim ? lim - pos : 0;
            int off = pos + this.offset;
            if (!-assertionsDisabled) {
                if (off < 0) {
                    i = 0;
                }
                if (i == 0) {
                    throw new AssertionError();
                }
            }
            return new DirectByteBuffer(this.memoryRef, -1, 0, rem, rem, off, this.isReadOnly);
        }
        throw new IllegalStateException("buffer is inaccessible");
    }

    public ByteBuffer duplicate() {
        if (this.memoryRef.isAccessible) {
            return new DirectByteBuffer(this.memoryRef, markValue(), position(), limit(), capacity(), this.offset, this.isReadOnly);
        }
        throw new IllegalStateException("buffer is inaccessible");
    }

    public ByteBuffer asReadOnlyBuffer() {
        if (this.memoryRef.isAccessible) {
            return new DirectByteBuffer(this.memoryRef, markValue(), position(), limit(), capacity(), this.offset, true);
        }
        throw new IllegalStateException("buffer is inaccessible");
    }

    public long address() {
        return this.address;
    }

    private long ix(int i) {
        return this.address + ((long) i);
    }

    private byte get(long a) {
        return Memory.peekByte(a);
    }

    public byte get() {
        if (this.memoryRef.isAccessible) {
            return get(ix(nextGetIndex()));
        }
        throw new IllegalStateException("buffer is inaccessible");
    }

    public byte get(int i) {
        if (this.memoryRef.isAccessible) {
            return get(ix(checkIndex(i)));
        }
        throw new IllegalStateException("buffer is inaccessible");
    }

    public ByteBuffer get(byte[] dst, int dstOffset, int length) {
        int rem = 0;
        if (this.memoryRef.isAccessible) {
            Buffer.checkBounds(dstOffset, length, dst.length);
            int pos = position();
            int lim = limit();
            if (!-assertionsDisabled) {
                if ((pos <= lim ? 1 : 0) == 0) {
                    throw new AssertionError();
                }
            }
            if (pos <= lim) {
                rem = lim - pos;
            }
            if (length > rem) {
                throw new BufferUnderflowException();
            }
            Memory.peekByteArray(ix(pos), dst, dstOffset, length);
            this.position = pos + length;
            return this;
        }
        throw new IllegalStateException("buffer is inaccessible");
    }

    public ByteBuffer put(long a, byte x) {
        Memory.pokeByte(a, x);
        return this;
    }

    public ByteBuffer put(byte x) {
        if (this.isReadOnly) {
            throw new ReadOnlyBufferException();
        } else if (this.memoryRef.isAccessible) {
            put(ix(nextPutIndex()), x);
            return this;
        } else {
            throw new IllegalStateException("buffer is inaccessible");
        }
    }

    public ByteBuffer put(int i, byte x) {
        if (this.isReadOnly) {
            throw new ReadOnlyBufferException();
        } else if (this.memoryRef.isAccessible) {
            put(ix(checkIndex(i)), x);
            return this;
        } else {
            throw new IllegalStateException("buffer is inaccessible");
        }
    }

    public ByteBuffer put(byte[] src, int srcOffset, int length) {
        int rem = 0;
        if (this.isReadOnly) {
            throw new ReadOnlyBufferException();
        } else if (this.memoryRef.isAccessible) {
            Buffer.checkBounds(srcOffset, length, src.length);
            int pos = position();
            int lim = limit();
            if (!-assertionsDisabled) {
                if ((pos <= lim ? 1 : 0) == 0) {
                    throw new AssertionError();
                }
            }
            if (pos <= lim) {
                rem = lim - pos;
            }
            if (length > rem) {
                throw new BufferOverflowException();
            }
            Memory.pokeByteArray(ix(pos), src, srcOffset, length);
            this.position = pos + length;
            return this;
        } else {
            throw new IllegalStateException("buffer is inaccessible");
        }
    }

    public ByteBuffer compact() {
        if (this.isReadOnly) {
            throw new ReadOnlyBufferException();
        } else if (this.memoryRef.isAccessible) {
            int pos = position();
            int lim = limit();
            if (!-assertionsDisabled) {
                if ((pos <= lim ? 1 : null) == null) {
                    throw new AssertionError();
                }
            }
            int rem = pos <= lim ? lim - pos : 0;
            System.arraycopy(this.hb, this.position + this.offset, this.hb, this.offset, remaining());
            position(rem);
            limit(capacity());
            discardMark();
            return this;
        } else {
            throw new IllegalStateException("buffer is inaccessible");
        }
    }

    public boolean isDirect() {
        return true;
    }

    public boolean isReadOnly() {
        return this.isReadOnly;
    }

    byte _get(int i) {
        return get(i);
    }

    void _put(int i, byte b) {
        put(i, b);
    }

    private char getChar(long a) {
        if (this.memoryRef.isAccessible) {
            return (char) Memory.peekShort((long) this.position, !this.nativeByteOrder);
        }
        throw new IllegalStateException("buffer is inaccessible");
    }

    public char getChar() {
        if (this.memoryRef.isAccessible) {
            int newPosition = this.position + 2;
            if (newPosition > limit()) {
                throw new BufferUnderflowException();
            }
            char x = (char) Memory.peekShort(ix(this.position), !this.nativeByteOrder);
            this.position = newPosition;
            return x;
        }
        throw new IllegalStateException("buffer is inaccessible");
    }

    public char getChar(int i) {
        if (this.memoryRef.isAccessible) {
            checkIndex(i, 2);
            return (char) Memory.peekShort(ix(i), !this.nativeByteOrder);
        }
        throw new IllegalStateException("buffer is inaccessible");
    }

    char getCharUnchecked(int i) {
        return (char) Memory.peekShort(ix(i), !this.nativeByteOrder);
    }

    void getUnchecked(int pos, char[] dst, int dstOffset, int length) {
        Memory.peekCharArray(ix(pos), dst, dstOffset, length, !this.nativeByteOrder);
    }

    private ByteBuffer putChar(long a, char x) {
        Memory.pokeShort(a, (short) x, !this.nativeByteOrder);
        return this;
    }

    public ByteBuffer putChar(char x) {
        if (this.isReadOnly) {
            throw new ReadOnlyBufferException();
        } else if (this.memoryRef.isAccessible) {
            putChar(ix(nextPutIndex(2)), x);
            return this;
        } else {
            throw new IllegalStateException("buffer is inaccessible");
        }
    }

    public ByteBuffer putChar(int i, char x) {
        if (this.isReadOnly) {
            throw new ReadOnlyBufferException();
        } else if (this.memoryRef.isAccessible) {
            putChar(ix(checkIndex(i, 2)), x);
            return this;
        } else {
            throw new IllegalStateException("buffer is inaccessible");
        }
    }

    void putCharUnchecked(int i, char x) {
        putChar(ix(i), x);
    }

    void putUnchecked(int pos, char[] src, int srcOffset, int length) {
        Memory.pokeCharArray(ix(pos), src, srcOffset, length, !this.nativeByteOrder);
    }

    public CharBuffer asCharBuffer() {
        if (this.memoryRef.isAccessible) {
            int rem;
            int off = position();
            int lim = limit();
            if (!-assertionsDisabled) {
                if ((off <= lim ? 1 : 0) == 0) {
                    throw new AssertionError();
                }
            }
            if (off <= lim) {
                rem = lim - off;
            } else {
                rem = 0;
            }
            int size = rem >> 1;
            return new ByteBufferAsCharBuffer(this, -1, 0, size, size, off, order());
        }
        throw new IllegalStateException("buffer is inaccessible");
    }

    private short getShort(long a) {
        return Memory.peekShort(a, !this.nativeByteOrder);
    }

    public short getShort() {
        if (this.memoryRef.isAccessible) {
            return getShort(ix(nextGetIndex(2)));
        }
        throw new IllegalStateException("buffer is inaccessible");
    }

    public short getShort(int i) {
        if (this.memoryRef.isAccessible) {
            return getShort(ix(checkIndex(i, 2)));
        }
        throw new IllegalStateException("buffer is inaccessible");
    }

    short getShortUnchecked(int i) {
        return getShort(ix(i));
    }

    void getUnchecked(int pos, short[] dst, int dstOffset, int length) {
        Memory.peekShortArray(ix(pos), dst, dstOffset, length, !this.nativeByteOrder);
    }

    private ByteBuffer putShort(long a, short x) {
        Memory.pokeShort(a, x, !this.nativeByteOrder);
        return this;
    }

    public ByteBuffer putShort(short x) {
        if (this.isReadOnly) {
            throw new ReadOnlyBufferException();
        } else if (this.memoryRef.isAccessible) {
            putShort(ix(nextPutIndex(2)), x);
            return this;
        } else {
            throw new IllegalStateException("buffer is inaccessible");
        }
    }

    public ByteBuffer putShort(int i, short x) {
        if (this.isReadOnly) {
            throw new ReadOnlyBufferException();
        } else if (this.memoryRef.isAccessible) {
            putShort(ix(checkIndex(i, 2)), x);
            return this;
        } else {
            throw new IllegalStateException("buffer is inaccessible");
        }
    }

    void putShortUnchecked(int i, short x) {
        putShort(ix(i), x);
    }

    void putUnchecked(int pos, short[] src, int srcOffset, int length) {
        Memory.pokeShortArray(ix(pos), src, srcOffset, length, !this.nativeByteOrder);
    }

    public ShortBuffer asShortBuffer() {
        if (this.memoryRef.isAccessible) {
            int rem;
            int off = position();
            int lim = limit();
            if (!-assertionsDisabled) {
                if ((off <= lim ? 1 : 0) == 0) {
                    throw new AssertionError();
                }
            }
            if (off <= lim) {
                rem = lim - off;
            } else {
                rem = 0;
            }
            int size = rem >> 1;
            return new ByteBufferAsShortBuffer(this, -1, 0, size, size, off, order());
        }
        throw new IllegalStateException("buffer is inaccessible");
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

    int getIntUnchecked(int i) {
        return getInt(ix(i));
    }

    void getUnchecked(int pos, int[] dst, int dstOffset, int length) {
        Memory.peekIntArray(ix(pos), dst, dstOffset, length, !this.nativeByteOrder);
    }

    private ByteBuffer putInt(long a, int x) {
        Memory.pokeInt(a, x, !this.nativeByteOrder);
        return this;
    }

    public ByteBuffer putInt(int x) {
        if (this.isReadOnly) {
            throw new ReadOnlyBufferException();
        } else if (this.memoryRef.isAccessible) {
            putInt(ix(nextPutIndex(4)), x);
            return this;
        } else {
            throw new IllegalStateException("buffer is inaccessible");
        }
    }

    public ByteBuffer putInt(int i, int x) {
        if (this.isReadOnly) {
            throw new ReadOnlyBufferException();
        } else if (this.memoryRef.isAccessible) {
            putInt(ix(checkIndex(i, 4)), x);
            return this;
        } else {
            throw new IllegalStateException("buffer is inaccessible");
        }
    }

    void putIntUnchecked(int i, int x) {
        putInt(ix(i), x);
    }

    void putUnchecked(int pos, int[] src, int srcOffset, int length) {
        Memory.pokeIntArray(ix(pos), src, srcOffset, length, !this.nativeByteOrder);
    }

    public IntBuffer asIntBuffer() {
        if (this.memoryRef.isAccessible) {
            int rem;
            int off = position();
            int lim = limit();
            if (!-assertionsDisabled) {
                if ((off <= lim ? 1 : 0) == 0) {
                    throw new AssertionError();
                }
            }
            if (off <= lim) {
                rem = lim - off;
            } else {
                rem = 0;
            }
            int size = rem >> 2;
            return new ByteBufferAsIntBuffer(this, -1, 0, size, size, off, order());
        }
        throw new IllegalStateException("buffer is inaccessible");
    }

    private long getLong(long a) {
        return Memory.peekLong(a, !this.nativeByteOrder);
    }

    public long getLong() {
        if (this.memoryRef.isAccessible) {
            return getLong(ix(nextGetIndex(8)));
        }
        throw new IllegalStateException("buffer is inaccessible");
    }

    public long getLong(int i) {
        if (this.memoryRef.isAccessible) {
            return getLong(ix(checkIndex(i, 8)));
        }
        throw new IllegalStateException("buffer is inaccessible");
    }

    long getLongUnchecked(int i) {
        return getLong(ix(i));
    }

    void getUnchecked(int pos, long[] dst, int dstOffset, int length) {
        Memory.peekLongArray(ix(pos), dst, dstOffset, length, !this.nativeByteOrder);
    }

    private ByteBuffer putLong(long a, long x) {
        Memory.pokeLong(a, x, !this.nativeByteOrder);
        return this;
    }

    public ByteBuffer putLong(long x) {
        if (this.isReadOnly) {
            throw new ReadOnlyBufferException();
        } else if (this.memoryRef.isAccessible) {
            putLong(ix(nextPutIndex(8)), x);
            return this;
        } else {
            throw new IllegalStateException("buffer is inaccessible");
        }
    }

    public ByteBuffer putLong(int i, long x) {
        if (this.isReadOnly) {
            throw new ReadOnlyBufferException();
        } else if (this.memoryRef.isAccessible) {
            putLong(ix(checkIndex(i, 8)), x);
            return this;
        } else {
            throw new IllegalStateException("buffer is inaccessible");
        }
    }

    void putLongUnchecked(int i, long x) {
        putLong(ix(i), x);
    }

    void putUnchecked(int pos, long[] src, int srcOffset, int length) {
        Memory.pokeLongArray(ix(pos), src, srcOffset, length, !this.nativeByteOrder);
    }

    public LongBuffer asLongBuffer() {
        if (this.memoryRef.isAccessible) {
            int rem;
            int off = position();
            int lim = limit();
            if (!-assertionsDisabled) {
                if ((off <= lim ? 1 : 0) == 0) {
                    throw new AssertionError();
                }
            }
            if (off <= lim) {
                rem = lim - off;
            } else {
                rem = 0;
            }
            int size = rem >> 3;
            return new ByteBufferAsLongBuffer(this, -1, 0, size, size, off, order());
        }
        throw new IllegalStateException("buffer is inaccessible");
    }

    private float getFloat(long a) {
        return Float.intBitsToFloat(Memory.peekInt(a, !this.nativeByteOrder));
    }

    public float getFloat() {
        if (this.memoryRef.isAccessible) {
            return getFloat(ix(nextGetIndex(4)));
        }
        throw new IllegalStateException("buffer is inaccessible");
    }

    public float getFloat(int i) {
        if (this.memoryRef.isAccessible) {
            return getFloat(ix(checkIndex(i, 4)));
        }
        throw new IllegalStateException("buffer is inaccessible");
    }

    float getFloatUnchecked(int i) {
        return getFloat(ix(i));
    }

    void getUnchecked(int pos, float[] dst, int dstOffset, int length) {
        Memory.peekFloatArray(ix(pos), dst, dstOffset, length, !this.nativeByteOrder);
    }

    private ByteBuffer putFloat(long a, float x) {
        Memory.pokeInt(a, Float.floatToRawIntBits(x), !this.nativeByteOrder);
        return this;
    }

    public ByteBuffer putFloat(float x) {
        if (this.isReadOnly) {
            throw new ReadOnlyBufferException();
        } else if (this.memoryRef.isAccessible) {
            putFloat(ix(nextPutIndex(4)), x);
            return this;
        } else {
            throw new IllegalStateException("buffer is inaccessible");
        }
    }

    public ByteBuffer putFloat(int i, float x) {
        if (this.isReadOnly) {
            throw new ReadOnlyBufferException();
        } else if (this.memoryRef.isAccessible) {
            putFloat(ix(checkIndex(i, 4)), x);
            return this;
        } else {
            throw new IllegalStateException("buffer is inaccessible");
        }
    }

    void putFloatUnchecked(int i, float x) {
        putFloat(ix(i), x);
    }

    void putUnchecked(int pos, float[] src, int srcOffset, int length) {
        Memory.pokeFloatArray(ix(pos), src, srcOffset, length, !this.nativeByteOrder);
    }

    public FloatBuffer asFloatBuffer() {
        if (this.memoryRef.isAccessible) {
            int rem;
            int off = position();
            int lim = limit();
            if (!-assertionsDisabled) {
                if ((off <= lim ? 1 : 0) == 0) {
                    throw new AssertionError();
                }
            }
            if (off <= lim) {
                rem = lim - off;
            } else {
                rem = 0;
            }
            int size = rem >> 2;
            return new ByteBufferAsFloatBuffer(this, -1, 0, size, size, off, order());
        }
        throw new IllegalStateException("buffer is inaccessible");
    }

    private double getDouble(long a) {
        return Double.longBitsToDouble(Memory.peekLong(a, !this.nativeByteOrder));
    }

    public double getDouble() {
        if (this.memoryRef.isAccessible) {
            return getDouble(ix(nextGetIndex(8)));
        }
        throw new IllegalStateException("buffer is inaccessible");
    }

    public double getDouble(int i) {
        if (this.memoryRef.isAccessible) {
            return getDouble(ix(checkIndex(i, 8)));
        }
        throw new IllegalStateException("buffer is inaccessible");
    }

    double getDoubleUnchecked(int i) {
        return getDouble(ix(i));
    }

    void getUnchecked(int pos, double[] dst, int dstOffset, int length) {
        Memory.peekDoubleArray(ix(pos), dst, dstOffset, length, !this.nativeByteOrder);
    }

    private ByteBuffer putDouble(long a, double x) {
        Memory.pokeLong(a, Double.doubleToRawLongBits(x), !this.nativeByteOrder);
        return this;
    }

    public ByteBuffer putDouble(double x) {
        if (this.isReadOnly) {
            throw new ReadOnlyBufferException();
        } else if (this.memoryRef.isAccessible) {
            putDouble(ix(nextPutIndex(8)), x);
            return this;
        } else {
            throw new IllegalStateException("buffer is inaccessible");
        }
    }

    public ByteBuffer putDouble(int i, double x) {
        if (this.isReadOnly) {
            throw new ReadOnlyBufferException();
        } else if (this.memoryRef.isAccessible) {
            putDouble(ix(checkIndex(i, 8)), x);
            return this;
        } else {
            throw new IllegalStateException("buffer is inaccessible");
        }
    }

    void putDoubleUnchecked(int i, double x) {
        putDouble(ix(i), x);
    }

    void putUnchecked(int pos, double[] src, int srcOffset, int length) {
        Memory.pokeDoubleArray(ix(pos), src, srcOffset, length, !this.nativeByteOrder);
    }

    public DoubleBuffer asDoubleBuffer() {
        if (this.memoryRef.isAccessible) {
            int rem;
            int off = position();
            int lim = limit();
            if (!-assertionsDisabled) {
                if ((off <= lim ? 1 : 0) == 0) {
                    throw new AssertionError();
                }
            }
            if (off <= lim) {
                rem = lim - off;
            } else {
                rem = 0;
            }
            int size = rem >> 3;
            return new ByteBufferAsDoubleBuffer(this, -1, 0, size, size, off, order());
        }
        throw new IllegalStateException("buffer is inaccessible");
    }

    public boolean isAccessible() {
        return this.memoryRef.isAccessible;
    }

    public void setAccessible(boolean value) {
        this.memoryRef.isAccessible = value;
    }
}
