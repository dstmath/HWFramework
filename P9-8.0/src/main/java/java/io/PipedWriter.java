package java.io;

public class PipedWriter extends Writer {
    private boolean closed = false;
    private PipedReader sink;

    public PipedWriter(PipedReader snk) throws IOException {
        connect(snk);
    }

    public synchronized void connect(PipedReader snk) throws IOException {
        if (snk == null) {
            throw new NullPointerException();
        } else if (this.sink != null || snk.connected) {
            throw new IOException("Already connected");
        } else if (snk.closedByReader || this.closed) {
            throw new IOException("Pipe closed");
        } else {
            this.sink = snk;
            snk.in = -1;
            snk.out = 0;
            snk.connected = true;
        }
    }

    public void write(int c) throws IOException {
        if (this.sink == null) {
            throw new IOException("Pipe not connected");
        }
        this.sink.receive(c);
    }

    public void write(char[] cbuf, int off, int len) throws IOException {
        if (this.sink == null) {
            throw new IOException("Pipe not connected");
        } else if ((((off | len) | (off + len)) | (cbuf.length - (off + len))) < 0) {
            throw new IndexOutOfBoundsException();
        } else {
            this.sink.receive(cbuf, off, len);
        }
    }

    public synchronized void flush() throws IOException {
        if (this.sink != null) {
            if (this.sink.closedByReader || this.closed) {
                throw new IOException("Pipe closed");
            }
            synchronized (this.sink) {
                this.sink.notifyAll();
            }
        }
    }

    public void close() throws IOException {
        this.closed = true;
        if (this.sink != null) {
            this.sink.receivedLast();
        }
    }
}
