package java.util.zip;

import dalvik.system.CloseGuard;

public class Deflater {
    static final /* synthetic */ boolean -assertionsDisabled = false;
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.zip.Deflater.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.util.zip.Deflater.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: java.util.zip.Deflater.<clinit>():void");
    }

    private native int deflateBytes(long j, byte[] bArr, int i, int i2, int i3);

    private static native void end(long j);

    private static native int getAdler(long j);

    private static native long init(int i, int i2, boolean z);

    private static native void reset(long j);

    private static native void setDictionary(long j, byte[] bArr, int i, int i2);

    public Deflater(int level, boolean nowrap) {
        this.buf = new byte[NO_FLUSH];
        this.guard = CloseGuard.get();
        this.level = level;
        this.strategy = NO_FLUSH;
        this.zsRef = new ZStreamRef(init(level, NO_FLUSH, nowrap));
        this.guard.open("end");
    }

    public Deflater(int level) {
        this(level, -assertionsDisabled);
    }

    public Deflater() {
        this(DEFAULT_COMPRESSION, -assertionsDisabled);
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
        setInput(b, NO_FLUSH, b.length);
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
        setDictionary(b, NO_FLUSH, b.length);
    }

    public void setStrategy(int strategy) {
        switch (strategy) {
            case NO_FLUSH /*0*/:
            case FILTERED /*1*/:
            case SYNC_FLUSH /*2*/:
                synchronized (this.zsRef) {
                    if (this.strategy != strategy) {
                        this.strategy = strategy;
                        this.setParams = true;
                    }
                    break;
                }
            default:
                throw new IllegalArgumentException();
        }
    }

    public void setLevel(int level) {
        if ((level < 0 || level > BEST_COMPRESSION) && level != DEFAULT_COMPRESSION) {
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
        return this.len <= 0 ? true : -assertionsDisabled;
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
        return deflate(b, off, len, NO_FLUSH);
    }

    public int deflate(byte[] b) {
        return deflate(b, NO_FLUSH, b.length, NO_FLUSH);
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
                if (flush == 0 || flush == SYNC_FLUSH || flush == FULL_FLUSH) {
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
            this.len = NO_FLUSH;
            this.off = NO_FLUSH;
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
