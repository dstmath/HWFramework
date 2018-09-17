package java.io;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class BufferedReader extends Reader {
    private static final int INVALIDATED = -2;
    private static final int UNMARKED = -1;
    private static int defaultCharBufferSize = 8192;
    private static int defaultExpectedLineLength = 80;
    private char[] cb;
    private Reader in;
    private int markedChar;
    private boolean markedSkipLF;
    private int nChars;
    private int nextChar;
    private int readAheadLimit;
    private boolean skipLF;

    public BufferedReader(Reader in, int sz) {
        super(in);
        this.markedChar = -1;
        this.readAheadLimit = 0;
        this.skipLF = false;
        this.markedSkipLF = false;
        if (sz <= 0) {
            throw new IllegalArgumentException("Buffer size <= 0");
        }
        this.in = in;
        this.cb = new char[sz];
        this.nChars = 0;
        this.nextChar = 0;
    }

    public BufferedReader(Reader in) {
        this(in, defaultCharBufferSize);
    }

    private void ensureOpen() throws IOException {
        if (this.in == null) {
            throw new IOException("Stream closed");
        }
    }

    private void fill() throws IOException {
        int dst;
        int n;
        if (this.markedChar <= -1) {
            dst = 0;
        } else {
            int delta = this.nextChar - this.markedChar;
            if (delta >= this.readAheadLimit) {
                this.markedChar = -2;
                this.readAheadLimit = 0;
                dst = 0;
            } else {
                if (this.readAheadLimit <= this.cb.length) {
                    System.arraycopy(this.cb, this.markedChar, this.cb, 0, delta);
                    this.markedChar = 0;
                    dst = delta;
                } else {
                    int nlength = this.cb.length * 2;
                    if (nlength > this.readAheadLimit) {
                        nlength = this.readAheadLimit;
                    }
                    char[] ncb = new char[nlength];
                    System.arraycopy(this.cb, this.markedChar, ncb, 0, delta);
                    this.cb = ncb;
                    this.markedChar = 0;
                    dst = delta;
                }
                this.nChars = delta;
                this.nextChar = delta;
            }
        }
        do {
            n = this.in.read(this.cb, dst, this.cb.length - dst);
        } while (n == 0);
        if (n > 0) {
            this.nChars = dst + n;
            this.nextChar = dst;
        }
    }

    /* JADX WARNING: Missing block: B:22:?, code:
            r0 = r4.cb;
            r2 = r4.nextChar;
            r4.nextChar = r2 + 1;
            r0 = r0[r2];
     */
    /* JADX WARNING: Missing block: B:24:0x003e, code:
            return r0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int read() throws IOException {
        synchronized (this.lock) {
            ensureOpen();
            while (true) {
                if (this.nextChar >= this.nChars) {
                    fill();
                    if (this.nextChar >= this.nChars) {
                        return -1;
                    }
                }
                if (this.skipLF) {
                    this.skipLF = false;
                    if (this.cb[this.nextChar] == 10) {
                        this.nextChar++;
                    }
                }
                break;
            }
        }
    }

    private int read1(char[] cbuf, int off, int len) throws IOException {
        if (this.nextChar >= this.nChars) {
            if (len >= this.cb.length && this.markedChar <= -1 && (this.skipLF ^ 1) != 0) {
                return this.in.read(cbuf, off, len);
            }
            fill();
        }
        if (this.nextChar >= this.nChars) {
            return -1;
        }
        if (this.skipLF) {
            this.skipLF = false;
            if (this.cb[this.nextChar] == 10) {
                this.nextChar++;
                if (this.nextChar >= this.nChars) {
                    fill();
                }
                if (this.nextChar >= this.nChars) {
                    return -1;
                }
            }
        }
        int n = Math.min(len, this.nChars - this.nextChar);
        System.arraycopy(this.cb, this.nextChar, cbuf, off, n);
        this.nextChar += n;
        return n;
    }

    /* JADX WARNING: Missing block: B:35:0x0042, code:
            return r0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int read(char[] cbuf, int off, int len) throws IOException {
        synchronized (this.lock) {
            ensureOpen();
            if (off >= 0 && off <= cbuf.length && len >= 0) {
                if (off + len <= cbuf.length && off + len >= 0) {
                    if (len != 0) {
                        int n = read1(cbuf, off, len);
                        if (n > 0) {
                            while (n < len) {
                                if (!this.in.ready()) {
                                    break;
                                }
                                int n1 = read1(cbuf, off + n, len - n);
                                if (n1 <= 0) {
                                    break;
                                }
                                n += n1;
                            }
                        } else {
                            return n;
                        }
                    }
                    return 0;
                }
            }
            throw new IndexOutOfBoundsException();
        }
    }

    /* JADX WARNING: Missing block: B:20:0x0032, code:
            return null;
     */
    /* JADX WARNING: Missing block: B:36:0x005e, code:
            if (r5 != null) goto L_0x0079;
     */
    /* JADX WARNING: Missing block: B:37:0x0060, code:
            r7 = new java.lang.String(r14.cb, r6, r2 - r6);
     */
    /* JADX WARNING: Missing block: B:38:0x0069, code:
            r14.nextChar++;
     */
    /* JADX WARNING: Missing block: B:39:0x006f, code:
            if (r0 != 13) goto L_0x0074;
     */
    /* JADX WARNING: Missing block: B:40:0x0071, code:
            r14.skipLF = true;
     */
    /* JADX WARNING: Missing block: B:42:0x0075, code:
            return r7;
     */
    /* JADX WARNING: Missing block: B:45:?, code:
            r5.append(r14.cb, r6, r2 - r6);
            r7 = r5.toString();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    String readLine(boolean ignoreLF) throws IOException {
        Throwable th;
        StringBuffer s = null;
        synchronized (this.lock) {
            try {
                ensureOpen();
                boolean omitLF = !ignoreLF ? this.skipLF : true;
                while (true) {
                    StringBuffer s2;
                    try {
                        s2 = s;
                        if (this.nextChar >= this.nChars) {
                            fill();
                        }
                        if (this.nextChar < this.nChars) {
                            boolean eol = false;
                            char c = 0;
                            if (omitLF) {
                                if (this.cb[this.nextChar] == 10) {
                                    this.nextChar++;
                                }
                            }
                            this.skipLF = false;
                            omitLF = false;
                            int i = this.nextChar;
                            while (i < this.nChars) {
                                c = this.cb[i];
                                if (c == 10 || c == 13) {
                                    eol = true;
                                    break;
                                }
                                i++;
                            }
                            int startChar = this.nextChar;
                            this.nextChar = i;
                            if (eol) {
                                break;
                            }
                            if (s2 == null) {
                                s = new StringBuffer(defaultExpectedLineLength);
                            } else {
                                s = s2;
                            }
                            s.append(this.cb, startChar, i - startChar);
                        } else if (s2 == null || s2.length() <= 0) {
                        } else {
                            String stringBuffer = s2.toString();
                            return stringBuffer;
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        s = s2;
                        throw th;
                    }
                }
            } catch (Throwable th3) {
                th = th3;
            }
        }
    }

    public String readLine() throws IOException {
        return readLine(false);
    }

    public long skip(long n) throws IOException {
        if (n < 0) {
            throw new IllegalArgumentException("skip value is negative");
        }
        long j;
        synchronized (this.lock) {
            ensureOpen();
            long r = n;
            while (r > 0) {
                if (this.nextChar >= this.nChars) {
                    fill();
                }
                if (this.nextChar >= this.nChars) {
                    break;
                }
                if (this.skipLF) {
                    this.skipLF = false;
                    if (this.cb[this.nextChar] == 10) {
                        this.nextChar++;
                    }
                }
                long d = (long) (this.nChars - this.nextChar);
                if (r <= d) {
                    this.nextChar = (int) (((long) this.nextChar) + r);
                    r = 0;
                    break;
                }
                r -= d;
                this.nextChar = this.nChars;
            }
            j = n - r;
        }
        return j;
    }

    public boolean ready() throws IOException {
        boolean ready;
        synchronized (this.lock) {
            ensureOpen();
            if (this.skipLF) {
                if (this.nextChar >= this.nChars && this.in.ready()) {
                    fill();
                }
                if (this.nextChar < this.nChars) {
                    if (this.cb[this.nextChar] == 10) {
                        this.nextChar++;
                    }
                    this.skipLF = false;
                }
            }
            ready = this.nextChar >= this.nChars ? this.in.ready() : true;
        }
        return ready;
    }

    public boolean markSupported() {
        return true;
    }

    public void mark(int readAheadLimit) throws IOException {
        if (readAheadLimit < 0) {
            throw new IllegalArgumentException("Read-ahead limit < 0");
        }
        synchronized (this.lock) {
            ensureOpen();
            this.readAheadLimit = readAheadLimit;
            this.markedChar = this.nextChar;
            this.markedSkipLF = this.skipLF;
        }
    }

    public void reset() throws IOException {
        synchronized (this.lock) {
            ensureOpen();
            if (this.markedChar < 0) {
                String str;
                if (this.markedChar == -2) {
                    str = "Mark invalid";
                } else {
                    str = "Stream not marked";
                }
                throw new IOException(str);
            }
            this.nextChar = this.markedChar;
            this.skipLF = this.markedSkipLF;
        }
    }

    public void close() throws IOException {
        synchronized (this.lock) {
            if (this.in == null) {
                return;
            }
            try {
                this.in.close();
                this.in = null;
                this.cb = null;
            } catch (Throwable th) {
                this.in = null;
                this.cb = null;
            }
        }
    }

    public Stream<String> lines() {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(new Iterator<String>() {
            String nextLine = null;

            public boolean hasNext() {
                boolean z = true;
                if (this.nextLine != null) {
                    return true;
                }
                try {
                    this.nextLine = BufferedReader.this.readLine();
                    if (this.nextLine == null) {
                        z = false;
                    }
                    return z;
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }

            public String next() {
                if (this.nextLine != null || hasNext()) {
                    String line = this.nextLine;
                    this.nextLine = null;
                    return line;
                }
                throw new NoSuchElementException();
            }
        }, 272), false);
    }
}
