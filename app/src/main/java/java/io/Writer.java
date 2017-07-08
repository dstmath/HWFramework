package java.io;

public abstract class Writer implements Appendable, Closeable, Flushable {
    protected Object lock;
    private char[] writeBuffer;
    private final int writeBufferSize;

    public abstract void close() throws IOException;

    public abstract void flush() throws IOException;

    public abstract void write(char[] cArr, int i, int i2) throws IOException;

    protected Writer() {
        this.writeBufferSize = Record.maxExpansion;
        this.lock = this;
    }

    protected Writer(Object lock) {
        this.writeBufferSize = Record.maxExpansion;
        if (lock == null) {
            throw new NullPointerException();
        }
        this.lock = lock;
    }

    public void write(int c) throws IOException {
        synchronized (this.lock) {
            if (this.writeBuffer == null) {
                this.writeBuffer = new char[Record.maxExpansion];
            }
            this.writeBuffer[0] = (char) c;
            write(this.writeBuffer, 0, 1);
        }
    }

    public void write(char[] cbuf) throws IOException {
        write(cbuf, 0, cbuf.length);
    }

    public void write(String str) throws IOException {
        write(str, 0, str.length());
    }

    public void write(String str, int off, int len) throws IOException {
        synchronized (this.lock) {
            char[] cbuf;
            if (len <= Record.maxExpansion) {
                if (this.writeBuffer == null) {
                    this.writeBuffer = new char[Record.maxExpansion];
                }
                cbuf = this.writeBuffer;
            } else {
                cbuf = new char[len];
            }
            str.getChars(off, off + len, cbuf, 0);
            write(cbuf, 0, len);
        }
    }

    public Writer append(CharSequence csq) throws IOException {
        if (csq == null) {
            write("null");
        } else {
            write(csq.toString());
        }
        return this;
    }

    public Writer append(CharSequence csq, int start, int end) throws IOException {
        CharSequence cs;
        if (csq == null) {
            cs = "null";
        } else {
            cs = csq;
        }
        write(cs.subSequence(start, end).toString());
        return this;
    }

    public Writer append(char c) throws IOException {
        write((int) c);
        return this;
    }
}
