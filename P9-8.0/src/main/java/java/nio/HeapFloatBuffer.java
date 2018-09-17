package java.nio;

class HeapFloatBuffer extends FloatBuffer {
    HeapFloatBuffer(int cap, int lim) {
        this(cap, lim, false);
    }

    HeapFloatBuffer(int cap, int lim, boolean isReadOnly) {
        super(-1, 0, lim, cap, new float[cap], 0);
        this.isReadOnly = isReadOnly;
    }

    HeapFloatBuffer(float[] buf, int off, int len) {
        this(buf, off, len, false);
    }

    HeapFloatBuffer(float[] buf, int off, int len, boolean isReadOnly) {
        super(-1, off, off + len, buf.length, buf, 0);
        this.isReadOnly = isReadOnly;
    }

    protected HeapFloatBuffer(float[] buf, int mark, int pos, int lim, int cap, int off) {
        this(buf, mark, pos, lim, cap, off, false);
    }

    protected HeapFloatBuffer(float[] buf, int mark, int pos, int lim, int cap, int off, boolean isReadOnly) {
        super(mark, pos, lim, cap, buf, off);
        this.isReadOnly = isReadOnly;
    }

    public FloatBuffer slice() {
        return new HeapFloatBuffer(this.hb, -1, 0, remaining(), remaining(), position() + this.offset, this.isReadOnly);
    }

    public FloatBuffer duplicate() {
        return new HeapFloatBuffer(this.hb, markValue(), position(), limit(), capacity(), this.offset, this.isReadOnly);
    }

    public FloatBuffer asReadOnlyBuffer() {
        return new HeapFloatBuffer(this.hb, markValue(), position(), limit(), capacity(), this.offset, true);
    }

    protected int ix(int i) {
        return this.offset + i;
    }

    public float get() {
        return this.hb[ix(nextGetIndex())];
    }

    public float get(int i) {
        return this.hb[ix(checkIndex(i))];
    }

    public FloatBuffer get(float[] dst, int offset, int length) {
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

    public FloatBuffer put(float x) {
        if (this.isReadOnly) {
            throw new ReadOnlyBufferException();
        }
        this.hb[ix(nextPutIndex())] = x;
        return this;
    }

    public FloatBuffer put(int i, float x) {
        if (this.isReadOnly) {
            throw new ReadOnlyBufferException();
        }
        this.hb[ix(checkIndex(i))] = x;
        return this;
    }

    public FloatBuffer put(float[] src, int offset, int length) {
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

    public FloatBuffer put(FloatBuffer src) {
        if (src == this) {
            throw new IllegalArgumentException();
        } else if (this.isReadOnly) {
            throw new ReadOnlyBufferException();
        } else {
            int n;
            if (src instanceof HeapFloatBuffer) {
                HeapFloatBuffer sb = (HeapFloatBuffer) src;
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

    public FloatBuffer compact() {
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
