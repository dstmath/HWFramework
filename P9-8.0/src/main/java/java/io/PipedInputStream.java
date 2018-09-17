package java.io;

import libcore.io.IoUtils;

public class PipedInputStream extends InputStream {
    static final /* synthetic */ boolean -assertionsDisabled = (PipedInputStream.class.desiredAssertionStatus() ^ 1);
    private static final int DEFAULT_PIPE_SIZE = 1024;
    protected static final int PIPE_SIZE = 1024;
    protected byte[] buffer;
    volatile boolean closedByReader;
    boolean closedByWriter;
    boolean connected;
    protected int in;
    protected int out;
    Thread readSide;
    Thread writeSide;

    public PipedInputStream(PipedOutputStream src) throws IOException {
        this(src, 1024);
    }

    public PipedInputStream(PipedOutputStream src, int pipeSize) throws IOException {
        this.closedByWriter = -assertionsDisabled;
        this.closedByReader = -assertionsDisabled;
        this.connected = -assertionsDisabled;
        this.in = -1;
        this.out = 0;
        initPipe(pipeSize);
        connect(src);
    }

    public PipedInputStream() {
        this.closedByWriter = -assertionsDisabled;
        this.closedByReader = -assertionsDisabled;
        this.connected = -assertionsDisabled;
        this.in = -1;
        this.out = 0;
        initPipe(1024);
    }

    public PipedInputStream(int pipeSize) {
        this.closedByWriter = -assertionsDisabled;
        this.closedByReader = -assertionsDisabled;
        this.connected = -assertionsDisabled;
        this.in = -1;
        this.out = 0;
        initPipe(pipeSize);
    }

    private void initPipe(int pipeSize) {
        if (pipeSize <= 0) {
            throw new IllegalArgumentException("Pipe Size <= 0");
        }
        this.buffer = new byte[pipeSize];
    }

    public void connect(PipedOutputStream src) throws IOException {
        src.connect(this);
    }

    protected synchronized void receive(int b) throws IOException {
        checkStateForReceive();
        this.writeSide = Thread.currentThread();
        if (this.in == this.out) {
            awaitSpace();
        }
        if (this.in < 0) {
            this.in = 0;
            this.out = 0;
        }
        byte[] bArr = this.buffer;
        int i = this.in;
        this.in = i + 1;
        bArr[i] = (byte) (b & 255);
        if (this.in >= this.buffer.length) {
            this.in = 0;
        }
    }

    synchronized void receive(byte[] b, int off, int len) throws IOException {
        checkStateForReceive();
        this.writeSide = Thread.currentThread();
        int bytesToTransfer = len;
        while (bytesToTransfer > 0) {
            if (this.in == this.out) {
                awaitSpace();
            }
            int nextTransferAmount = 0;
            if (this.out < this.in) {
                nextTransferAmount = this.buffer.length - this.in;
            } else if (this.in < this.out) {
                if (this.in == -1) {
                    this.out = 0;
                    this.in = 0;
                    nextTransferAmount = this.buffer.length - this.in;
                } else {
                    nextTransferAmount = this.out - this.in;
                }
            }
            if (nextTransferAmount > bytesToTransfer) {
                nextTransferAmount = bytesToTransfer;
            }
            if (-assertionsDisabled || nextTransferAmount > 0) {
                System.arraycopy(b, off, this.buffer, this.in, nextTransferAmount);
                bytesToTransfer -= nextTransferAmount;
                off += nextTransferAmount;
                this.in += nextTransferAmount;
                if (this.in >= this.buffer.length) {
                    this.in = 0;
                }
            } else {
                throw new AssertionError();
            }
        }
    }

    private void checkStateForReceive() throws IOException {
        if (!this.connected) {
            throw new IOException("Pipe not connected");
        } else if (this.closedByWriter || this.closedByReader) {
            throw new IOException("Pipe closed");
        } else if (this.readSide != null && (this.readSide.isAlive() ^ 1) != 0) {
            throw new IOException("Read end dead");
        }
    }

    private void awaitSpace() throws IOException {
        while (this.in == this.out) {
            checkStateForReceive();
            notifyAll();
            try {
                wait(1000);
            } catch (InterruptedException e) {
                IoUtils.throwInterruptedIoException();
            }
        }
    }

    synchronized void receivedLast() {
        this.closedByWriter = true;
        notifyAll();
    }

    /* JADX WARNING: Missing block: B:56:0x009a, code:
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
            byte[] bArr = this.buffer;
            int i = this.out;
            this.out = i + 1;
            int ret = bArr[i] & 255;
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

    /* JADX WARNING: Missing block: B:47:0x007b, code:
            return r2;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized int read(byte[] b, int off, int len) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        } else {
            int c = read();
            if (c < 0) {
                return -1;
            }
            b[off] = (byte) c;
            int rlen = 1;
            while (this.in >= 0 && len > 1) {
                int available;
                if (this.in > this.out) {
                    available = Math.min(this.buffer.length - this.out, this.in - this.out);
                } else {
                    available = this.buffer.length - this.out;
                }
                if (available > len - 1) {
                    available = len - 1;
                }
                System.arraycopy(this.buffer, this.out, b, off + rlen, available);
                this.out += available;
                rlen += available;
                len -= available;
                if (this.out >= this.buffer.length) {
                    this.out = 0;
                }
                if (this.in == this.out) {
                    this.in = -1;
                }
            }
        }
    }

    public synchronized int available() throws IOException {
        if (this.in < 0) {
            return 0;
        }
        if (this.in == this.out) {
            return this.buffer.length;
        } else if (this.in > this.out) {
            return this.in - this.out;
        } else {
            return (this.in + this.buffer.length) - this.out;
        }
    }

    public void close() throws IOException {
        this.closedByReader = true;
        synchronized (this) {
            this.in = -1;
        }
    }
}
