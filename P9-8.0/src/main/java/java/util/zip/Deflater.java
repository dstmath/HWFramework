package java.util.zip;

import dalvik.system.CloseGuard;

public class Deflater {
    static final /* synthetic */ boolean -assertionsDisabled = (Deflater.class.desiredAssertionStatus() ^ 1);
    public static final int BEST_COMPRESSION = 9;
    public static final int BEST_SPEED = 1;
    public static final int DEFAULT_COMPRESSION = -1;
    public static final int DEFAULT_STRATEGY = 0;
    public static final int DEFLATED = 8;
    public static final int FILTERED = 1;
    public static final int FULL_FLUSH = 3;
    public static final int HUFFMAN_ONLY = 2;
    public static final int NO_COMPRESSION = 0;
    public static final int NO_FLUSH = 0;
    public static final int SYNC_FLUSH = 2;
    private byte[] buf;
    private long bytesRead;
    private long bytesWritten;
    private boolean finish;
    private boolean finished;
    private final CloseGuard guard;
    private int len;
    private int level;
    private int off;
    private boolean setParams;
    private int strategy;
    private final ZStreamRef zsRef;

    private native int deflateBytes(long j, byte[] bArr, int i, int i2, int i3);

    private static native void end(long j);

    private static native int getAdler(long j);

    private static native long init(int i, int i2, boolean z);

    private static native void reset(long j);

    private static native void setDictionary(long j, byte[] bArr, int i, int i2);

    public Deflater(int level, boolean nowrap) {
        this.buf = new byte[0];
        this.guard = CloseGuard.get();
        this.level = level;
        this.strategy = 0;
        this.zsRef = new ZStreamRef(init(level, 0, nowrap));
        this.guard.open("end");
    }

    public Deflater(int level) {
        this(level, -assertionsDisabled);
    }

    public Deflater() {
        this(-1, -assertionsDisabled);
    }

    public void setInput(byte[] b, int off, int len) {
        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || off > b.length - len) {
            throw new ArrayIndexOutOfBoundsException();
        } else {
            synchronized (this.zsRef) {
                this.buf = b;
                this.off = off;
                this.len = len;
            }
        }
    }

    public void setInput(byte[] b) {
        setInput(b, 0, b.length);
    }

    public void setDictionary(byte[] b, int off, int len) {
        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || off > b.length - len) {
            throw new ArrayIndexOutOfBoundsException();
        } else {
            synchronized (this.zsRef) {
                ensureOpen();
                setDictionary(this.zsRef.address(), b, off, len);
            }
        }
    }

    public void setDictionary(byte[] b) {
        setDictionary(b, 0, b.length);
    }

    public void setStrategy(int strategy) {
        switch (strategy) {
            case 0:
            case 1:
            case 2:
                synchronized (this.zsRef) {
                    if (this.strategy != strategy) {
                        this.strategy = strategy;
                        this.setParams = true;
                    }
                }
                return;
            default:
                throw new IllegalArgumentException();
        }
    }

    public void setLevel(int level) {
        if ((level < 0 || level > 9) && level != -1) {
            throw new IllegalArgumentException("invalid compression level");
        }
        synchronized (this.zsRef) {
            if (this.level != level) {
                this.level = level;
                this.setParams = true;
            }
        }
    }

    public boolean needsInput() {
        boolean z = -assertionsDisabled;
        synchronized (this.zsRef) {
            if (this.len <= 0) {
                z = true;
            }
        }
        return z;
    }

    public void finish() {
        synchronized (this.zsRef) {
            this.finish = true;
        }
    }

    public boolean finished() {
        boolean z;
        synchronized (this.zsRef) {
            z = this.finished;
        }
        return z;
    }

    public int deflate(byte[] b, int off, int len) {
        return deflate(b, off, len, 0);
    }

    public int deflate(byte[] b) {
        return deflate(b, 0, b.length, 0);
    }

    public int deflate(byte[] b, int off, int len, int flush) {
        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || off > b.length - len) {
            throw new ArrayIndexOutOfBoundsException();
        } else {
            int n;
            synchronized (this.zsRef) {
                ensureOpen();
                if (flush == 0 || flush == 2 || flush == 3) {
                    int thisLen = this.len;
                    n = deflateBytes(this.zsRef.address(), b, off, len, flush);
                    this.bytesWritten += (long) n;
                    this.bytesRead += (long) (thisLen - this.len);
                } else {
                    throw new IllegalArgumentException();
                }
            }
            return n;
        }
    }

    public int getAdler() {
        int adler;
        synchronized (this.zsRef) {
            ensureOpen();
            adler = getAdler(this.zsRef.address());
        }
        return adler;
    }

    public int getTotalIn() {
        return (int) getBytesRead();
    }

    public long getBytesRead() {
        long j;
        synchronized (this.zsRef) {
            ensureOpen();
            j = this.bytesRead;
        }
        return j;
    }

    public int getTotalOut() {
        return (int) getBytesWritten();
    }

    public long getBytesWritten() {
        long j;
        synchronized (this.zsRef) {
            ensureOpen();
            j = this.bytesWritten;
        }
        return j;
    }

    public void reset() {
        synchronized (this.zsRef) {
            ensureOpen();
            reset(this.zsRef.address());
            this.finish = -assertionsDisabled;
            this.finished = -assertionsDisabled;
            this.len = 0;
            this.off = 0;
            this.bytesWritten = 0;
            this.bytesRead = 0;
        }
    }

    public void end() {
        synchronized (this.zsRef) {
            this.guard.close();
            long addr = this.zsRef.address();
            this.zsRef.clear();
            if (addr != 0) {
                end(addr);
                this.buf = null;
            }
        }
    }

    protected void finalize() {
        if (this.guard != null) {
            this.guard.warnIfOpen();
        }
        end();
    }

    private void ensureOpen() {
        if (!-assertionsDisabled && !Thread.holdsLock(this.zsRef)) {
            throw new AssertionError();
        } else if (this.zsRef.address() == 0) {
            throw new NullPointerException("Deflater has been closed");
        }
    }
}
