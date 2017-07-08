package java.io;

import java.util.Arrays;

public class CharArrayWriter extends Writer {
    protected char[] buf;
    protected int count;

    public CharArrayWriter() {
        this(32);
    }

    public CharArrayWriter(int initialSize) {
        if (initialSize < 0) {
            throw new IllegalArgumentException("Negative initial size: " + initialSize);
        }
        this.buf = new char[initialSize];
    }

    public void write(int c) {
        synchronized (this.lock) {
            int newcount = this.count + 1;
            if (newcount > this.buf.length) {
                this.buf = Arrays.copyOf(this.buf, Math.max(this.buf.length << 1, newcount));
            }
            this.buf[this.count] = (char) c;
            this.count = newcount;
        }
    }

    public void write(char[] c, int off, int len) {
        if (off < 0 || off > c.length || len < 0 || off + len > c.length || off + len < 0) {
            throw new IndexOutOfBoundsException();
        } else if (len != 0) {
            synchronized (this.lock) {
                int newcount = this.count + len;
                if (newcount > this.buf.length) {
                    this.buf = Arrays.copyOf(this.buf, Math.max(this.buf.length << 1, newcount));
                }
                System.arraycopy(c, off, this.buf, this.count, len);
                this.count = newcount;
            }
        }
    }

    public void write(String str, int off, int len) {
        synchronized (this.lock) {
            int newcount = this.count + len;
            if (newcount > this.buf.length) {
                this.buf = Arrays.copyOf(this.buf, Math.max(this.buf.length << 1, newcount));
            }
            str.getChars(off, off + len, this.buf, this.count);
            this.count = newcount;
        }
    }

    public void writeTo(Writer out) throws IOException {
        synchronized (this.lock) {
            out.write(this.buf, 0, this.count);
        }
    }

    public CharArrayWriter append(CharSequence csq) {
        String s = csq == null ? "null" : csq.toString();
        write(s, 0, s.length());
        return this;
    }

    public CharArrayWriter append(CharSequence csq, int start, int end) {
        if (csq == null) {
            csq = "null";
        }
        String s = csq.subSequence(start, end).toString();
        write(s, 0, s.length());
        return this;
    }

    public CharArrayWriter append(char c) {
        write(c);
        return this;
    }

    public void reset() {
        this.count = 0;
    }

    public char[] toCharArray() {
        char[] copyOf;
        synchronized (this.lock) {
            copyOf = Arrays.copyOf(this.buf, this.count);
        }
        return copyOf;
    }

    public int size() {
        return this.count;
    }

    public String toString() {
        String str;
        synchronized (this.lock) {
            str = new String(this.buf, 0, this.count);
        }
        return str;
    }

    public void flush() {
    }

    public void close() {
    }
}
