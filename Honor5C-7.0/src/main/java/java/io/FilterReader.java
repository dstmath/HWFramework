package java.io;

public abstract class FilterReader extends Reader {
    protected Reader in;

    protected FilterReader(Reader in) {
        super(in);
        this.in = in;
    }

    public int read() throws IOException {
        return this.in.read();
    }

    public int read(char[] cbuf, int off, int len) throws IOException {
        return this.in.read(cbuf, off, len);
    }

    public long skip(long n) throws IOException {
        return this.in.skip(n);
    }

    public boolean ready() throws IOException {
        return this.in.ready();
    }

    public boolean markSupported() {
        return this.in.markSupported();
    }

    public void mark(int readAheadLimit) throws IOException {
        this.in.mark(readAheadLimit);
    }

    public void reset() throws IOException {
        this.in.reset();
    }

    public void close() throws IOException {
        this.in.close();
    }
}
