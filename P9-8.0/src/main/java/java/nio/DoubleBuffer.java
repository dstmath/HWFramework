package java.nio;

public abstract class DoubleBuffer extends Buffer implements Comparable<DoubleBuffer> {
    final double[] hb;
    boolean isReadOnly;
    final int offset;

    public abstract DoubleBuffer asReadOnlyBuffer();

    public abstract DoubleBuffer compact();

    public abstract DoubleBuffer duplicate();

    public abstract double get();

    public abstract double get(int i);

    public abstract boolean isDirect();

    public abstract ByteOrder order();

    public abstract DoubleBuffer put(double d);

    public abstract DoubleBuffer put(int i, double d);

    public abstract DoubleBuffer slice();

    DoubleBuffer(int mark, int pos, int lim, int cap, double[] hb, int offset) {
        super(mark, pos, lim, cap, 3);
        this.hb = hb;
        this.offset = offset;
    }

    DoubleBuffer(int mark, int pos, int lim, int cap) {
        this(mark, pos, lim, cap, null, 0);
    }

    public static DoubleBuffer allocate(int capacity) {
        if (capacity >= 0) {
            return new HeapDoubleBuffer(capacity, capacity);
        }
        throw new IllegalArgumentException();
    }

    public static DoubleBuffer wrap(double[] array, int offset, int length) {
        try {
            return new HeapDoubleBuffer(array, offset, length);
        } catch (IllegalArgumentException e) {
            throw new IndexOutOfBoundsException();
        }
    }

    public static DoubleBuffer wrap(double[] array) {
        return wrap(array, 0, array.length);
    }

    public DoubleBuffer get(double[] dst, int offset, int length) {
        Buffer.checkBounds(offset, length, dst.length);
        if (length > remaining()) {
            throw new BufferUnderflowException();
        }
        int end = offset + length;
        for (int i = offset; i < end; i++) {
            dst[i] = get();
        }
        return this;
    }

    public DoubleBuffer get(double[] dst) {
        return get(dst, 0, dst.length);
    }

    public DoubleBuffer put(DoubleBuffer src) {
        if (src == this) {
            throw new IllegalArgumentException();
        }
        int n = src.remaining();
        if (n > remaining()) {
            throw new BufferOverflowException();
        }
        for (int i = 0; i < n; i++) {
            put(src.get());
        }
        return this;
    }

    public DoubleBuffer put(double[] src, int offset, int length) {
        Buffer.checkBounds(offset, length, src.length);
        if (length > remaining()) {
            throw new BufferOverflowException();
        }
        int end = offset + length;
        for (int i = offset; i < end; i++) {
            put(src[i]);
        }
        return this;
    }

    public final DoubleBuffer put(double[] src) {
        return put(src, 0, src.length);
    }

    public final boolean hasArray() {
        return this.hb != null ? this.isReadOnly ^ 1 : false;
    }

    public final double[] array() {
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
        for (int i = limit() - 1; i >= position(); i--) {
            h = (h * 31) + ((int) get(i));
        }
        return h;
    }

    public boolean equals(Object ob) {
        if (this == ob) {
            return true;
        }
        if (!(ob instanceof DoubleBuffer)) {
            return false;
        }
        DoubleBuffer that = (DoubleBuffer) ob;
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

    private static boolean equals(double x, double y) {
        if (x != y) {
            return Double.isNaN(x) ? Double.isNaN(y) : false;
        } else {
            return true;
        }
    }

    public int compareTo(DoubleBuffer that) {
        int n = position() + Math.min(remaining(), that.remaining());
        int i = position();
        int j = that.position();
        while (i < n) {
            int cmp = Double.compare(get(i), that.get(j));
            if (cmp != 0) {
                return cmp;
            }
            i++;
            j++;
        }
        return remaining() - that.remaining();
    }

    private static int compare(double x, double y) {
        if (x < y) {
            return -1;
        }
        if (x > y) {
            return 1;
        }
        if (x == y) {
            return 0;
        }
        if (Double.isNaN(x)) {
            return Double.isNaN(y) ? 0 : 1;
        } else {
            return -1;
        }
    }
}
