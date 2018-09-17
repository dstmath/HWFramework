package java.io;

public class StringWriter extends Writer {
    private StringBuffer buf;

    public StringWriter() {
        this.buf = new StringBuffer();
        this.lock = this.buf;
    }

    public StringWriter(int initialSize) {
        if (initialSize < 0) {
            throw new IllegalArgumentException("Negative buffer size");
        }
        this.buf = new StringBuffer(initialSize);
        this.lock = this.buf;
    }

    public void write(int c) {
        this.buf.append((char) c);
    }

    public void write(char[] cbuf, int off, int len) {
        if (off < 0 || off > cbuf.length || len < 0 || off + len > cbuf.length || off + len < 0) {
            throw new IndexOutOfBoundsException();
        } else if (len != 0) {
            this.buf.append(cbuf, off, len);
        }
    }

    public void write(String str) {
        this.buf.append(str);
    }

    public void write(String str, int off, int len) {
        this.buf.append(str.substring(off, off + len));
    }

    public StringWriter append(CharSequence csq) {
        if (csq == null) {
            write("null");
        } else {
            write(csq.toString());
        }
        return this;
    }

    public StringWriter append(CharSequence csq, int start, int end) {
        write((csq == null ? "null" : csq).subSequence(start, end).toString());
        return this;
    }

    public StringWriter append(char c) {
        write((int) c);
        return this;
    }

    public String toString() {
        return this.buf.toString();
    }

    public StringBuffer getBuffer() {
        return this.buf;
    }

    public void flush() {
    }

    public void close() throws IOException {
    }
}
