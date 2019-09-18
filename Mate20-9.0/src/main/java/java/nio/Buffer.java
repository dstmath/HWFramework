package java.nio;

public abstract class Buffer {
    static final int SPLITERATOR_CHARACTERISTICS = 16464;
    final int _elementSizeShift;
    long address;
    private int capacity;
    private int limit;
    private int mark = -1;
    int position = 0;

    public abstract Object array();

    public abstract int arrayOffset();

    public abstract boolean hasArray();

    public abstract boolean isDirect();

    public abstract boolean isReadOnly();

    Buffer(int mark2, int pos, int lim, int cap, int elementSizeShift) {
        if (cap >= 0) {
            this.capacity = cap;
            limit(lim);
            position(pos);
            if (mark2 >= 0) {
                if (mark2 <= pos) {
                    this.mark = mark2;
                } else {
                    throw new IllegalArgumentException("mark > position: (" + mark2 + " > " + pos + ")");
                }
            }
            this._elementSizeShift = elementSizeShift;
            return;
        }
        throw new IllegalArgumentException("Negative capacity: " + cap);
    }

    public final int capacity() {
        return this.capacity;
    }

    public final int position() {
        return this.position;
    }

    public final Buffer position(int newPosition) {
        if (newPosition > this.limit || newPosition < 0) {
            throw new IllegalArgumentException("Bad position " + newPosition + "/" + this.limit);
        }
        this.position = newPosition;
        if (this.mark > this.position) {
            this.mark = -1;
        }
        return this;
    }

    public final int limit() {
        return this.limit;
    }

    public final Buffer limit(int newLimit) {
        if (newLimit > this.capacity || newLimit < 0) {
            throw new IllegalArgumentException();
        }
        this.limit = newLimit;
        if (this.position > this.limit) {
            this.position = this.limit;
        }
        if (this.mark > this.limit) {
            this.mark = -1;
        }
        return this;
    }

    public final Buffer mark() {
        this.mark = this.position;
        return this;
    }

    public final Buffer reset() {
        int m = this.mark;
        if (m >= 0) {
            this.position = m;
            return this;
        }
        throw new InvalidMarkException();
    }

    public final Buffer clear() {
        this.position = 0;
        this.limit = this.capacity;
        this.mark = -1;
        return this;
    }

    public final Buffer flip() {
        this.limit = this.position;
        this.position = 0;
        this.mark = -1;
        return this;
    }

    public final Buffer rewind() {
        this.position = 0;
        this.mark = -1;
        return this;
    }

    public final int remaining() {
        return this.limit - this.position;
    }

    public final boolean hasRemaining() {
        return this.position < this.limit;
    }

    /* access modifiers changed from: package-private */
    public final int nextGetIndex() {
        if (this.position < this.limit) {
            int i = this.position;
            this.position = i + 1;
            return i;
        }
        throw new BufferUnderflowException();
    }

    /* access modifiers changed from: package-private */
    public final int nextGetIndex(int nb) {
        if (this.limit - this.position >= nb) {
            int p = this.position;
            this.position += nb;
            return p;
        }
        throw new BufferUnderflowException();
    }

    /* access modifiers changed from: package-private */
    public final int nextPutIndex() {
        if (this.position < this.limit) {
            int i = this.position;
            this.position = i + 1;
            return i;
        }
        throw new BufferOverflowException();
    }

    /* access modifiers changed from: package-private */
    public final int nextPutIndex(int nb) {
        if (this.limit - this.position >= nb) {
            int p = this.position;
            this.position += nb;
            return p;
        }
        throw new BufferOverflowException();
    }

    /* access modifiers changed from: package-private */
    public final int checkIndex(int i) {
        if (i >= 0 && i < this.limit) {
            return i;
        }
        throw new IndexOutOfBoundsException("index=" + i + " out of bounds (limit=" + this.limit + ")");
    }

    /* access modifiers changed from: package-private */
    public final int checkIndex(int i, int nb) {
        if (i >= 0 && nb <= this.limit - i) {
            return i;
        }
        throw new IndexOutOfBoundsException("index=" + i + " out of bounds (limit=" + this.limit + ", nb=" + nb + ")");
    }

    /* access modifiers changed from: package-private */
    public final int markValue() {
        return this.mark;
    }

    /* access modifiers changed from: package-private */
    public final void truncate() {
        this.mark = -1;
        this.position = 0;
        this.limit = 0;
        this.capacity = 0;
    }

    /* access modifiers changed from: package-private */
    public final void discardMark() {
        this.mark = -1;
    }

    static void checkBounds(int off, int len, int size) {
        if ((off | len | (off + len) | (size - (off + len))) < 0) {
            throw new IndexOutOfBoundsException("off=" + off + ", len=" + len + " out of bounds (size=" + size + ")");
        }
    }

    public int getElementSizeShift() {
        return this._elementSizeShift;
    }
}
