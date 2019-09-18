package java.io;

public class LineNumberReader extends BufferedReader {
    private static final int maxSkipBufferSize = 8192;
    private int lineNumber = 0;
    private int markedLineNumber;
    private boolean markedSkipLF;
    private char[] skipBuffer = null;
    private boolean skipLF;

    public LineNumberReader(Reader in) {
        super(in);
    }

    public LineNumberReader(Reader in, int sz) {
        super(in, sz);
    }

    public void setLineNumber(int lineNumber2) {
        this.lineNumber = lineNumber2;
    }

    public int getLineNumber() {
        return this.lineNumber;
    }

    public int read() throws IOException {
        synchronized (this.lock) {
            int c = super.read();
            if (this.skipLF) {
                if (c == 10) {
                    c = super.read();
                }
                this.skipLF = false;
            }
            if (c != 10) {
                if (c != 13) {
                    return c;
                }
                this.skipLF = true;
            }
            this.lineNumber++;
            return 10;
        }
    }

    public int read(char[] cbuf, int off, int len) throws IOException {
        int n;
        synchronized (this.lock) {
            n = super.read(cbuf, off, len);
            for (int i = off; i < off + n; i++) {
                char c = cbuf[i];
                if (this.skipLF) {
                    this.skipLF = false;
                    if (c == 10) {
                    }
                }
                if (c != 10) {
                    if (c != 13) {
                    } else {
                        this.skipLF = true;
                    }
                }
                this.lineNumber++;
            }
        }
        return n;
    }

    public String readLine() throws IOException {
        String l;
        synchronized (this.lock) {
            l = super.readLine(this.skipLF);
            this.skipLF = false;
            if (l != null) {
                this.lineNumber++;
            }
        }
        return l;
    }

    public long skip(long n) throws IOException {
        long j;
        if (n >= 0) {
            int nn = (int) Math.min(n, 8192);
            synchronized (this.lock) {
                if (this.skipBuffer == null || this.skipBuffer.length < nn) {
                    this.skipBuffer = new char[nn];
                }
                long r = n;
                while (true) {
                    if (r <= 0) {
                        break;
                    }
                    int nc = read(this.skipBuffer, 0, (int) Math.min(r, (long) nn));
                    if (nc == -1) {
                        break;
                    }
                    r -= (long) nc;
                }
                j = n - r;
            }
            return j;
        }
        throw new IllegalArgumentException("skip() value is negative");
    }

    public void mark(int readAheadLimit) throws IOException {
        synchronized (this.lock) {
            super.mark(readAheadLimit);
            this.markedLineNumber = this.lineNumber;
            this.markedSkipLF = this.skipLF;
        }
    }

    public void reset() throws IOException {
        synchronized (this.lock) {
            super.reset();
            this.lineNumber = this.markedLineNumber;
            this.skipLF = this.markedSkipLF;
        }
    }
}
