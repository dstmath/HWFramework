package java.nio;

class HeapShortBuffer extends ShortBuffer {
    HeapShortBuffer(int cap, int lim) {
        this(cap, lim, false);
    }

    HeapShortBuffer(int cap, int lim, boolean isReadOnly) {
        super(-1, 0, lim, cap, new short[cap], 0);
        this.isReadOnly = isReadOnly;
    }

    HeapShortBuffer(short[] buf, int off, int len) {
        this(buf, off, len, false);
    }

    HeapShortBuffer(short[] buf, int off, int len, boolean isReadOnly) {
        super(-1, off, off + len, buf.length, buf, 0);
        this.isReadOnly = isReadOnly;
    }

    protected HeapShortBuffer(short[] buf, int mark, int pos, int lim, int cap, int off) {
        this(buf, mark, pos, lim, cap, off, false);
    }

    protected HeapShortBuffer(short[] buf, int mark, int pos, int lim, int cap, int off, boolean isReadOnly) {
        super(mark, pos, lim, cap, buf, off);
        this.isReadOnly = isReadOnly;
    }

    public ShortBuffer slice() {
        HeapShortBuffer heapShortBuffer = new HeapShortBuffer(this.hb, -1, 0, remaining(), remaining(), position() + this.offset, this.isReadOnly);
        return heapShortBuffer;
    }

    public ShortBuffer duplicate() {
        HeapShortBuffer heapShortBuffer = new HeapShortBuffer(this.hb, markValue(), position(), limit(), capacity(), this.offset, this.isReadOnly);
        return heapShortBuffer;
    }

    public ShortBuffer asReadOnlyBuffer() {
        HeapShortBuffer heapShortBuffer = new HeapShortBuffer(this.hb, markValue(), position(), limit(), capacity(), this.offset, true);
        return heapShortBuffer;
    }

    /* access modifiers changed from: protected */
    public int ix(int i) {
        return this.offset + i;
    }

    public short get() {
        return this.hb[ix(nextGetIndex())];
    }

    public short get(int i) {
        return this.hb[ix(checkIndex(i))];
    }

    public ShortBuffer get(short[] dst, int offset, int length) {
        checkBounds(offset, length, dst.length);
        if (length <= remaining()) {
            System.arraycopy((Object) this.hb, ix(position()), (Object) dst, offset, length);
            position(position() + length);
            return this;
        }
        throw new BufferUnderflowException();
    }

    public boolean isDirect() {
        return false;
    }

    public boolean isReadOnly() {
        return this.isReadOnly;
    }

    public ShortBuffer put(short x) {
        if (!this.isReadOnly) {
            this.hb[ix(nextPutIndex())] = x;
            return this;
        }
        throw new ReadOnlyBufferException();
    }

    public ShortBuffer put(int i, short x) {
        if (!this.isReadOnly) {
            this.hb[ix(checkIndex(i))] = x;
            return this;
        }
        throw new ReadOnlyBufferException();
    }

    public ShortBuffer put(short[] src, int offset, int length) {
        if (!this.isReadOnly) {
            checkBounds(offset, length, src.length);
            if (length <= remaining()) {
                System.arraycopy((Object) src, offset, (Object) this.hb, ix(position()), length);
                position(position() + length);
                return this;
            }
            throw new BufferOverflowException();
        }
        throw new ReadOnlyBufferException();
    }

    public ShortBuffer put(ShortBuffer src) {
        if (src == this) {
            throw new IllegalArgumentException();
        } else if (!this.isReadOnly) {
            if (src instanceof HeapShortBuffer) {
                HeapShortBuffer sb = (HeapShortBuffer) src;
                int n = sb.remaining();
                if (n <= remaining()) {
                    System.arraycopy((Object) sb.hb, sb.ix(sb.position()), (Object) this.hb, ix(position()), n);
                    sb.position(sb.position() + n);
                    position(position() + n);
                } else {
                    throw new BufferOverflowException();
                }
            } else if (src.isDirect()) {
                int n2 = src.remaining();
                if (n2 <= remaining()) {
                    src.get(this.hb, ix(position()), n2);
                    position(position() + n2);
                } else {
                    throw new BufferOverflowException();
                }
            } else {
                super.put(src);
            }
            return this;
        } else {
            throw new ReadOnlyBufferException();
        }
    }

    public ShortBuffer compact() {
        if (!this.isReadOnly) {
            System.arraycopy((Object) this.hb, ix(position()), (Object) this.hb, ix(0), remaining());
            position(remaining());
            limit(capacity());
            discardMark();
            return this;
        }
        throw new ReadOnlyBufferException();
    }

    public ByteOrder order() {
        return ByteOrder.nativeOrder();
    }
}
