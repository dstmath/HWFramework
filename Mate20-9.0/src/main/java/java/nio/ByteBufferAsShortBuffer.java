package java.nio;

import libcore.io.Memory;

class ByteBufferAsShortBuffer extends ShortBuffer {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    protected final ByteBuffer bb;
    protected final int offset;
    private final ByteOrder order;

    ByteBufferAsShortBuffer(ByteBuffer bb2, int mark, int pos, int lim, int cap, int off, ByteOrder order2) {
        super(mark, pos, lim, cap);
        this.bb = bb2.duplicate();
        this.isReadOnly = bb2.isReadOnly;
        if (bb2 instanceof DirectByteBuffer) {
            this.address = bb2.address + ((long) off);
        }
        this.bb.order(order2);
        this.order = order2;
        this.offset = off;
    }

    public ShortBuffer slice() {
        int pos = position();
        int lim = limit();
        int rem = pos <= lim ? lim - pos : 0;
        ByteBufferAsShortBuffer byteBufferAsShortBuffer = new ByteBufferAsShortBuffer(this.bb, -1, 0, rem, rem, (pos << 1) + this.offset, this.order);
        return byteBufferAsShortBuffer;
    }

    public ShortBuffer duplicate() {
        ByteBufferAsShortBuffer byteBufferAsShortBuffer = new ByteBufferAsShortBuffer(this.bb, markValue(), position(), limit(), capacity(), this.offset, this.order);
        return byteBufferAsShortBuffer;
    }

    public ShortBuffer asReadOnlyBuffer() {
        ByteBufferAsShortBuffer byteBufferAsShortBuffer = new ByteBufferAsShortBuffer(this.bb.asReadOnlyBuffer(), markValue(), position(), limit(), capacity(), this.offset, this.order);
        return byteBufferAsShortBuffer;
    }

    /* access modifiers changed from: protected */
    public int ix(int i) {
        return (i << 1) + this.offset;
    }

    public short get() {
        return get(nextGetIndex());
    }

    public short get(int i) {
        return this.bb.getShortUnchecked(ix(checkIndex(i)));
    }

    public ShortBuffer get(short[] dst, int offset2, int length) {
        checkBounds(offset2, length, dst.length);
        if (length <= remaining()) {
            this.bb.getUnchecked(ix(this.position), dst, offset2, length);
            this.position += length;
            return this;
        }
        throw new BufferUnderflowException();
    }

    public ShortBuffer put(short x) {
        put(nextPutIndex(), x);
        return this;
    }

    public ShortBuffer put(int i, short x) {
        if (!this.isReadOnly) {
            this.bb.putShortUnchecked(ix(checkIndex(i)), x);
            return this;
        }
        throw new ReadOnlyBufferException();
    }

    public ShortBuffer put(short[] src, int offset2, int length) {
        checkBounds(offset2, length, src.length);
        if (length <= remaining()) {
            this.bb.putUnchecked(ix(this.position), src, offset2, length);
            this.position += length;
            return this;
        }
        throw new BufferOverflowException();
    }

    public ShortBuffer compact() {
        if (!this.isReadOnly) {
            int pos = position();
            int lim = limit();
            int rem = pos <= lim ? lim - pos : 0;
            if (!(this.bb instanceof DirectByteBuffer)) {
                System.arraycopy(this.bb.array(), ix(pos), this.bb.array(), ix(0), rem << 1);
            } else {
                Memory.memmove(this, ix(0), this, ix(pos), (long) (rem << 1));
            }
            position(rem);
            limit(capacity());
            discardMark();
            return this;
        }
        throw new ReadOnlyBufferException();
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
