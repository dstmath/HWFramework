package java.io;

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Formatter;
import java.util.Locale;

public class PrintStream extends FilterOutputStream implements Appendable, Closeable {
    private final boolean autoFlush;
    private OutputStreamWriter charOut;
    private Charset charset;
    private boolean closing;
    private Formatter formatter;
    private BufferedWriter textOut;
    private boolean trouble;

    private static <T> T requireNonNull(T obj, String message) {
        if (obj != null) {
            return obj;
        }
        throw new NullPointerException(message);
    }

    private static Charset toCharset(String csn) throws UnsupportedEncodingException {
        requireNonNull(csn, "charsetName");
        try {
            return Charset.forName(csn);
        } catch (IllegalCharsetNameException | UnsupportedCharsetException e) {
            throw new UnsupportedEncodingException(csn);
        }
    }

    private PrintStream(boolean autoFlush2, OutputStream out) {
        super(out);
        this.trouble = false;
        this.closing = false;
        this.autoFlush = autoFlush2;
    }

    private PrintStream(boolean autoFlush2, OutputStream out, Charset charset2) {
        super(out);
        this.trouble = false;
        this.closing = false;
        this.autoFlush = autoFlush2;
        this.charset = charset2;
    }

    private PrintStream(boolean autoFlush2, Charset charset2, OutputStream out) throws UnsupportedEncodingException {
        this(autoFlush2, out, charset2);
    }

    public PrintStream(OutputStream out) {
        this(out, false);
    }

    public PrintStream(OutputStream out, boolean autoFlush2) {
        this(autoFlush2, (OutputStream) requireNonNull(out, "Null output stream"));
    }

    public PrintStream(OutputStream out, boolean autoFlush2, String encoding) throws UnsupportedEncodingException {
        this(autoFlush2, (OutputStream) requireNonNull(out, "Null output stream"), toCharset(encoding));
    }

    public PrintStream(String fileName) throws FileNotFoundException {
        this(false, (OutputStream) new FileOutputStream(fileName));
    }

    public PrintStream(String fileName, String csn) throws FileNotFoundException, UnsupportedEncodingException {
        this(false, toCharset(csn), (OutputStream) new FileOutputStream(fileName));
    }

    public PrintStream(File file) throws FileNotFoundException {
        this(false, (OutputStream) new FileOutputStream(file));
    }

    public PrintStream(File file, String csn) throws FileNotFoundException, UnsupportedEncodingException {
        this(false, toCharset(csn), (OutputStream) new FileOutputStream(file));
    }

    private void ensureOpen() throws IOException {
        if (this.out == null) {
            throw new IOException("Stream closed");
        }
    }

    public void flush() {
        synchronized (this) {
            try {
                ensureOpen();
                this.out.flush();
            } catch (IOException e) {
                this.trouble = true;
            }
        }
    }

    private BufferedWriter getTextOut() {
        OutputStreamWriter outputStreamWriter;
        if (this.textOut == null) {
            if (this.charset != null) {
                outputStreamWriter = new OutputStreamWriter((OutputStream) this, this.charset);
            } else {
                outputStreamWriter = new OutputStreamWriter(this);
            }
            this.charOut = outputStreamWriter;
            this.textOut = new BufferedWriter(this.charOut);
        }
        return this.textOut;
    }

    public void close() {
        synchronized (this) {
            if (!this.closing) {
                this.closing = true;
                try {
                    if (this.textOut != null) {
                        this.textOut.close();
                    }
                    this.out.close();
                } catch (IOException e) {
                    this.trouble = true;
                }
                this.textOut = null;
                this.charOut = null;
                this.out = null;
            }
        }
    }

    public boolean checkError() {
        if (this.out != null) {
            flush();
        }
        if (this.out instanceof PrintStream) {
            return ((PrintStream) this.out).checkError();
        }
        return this.trouble;
    }

    /* access modifiers changed from: protected */
    public void setError() {
        this.trouble = true;
    }

    /* access modifiers changed from: protected */
    public void clearError() {
        this.trouble = false;
    }

    public void write(int b) {
        try {
            synchronized (this) {
                ensureOpen();
                this.out.write(b);
                if (b == 10 && this.autoFlush) {
                    this.out.flush();
                }
            }
        } catch (InterruptedIOException e) {
            Thread.currentThread().interrupt();
        } catch (IOException e2) {
            this.trouble = true;
        }
    }

    public void write(byte[] buf, int off, int len) {
        try {
            synchronized (this) {
                ensureOpen();
                this.out.write(buf, off, len);
                if (this.autoFlush) {
                    this.out.flush();
                }
            }
        } catch (InterruptedIOException e) {
            Thread.currentThread().interrupt();
        } catch (IOException e2) {
            this.trouble = true;
        }
    }

