package java.util.zip;

import dalvik.system.CloseGuard;

public class Inflater {
    static final /* synthetic */ boolean -assertionsDisabled = (Inflater.class.desiredAssertionStatus() ^ 1);
    private static final byte[] defaultBuf = new byte[0];
    private byte[] buf;
    private long bytesRead;
    private long bytesWritten;
    private boolean finished;
    private final CloseGuard guard;
    private int len;
    private boolean needDict;
    private int off;
    private final ZStreamRef zsRef;

    private static native void end(long j);

    private static native int getAdler(long j);

    private native int inflateBytes(long j, byte[] bArr, int i, int i2) throws DataFormatException;

    private static native long init(boolean z);

    private static native void reset(long j);

    private static native void setDictionary(long j, byte[] bArr, int i, int i2);

    public Inflater(boolean nowrap) {
        this.buf = defaultBuf;
        this.guard = CloseGuard.get();
        this.zsRef = new ZStreamRef(init(nowrap));
        this.guard.open("end");
    }

    public Inflater() {
        this(false);
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
                this.needDict = false;
            }
        }
    }

    public void setDictionary(byte[] b) {
        setDictionary(b, 0, b.length);
    }

    public int getRemaining() {
        int i;
        synchronized (this.zsRef) {
            i = this.len;
        }
        return i;
    }

    public boolean needsInput() {
        boolean z = false;
        synchronized (this.zsRef) {
            if (this.len <= 0) {
                z = true;
            }
        }
        return z;
    }

    public boolean needsDictionary() {
        boolean z;
        synchronized (this.zsRef) {
            z = this.needDict;
        }
        return z;
    }

    public boolean finished() {
        boolean z;
        synchronized (this.zsRef) {
            z = this.finished;
        }
        return z;
    }

    public int inflate(byte[] b, int off, int len) throws DataFormatException {
        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || off > b.length - len) {
            throw new ArrayIndexOutOfBoundsException();
        } else {
            int n;
            synchronized (this.zsRef) {
                ensureOpen();
                int thisLen = this.len;
                n = inflateBytes(this.zsRef.address(), b, off, len);
                this.bytesWritten += (long) n;
                this.bytesRead += (long) (thisLen - this.len);
            }
            return n;
        }
    }

    public int inflate(byte[] b) throws DataFormatException {
        return inflate(b, 0, b.length);
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
            this.buf = defaultBuf;
            this.finished = false;
            this.needDict = false;
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
            throw new IllegalStateException("Inflater has been closed");
        }
    }

    boolean ended() {
        boolean z;
        synchronized (this.zsRef) {
            z = this.zsRef.address() == 0;
        }
        return z;
    }
}
