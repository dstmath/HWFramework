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
        this(src, 1024);
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
        initPipe(1024);
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
        } else if (this.readSide == null || (this.readSide.isAlive() ^ 1) == 0) {
            this.writeSide = Thread.currentThread();
            while (this.in == this.out) {
                if (this.readSide == null || (this.readSide.isAlive() ^ 1) == 0) {
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

    /* JADX WARNING: Missing block: B:56:0x0098, code:
            return r1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized int read() throws IOException {
        if (!this.connected) {
            throw new IOException("Pipe not connected");
        } else if (this.closedByReader) {
            throw new IOException("Pipe closed");
        } else if (this.writeSide == null || (this.writeSide.isAlive() ^ 1) == 0 || (this.closedByWriter ^ 1) == 0 || this.in >= 0) {
            this.readSide = Thread.currentThread();
            int trials = 2;
            while (this.in < 0) {
                if (this.closedByWriter) {
                    return -1;
                }
                if (!(this.writeSide == null || (this.writeSide.isAlive() ^ 1) == 0)) {
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
        } else {
            throw new IOException("Write end dead");
        }
    }

    /* JADX WARNING: Missing block: B:58:0x0094, code:
            return r1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized int read(char[] cbuf, int off, int len) throws IOException {
        if (!this.connected) {
            throw new IOException("Pipe not connected");
        } else if (this.closedByReader) {
            throw new IOException("Pipe closed");
        } else if (this.writeSide == null || (this.writeSide.isAlive() ^ 1) == 0 || (this.closedByWriter ^ 1) == 0 || this.in >= 0) {
            if (off >= 0 && off <= cbuf.length && len >= 0 && off + len <= cbuf.length && off + len >= 0) {
                if (len != 0) {
                    int c = read();
                    if (c >= 0) {
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
                    } else {
                        return -1;
                    }
                }
                return 0;
            }
            throw new IndexOutOfBoundsException();
        } else {
            throw new IOException("Write end dead");
        }
    }

    public synchronized boolean ready() throws IOException {
        if (!this.connected) {
            throw new IOException("Pipe not connected");
        } else if (this.closedByReader) {
            throw new IOException("Pipe closed");
        } else if (this.writeSide != null && (this.writeSide.isAlive() ^ 1) != 0 && (this.closedByWriter ^ 1) != 0 && this.in < 0) {
            throw new IOException("Write end dead");
        } else if (this.in < 0) {
            return false;
        } else {
            return true;
        }
    }

    public void close() throws IOException {
        this.in = -1;
        this.closedByReader = true;
    }
}
