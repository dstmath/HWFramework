package java.io;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Formatter;
import sun.nio.cs.StreamDecoder;
import sun.nio.cs.StreamEncoder;

public final class Console implements Flushable {
    static final /* synthetic */ boolean -assertionsDisabled = (Console.class.desiredAssertionStatus() ^ 1);
    private static Console cons;
    private static boolean echoOff;
    private Charset cs;
    private Formatter formatter;
    private Writer out;
    private PrintWriter pw;
    private char[] rcb;
    private Object readLock;
    private Reader reader;
    private Object writeLock;

    class LineReader extends Reader {
        private char[] cb = new char[1024];
        private Reader in;
        boolean leftoverLF = false;
        private int nChars = 0;
        private int nextChar = 0;

        LineReader(Reader in) {
            this.in = in;
        }

        public void close() {
        }

        public boolean ready() throws IOException {
            return this.in.ready();
        }

        /* JADX WARNING: Missing block: B:59:0x00a7, code:
            if (r5 != r2) goto L_0x00b8;
     */
        /* JADX WARNING: Missing block: B:62:0x00af, code:
            if (r13 != java.io.Console.-get0(r12.this$0)) goto L_0x00f8;
     */
        /* JADX WARNING: Missing block: B:63:0x00b1, code:
            r13 = java.io.Console.-wrap0(r12.this$0);
            r2 = r13.length;
     */
        /* JADX WARNING: Missing block: B:65:0x00bc, code:
            if (r12.nextChar != r12.nChars) goto L_0x00d7;
     */
        /* JADX WARNING: Missing block: B:67:0x00c4, code:
            if (r12.in.ready() == false) goto L_0x00d7;
     */
        /* JADX WARNING: Missing block: B:68:0x00c6, code:
            r12.nChars = r12.in.read(r12.cb, 0, r12.cb.length);
            r12.nextChar = 0;
     */
        /* JADX WARNING: Missing block: B:70:0x00db, code:
            if (r12.nextChar >= r12.nChars) goto L_0x00f4;
     */
        /* JADX WARNING: Missing block: B:73:0x00e5, code:
            if (r12.cb[r12.nextChar] != 10) goto L_0x00f4;
     */
        /* JADX WARNING: Missing block: B:74:0x00e7, code:
            r6 = r5 + 1;
     */
        /* JADX WARNING: Missing block: B:76:?, code:
            r13[r5] = 10;
            r12.nextChar++;
     */
        /* JADX WARNING: Missing block: B:77:0x00f3, code:
            r5 = r6;
     */
        /* JADX WARNING: Missing block: B:78:0x00f4, code:
            r7 = r5 - r14;
     */
        /* JADX WARNING: Missing block: B:80:0x00f7, code:
            return r7;
     */
        /* JADX WARNING: Missing block: B:83:?, code:
            r12.leftoverLF = true;
     */
        /* JADX WARNING: Missing block: B:84:0x00fb, code:
            r7 = r5 - r14;
     */
        /* JADX WARNING: Missing block: B:86:0x00fe, code:
            return r7;
     */
        /* JADX WARNING: Missing block: B:96:0x0118, code:
            if (r3 == false) goto L_0x011e;
     */
        /* JADX WARNING: Missing block: B:97:0x011a, code:
            r7 = r6 - r14;
     */
        /* JADX WARNING: Missing block: B:99:0x011d, code:
            return r7;
     */
        /* JADX WARNING: Missing block: B:108:0x012c, code:
            r7 = th;
     */
        /* JADX WARNING: Missing block: B:109:0x012d, code:
            r5 = r6;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public int read(char[] cbuf, int offset, int length) throws IOException {
            char c;
            Throwable th;
            int off = offset;
            int end = offset + length;
            if (offset < 0 || offset > cbuf.length || length < 0 || end < 0 || end > cbuf.length) {
                throw new IndexOutOfBoundsException();
            }
            synchronized (Console.this.readLock) {
                boolean eof = false;
                char c2 = 0;
                loop0:
                while (true) {
                    try {
                        int i;
                        int off2;
                        if (this.nextChar >= this.nChars) {
                            int n;
                            do {
                                n = this.in.read(this.cb, 0, this.cb.length);
                            } while (n == 0);
                            if (n > 0) {
                                this.nChars = n;
                                this.nextChar = 0;
                                if (!(n >= this.cb.length || this.cb[n - 1] == 10 || this.cb[n - 1] == 13)) {
                                    eof = true;
                                }
                            } else if (off - offset == 0) {
                                return -1;
                            } else {
                                i = off - offset;
                                return i;
                            }
                        }
                        if (this.leftoverLF && cbuf == Console.this.rcb && this.cb[this.nextChar] == 10) {
                            this.nextChar++;
                        }
                        this.leftoverLF = false;
                        while (true) {
                            try {
                                c = c2;
                                off2 = off;
                                if (this.nextChar >= this.nChars) {
                                    break;
                                }
                                c2 = this.cb[this.nextChar];
                                off = off2 + 1;
                                try {
                                    cbuf[off2] = c2;
                                    char[] cArr = this.cb;
                                    int i2 = this.nextChar;
                                    this.nextChar = i2 + 1;
                                    cArr[i2] = 0;
                                    if (c2 == 10) {
                                        i = off - offset;
                                        return i;
                                    } else if (c2 == 13) {
                                        break loop0;
                                    } else if (off == end) {
                                        if (cbuf == Console.this.rcb) {
                                            cbuf = Console.this.grow();
                                            end = cbuf.length;
                                        } else {
                                            i = off - offset;
                                            return i;
                                        }
                                    }
                                } catch (Throwable th2) {
                                    th = th2;
                                    c2 = c;
                                }
                            } catch (Throwable th3) {
                                th = th3;
                                c2 = c;
                                off = off2;
                                throw th;
                            }
                        }
                        c2 = c;
                        off = off2;
                    } catch (Throwable th4) {
                        th = th4;
                    }
                }
            }
        }
    }

    private static native boolean echo(boolean z) throws IOException;

    private static native String encoding();

    private static native boolean istty();

    public PrintWriter writer() {
        return this.pw;
    }

    public Reader reader() {
        return this.reader;
    }

    public Console format(String fmt, Object... args) {
        this.formatter.format(fmt, args).flush();
        return this;
    }

    public Console printf(String format, Object... args) {
        return format(format, args);
    }

    public String readLine(String fmt, Object... args) {
        String line = null;
        synchronized (this.writeLock) {
            synchronized (this.readLock) {
                if (fmt.length() != 0) {
                    this.pw.format(fmt, args);
                }
                try {
                    char[] ca = readline(false);
                    if (ca != null) {
                        line = new String(ca);
                    }
                } catch (IOException x) {
                    throw new IOError(x);
                }
            }
        }
        return line;
    }

    public String readLine() {
        return readLine("", new Object[0]);
    }

    public char[] readPassword(String fmt, Object... args) {
        char[] passwd = null;
        synchronized (this.writeLock) {
            synchronized (this.readLock) {
                IOError ioe;
                try {
                    echoOff = echo(false);
                    ioe = null;
                    if (fmt.length() != 0) {
                        this.pw.format(fmt, args);
                    }
                    passwd = readline(true);
                    try {
                        echoOff = echo(true);
                    } catch (IOException x) {
                        ioe = new IOError(x);
                    }
                    if (ioe != null) {
                        throw ioe;
                    }
                } catch (IOException x2) {
                    IOError ioe2 = new IOError(x2);
                    try {
                        echoOff = echo(true);
                        ioe = ioe2;
                    } catch (IOException x22) {
                        if (ioe2 == null) {
                            ioe = new IOError(x22);
                        } else {
                            ioe2.addSuppressed(x22);
                            ioe = ioe2;
                        }
                    }
                    if (ioe != null) {
                        throw ioe;
                    }
                } catch (IOException x222) {
                    throw new IOError(x222);
                } catch (Throwable th) {
                    try {
                        echoOff = echo(true);
                    } catch (IOException x2222) {
                        ioe = new IOError(x2222);
                    }
                    if (ioe != null) {
                    }
                }
                this.pw.println();
            }
        }
        return passwd;
    }

    public char[] readPassword() {
        return readPassword("", new Object[0]);
    }

    public void flush() {
        this.pw.flush();
    }

    private char[] readline(boolean zeroOut) throws IOException {
        int len = this.reader.read(this.rcb, 0, this.rcb.length);
        if (len < 0) {
            return null;
        }
        if (this.rcb[len - 1] == 13) {
            len--;
        } else if (this.rcb[len - 1] == 10) {
            len--;
            if (len > 0 && this.rcb[len - 1] == 13) {
                len--;
            }
        }
        char[] b = new char[len];
        if (len > 0) {
            System.arraycopy(this.rcb, 0, b, 0, len);
            if (zeroOut) {
                Arrays.fill(this.rcb, 0, len, ' ');
            }
        }
        return b;
    }

    private char[] grow() {
        if (-assertionsDisabled || Thread.holdsLock(this.readLock)) {
            char[] t = new char[(this.rcb.length * 2)];
            System.arraycopy(this.rcb, 0, t, 0, this.rcb.length);
            this.rcb = t;
            return this.rcb;
        }
        throw new AssertionError();
    }

    public static Console console() {
        if (!istty()) {
            return null;
        }
        if (cons == null) {
            cons = new Console();
        }
        return cons;
    }

    private Console() {
        this(new FileInputStream(FileDescriptor.in), new FileOutputStream(FileDescriptor.out));
    }

    private Console(InputStream inStream, OutputStream outStream) {
        this.readLock = new Object();
        this.writeLock = new Object();
        String csname = encoding();
        if (csname != null) {
            try {
                this.cs = Charset.forName(csname);
            } catch (Exception e) {
            }
        }
        if (this.cs == null) {
            this.cs = Charset.defaultCharset();
        }
        this.out = StreamEncoder.forOutputStreamWriter(outStream, this.writeLock, this.cs);
        this.pw = new PrintWriter(this.out, true) {
            public void close() {
            }
        };
        this.formatter = new Formatter(this.out);
        this.reader = new LineReader(StreamDecoder.forInputStreamReader(inStream, this.readLock, this.cs));
        this.rcb = new char[1024];
    }
}
