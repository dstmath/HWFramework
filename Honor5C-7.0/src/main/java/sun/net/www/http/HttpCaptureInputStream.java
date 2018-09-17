package sun.net.www.http;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class HttpCaptureInputStream extends FilterInputStream {
    private HttpCapture capture;

    public HttpCaptureInputStream(InputStream in, HttpCapture cap) {
        super(in);
        this.capture = null;
        this.capture = cap;
    }

    public int read() throws IOException {
        int i = super.read();
        this.capture.received(i);
        return i;
    }

    public void close() throws IOException {
        try {
            this.capture.flush();
        } catch (IOException e) {
        }
        super.close();
    }

    public int read(byte[] b) throws IOException {
        int ret = super.read(b);
        for (int i = 0; i < ret; i++) {
            this.capture.received(b[i]);
        }
        return ret;
    }

    public int read(byte[] b, int off, int len) throws IOException {
        int ret = super.read(b, off, len);
        for (int i = 0; i < ret; i++) {
            this.capture.received(b[off + i]);
        }
        return ret;
    }
}
