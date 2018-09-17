package java.io;

public class PipedOutputStream extends OutputStream {
    private PipedInputStream sink;

    public PipedOutputStream(PipedInputStream snk) throws IOException {
        connect(snk);
    }

    public synchronized void connect(PipedInputStream snk) throws IOException {
        if (snk == null) {
            throw new NullPointerException();
        } else if (this.sink != null || snk.connected) {
            throw new IOException("Already connected");
        } else {
            this.sink = snk;
            snk.in = -1;
            snk.out = 0;
            snk.connected = true;
        }
    }

    public void write(int b) throws IOException {
        if (this.sink == null) {
            throw new IOException("Pipe not connected");
        }
        this.sink.receive(b);
    }

    public void write(byte[] b, int off, int len) throws IOException {
        if (this.sink == null) {
            throw new IOException("Pipe not connected");
        } else if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || off > b.length || len < 0 || off + len > b.length || off + len < 0) {
            throw new IndexOutOfBoundsException();
        } else if (len != 0) {
            this.sink.receive(b, off, len);
        }
    }

    public synchronized void flush() throws IOException {
        if (this.sink != null) {
            synchronized (this.sink) {
                this.sink.notifyAll();
            }
        }
    }

    public void close() throws IOException {
        if (this.sink != null) {
            this.sink.receivedLast();
        }
    }
}
