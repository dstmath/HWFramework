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
        if (pipeSize > 0) {
            this.buffer = new char[pipeSize];
            return;
        }
        throw new IllegalArgumentException("Pipe size <= 0");
    }

    public void connect(PipedWriter src) throws IOException {
        src.connect(this);
    }

    /* access modifiers changed from: package-private */
    public synchronized void receive(int c) throws IOException {
        if (!this.connected) {
            throw new IOException("Pipe not connected");
        } else if (this.closedByWriter || this.closedByReader) {
            throw new IOException("Pipe closed");
        } else {
            if (this.readSide != null) {
                if (!this.readSide.isAlive()) {
                    throw new IOException("Read end dead");
                }
            }
            this.writeSide = Thread.currentThread();
            while (this.in == this.out) {
                if (this.readSide != null) {
                    if (!this.readSide.isAlive()) {
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
        }
    }

    /* Debug info: failed to restart local var, previous not found, register: 1 */
    /* access modifiers changed from: package-private */
    public synchronized void receive(char[] c, int off, int len) throws IOException {
        while (true) {
            len--;
            if (len >= 0) {
                int off2 = off + 1;
                receive(c[off]);
                off = off2;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public synchronized void receivedLast() {
        this.closedByWriter = true;
        notifyAll();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:48:0x007c, code lost:
        return r1;
     */
    public synchronized int read() throws IOException {
        if (!this.connected) {
            throw new IOException("Pipe not connected");
        } else if (!this.closedByReader) {
            if (this.writeSide != null && !this.writeSide.isAlive() && !this.closedByWriter) {
                if (this.in < 0) {
                    throw new IOException("Write end dead");
                }
            }
            this.readSide = Thread.currentThread();
            int trials = 2;
            while (this.in < 0) {
                if (this.closedByWriter) {
                    return -1;
                }
                if (this.writeSide != null && !this.writeSide.isAlive()) {
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
            char ret = cArr[i];
            if (this.out >= this.buffer.length) {
                this.out = 0;
            }
            if (this.in == this.out) {
                this.in = -1;
            }
        } else {
            throw new IOException("Pipe closed");
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:49:0x0073, code lost:
        return r3;
     */
    public synchronized int read(char[] cbuf, int off, int len) throws IOException {
        if (!this.connected) {
            throw new IOException("Pipe not connected");
        } else if (!this.closedByReader) {
            if (this.writeSide != null && !this.writeSide.isAlive() && !this.closedByWriter) {
                if (this.in < 0) {
                    throw new IOException("Write end dead");
                }
            }
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
                            char[] cArr = this.buffer;
                            int i = this.out;
                            this.out = i + 1;
                            cbuf[off + rlen] = cArr[i];
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
                } else {
                    return 0;
                }
            } else {
                throw new IndexOutOfBoundsException();
            }
        } else {
            throw new IOException("Pipe closed");
        }
    }

    public synchronized boolean ready() throws IOException {
        if (!this.connected) {
            throw new IOException("Pipe not connected");
        } else if (!this.closedByReader) {
            if (this.writeSide != null && !this.writeSide.isAlive() && !this.closedByWriter) {
                if (this.in < 0) {
                    throw new IOException("Write end dead");
                }
            }
            if (this.in < 0) {
                return false;
            }
            return true;
        } else {
            throw new IOException("Pipe closed");
        }
    }

    public void close() throws IOException {
        this.in = -1;
        this.closedByReader = true;
    }
}
