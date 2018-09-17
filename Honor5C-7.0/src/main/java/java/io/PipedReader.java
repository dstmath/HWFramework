package java.io;

import libcore.io.IoUtils;

public class PipedReader extends Reader {
    private static final int DEFAULT_PIPE_SIZE = 1024;
    char[] buffer;
    boolean closedByReader;
    boolean closedByWriter;
    boolean connected;
    int in;
    int out;
    Thread readSide;
    Thread writeSide;

    public PipedReader(PipedWriter src) throws IOException {
        this(src, DEFAULT_PIPE_SIZE);
    }

    public PipedReader(PipedWriter src, int pipeSize) throws IOException {
        this.closedByWriter = false;
        this.closedByReader = false;
        this.connected = false;
        this.in = -1;
        this.out = 0;
        initPipe(pipeSize);
        connect(src);
    }

    public PipedReader() {
        this.closedByWriter = false;
        this.closedByReader = false;
        this.connected = false;
        this.in = -1;
        this.out = 0;
        initPipe(DEFAULT_PIPE_SIZE);
    }

    public PipedReader(int pipeSize) {
        this.closedByWriter = false;
        this.closedByReader = false;
        this.connected = false;
        this.in = -1;
        this.out = 0;
        initPipe(pipeSize);
    }

    private void initPipe(int pipeSize) {
        if (pipeSize <= 0) {
            throw new IllegalArgumentException("Pipe size <= 0");
        }
        this.buffer = new char[pipeSize];
    }

    public void connect(PipedWriter src) throws IOException {
        src.connect(this);
    }

    synchronized void receive(int c) throws IOException {
        if (!this.connected) {
            throw new IOException("Pipe not connected");
        } else if (this.closedByWriter || this.closedByReader) {
            throw new IOException("Pipe closed");
        } else if (this.readSide == null || this.readSide.isAlive()) {
            this.writeSide = Thread.currentThread();
            while (this.in == this.out) {
                if (this.readSide == null || this.readSide.isAlive()) {
                    notifyAll();
                    try {
                        wait(1000);
                    } catch (InterruptedException e) {
                        IoUtils.throwInterruptedIoException();
                    }
                } else {
                    throw new IOException("Pipe broken");
                }
            }
            if (this.in < 0) {
                this.in = 0;
                this.out = 0;
            }
            char[] cArr = this.buffer;
            int i = this.in;
            this.in = i + 1;
            cArr[i] = (char) c;
            if (this.in >= this.buffer.length) {
                this.in = 0;
            }
        } else {
            throw new IOException("Read end dead");
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    synchronized void receive(char[] c, int off, int len) throws IOException {
        while (true) {
            len--;
            if (len >= 0) {
                int off2 = off + 1;
                receive(c[off]);
                off = off2;
            }
        }
    }

    synchronized void receivedLast() {
        this.closedByWriter = true;
        notifyAll();
    }

    public synchronized int read() throws IOException {
        if (!this.connected) {
            throw new IOException("Pipe not connected");
        } else if (this.closedByReader) {
            throw new IOException("Pipe closed");
        } else {
            if (!(this.writeSide == null || this.writeSide.isAlive())) {
                if (!this.closedByWriter && this.in < 0) {
                    throw new IOException("Write end dead");
                }
            }
            this.readSide = Thread.currentThread();
            int trials = 2;
            while (this.in < 0) {
                if (this.closedByWriter) {
                    return -1;
                }
                if (!(this.writeSide == null || this.writeSide.isAlive())) {
                    trials--;
                    if (trials < 0) {
                        throw new IOException("Pipe broken");
                    }
                }
                notifyAll();
                try {
                    wait(1000);
                } catch (InterruptedException e) {
                    IoUtils.throwInterruptedIoException();
                }
            }
            char[] cArr = this.buffer;
            int i = this.out;
            this.out = i + 1;
            int ret = cArr[i];
            if (this.out >= this.buffer.length) {
                this.out = 0;
            }
            if (this.in == this.out) {
                this.in = -1;
            }
            return ret;
        }
    }

    public synchronized int read(char[] cbuf, int off, int len) throws IOException {
        if (!this.connected) {
            throw new IOException("Pipe not connected");
        } else if (this.closedByReader) {
            throw new IOException("Pipe closed");
        } else if (this.writeSide != null && !this.writeSide.isAlive() && !this.closedByWriter && this.in < 0) {
            throw new IOException("Write end dead");
        } else if (off < 0 || off > cbuf.length || len < 0 || off + len > cbuf.length || off + len < 0) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        } else {
            int c = read();
            if (c < 0) {
                return -1;
            }
            cbuf[off] = (char) c;
            int rlen = 1;
            while (this.in >= 0) {
                len--;
                if (len <= 0) {
                    break;
                }
                int i = off + rlen;
                char[] cArr = this.buffer;
                int i2 = this.out;
                this.out = i2 + 1;
                cbuf[i] = cArr[i2];
                rlen++;
                if (this.out >= this.buffer.length) {
                    this.out = 0;
                }
                if (this.in == this.out) {
                    this.in = -1;
                }
            }
            return rlen;
        }
    }

    public synchronized boolean ready() throws IOException {
        if (!this.connected) {
            throw new IOException("Pipe not connected");
        } else if (this.closedByReader) {
            throw new IOException("Pipe closed");
        } else {
            if (!(this.writeSide == null || this.writeSide.isAlive())) {
                if (!this.closedByWriter && this.in < 0) {
                    throw new IOException("Write end dead");
                }
            }
            if (this.in < 0) {
                return false;
            }
            return true;
        }
    }

    public void close() throws IOException {
        this.in = -1;
        this.closedByReader = true;
    }
}
