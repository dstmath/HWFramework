package java.nio;

public abstract class FloatBuffer extends Buffer implements Comparable<FloatBuffer> {
    final float[] hb;
    boolean isReadOnly;
    final int offset;

    public abstract FloatBuffer asReadOnlyBuffer();

    public abstract FloatBuffer compact();

    public abstract FloatBuffer duplicate();

    public abstract float get();

    public abstract float get(int i);

    public abstract boolean isDirect();

    public abstract ByteOrder order();

    public abstract FloatBuffer put(float f);

    public abstract FloatBuffer put(int i, float f);

    public abstract FloatBuffer slice();

    FloatBuffer(int mark, int pos, int lim, int cap, float[] hb, int offset) {
        super(mark, pos, lim, cap, 2);
        this.hb = hb;
        this.offset = offset;
    }

    FloatBuffer(int mark, int pos, int lim, int cap) {
        this(mark, pos, lim, cap, null, 0);
    }

    public static FloatBuffer allocate(int capacity) {
        if (capacity >= 0) {
            return new HeapFloatBuffer(capacity, capacity);
        }
        throw new IllegalArgumentException();
    }

    public static FloatBuffer wrap(float[] array, int offset, int length) {
        try {
            return new HeapFloatBuffer(array, offset, length);
        } catch (IllegalArgumentException e) {
            throw new IndexOutOfBoundsException();
        }
    }

    public static FloatBuffer wrap(float[] array) {
        return wrap(array, 0, array.length);
    }

    public FloatBuffer get(float[] dst, int offset, int length) {
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

    public FloatBuffer get(float[] dst) {
        return get(dst, 0, dst.length);
    }

    public FloatBuffer put(FloatBuffer src) {
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

    public FloatBuffer put(float[] src, int offset, int length) {
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

    public final FloatBuffer put(float[] src) {
        return put(src, 0, src.length);
    }

    public final boolean hasArray() {
        return this.hb != null ? this.isReadOnly ^ 1 : false;
    }

    public final float[] array() {
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
        if (!(ob instanceof FloatBuffer)) {
            return false;
        }
        FloatBuffer that = (FloatBuffer) ob;
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

    private static boolean equals(float x, float y) {
        if (x != y) {
            return Float.isNaN(x) ? Float.isNaN(y) : false;
        } else {
            return true;
        }
    }

    public int compareTo(FloatBuffer that) {
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

    private static int compare(float x, float y) {
        if (x < y) {
            return -1;
        }
        if (x > y) {
            return 1;
        }
        if (x == y) {
            return 0;
        }
        if (Float.isNaN(x)) {
            return Float.isNaN(y) ? 0 : 1;
        } else {
            return -1;
        }
    }
}
