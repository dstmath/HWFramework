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
        Throwable th;
        Throwable th2 = null;
        OutputStream outputStream = null;
        try {
            outputStream = this.out;
            flush();
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (Throwable th3) {
                    th2 = th3;
                }
            }
            if (th2 != null) {
                throw th2;
            }
            return;
        } catch (Throwable th22) {
            Throwable th4 = th22;
            th22 = th;
            th = th4;
        }
        if (outputStream != null) {
            try {
                outputStream.close();
            } catch (Throwable th5) {
                if (th22 == null) {
                    th22 = th5;
                } else if (th22 != th5) {
                    th22.addSuppressed(th5);
                }
            }
        }
        if (th22 != null) {
            throw th22;
        }
        throw th;
    }
}
