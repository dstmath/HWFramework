package java.nio;

public abstract class IntBuffer extends Buffer implements Comparable<IntBuffer> {
    final int[] hb;
    boolean isReadOnly;
    final int offset;

    public abstract IntBuffer asReadOnlyBuffer();

    public abstract IntBuffer compact();

    public abstract IntBuffer duplicate();

    public abstract int get();

    public abstract int get(int i);

    public abstract boolean isDirect();

    public abstract ByteOrder order();

    public abstract IntBuffer put(int i);

    public abstract IntBuffer put(int i, int i2);

    public abstract IntBuffer slice();

    IntBuffer(int mark, int pos, int lim, int cap, int[] hb2, int offset2) {
        super(mark, pos, lim, cap, 2);
        this.hb = hb2;
        this.offset = offset2;
    }

    IntBuffer(int mark, int pos, int lim, int cap) {
        this(mark, pos, lim, cap, null, 0);
    }

    public static IntBuffer allocate(int capacity) {
        if (capacity >= 0) {
            return new HeapIntBuffer(capacity, capacity);
        }
        throw new IllegalArgumentException();
    }

    public static IntBuffer wrap(int[] array, int offset2, int length) {
        try {
            return new HeapIntBuffer(array, offset2, length);
        } catch (IllegalArgumentException e) {
            throw new IndexOutOfBoundsException();
        }
    }

    public static IntBuffer wrap(int[] array) {
        return wrap(array, 0, array.length);
    }

    public IntBuffer get(int[] dst, int offset2, int length) {
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

    public IntBuffer get(int[] dst) {
        return get(dst, 0, dst.length);
    }

    public IntBuffer put(IntBuffer src) {
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

    public IntBuffer put(int[] src, int offset2, int length) {
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

    public final IntBuffer put(int[] src) {
        return put(src, 0, src.length);
    }

    public final boolean hasArray() {
        return this.hb != null && !this.isReadOnly;
    }

    public final int[] array() {
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
            h = (31 * h) + get(i);
        }
        return h;
    }

    public boolean equals(Object ob) {
        if (this == ob) {
            return true;
        }
        if (!(ob instanceof IntBuffer)) {
            return false;
        }
        IntBuffer that = (IntBuffer) ob;
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

    private static boolean equals(int x, int y) {
        return x == y;
    }

    public int compareTo(IntBuffer that) {
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

    private static int compare(int x, int y) {
        return Integer.compare(x, y);
    }
}
