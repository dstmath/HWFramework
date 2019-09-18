package java.io;

public class FilterOutputStream extends OutputStream {
    protected OutputStream out;

    public FilterOutputStream(OutputStream out2) {
        this.out = out2;
    }

    public void write(int b) throws IOException {
        this.out.write(b);
    }

    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    public void write(byte[] b, int off, int len) throws IOException {
        if ((off | len | (b.length - (len + off)) | (off + len)) >= 0) {
            for (int i = 0; i < len; i++) {
                write((int) b[off + i]);
            }
            return;
        }
        throw new IndexOutOfBoundsException();
    }

    public void flush() throws IOException {
        this.out.flush();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0010, code lost:
        r1 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:5:0x000b, code lost:
        r1 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:6:0x000c, code lost:
        r2 = null;
     */
    public void close() throws IOException {
        OutputStream ostream = this.out;
        flush();
        if (ostream != null) {
            ostream.close();
            return;
        }
        return;
        if (ostream != null) {
            if (r2 != null) {
                try {
                    ostream.close();
                } catch (Throwable th) {
                    r2.addSuppressed(th);
                }
            } else {
                ostream.close();
            }
        }
        throw th;
        throw th;
    }
}
