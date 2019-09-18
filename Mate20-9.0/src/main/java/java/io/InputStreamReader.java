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
        } catch (UnsupportedEncodingException e) {
            throw new Error((Throwable) e);
        }
    }

    public InputStreamReader(InputStream in, String charsetName) throws UnsupportedEncodingException {
        super(in);
        if (charsetName != null) {
            this.sd = StreamDecoder.forInputStreamReader(in, (Object) this, charsetName);
            return;
        }
        throw new NullPointerException("charsetName");
    }

    public InputStreamReader(InputStream in, Charset cs) {
        super(in);
        if (cs != null) {
            this.sd = StreamDecoder.forInputStreamReader(in, (Object) this, cs);
            return;
        }
        throw new NullPointerException("charset");
    }

    public InputStreamReader(InputStream in, CharsetDecoder dec) {
        super(in);
        if (dec != null) {
            this.sd = StreamDecoder.forInputStreamReader(in, (Object) this, dec);
            return;
        }
        throw new NullPointerException("charset decoder");
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
