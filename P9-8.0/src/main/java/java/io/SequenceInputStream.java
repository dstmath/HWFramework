package java.io;

import java.util.Enumeration;
import java.util.Vector;

public class SequenceInputStream extends InputStream {
    Enumeration<? extends InputStream> e;
    InputStream in;

    public SequenceInputStream(Enumeration<? extends InputStream> e) {
        this.e = e;
        try {
            nextStream();
        } catch (IOException e2) {
            throw new Error("panic");
        }
    }

    public SequenceInputStream(InputStream s1, InputStream s2) {
        Vector<InputStream> v = new Vector(2);
        v.addElement(s1);
        v.addElement(s2);
        this.e = v.elements();
        try {
            nextStream();
        } catch (IOException e) {
            throw new Error("panic");
        }
    }

    final void nextStream() throws IOException {
        if (this.in != null) {
            this.in.close();
        }
        if (this.e.hasMoreElements()) {
            this.in = (InputStream) this.e.nextElement();
            if (this.in == null) {
                throw new NullPointerException();
            }
            return;
        }
        this.in = null;
    }

    public int available() throws IOException {
        if (this.in == null) {
            return 0;
        }
        return this.in.available();
    }

    public int read() throws IOException {
        while (this.in != null) {
            int c = this.in.read();
            if (c != -1) {
                return c;
            }
            nextStream();
        }
        return -1;
    }

    public int read(byte[] b, int off, int len) throws IOException {
        if (this.in == null) {
            return -1;
        }
        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        } else {
            do {
                int n = this.in.read(b, off, len);
                if (n > 0) {
                    return n;
                }
                nextStream();
            } while (this.in != null);
            return -1;
        }
    }

    public void close() throws IOException {
        do {
            nextStream();
        } while (this.in != null);
    }
}
