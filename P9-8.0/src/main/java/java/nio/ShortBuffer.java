package java.nio;

public abstract class ShortBuffer extends Buffer implements Comparable<ShortBuffer> {
    final short[] hb;
    boolean isReadOnly;
    final int offset;

    public abstract ShortBuffer asReadOnlyBuffer();

    public abstract ShortBuffer compact();

    public abstract ShortBuffer duplicate();

    public abstract short get();

    public abstract short get(int i);

    public abstract boolean isDirect();

    public abstract ByteOrder order();

    public abstract ShortBuffer put(int i, short s);

    public abstract ShortBuffer put(short s);

    public abstract ShortBuffer slice();

    ShortBuffer(int mark, int pos, int lim, int cap, short[] hb, int offset) {
        super(mark, pos, lim, cap, 1);
        this.hb = hb;
        this.offset = offset;
    }

    ShortBuffer(int mark, int pos, int lim, int cap) {
        this(mark, pos, lim, cap, null, 0);
    }

    public static ShortBuffer allocate(int capacity) {
        if (capacity >= 0) {
            return new HeapShortBuffer(capacity, capacity);
        }
        throw new IllegalArgumentException();
    }

    public static ShortBuffer wrap(short[] array, int offset, int length) {
        try {
            return new HeapShortBuffer(array, offset, length);
        } catch (IllegalArgumentException e) {
            throw new IndexOutOfBoundsException();
        }
    }

    public static ShortBuffer wrap(short[] array) {
        return wrap(array, 0, array.length);
    }

    public ShortBuffer get(short[] dst, int offset, int length) {
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

    public ShortBuffer get(short[] dst) {
        return get(dst, 0, dst.length);
    }

    public ShortBuffer put(ShortBuffer src) {
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

    public ShortBuffer put(short[] src, int offset, int length) {
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

    public final ShortBuffer put(short[] src) {
        return put(src, 0, src.length);
    }

    public final boolean hasArray() {
        return this.hb != null ? this.isReadOnly ^ 1 : false;
    }

    public final short[] array() {
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
            h = (h * 31) + get(i);
        }
        return h;
    }

    public boolean equals(Object ob) {
        if (this == ob) {
            return true;
        }
        if (!(ob instanceof ShortBuffer)) {
            return false;
        }
        ShortBuffer that = (ShortBuffer) ob;
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

    private static boolean equals(short x, short y) {
        return x == y;
    }

    public int compareTo(ShortBuffer that) {
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

    private static int compare(short x, short y) {
        return Short.compare(x, y);
    }
}
