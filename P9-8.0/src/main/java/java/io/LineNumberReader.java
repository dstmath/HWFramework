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

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
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
            switch (c) {
                case 10:
                    break;
                case 13:
                    this.skipLF = true;
                    break;
                default:
                    return c;
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
                int c = cbuf[i];
                if (this.skipLF) {
                    this.skipLF = false;
                    if (c == 10) {
                        continue;
                    }
                }
                switch (c) {
                    case 10:
                        break;
                    case 13:
                        this.skipLF = true;
                        break;
                    default:
                        continue;
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
        if (n < 0) {
            throw new IllegalArgumentException("skip() value is negative");
        }
        long j;
        int nn = (int) Math.min(n, 8192);
        synchronized (this.lock) {
            if (this.skipBuffer == null || this.skipBuffer.length < nn) {
                this.skipBuffer = new char[nn];
            }
            long r = n;
            while (r > 0) {
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
