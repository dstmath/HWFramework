package java.io;

public abstract class FilterWriter extends Writer {
    protected Writer out;

    protected FilterWriter(Writer out) {
        super(out);
        this.out = out;
    }

    public void write(int c) throws IOException {
        this.out.write(c);
    }

    public void write(char[] cbuf, int off, int len) throws IOException {
        this.out.write(cbuf, off, len);
    }

    public void write(String str, int off, int len) throws IOException {
        this.out.write(str, off, len);
    }

    public void flush() throws IOException {
        this.out.flush();
    }

    public void close() throws IOException {
        this.out.close();
    }
}
