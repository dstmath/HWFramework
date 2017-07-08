package java.nio;

import libcore.io.Memory;

class ByteBufferAsCharBuffer extends CharBuffer {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    protected final ByteBuffer bb;
    protected final int offset;
    private final ByteOrder order;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.nio.ByteBufferAsCharBuffer.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.nio.ByteBufferAsCharBuffer.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: java.nio.ByteBufferAsCharBuffer.<clinit>():void");
    }

    ByteBufferAsCharBuffer(ByteBuffer bb, int mark, int pos, int lim, int cap, int off, ByteOrder order) {
        super(mark, pos, lim, cap);
        this.bb = bb;
        this.isReadOnly = bb.isReadOnly;
        if (bb instanceof DirectByteBuffer) {
            this.address = bb.address + ((long) off);
        }
        this.order = order;
        this.offset = off;
    }

    public CharBuffer slice() {
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
        return new ByteBufferAsCharBuffer(this.bb, -1, 0, rem, rem, off, this.order);
    }

    public CharBuffer duplicate() {
        return new ByteBufferAsCharBuffer(this.bb, markValue(), position(), limit(), capacity(), this.offset, this.order);
    }

    public CharBuffer asReadOnlyBuffer() {
        return new ByteBufferAsCharBuffer(this.bb.asReadOnlyBuffer(), markValue(), position(), limit(), capacity(), this.offset, this.order);
    }

    protected int ix(int i) {
        return (i << 1) + this.offset;
    }

    public char get() {
        return get(nextGetIndex());
    }

    public char get(int i) {
        return this.bb.getCharUnchecked(ix(checkIndex(i)));
    }

    public CharBuffer get(char[] dst, int offset, int length) {
        Buffer.checkBounds(offset, length, dst.length);
        if (length > remaining()) {
            throw new BufferUnderflowException();
        }
        this.bb.getUnchecked(ix(this.position), dst, offset, length);
        this.position += length;
        return this;
    }

    char getUnchecked(int i) {
        return this.bb.getCharUnchecked(ix(i));
    }

    public CharBuffer put(char x) {
        put(nextPutIndex(), x);
        return this;
    }

    public CharBuffer put(int i, char x) {
        if (this.isReadOnly) {
            throw new ReadOnlyBufferException();
        }
        this.bb.putCharUnchecked(ix(checkIndex(i)), x);
        return this;
    }

    public CharBuffer put(char[] src, int offset, int length) {
        Buffer.checkBounds(offset, length, src.length);
        if (length > remaining()) {
            throw new BufferOverflowException();
        }
        this.bb.putUnchecked(ix(this.position), src, offset, length);
        this.position += length;
        return this;
    }

    public CharBuffer compact() {
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

    public String toString(int start, int end) {
        if (end > limit() || start > end) {
            throw new IndexOutOfBoundsException();
        }
        try {
            char[] ca = new char[(end - start)];
            CharBuffer cb = CharBuffer.wrap(ca);
            CharBuffer db = duplicate();
            db.position(start);
            db.limit(end);
            cb.put(db);
            return new String(ca);
        } catch (StringIndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException();
        }
    }

    public CharBuffer subSequence(int start, int end) {
        Object obj = null;
        int pos = position();
        int lim = limit();
        if (!-assertionsDisabled) {
            if (pos <= lim) {
                obj = 1;
            }
            if (obj == null) {
                throw new AssertionError();
            }
        }
        if (pos > lim) {
            pos = lim;
        }
        int len = lim - pos;
        if (start >= 0 && end <= len && start <= end) {
            return new ByteBufferAsCharBuffer(this.bb, -1, pos + start, pos + end, capacity(), this.offset, this.order);
        }
        throw new IndexOutOfBoundsException();
    }

    public ByteOrder order() {
        return this.order;
    }
}
