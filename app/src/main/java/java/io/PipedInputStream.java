package java.io;

import libcore.io.IoUtils;

public class PipedInputStream extends InputStream {
    static final /* synthetic */ boolean -assertionsDisabled = false;
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.io.PipedInputStream.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.io.PipedInputStream.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: java.io.PipedInputStream.<clinit>():void");
    }

    public PipedInputStream(PipedOutputStream src) throws IOException {
        this(src, PIPE_SIZE);
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
        initPipe(PIPE_SIZE);
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
            if (!-assertionsDisabled) {
                Object obj;
                if (nextTransferAmount > 0) {
                    obj = 1;
                } else {
                    obj = null;
                }
                if (obj == null) {
                    throw new AssertionError();
                }
            }
            System.arraycopy(b, off, this.buffer, this.in, nextTransferAmount);
            bytesToTransfer -= nextTransferAmount;
            off += nextTransferAmount;
            this.in += nextTransferAmount;
            if (this.in >= this.buffer.length) {
                this.in = 0;
            }
        }
    }

    private void checkStateForReceive() throws IOException {
        if (!this.connected) {
            throw new IOException("Pipe not connected");
        } else if (this.closedByWriter || this.closedByReader) {
            throw new IOException("Pipe closed");
        } else if (this.readSide != null && !this.readSide.isAlive()) {
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
            return ret;
        }
    }

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
            return rlen;
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
