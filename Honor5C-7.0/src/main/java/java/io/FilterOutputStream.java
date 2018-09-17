package java.io;

public class FilterOutputStream extends OutputStream {
    protected OutputStream out;

    public FilterOutputStream(OutputStream out) {
        this.out = out;
    }

    public void write(int b) throws IOException {
        this.out.write(b);
    }

    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    public void write(byte[] b, int off, int len) throws IOException {
        if ((((off | len) | (b.length - (len + off))) | (off + len)) < 0) {
            throw new IndexOutOfBoundsException();
        }
        for (int i = 0; i < len; i++) {
            write(b[off + i]);
        }
    }

    public void flush() throws IOException {
        this.out.flush();
    }

    public void close() throws IOException {
        try {
            flush();
        } catch (IOException e) {
        }
        this.out.close();
    }
}
