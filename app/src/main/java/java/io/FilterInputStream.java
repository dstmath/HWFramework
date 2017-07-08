package java.io;

public class FilterInputStream extends InputStream {
    protected volatile InputStream in;

    protected FilterInputStream(InputStream in) {
        this.in = in;
    }

    public int read() throws IOException {
        return this.in.read();
    }

    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    public int read(byte[] b, int off, int len) throws IOException {
        return this.in.read(b, off, len);
    }

    public long skip(long n) throws IOException {
        return this.in.skip(n);
    }

    public int available() throws IOException {
        return this.in.available();
    }

    public void close() throws IOException {
        this.in.close();
    }

    public synchronized void mark(int readlimit) {
        this.in.mark(readlimit);
    }

    public synchronized void reset() throws IOException {
        this.in.reset();
    }

    public boolean markSupported() {
        return this.in.markSupported();
    }
}
