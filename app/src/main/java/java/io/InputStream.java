package java.io;

public abstract class InputStream implements Closeable {
    private static final int MAX_SKIP_BUFFER_SIZE = 2048;

    public abstract int read() throws IOException;

    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    public int read(byte[] b, int off, int len) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        } else {
            int c = read();
            if (c == -1) {
                return -1;
            }
            b[off] = (byte) c;
            int i = 1;
            while (i < len) {
                try {
                    c = read();
                    if (c == -1) {
                        break;
                    }
                    b[off + i] = (byte) c;
                    i++;
                } catch (IOException e) {
                }
            }
            return i;
        }
    }

    public long skip(long n) throws IOException {
        long remaining = n;
        if (n <= 0) {
            return 0;
        }
        int size = (int) Math.min(2048, n);
        byte[] skipBuffer = new byte[size];
        while (remaining > 0) {
            int nr = read(skipBuffer, 0, (int) Math.min((long) size, remaining));
            if (nr < 0) {
                break;
            }
            remaining -= (long) nr;
        }
        return n - remaining;
    }

    public int available() throws IOException {
        return 0;
    }

    public void close() throws IOException {
    }

    public synchronized void mark(int readlimit) {
    }

    public synchronized void reset() throws IOException {
        throw new IOException("mark/reset not supported");
    }

    public boolean markSupported() {
        return false;
    }
}
