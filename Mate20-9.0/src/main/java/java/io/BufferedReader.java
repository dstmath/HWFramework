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

    public BufferedReader(Reader in2, int sz) {
        super(in2);
        this.markedChar = -1;
        this.readAheadLimit = 0;
        this.skipLF = false;
        this.markedSkipLF = false;
        if (sz > 0) {
            this.in = in2;
            this.cb = new char[sz];
            this.nChars = 0;
            this.nextChar = 0;
            return;
        }
        throw new IllegalArgumentException("Buffer size <= 0");
    }

    public BufferedReader(Reader in2) {
        this(in2, defaultCharBufferSize);
    }

    private void ensureOpen() throws IOException {
        if (this.in == null) {
            throw new IOException("Stream closed");
        }
    }

    private void fill() throws IOException {
        int dst;
        int n;
        int dst2;
        if (this.markedChar <= -1) {
            dst = 0;
        } else {
            int delta = this.nextChar - this.markedChar;
            if (delta >= this.readAheadLimit) {
                this.markedChar = -2;
                this.readAheadLimit = 0;
                dst2 = 0;
            } else {
                if (this.readAheadLimit <= this.cb.length) {
                    System.arraycopy((Object) this.cb, this.markedChar, (Object) this.cb, 0, delta);
                    this.markedChar = 0;
                    dst2 = delta;
                } else {
                    int nlength = this.cb.length * 2;
                    if (nlength > this.readAheadLimit) {
                        nlength = this.readAheadLimit;
                    }
                    char[] ncb = new char[nlength];
                    System.arraycopy((Object) this.cb, this.markedChar, (Object) ncb, 0, delta);
                    this.cb = ncb;
                    this.markedChar = 0;
                    dst2 = delta;
                }
                this.nChars = delta;
                this.nextChar = delta;
            }
            dst = dst2;
        }
        do {
            n = this.in.read(this.cb, dst, this.cb.length - dst);
        } while (n == 0);
        if (n > 0) {
            this.nChars = dst + n;
            this.nextChar = dst;
        }
    }

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
                if (!this.skipLF) {
                    break;
                }
                this.skipLF = false;
                if (this.cb[this.nextChar] != 10) {
                    break;
                }
                this.nextChar++;
            }
            char[] cArr = this.cb;
            int i = this.nextChar;
            this.nextChar = i + 1;
            char c = cArr[i];
            return c;
        }
    }

    private int read1(char[] cbuf, int off, int len) throws IOException {
        if (this.nextChar >= this.nChars) {
            if (len >= this.cb.length && this.markedChar <= -1 && !this.skipLF) {
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
        System.arraycopy((Object) this.cb, this.nextChar, (Object) cbuf, off, n);
        this.nextChar += n;
        return n;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:28:0x003b, code lost:
        return r1;
     */
    public int read(char[] cbuf, int off, int len) throws IOException {
        synchronized (this.lock) {
            ensureOpen();
            if (off >= 0 && off <= cbuf.length && len >= 0 && off + len <= cbuf.length && off + len >= 0) {
                if (len != 0) {
                    int n = read1(cbuf, off, len);
                    if (n > 0) {
                        while (true) {
                            if (n >= len || !this.in.ready()) {
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
                } else {
                    return 0;
                }
            } else {
                throw new IndexOutOfBoundsException();
            }
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x005e, code lost:
        r5 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x0087, code lost:
        return r2;
     */
    /* JADX WARNING: Removed duplicated region for block: B:12:0x0019  */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x0033  */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x0022 A[SYNTHETIC] */
    public String readLine(boolean ignoreLF) throws IOException {
        boolean omitLF;
        int i;
        int startChar;
        String str;
        StringBuffer s = null;
        synchronized (this.lock) {
            ensureOpen();
            if (!ignoreLF) {
                if (!this.skipLF) {
                    omitLF = false;
                    while (true) {
                        if (this.nextChar >= this.nChars) {
                            fill();
                        }
                        if (this.nextChar >= this.nChars) {
                            boolean eol = false;
                            char c = 0;
                            if (omitLF && this.cb[this.nextChar] == 10) {
                                this.nextChar++;
                            }
                            this.skipLF = false;
                            omitLF = false;
                            i = this.nextChar;
                            while (true) {
                                if (i >= this.nChars) {
                                    break;
                                }
                                c = this.cb[i];
                                if (c == 10) {
                                    break;
                                } else if (c == 13) {
                                    break;
                                } else {
                                    i++;
                                }
                            }
                            startChar = this.nextChar;
                            this.nextChar = i;
                            if (eol) {
                                if (s == null) {
                                    str = new String(this.cb, startChar, i - startChar);
                                } else {
                                    s.append(this.cb, startChar, i - startChar);
                                    str = s.toString();
                                }
                                this.nextChar++;
                                if (c == 13) {
                                    this.skipLF = true;
                                }
                            } else {
                                if (s == null) {
                                    s = new StringBuffer(defaultExpectedLineLength);
                                }
                                s.append(this.cb, startChar, i - startChar);
                            }
                        } else if (s == null || s.length() <= 0) {
                            return null;
                        } else {
                            String stringBuffer = s.toString();
                            return stringBuffer;
                        }
                    }
                }
            }
            omitLF = true;
            while (true) {
                if (this.nextChar >= this.nChars) {
                }
                if (this.nextChar >= this.nChars) {
                }
                s.append(this.cb, startChar, i - startChar);
            }
        }
    }

    public String readLine() throws IOException {
        return readLine(false);
    }

    public long skip(long n) throws IOException {
        long j;
        if (n >= 0) {
            synchronized (this.lock) {
                ensureOpen();
                long r = n;
                while (true) {
                    if (r <= 0) {
                        break;
                    }
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
        throw new IllegalArgumentException("skip value is negative");
    }

    public boolean ready() throws IOException {
        boolean z;
        synchronized (this.lock) {
            ensureOpen();
            z = false;
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
            if (this.nextChar >= this.nChars) {
                if (this.in.ready()) {
                }
            }
            z = true;
        }
        return z;
    }

    public boolean markSupported() {
        return true;
    }

    public void mark(int readAheadLimit2) throws IOException {
        if (readAheadLimit2 >= 0) {
            synchronized (this.lock) {
                ensureOpen();
                this.readAheadLimit = readAheadLimit2;
                this.markedChar = this.nextChar;
                this.markedSkipLF = this.skipLF;
            }
            return;
        }
        throw new IllegalArgumentException("Read-ahead limit < 0");
    }

    public void reset() throws IOException {
        String str;
        synchronized (this.lock) {
            ensureOpen();
            if (this.markedChar < 0) {
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
            if (this.in != null) {
                try {
                    this.in.close();
                } finally {
                    this.in = null;
                    this.cb = null;
                }
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
