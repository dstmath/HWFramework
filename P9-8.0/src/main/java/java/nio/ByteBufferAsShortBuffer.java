package java.nio;

import libcore.io.Memory;

class ByteBufferAsShortBuffer extends ShortBuffer {
    static final /* synthetic */ boolean -assertionsDisabled = (ByteBufferAsShortBuffer.class.desiredAssertionStatus() ^ 1);
    protected final ByteBuffer bb;
    protected final int offset;
    private final ByteOrder order;

    ByteBufferAsShortBuffer(ByteBuffer bb, int mark, int pos, int lim, int cap, int off, ByteOrder order) {
        super(mark, pos, lim, cap);
        this.bb = bb.duplicate();
        this.isReadOnly = bb.isReadOnly;
        if (bb instanceof DirectByteBuffer) {
            this.address = bb.address + ((long) off);
        }
        this.bb.order(order);
        this.order = order;
        this.offset = off;
    }

    public ShortBuffer slice() {
        int pos = position();
        int lim = limit();
        if (-assertionsDisabled || pos <= lim) {
            int rem = pos <= lim ? lim - pos : 0;
            int off = (pos << 1) + this.offset;
            if (-assertionsDisabled || off >= 0) {
                return new ByteBufferAsShortBuffer(this.bb, -1, 0, rem, rem, off, this.order);
            }
            throw new AssertionError();
        }
        throw new AssertionError();
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
        if (-assertionsDisabled || pos <= lim) {
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
        throw new AssertionError();
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
