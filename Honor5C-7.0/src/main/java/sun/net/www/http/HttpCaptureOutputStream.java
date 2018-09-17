package sun.net.www.http;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class HttpCaptureOutputStream extends FilterOutputStream {
    private HttpCapture capture;

    public HttpCaptureOutputStream(OutputStream out, HttpCapture cap) {
        super(out);
        this.capture = null;
        this.capture = cap;
    }

    public void write(int b) throws IOException {
        this.capture.sent(b);
        this.out.write(b);
    }

    public void write(byte[] ba) throws IOException {
        for (byte b : ba) {
            this.capture.sent(b);
        }
        this.out.write(ba);
    }

    public void write(byte[] b, int off, int len) throws IOException {
        for (int i = off; i < len; i++) {
            this.capture.sent(b[i]);
        }
        this.out.write(b, off, len);
    }

    public void flush() throws IOException {
        try {
            this.capture.flush();
        } catch (IOException e) {
        }
        super.flush();
    }
}