    private void write(char[] buf) {
        try {
            synchronized (this) {
                ensureOpen();
                BufferedWriter textOut2 = getTextOut();
                textOut2.write(buf);
                textOut2.flushBuffer();
                this.charOut.flushBuffer();
                if (this.autoFlush) {
                    for (char c : buf) {
                        if (c == 10) {
                            this.out.flush();
                        }
                    }
                }
            }
        } catch (InterruptedIOException e) {
            Thread.currentThread().interrupt();
        } catch (IOException e2) {
            this.trouble = true;
        }
    }

    private void write(String s) {
        try {
            synchronized (this) {
                ensureOpen();
                BufferedWriter textOut2 = getTextOut();
                textOut2.write(s);
                textOut2.flushBuffer();
                this.charOut.flushBuffer();
                if (this.autoFlush && s.indexOf(10) >= 0) {
                    this.out.flush();
                }
            }
        } catch (InterruptedIOException e) {
            Thread.currentThread().interrupt();
        } catch (IOException e2) {
            this.trouble = true;
        }
    }

    private void newLine() {
        try {
            synchronized (this) {
                ensureOpen();
                BufferedWriter textOut2 = getTextOut();
                textOut2.newLine();
                textOut2.flushBuffer();
                this.charOut.flushBuffer();
                if (this.autoFlush) {
                    this.out.flush();
                }
            }
        } catch (InterruptedIOException e) {
            Thread.currentThread().interrupt();
        } catch (IOException e2) {
            this.trouble = true;
        }
    }

    public void print(boolean b) {
        write(b ? "true" : "false");
    }

    public void print(char c) {
        write(String.valueOf(c));
    }

    public void print(int i) {
        write(String.valueOf(i));
    }

    public void print(long l) {
        write(String.valueOf(l));
    }

    public void print(float f) {
        write(String.valueOf(f));
    }

    public void print(double d) {
        write(String.valueOf(d));
    }

    public void print(char[] s) {
        write(s);
    }

    public void print(String s) {
        if (s == null) {
            s = "null";
        }
        write(s);
    }

    public void print(Object obj) {
        write(String.valueOf(obj));
    }

    public void println() {
        newLine();
    }

    public void println(boolean x) {
        synchronized (this) {
            print(x);
            newLine();
        }
    }

    public void println(char x) {
        synchronized (this) {
            print(x);
            newLine();
        }
    }

    public void println(int x) {
        synchronized (this) {
            print(x);
            newLine();
        }
    }

    public void println(long x) {
        synchronized (this) {
            print(x);
            newLine();
        }
    }

    public void println(float x) {
        synchronized (this) {
            print(x);
            newLine();
        }
    }

    public void println(double x) {
        synchronized (this) {
            print(x);
            newLine();
        }
    }

    public void println(char[] x) {
        synchronized (this) {
            print(x);
            newLine();
        }
    }

    public void println(String x) {
        synchronized (this) {
            print(x);
            newLine();
        }
    }

    public void println(Object x) {
        String s = String.valueOf(x);
        synchronized (this) {
            print(s);
            newLine();
        }
    }

    public PrintStream printf(String format, Object... args) {
        return format(format, args);
    }

    public PrintStream printf(Locale l, String format, Object... args) {
        return format(l, format, args);
    }

    public PrintStream format(String format, Object... args) {
        try {
            synchronized (this) {
                ensureOpen();
                if (this.formatter == null || this.formatter.locale() != Locale.getDefault()) {
                    this.formatter = new Formatter((Appendable) this);
                }
                this.formatter.format(Locale.getDefault(), format, args);
            }
        } catch (InterruptedIOException e) {
            Thread.currentThread().interrupt();
        } catch (IOException e2) {
            this.trouble = true;
        }
        return this;
    }

    public PrintStream format(Locale l, String format, Object... args) {
        try {
            synchronized (this) {
                ensureOpen();
                if (this.formatter == null || this.formatter.locale() != l) {
                    this.formatter = new Formatter((Appendable) this, l);
                }
                this.formatter.format(l, format, args);
            }
        } catch (InterruptedIOException e) {
            Thread.currentThread().interrupt();
        } catch (IOException e2) {
            this.trouble = true;
        }
        return this;
    }

    public PrintStream append(CharSequence csq) {
        if (csq == null) {
            print("null");
        } else {
            print(csq.toString());
        }
        return this;
    }

    public PrintStream append(CharSequence csq, int start, int end) {
        write((csq == null ? "null" : csq).subSequence(start, end).toString());
        return this;
    }

    public PrintStream append(char c) {
        print(c);
        return this;
    }
}
