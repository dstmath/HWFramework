package java.nio;

import libcore.io.Memory;

class ByteBufferAsIntBuffer extends IntBuffer {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    protected final ByteBuffer bb;
    protected final int offset;
    private final ByteOrder order;

    ByteBufferAsIntBuffer(ByteBuffer bb2, int mark, int pos, int lim, int cap, int off, ByteOrder order2) {
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

    public IntBuffer slice() {
        int pos = position();
        int lim = limit();
        int rem = pos <= lim ? lim - pos : 0;
        ByteBufferAsIntBuffer byteBufferAsIntBuffer = new ByteBufferAsIntBuffer(this.bb, -1, 0, rem, rem, (pos << 2) + this.offset, this.order);
        return byteBufferAsIntBuffer;
    }

    public IntBuffer duplicate() {
        ByteBufferAsIntBuffer byteBufferAsIntBuffer = new ByteBufferAsIntBuffer(this.bb, markValue(), position(), limit(), capacity(), this.offset, this.order);
        return byteBufferAsIntBuffer;
    }

    public IntBuffer asReadOnlyBuffer() {
        ByteBufferAsIntBuffer byteBufferAsIntBuffer = new ByteBufferAsIntBuffer(this.bb.asReadOnlyBuffer(), markValue(), position(), limit(), capacity(), this.offset, this.order);
        return byteBufferAsIntBuffer;
    }

    /* access modifiers changed from: protected */
    public int ix(int i) {
        return (i << 2) + this.offset;
    }

    public int get() {
        return get(nextGetIndex());
    }

    public int get(int i) {
        return this.bb.getIntUnchecked(ix(checkIndex(i)));
    }

    public IntBuffer get(int[] dst, int offset2, int length) {
        checkBounds(offset2, length, dst.length);
        if (length <= remaining()) {
            this.bb.getUnchecked(ix(this.position), dst, offset2, length);
            this.position += length;
            return this;
        }
        throw new BufferUnderflowException();
    }

    public IntBuffer put(int x) {
        put(nextPutIndex(), x);
        return this;
    }

    public IntBuffer put(int i, int x) {
        if (!this.isReadOnly) {
            this.bb.putIntUnchecked(ix(checkIndex(i)), x);
            return this;
        }
        throw new ReadOnlyBufferException();
    }

    public IntBuffer put(int[] src, int offset2, int length) {
        checkBounds(offset2, length, src.length);
        if (length <= remaining()) {
            this.bb.putUnchecked(ix(this.position), src, offset2, length);
            this.position += length;
            return this;
        }
        throw new BufferOverflowException();
    }

    public IntBuffer compact() {
        if (!this.isReadOnly) {
            int pos = position();
            int lim = limit();
            int rem = pos <= lim ? lim - pos : 0;
            if (!(this.bb instanceof DirectByteBuffer)) {
                System.arraycopy(this.bb.array(), ix(pos), this.bb.array(), ix(0), rem << 2);
            } else {
                Memory.memmove(this, ix(0), this, ix(pos), (long) (rem << 2));
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
