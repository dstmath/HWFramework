package java.io;

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
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

    private static Charset toCharset(String csn) throws UnsupportedEncodingException {
        Objects.requireNonNull(csn, "charsetName");
        try {
            return Charset.forName(csn);
        } catch (IllegalCharsetNameException | UnsupportedCharsetException e) {
            throw new UnsupportedEncodingException(csn);
        }
    }

    public PrintWriter(Writer out2) {
        this(out2, false);
    }

    public PrintWriter(Writer out2, boolean autoFlush2) {
        super(out2);
        this.trouble = false;
        this.psOut = null;
        this.out = out2;
        this.autoFlush = autoFlush2;
        this.lineSeparator = (String) AccessController.doPrivileged(new GetPropertyAction("line.separator"));
    }

    public PrintWriter(OutputStream out2) {
        this(out2, false);
    }

    public PrintWriter(OutputStream out2, boolean autoFlush2) {
        this((Writer) new BufferedWriter(new OutputStreamWriter(out2)), autoFlush2);
        if (out2 instanceof PrintStream) {
            this.psOut = (PrintStream) out2;
        }
    }

    public PrintWriter(String fileName) throws FileNotFoundException {
        this((Writer) new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName))), false);
    }

    private PrintWriter(Charset charset, File file) throws FileNotFoundException {
        this((Writer) new BufferedWriter(new OutputStreamWriter((OutputStream) new FileOutputStream(file), charset)), false);
    }

    public PrintWriter(String fileName, String csn) throws FileNotFoundException, UnsupportedEncodingException {
        this(toCharset(csn), new File(fileName));
    }

    public PrintWriter(File file) throws FileNotFoundException {
        this((Writer) new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file))), false);
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
                if (this.out != null) {
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
            return ((PrintWriter) this.out).checkError();
        }
        if (this.psOut != null) {
            return this.psOut.checkError();
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
