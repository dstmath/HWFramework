package java.nio;

class HeapIntBuffer extends IntBuffer {
    HeapIntBuffer(int cap, int lim) {
        this(cap, lim, false);
    }

    HeapIntBuffer(int cap, int lim, boolean isReadOnly) {
        super(-1, 0, lim, cap, new int[cap], 0);
        this.isReadOnly = isReadOnly;
    }

    HeapIntBuffer(int[] buf, int off, int len) {
        this(buf, off, len, false);
    }

    HeapIntBuffer(int[] buf, int off, int len, boolean isReadOnly) {
        super(-1, off, off + len, buf.length, buf, 0);
        this.isReadOnly = isReadOnly;
    }

    protected HeapIntBuffer(int[] buf, int mark, int pos, int lim, int cap, int off) {
        this(buf, mark, pos, lim, cap, off, false);
    }

    protected HeapIntBuffer(int[] buf, int mark, int pos, int lim, int cap, int off, boolean isReadOnly) {
        super(mark, pos, lim, cap, buf, off);
        this.isReadOnly = isReadOnly;
    }

    public IntBuffer slice() {
        return new HeapIntBuffer(this.hb, -1, 0, remaining(), remaining(), position() + this.offset, this.isReadOnly);
    }

    public IntBuffer duplicate() {
        return new HeapIntBuffer(this.hb, markValue(), position(), limit(), capacity(), this.offset, this.isReadOnly);
    }

    public IntBuffer asReadOnlyBuffer() {
        return new HeapIntBuffer(this.hb, markValue(), position(), limit(), capacity(), this.offset, true);
    }

    protected int ix(int i) {
        return this.offset + i;
    }

    public int get() {
        return this.hb[ix(nextGetIndex())];
    }

    public int get(int i) {
        return this.hb[ix(checkIndex(i))];
    }

    public IntBuffer get(int[] dst, int offset, int length) {
        Buffer.checkBounds(offset, length, dst.length);
        if (length > remaining()) {
            throw new BufferUnderflowException();
        }
        System.arraycopy(this.hb, ix(position()), dst, offset, length);
        position(position() + length);
        return this;
    }

    public boolean isDirect() {
        return false;
    }

    public boolean isReadOnly() {
        return this.isReadOnly;
    }

    public IntBuffer put(int x) {
        if (this.isReadOnly) {
            throw new ReadOnlyBufferException();
        }
        this.hb[ix(nextPutIndex())] = x;
        return this;
    }

    public IntBuffer put(int i, int x) {
        if (this.isReadOnly) {
            throw new ReadOnlyBufferException();
        }
        this.hb[ix(checkIndex(i))] = x;
        return this;
    }

    public IntBuffer put(int[] src, int offset, int length) {
        if (this.isReadOnly) {
            throw new ReadOnlyBufferException();
        }
        Buffer.checkBounds(offset, length, src.length);
        if (length > remaining()) {
            throw new BufferOverflowException();
        }
        System.arraycopy(src, offset, this.hb, ix(position()), length);
        position(position() + length);
        return this;
    }

    public IntBuffer put(IntBuffer src) {
        if (src == this) {
            throw new IllegalArgumentException();
        } else if (this.isReadOnly) {
            throw new ReadOnlyBufferException();
        } else {
            int n;
            if (src instanceof HeapIntBuffer) {
                HeapIntBuffer sb = (HeapIntBuffer) src;
                n = sb.remaining();
                if (n > remaining()) {
                    throw new BufferOverflowException();
                }
                System.arraycopy(sb.hb, sb.ix(sb.position()), this.hb, ix(position()), n);
                sb.position(sb.position() + n);
                position(position() + n);
            } else if (src.isDirect()) {
                n = src.remaining();
                if (n > remaining()) {
                    throw new BufferOverflowException();
                }
                src.get(this.hb, ix(position()), n);
                position(position() + n);
            } else {
                super.put(src);
            }
            return this;
        }
    }

    public IntBuffer compact() {
        if (this.isReadOnly) {
            throw new ReadOnlyBufferException();
        }
        System.arraycopy(this.hb, ix(position()), this.hb, ix(0), remaining());
        position(remaining());
        limit(capacity());
        discardMark();
        return this;
    }

    public ByteOrder order() {
        return ByteOrder.nativeOrder();
    }
}
