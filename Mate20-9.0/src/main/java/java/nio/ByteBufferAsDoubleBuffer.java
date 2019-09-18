package java.nio;

import libcore.io.Memory;

class ByteBufferAsDoubleBuffer extends DoubleBuffer {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    protected final ByteBuffer bb;
    protected final int offset;
    private final ByteOrder order;

    ByteBufferAsDoubleBuffer(ByteBuffer bb2, int mark, int pos, int lim, int cap, int off, ByteOrder order2) {
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

    public DoubleBuffer slice() {
        int pos = position();
        int lim = limit();
        int rem = pos <= lim ? lim - pos : 0;
        ByteBufferAsDoubleBuffer byteBufferAsDoubleBuffer = new ByteBufferAsDoubleBuffer(this.bb, -1, 0, rem, rem, (pos << 3) + this.offset, this.order);
        return byteBufferAsDoubleBuffer;
    }

    public DoubleBuffer duplicate() {
        ByteBufferAsDoubleBuffer byteBufferAsDoubleBuffer = new ByteBufferAsDoubleBuffer(this.bb, markValue(), position(), limit(), capacity(), this.offset, this.order);
        return byteBufferAsDoubleBuffer;
    }

    public DoubleBuffer asReadOnlyBuffer() {
        ByteBufferAsDoubleBuffer byteBufferAsDoubleBuffer = new ByteBufferAsDoubleBuffer(this.bb.asReadOnlyBuffer(), markValue(), position(), limit(), capacity(), this.offset, this.order);
        return byteBufferAsDoubleBuffer;
    }

    /* access modifiers changed from: protected */
    public int ix(int i) {
        return (i << 3) + this.offset;
    }

    public double get() {
        return get(nextGetIndex());
    }

    public double get(int i) {
        return this.bb.getDoubleUnchecked(ix(checkIndex(i)));
    }

    public DoubleBuffer get(double[] dst, int offset2, int length) {
        checkBounds(offset2, length, dst.length);
        if (length <= remaining()) {
            this.bb.getUnchecked(ix(this.position), dst, offset2, length);
            this.position += length;
            return this;
        }
        throw new BufferUnderflowException();
    }

    public DoubleBuffer put(double x) {
        put(nextPutIndex(), x);
        return this;
    }

    public DoubleBuffer put(int i, double x) {
        if (!this.isReadOnly) {
            this.bb.putDoubleUnchecked(ix(checkIndex(i)), x);
            return this;
        }
        throw new ReadOnlyBufferException();
    }

    public DoubleBuffer put(double[] src, int offset2, int length) {
        checkBounds(offset2, length, src.length);
        if (length <= remaining()) {
            this.bb.putUnchecked(ix(this.position), src, offset2, length);
            this.position += length;
            return this;
        }
        throw new BufferOverflowException();
    }

    public DoubleBuffer compact() {
        if (!this.isReadOnly) {
            int pos = position();
            int lim = limit();
            int rem = pos <= lim ? lim - pos : 0;
            if (!(this.bb instanceof DirectByteBuffer)) {
                System.arraycopy(this.bb.array(), ix(pos), this.bb.array(), ix(0), rem << 3);
            } else {
                Memory.memmove(this, ix(0), this, ix(pos), (long) (rem << 3));
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
