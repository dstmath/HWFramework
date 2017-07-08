package sun.net.www.protocol.http;

import java.io.InputStream;

/* compiled from: HttpURLConnection */
class EmptyInputStream extends InputStream {
    EmptyInputStream() {
    }

    public int available() {
        return 0;
    }

    public int read() {
        return -1;
    }
}
