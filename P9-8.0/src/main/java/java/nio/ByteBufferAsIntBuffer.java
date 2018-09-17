package java.nio;

import libcore.io.Memory;

class ByteBufferAsIntBuffer extends IntBuffer {
    static final /* synthetic */ boolean -assertionsDisabled = (ByteBufferAsIntBuffer.class.desiredAssertionStatus() ^ 1);
    protected final ByteBuffer bb;
    protected final int offset;
    private final ByteOrder order;

    ByteBufferAsIntBuffer(ByteBuffer bb, int mark, int pos, int lim, int cap, int off, ByteOrder order) {
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

    public IntBuffer slice() {
        int pos = position();
        int lim = limit();
        if (-assertionsDisabled || pos <= lim) {
            int rem = pos <= lim ? lim - pos : 0;
            int off = (pos << 2) + this.offset;
            if (-assertionsDisabled || off >= 0) {
                return new ByteBufferAsIntBuffer(this.bb, -1, 0, rem, rem, off, this.order);
            }
            throw new AssertionError();
        }
        throw new AssertionError();
    }

    public IntBuffer duplicate() {
        return new ByteBufferAsIntBuffer(this.bb, markValue(), position(), limit(), capacity(), this.offset, this.order);
    }

    public IntBuffer asReadOnlyBuffer() {
        return new ByteBufferAsIntBuffer(this.bb.asReadOnlyBuffer(), markValue(), position(), limit(), capacity(), this.offset, this.order);
    }

    protected int ix(int i) {
        return (i << 2) + this.offset;
    }

    public int get() {
        return get(nextGetIndex());
    }

    public int get(int i) {
        return this.bb.getIntUnchecked(ix(checkIndex(i)));
    }

    public IntBuffer get(int[] dst, int offset, int length) {
        Buffer.checkBounds(offset, length, dst.length);
        if (length > remaining()) {
            throw new BufferUnderflowException();
        }
        this.bb.getUnchecked(ix(this.position), dst, offset, length);
        this.position += length;
        return this;
    }

    public IntBuffer put(int x) {
        put(nextPutIndex(), x);
        return this;
    }

    public IntBuffer put(int i, int x) {
        if (this.isReadOnly) {
            throw new ReadOnlyBufferException();
        }
        this.bb.putIntUnchecked(ix(checkIndex(i)), x);
        return this;
    }

    public IntBuffer put(int[] src, int offset, int length) {
        Buffer.checkBounds(offset, length, src.length);
        if (length > remaining()) {
            throw new BufferOverflowException();
        }
        this.bb.putUnchecked(ix(this.position), src, offset, length);
        this.position += length;
        return this;
    }

    public IntBuffer compact() {
        if (this.isReadOnly) {
            throw new ReadOnlyBufferException();
        }
        int pos = position();
        int lim = limit();
        if (-assertionsDisabled || pos <= lim) {
            int rem = pos <= lim ? lim - pos : 0;
            if (this.bb instanceof DirectByteBuffer) {
                Memory.memmove(this, ix(0), this, ix(pos), (long) (rem << 2));
            } else {
                System.arraycopy(this.bb.array(), ix(pos), this.bb.array(), ix(0), rem << 2);
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
