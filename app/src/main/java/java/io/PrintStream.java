package java.io;

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.util.Formatter;
import java.util.Locale;
import java.util.jar.Pack200.Unpacker;

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
        } catch (IllegalCharsetNameException e) {
            throw new UnsupportedEncodingException(csn);
        }
    }

    private PrintStream(boolean autoFlush, OutputStream out) {
        super(out);
        this.trouble = false;
        this.closing = false;
        this.autoFlush = autoFlush;
    }

    private PrintStream(boolean autoFlush, OutputStream out, Charset charset) {
        super(out);
        this.trouble = false;
        this.closing = false;
        this.autoFlush = autoFlush;
    }

    private PrintStream(boolean autoFlush, Charset charset, OutputStream out) throws UnsupportedEncodingException {
        this(autoFlush, out, charset);
    }

    public PrintStream(OutputStream out) {
        this(out, false);
    }

    public PrintStream(OutputStream out, boolean autoFlush) {
        this(autoFlush, (OutputStream) requireNonNull(out, "Null output stream"));
    }

    public PrintStream(OutputStream out, boolean autoFlush, String encoding) throws UnsupportedEncodingException {
        this(autoFlush, (OutputStream) requireNonNull(out, "Null output stream"), toCharset(encoding));
    }

    public PrintStream(String fileName) throws FileNotFoundException {
        this(false, new FileOutputStream(fileName));
    }

    public PrintStream(String fileName, String csn) throws FileNotFoundException, UnsupportedEncodingException {
        this(false, toCharset(csn), new FileOutputStream(fileName));
    }

    public PrintStream(File file) throws FileNotFoundException {
        this(false, new FileOutputStream(file));
    }

    public PrintStream(File file, String csn) throws FileNotFoundException, UnsupportedEncodingException {
        this(false, toCharset(csn), new FileOutputStream(file));
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
        if (this.textOut == null) {
            OutputStreamWriter outputStreamWriter;
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
            return this.out.checkError();
        }
        return this.trouble;
    }

    protected void setError() {
        this.trouble = true;
    }

    protected void clearError() {
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
                BufferedWriter textOut = getTextOut();
                textOut.write(buf);
                textOut.flushBuffer();
                this.charOut.flushBuffer();
                if (this.autoFlush) {
                    for (char c : buf) {
                        if (c == '\n') {
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
                BufferedWriter textOut = getTextOut();
                textOut.write(s);
                textOut.flushBuffer();
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
                BufferedWriter textOut = getTextOut();
                textOut.newLine();
                textOut.flushBuffer();
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
        write(b ? Unpacker.TRUE : Unpacker.FALSE);
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
        CharSequence cs;
        if (csq == null) {
            cs = "null";
        } else {
            cs = csq;
        }
        write(cs.subSequence(start, end).toString());
        return this;
    }

    public PrintStream append(char c) {
        print(c);
        return this;
    }
}
