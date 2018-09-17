package java.io;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import sun.nio.cs.StreamDecoder;

public class InputStreamReader extends Reader {
    private final StreamDecoder sd;

    public InputStreamReader(InputStream in) {
        super(in);
        try {
            this.sd = StreamDecoder.forInputStreamReader(in, (Object) this, (String) null);
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    public InputStreamReader(InputStream in, String charsetName) throws UnsupportedEncodingException {
        super(in);
        if (charsetName == null) {
            throw new NullPointerException("charsetName");
        }
        this.sd = StreamDecoder.forInputStreamReader(in, (Object) this, charsetName);
    }

    public InputStreamReader(InputStream in, Charset cs) {
        super(in);
        if (cs == null) {
            throw new NullPointerException("charset");
        }
        this.sd = StreamDecoder.forInputStreamReader(in, (Object) this, cs);
    }

    public InputStreamReader(InputStream in, CharsetDecoder dec) {
        super(in);
        if (dec == null) {
            throw new NullPointerException("charset decoder");
        }
        this.sd = StreamDecoder.forInputStreamReader(in, (Object) this, dec);
    }

    public String getEncoding() {
        return this.sd.getEncoding();
    }

    public int read() throws IOException {
        return this.sd.read();
    }

    public int read(char[] cbuf, int offset, int length) throws IOException {
        return this.sd.read(cbuf, offset, length);
    }

    public boolean ready() throws IOException {
        return this.sd.ready();
    }

    public void close() throws IOException {
        this.sd.close();
    }
}
