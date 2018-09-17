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
        int charAt;
        if (this.pos < this.count) {
            String str = this.buffer;
            int i = this.pos;
            this.pos = i + 1;
            charAt = str.charAt(i) & 255;
        } else {
            charAt = -1;
        }
        return charAt;
    }

    public synchronized int read(byte[] b, int off, int len) {
        Throwable th;
        if (b == null) {
            try {
                throw new NullPointerException();
            } catch (Throwable th2) {
                th = th2;
                throw th;
            }
        }
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
                int cnt = len;
                while (true) {
                    cnt--;
                    if (cnt < 0) {
                        return len;
                    }
                    int off2 = off + 1;
                    try {
                        int i = this.pos;
                        this.pos = i + 1;
                        b[off] = (byte) s.charAt(i);
                        off = off2;
                    } catch (Throwable th3) {
                        th = th3;
                        off = off2;
                    }
                }
            }
        }
        throw new IndexOutOfBoundsException();
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
