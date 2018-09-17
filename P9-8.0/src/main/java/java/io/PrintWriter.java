package java.io;

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.security.AccessController;
import java.util.Formatter;
import java.util.Locale;
import java.util.Objects;
import sun.security.action.GetPropertyAction;

public class PrintWriter extends Writer {
    private final boolean autoFlush;
    private Formatter formatter;
    private final String lineSeparator;
    protected Writer out;
    private PrintStream psOut;
    private boolean trouble;

    /* JADX WARNING: Removed duplicated region for block: B:4:0x000b A:{Splitter: B:1:0x0006, ExcHandler: java.nio.charset.IllegalCharsetNameException (e java.nio.charset.IllegalCharsetNameException)} */
    /* JADX WARNING: Missing block: B:6:0x0011, code:
            throw new java.io.UnsupportedEncodingException(r2);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static Charset toCharset(String csn) throws UnsupportedEncodingException {
        Objects.requireNonNull((Object) csn, "charsetName");
        try {
            return Charset.forName(csn);
        } catch (IllegalCharsetNameException e) {
        }
    }

    public PrintWriter(Writer out) {
        this(out, false);
    }

    public PrintWriter(Writer out, boolean autoFlush) {
        super(out);
        this.trouble = false;
        this.psOut = null;
        this.out = out;
        this.autoFlush = autoFlush;
        this.lineSeparator = (String) AccessController.doPrivileged(new GetPropertyAction("line.separator"));
    }

    public PrintWriter(OutputStream out) {
        this(out, false);
    }

    public PrintWriter(OutputStream out, boolean autoFlush) {
        this(new BufferedWriter(new OutputStreamWriter(out)), autoFlush);
        if (out instanceof PrintStream) {
            this.psOut = (PrintStream) out;
        }
    }

    public PrintWriter(String fileName) throws FileNotFoundException {
        this(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName))), false);
    }

    private PrintWriter(Charset charset, File file) throws FileNotFoundException {
        this(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), charset)), false);
    }

    public PrintWriter(String fileName, String csn) throws FileNotFoundException, UnsupportedEncodingException {
        this(toCharset(csn), new File(fileName));
    }

    public PrintWriter(File file) throws FileNotFoundException {
        this(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file))), false);
    }

    public PrintWriter(File file, String csn) throws FileNotFoundException, UnsupportedEncodingException {
        this(toCharset(csn), file);
    }

    private void ensureOpen() throws IOException {
        if (this.out == null) {
            throw new IOException("Stream closed");
        }
    }

    public void flush() {
        try {
            synchronized (this.lock) {
                ensureOpen();
                this.out.flush();
            }
        } catch (IOException e) {
            this.trouble = true;
        }
    }

    public void close() {
        try {
            synchronized (this.lock) {
                if (this.out == null) {
                } else {
                    this.out.close();
                    this.out = null;
                }
            }
        } catch (IOException e) {
            this.trouble = true;
        }
    }

    public boolean checkError() {
        if (this.out != null) {
            flush();
        }
        if (this.out instanceof PrintWriter) {
            return this.out.checkError();
        }
        if (this.psOut != null) {
            return this.psOut.checkError();
        }
        return this.trouble;
    }

    protected void setError() {
        this.trouble = true;
    }

    protected void clearError() {
        this.trouble = false;
    }

    public void write(int c) {
        try {
            synchronized (this.lock) {
                ensureOpen();
                this.out.write(c);
            }
        } catch (InterruptedIOException e) {
            Thread.currentThread().interrupt();
        } catch (IOException e2) {
            this.trouble = true;
        }
    }

    public void write(char[] buf, int off, int len) {
        try {
            synchronized (this.lock) {
                ensureOpen();
                this.out.write(buf, off, len);
            }
        } catch (InterruptedIOException e) {
            Thread.currentThread().interrupt();
        } catch (IOException e2) {
            this.trouble = true;
        }
    }

    public void write(char[] buf) {
        write(buf, 0, buf.length);
    }

    public void write(String s, int off, int len) {
        try {
            synchronized (this.lock) {
                ensureOpen();
                this.out.write(s, off, len);
            }
        } catch (InterruptedIOException e) {
            Thread.currentThread().interrupt();
        } catch (IOException e2) {
            this.trouble = true;
        }
    }

    public void write(String s) {
        write(s, 0, s.length());
    }

    private void newLine() {
        try {
            synchronized (this.lock) {
                ensureOpen();
                this.out.write(this.lineSeparator);
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
        write((int) c);
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
        synchronized (this.lock) {
            print(x);
            println();
        }
    }

    public void println(char x) {
        synchronized (this.lock) {
            print(x);
            println();
        }
    }

    public void println(int x) {
        synchronized (this.lock) {
            print(x);
            println();
        }
    }

    public void println(long x) {
        synchronized (this.lock) {
            print(x);
            println();
        }
    }

    public void println(float x) {
        synchronized (this.lock) {
            print(x);
            println();
        }
    }

    public void println(double x) {
        synchronized (this.lock) {
            print(x);
            println();
        }
    }

    public void println(char[] x) {
        synchronized (this.lock) {
            print(x);
            println();
        }
    }

    public void println(String x) {
        synchronized (this.lock) {
            print(x);
            println();
        }
    }

    public void println(Object x) {
        String s = String.valueOf(x);
        synchronized (this.lock) {
            print(s);
            println();
        }
    }

    public PrintWriter printf(String format, Object... args) {
        return format(format, args);
    }

    public PrintWriter printf(Locale l, String format, Object... args) {
        return format(l, format, args);
    }

    public PrintWriter format(String format, Object... args) {
        try {
            synchronized (this.lock) {
                ensureOpen();
                if (this.formatter == null || this.formatter.locale() != Locale.getDefault()) {
                    this.formatter = new Formatter((Appendable) this);
                }
                this.formatter.format(Locale.getDefault(), format, args);
                if (this.autoFlush) {
                    this.out.flush();
                }
            }
        } catch (InterruptedIOException e) {
            Thread.currentThread().interrupt();
        } catch (IOException e2) {
            this.trouble = true;
        }
        return this;
    }

    public PrintWriter format(Locale l, String format, Object... args) {
        try {
            synchronized (this.lock) {
                ensureOpen();
                if (this.formatter == null || this.formatter.locale() != l) {
                    this.formatter = new Formatter((Appendable) this, l);
                }
                this.formatter.format(l, format, args);
                if (this.autoFlush) {
                    this.out.flush();
                }
            }
        } catch (InterruptedIOException e) {
            Thread.currentThread().interrupt();
        } catch (IOException e2) {
            this.trouble = true;
        }
        return this;
    }

    public PrintWriter append(CharSequence csq) {
        if (csq == null) {
            write("null");
        } else {
            write(csq.toString());
        }
        return this;
    }

    public PrintWriter append(CharSequence csq, int start, int end) {
        write((csq == null ? "null" : csq).subSequence(start, end).toString());
        return this;
    }

    public PrintWriter append(char c) {
        write((int) c);
        return this;
    }
}
