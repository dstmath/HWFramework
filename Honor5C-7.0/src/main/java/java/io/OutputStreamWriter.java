package java.io;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import sun.nio.cs.StreamEncoder;

public class OutputStreamWriter extends Writer {
    private final StreamEncoder se;

    public OutputStreamWriter(OutputStream out, String charsetName) throws UnsupportedEncodingException {
        super(out);
        if (charsetName == null) {
            throw new NullPointerException("charsetName");
        }
        this.se = StreamEncoder.forOutputStreamWriter(out, (Object) this, charsetName);
    }

    public OutputStreamWriter(OutputStream out) {
        super(out);
        try {
            this.se = StreamEncoder.forOutputStreamWriter(out, (Object) this, (String) null);
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    public OutputStreamWriter(OutputStream out, Charset cs) {
        super(out);
        if (cs == null) {
            throw new NullPointerException("charset");
        }
        this.se = StreamEncoder.forOutputStreamWriter(out, (Object) this, cs);
    }

    public OutputStreamWriter(OutputStream out, CharsetEncoder enc) {
        super(out);
        if (enc == null) {
            throw new NullPointerException("charset encoder");
        }
        this.se = StreamEncoder.forOutputStreamWriter(out, (Object) this, enc);
    }

    public String getEncoding() {
        return this.se.getEncoding();
    }

    void flushBuffer() throws IOException {
        this.se.flushBuffer();
    }

    public void write(int c) throws IOException {
        this.se.write(c);
    }

    public void write(char[] cbuf, int off, int len) throws IOException {
        this.se.write(cbuf, off, len);
    }

    public void write(String str, int off, int len) throws IOException {
        this.se.write(str, off, len);
    }

    public void flush() throws IOException {
        this.se.flush();
    }

    public void close() throws IOException {
        this.se.close();
    }
}
