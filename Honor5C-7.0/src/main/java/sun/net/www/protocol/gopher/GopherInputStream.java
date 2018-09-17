package sun.net.www.protocol.gopher;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import sun.net.NetworkClient;

/* compiled from: GopherClient */
class GopherInputStream extends FilterInputStream {
    NetworkClient parent;

    GopherInputStream(NetworkClient o, InputStream fd) {
        super(fd);
        this.parent = o;
    }

    public void close() {
        try {
            this.parent.closeServer();
            super.close();
        } catch (IOException e) {
        }
    }
}
