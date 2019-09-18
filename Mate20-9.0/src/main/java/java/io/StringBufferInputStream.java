package java.io;

@Deprecated
public class StringBufferInputStream extends InputStream {
    protected String buffer;
    protected int count;
    protected int pos;

    public StringBufferInputStream(String s) {
        this.buffer = s;
        this.count = s.length();
    }

    public synchronized int read() {
        char c;
        if (this.pos < this.count) {
            String str = this.buffer;
            int i = this.pos;
            this.pos = i + 1;
            c = str.charAt(i) & 255;
        } else {
            c = 65535;
        }
        return c;
    }

    public synchronized int read(byte[] b, int off, int len) {
        if (b != null) {
            if (off >= 0) {
                if (off <= b.length && len >= 0 && off + len <= b.length && off + len >= 0) {
                    if (this.pos >= this.count) {
                        return -1;
                    }
                    int avail = this.count - this.pos;
                    if (len > avail) {
                        len = avail;
                    }
                    if (len <= 0) {
                        return 0;
                    }
                    String s = this.buffer;
                    int off2 = off;
                    int cnt = len;
                    while (true) {
                        cnt--;
                        if (cnt < 0) {
                            return len;
                        }
                        int off3 = off2 + 1;
                        int i = this.pos;
                        this.pos = i + 1;
                        b[off2] = (byte) s.charAt(i);
                        off2 = off3;
                    }
                }
            }
            throw new IndexOutOfBoundsException();
        }
        throw new NullPointerException();
    }

    public synchronized long skip(long n) {
        if (n < 0) {
            return 0;
        }
        if (n > ((long) (this.count - this.pos))) {
            n = (long) (this.count - this.pos);
        }
        this.pos = (int) (((long) this.pos) + n);
        return n;
    }

    public synchronized int available() {
        return this.count - this.pos;
    }

    public synchronized void reset() {
        this.pos = 0;
    }
}
