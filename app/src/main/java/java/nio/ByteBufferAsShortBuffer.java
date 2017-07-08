package java.nio;

import libcore.io.Memory;

class ByteBufferAsShortBuffer extends ShortBuffer {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    protected final ByteBuffer bb;
    protected final int offset;
    private final ByteOrder order;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.nio.ByteBufferAsShortBuffer.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.nio.ByteBufferAsShortBuffer.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: java.nio.ByteBufferAsShortBuffer.<clinit>():void");
    }

    ByteBufferAsShortBuffer(ByteBuffer bb, int mark, int pos, int lim, int cap, int off, ByteOrder order) {
        super(mark, pos, lim, cap);
        this.bb = bb;
        this.isReadOnly = bb.isReadOnly;
        if (bb instanceof DirectByteBuffer) {
            this.address = bb.address + ((long) off);
        }
        this.order = order;
        this.offset = off;
    }

    public ShortBuffer slice() {
        int i = 1;
        int pos = position();
        int lim = limit();
        if (!-assertionsDisabled) {
            if ((pos <= lim ? 1 : 0) == 0) {
                throw new AssertionError();
            }
        }
        int rem = pos <= lim ? lim - pos : 0;
        int off = (pos << 1) + this.offset;
        if (!-assertionsDisabled) {
            if (off < 0) {
                i = 0;
            }
            if (i == 0) {
                throw new AssertionError();
            }
        }
        return new ByteBufferAsShortBuffer(this.bb, -1, 0, rem, rem, off, this.order);
    }

    public ShortBuffer duplicate() {
        return new ByteBufferAsShortBuffer(this.bb, markValue(), position(), limit(), capacity(), this.offset, this.order);
    }

    public ShortBuffer asReadOnlyBuffer() {
        return new ByteBufferAsShortBuffer(this.bb.asReadOnlyBuffer(), markValue(), position(), limit(), capacity(), this.offset, this.order);
    }

    protected int ix(int i) {
        return (i << 1) + this.offset;
    }

    public short get() {
        return get(nextGetIndex());
    }

    public short get(int i) {
        return this.bb.getShortUnchecked(ix(checkIndex(i)));
    }

    public ShortBuffer get(short[] dst, int offset, int length) {
        Buffer.checkBounds(offset, length, dst.length);
        if (length > remaining()) {
            throw new BufferUnderflowException();
        }
        this.bb.getUnchecked(ix(this.position), dst, offset, length);
        this.position += length;
        return this;
    }

    public ShortBuffer put(short x) {
        put(nextPutIndex(), x);
        return this;
    }

    public ShortBuffer put(int i, short x) {
        if (this.isReadOnly) {
            throw new ReadOnlyBufferException();
        }
        this.bb.putShortUnchecked(ix(checkIndex(i)), x);
        return this;
    }

    public ShortBuffer put(short[] src, int offset, int length) {
        Buffer.checkBounds(offset, length, src.length);
        if (length > remaining()) {
            throw new BufferOverflowException();
        }
        this.bb.putUnchecked(ix(this.position), src, offset, length);
        this.position += length;
        return this;
    }

    public ShortBuffer compact() {
        if (this.isReadOnly) {
            throw new ReadOnlyBufferException();
        }
        int pos = position();
        int lim = limit();
        if (!-assertionsDisabled) {
            if ((pos <= lim ? 1 : 0) == 0) {
                throw new AssertionError();
            }
        }
        int rem = pos <= lim ? lim - pos : 0;
        if (this.bb instanceof DirectByteBuffer) {
            Memory.memmove(this, ix(0), this, ix(pos), (long) (rem << 1));
        } else {
            System.arraycopy(this.bb.array(), ix(pos), this.bb.array(), ix(0), rem << 1);
        }
        position(rem);
        limit(capacity());
        discardMark();
        return this;
    }

    public boolean isDirect() {
        return this.bb.isDirect();
    }

    public boolean isReadOnly() {
        return this.isReadOnly;
    }

    public ByteOrder order() {
        return this.order;
    }
}
