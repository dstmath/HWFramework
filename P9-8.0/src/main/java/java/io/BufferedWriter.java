package java.io;

import java.security.AccessController;
import sun.security.action.GetPropertyAction;

public class BufferedWriter extends Writer {
    private static int defaultCharBufferSize = 8192;
    private char[] cb;
    private String lineSeparator;
    private int nChars;
    private int nextChar;
    private Writer out;

    public BufferedWriter(Writer out) {
        this(out, defaultCharBufferSize);
    }

    public BufferedWriter(Writer out, int sz) {
        super(out);
        if (sz <= 0) {
            throw new IllegalArgumentException("Buffer size <= 0");
        }
        this.out = out;
        this.cb = new char[sz];
        this.nChars = sz;
        this.nextChar = 0;
        this.lineSeparator = (String) AccessController.doPrivileged(new GetPropertyAction("line.separator"));
    }

    private void ensureOpen() throws IOException {
        if (this.out == null) {
            throw new IOException("Stream closed");
        }
    }

    void flushBuffer() throws IOException {
        synchronized (this.lock) {
            ensureOpen();
            if (this.nextChar == 0) {
                return;
            }
            this.out.write(this.cb, 0, this.nextChar);
            this.nextChar = 0;
        }
    }

    public void write(int c) throws IOException {
        synchronized (this.lock) {
            ensureOpen();
            if (this.nextChar >= this.nChars) {
                flushBuffer();
            }
            char[] cArr = this.cb;
            int i = this.nextChar;
            this.nextChar = i + 1;
            cArr[i] = (char) c;
        }
    }

    private int min(int a, int b) {
        if (a < b) {
            return a;
        }
        return b;
    }

    public void write(char[] cbuf, int off, int len) throws IOException {
        synchronized (this.lock) {
            ensureOpen();
            if (off >= 0 && off <= cbuf.length && len >= 0) {
                if (off + len <= cbuf.length && off + len >= 0) {
                    if (len == 0) {
                        return;
                    } else if (len >= this.nChars) {
                        flushBuffer();
                        this.out.write(cbuf, off, len);
                        return;
                    } else {
                        int b = off;
                        int t = off + len;
                        while (b < t) {
                            int d = min(this.nChars - this.nextChar, t - b);
                            System.arraycopy(cbuf, b, this.cb, this.nextChar, d);
                            b += d;
                            this.nextChar += d;
                            if (this.nextChar >= this.nChars) {
                                flushBuffer();
                            }
                        }
                        return;
                    }
                }
            }
            throw new IndexOutOfBoundsException();
        }
    }

    public void write(String s, int off, int len) throws IOException {
        synchronized (this.lock) {
            ensureOpen();
            int b = off;
            int t = off + len;
            while (b < t) {
                int d = min(this.nChars - this.nextChar, t - b);
                s.getChars(b, b + d, this.cb, this.nextChar);
                b += d;
                this.nextChar += d;
                if (this.nextChar >= this.nChars) {
                    flushBuffer();
                }
            }
        }
    }

    public void newLine() throws IOException {
        write(this.lineSeparator);
    }

    public void flush() throws IOException {
        synchronized (this.lock) {
            flushBuffer();
            this.out.flush();
        }
    }

    public void close() throws IOException {
        Writer writer;
        Throwable th;
        Throwable th2 = null;
        synchronized (this.lock) {
            if (this.out == null) {
                return;
            }
            writer = null;
            try {
                writer = this.out;
                flushBuffer();
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (Throwable th3) {
                        th2 = th3;
                    }
                }
                if (th2 != null) {
                    try {
                        throw th2;
                    } catch (Throwable th4) {
                        this.out = null;
                        this.cb = null;
                    }
                } else {
                    this.out = null;
                    this.cb = null;
                    return;
                }
            } catch (Throwable th22) {
                Throwable th5 = th22;
                th22 = th;
                th = th5;
            }
        }
        if (writer != null) {
            try {
                writer.close();
            } catch (Throwable th6) {
                if (th22 == null) {
                    th22 = th6;
                } else if (th22 != th6) {
                    th22.addSuppressed(th6);
                }
            }
        }
        if (th22 != null) {
            throw th22;
        }
        throw th;
    }
}
