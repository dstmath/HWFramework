package java.nio;

public abstract class LongBuffer extends Buffer implements Comparable<LongBuffer> {
    final long[] hb;
    boolean isReadOnly;
    final int offset;

    public abstract LongBuffer asReadOnlyBuffer();

    public abstract LongBuffer compact();

    public abstract LongBuffer duplicate();

    public abstract long get();

    public abstract long get(int i);

    public abstract boolean isDirect();

    public abstract ByteOrder order();

    public abstract LongBuffer put(int i, long j);

    public abstract LongBuffer put(long j);

    public abstract LongBuffer slice();

    LongBuffer(int mark, int pos, int lim, int cap, long[] hb2, int offset2) {
        super(mark, pos, lim, cap, 3);
        this.hb = hb2;
        this.offset = offset2;
    }

    LongBuffer(int mark, int pos, int lim, int cap) {
        this(mark, pos, lim, cap, null, 0);
    }

    public static LongBuffer allocate(int capacity) {
        if (capacity >= 0) {
            return new HeapLongBuffer(capacity, capacity);
        }
        throw new IllegalArgumentException();
    }

    public static LongBuffer wrap(long[] array, int offset2, int length) {
        try {
            return new HeapLongBuffer(array, offset2, length);
        } catch (IllegalArgumentException e) {
            throw new IndexOutOfBoundsException();
        }
    }

    public static LongBuffer wrap(long[] array) {
        return wrap(array, 0, array.length);
    }

    public LongBuffer get(long[] dst, int offset2, int length) {
        checkBounds(offset2, length, dst.length);
        if (length <= remaining()) {
            int end = offset2 + length;
            for (int i = offset2; i < end; i++) {
                dst[i] = get();
            }
            return this;
        }
        throw new BufferUnderflowException();
    }

    public LongBuffer get(long[] dst) {
        return get(dst, 0, dst.length);
    }

    public LongBuffer put(LongBuffer src) {
        if (src != this) {
            int n = src.remaining();
            if (n <= remaining()) {
                for (int i = 0; i < n; i++) {
                    put(src.get());
                }
                return this;
            }
            throw new BufferOverflowException();
        }
        throw new IllegalArgumentException();
    }

    public LongBuffer put(long[] src, int offset2, int length) {
        checkBounds(offset2, length, src.length);
        if (length <= remaining()) {
            int end = offset2 + length;
            for (int i = offset2; i < end; i++) {
                put(src[i]);
            }
            return this;
        }
        throw new BufferOverflowException();
    }

    public final LongBuffer put(long[] src) {
        return put(src, 0, src.length);
    }

    public final boolean hasArray() {
        return this.hb != null && !this.isReadOnly;
    }

    public final long[] array() {
        if (this.hb == null) {
            throw new UnsupportedOperationException();
        } else if (!this.isReadOnly) {
            return this.hb;
        } else {
            throw new ReadOnlyBufferException();
        }
    }

    public final int arrayOffset() {
        if (this.hb == null) {
            throw new UnsupportedOperationException();
        } else if (!this.isReadOnly) {
            return this.offset;
        } else {
            throw new ReadOnlyBufferException();
        }
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(getClass().getName());
        sb.append("[pos=");
        sb.append(position());
        sb.append(" lim=");
        sb.append(limit());
        sb.append(" cap=");
        sb.append(capacity());
        sb.append("]");
        return sb.toString();
    }

    public int hashCode() {
        int h = 1;
        int p = position();
        for (int i = limit() - 1; i >= p; i--) {
            h = (31 * h) + ((int) get(i));
        }
        return h;
    }

    public boolean equals(Object ob) {
        if (this == ob) {
            return true;
        }
        if (!(ob instanceof LongBuffer)) {
            return false;
        }
        LongBuffer that = (LongBuffer) ob;
        if (remaining() != that.remaining()) {
            return false;
        }
        int p = position();
        int i = limit() - 1;
        int j = that.limit() - 1;
        while (i >= p) {
            if (!equals(get(i), that.get(j))) {
                return false;
            }
            i--;
            j--;
        }
        return true;
    }

    private static boolean equals(long x, long y) {
        return x == y;
    }

    public int compareTo(LongBuffer that) {
        int n = position() + Math.min(remaining(), that.remaining());
        int i = position();
        int j = that.position();
        while (i < n) {
            int cmp = compare(get(i), that.get(j));
            if (cmp != 0) {
                return cmp;
            }
            i++;
            j++;
        }
        return remaining() - that.remaining();
    }

    private static int compare(long x, long y) {
        return Long.compare(x, y);
    }
}
