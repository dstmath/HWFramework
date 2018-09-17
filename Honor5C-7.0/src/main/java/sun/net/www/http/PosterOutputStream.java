package sun.net.www.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class PosterOutputStream extends ByteArrayOutputStream {
    private boolean closed;

    public PosterOutputStream() {
        super(Record.maxPadding);
    }

    public synchronized void write(int b) {
        if (!this.closed) {
            super.write(b);
        }
    }

    public synchronized void write(byte[] b, int off, int len) {
        if (!this.closed) {
            super.write(b, off, len);
        }
    }

    public synchronized void reset() {
        if (!this.closed) {
            super.reset();
        }
    }

    public synchronized void close() throws IOException {
        this.closed = true;
        super.close();
    }
}
