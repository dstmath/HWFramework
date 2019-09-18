package java.nio;

import java.io.IOException;
import java.util.Spliterator;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

public abstract class CharBuffer extends Buffer implements Comparable<CharBuffer>, Appendable, CharSequence, Readable {
    final char[] hb;
    boolean isReadOnly;
    final int offset;

    public abstract CharBuffer asReadOnlyBuffer();

    public abstract CharBuffer compact();

    public abstract CharBuffer duplicate();

    public abstract char get();

    public abstract char get(int i);

    /* access modifiers changed from: package-private */
    public abstract char getUnchecked(int i);

    public abstract boolean isDirect();

    public abstract ByteOrder order();

    public abstract CharBuffer put(char c);

    public abstract CharBuffer put(int i, char c);

    public abstract CharBuffer slice();

    public abstract CharBuffer subSequence(int i, int i2);

    /* access modifiers changed from: package-private */
    public abstract String toString(int i, int i2);

    CharBuffer(int mark, int pos, int lim, int cap, char[] hb2, int offset2) {
        super(mark, pos, lim, cap, 1);
        this.hb = hb2;
        this.offset = offset2;
    }

    CharBuffer(int mark, int pos, int lim, int cap) {
        this(mark, pos, lim, cap, null, 0);
    }

    public static CharBuffer allocate(int capacity) {
        if (capacity >= 0) {
            return new HeapCharBuffer(capacity, capacity);
        }
        throw new IllegalArgumentException();
    }

    public static CharBuffer wrap(char[] array, int offset2, int length) {
        try {
            return new HeapCharBuffer(array, offset2, length);
        } catch (IllegalArgumentException e) {
            throw new IndexOutOfBoundsException();
        }
    }

    public static CharBuffer wrap(char[] array) {
        return wrap(array, 0, array.length);
    }

    public int read(CharBuffer target) throws IOException {
        int targetRemaining = target.remaining();
        int remaining = remaining();
        if (remaining == 0) {
            return -1;
        }
        int n = Math.min(remaining, targetRemaining);
        int limit = limit();
        if (targetRemaining < remaining) {
            limit(position() + n);
        }
        if (n > 0) {
            try {
                target.put(this);
            } catch (Throwable th) {
                limit(limit);
                throw th;
            }
        }
        limit(limit);
        return n;
    }

    public static CharBuffer wrap(CharSequence csq, int start, int end) {
        try {
            return new StringCharBuffer(csq, start, end);
        } catch (IllegalArgumentException e) {
            throw new IndexOutOfBoundsException();
        }
    }

    public static CharBuffer wrap(CharSequence csq) {
        return wrap(csq, 0, csq.length());
    }

    public CharBuffer get(char[] dst, int offset2, int length) {
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

    public CharBuffer get(char[] dst) {
        return get(dst, 0, dst.length);
    }

    public CharBuffer put(CharBuffer src) {
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

    public CharBuffer put(char[] src, int offset2, int length) {
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

    public final CharBuffer put(char[] src) {
        return put(src, 0, src.length);
    }

    public CharBuffer put(String src, int start, int end) {
        checkBounds(start, end - start, src.length());
        if (start == end) {
            return this;
        }
        if (isReadOnly()) {
            throw new ReadOnlyBufferException();
        } else if (end - start <= remaining()) {
            for (int i = start; i < end; i++) {
                put(src.charAt(i));
            }
            return this;
        } else {
            throw new BufferOverflowException();
        }
    }

    public final CharBuffer put(String src) {
        return put(src, 0, src.length());
    }

    public final boolean hasArray() {
        return this.hb != null && !this.isReadOnly;
    }

    public final char[] array() {
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
        if (!(ob instanceof CharBuffer)) {
            return false;
        }
        CharBuffer that = (CharBuffer) ob;
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

    private static boolean equals(char x, char y) {
        return x == y;
    }

    public int compareTo(CharBuffer that) {
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

    private static int compare(char x, char y) {
        return Character.compare(x, y);
    }

    public String toString() {
        return toString(position(), limit());
    }

    public final int length() {
        return remaining();
    }

    public final char charAt(int index) {
        return get(position() + checkIndex(index, 1));
    }

    public CharBuffer append(CharSequence csq) {
        if (csq == null) {
            return put("null");
        }
        return put(csq.toString());
    }

    public CharBuffer append(CharSequence csq, int start, int end) {
        return put((csq == null ? "null" : csq).subSequence(start, end).toString());
    }

    public CharBuffer append(char c) {
        return put(c);
    }

    public IntStream chars() {
        return StreamSupport.intStream(new Supplier() {
            public final Object get() {
                return CharBuffer.lambda$chars$0(CharBuffer.this);
            }
        }, 16464, false);
    }

    static /* synthetic */ Spliterator.OfInt lambda$chars$0(CharBuffer self) {
        return new CharBufferSpliterator(self);
    }
}
