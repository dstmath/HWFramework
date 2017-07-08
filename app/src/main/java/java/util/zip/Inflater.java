package java.util.zip;

import dalvik.system.CloseGuard;

public class Inflater {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    private static final byte[] defaultBuf = null;
    private byte[] buf;
    private long bytesRead;
    private long bytesWritten;
    private boolean finished;
    private final CloseGuard guard;
    private int len;
    private boolean needDict;
    private int off;
    private final ZStreamRef zsRef;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.zip.Inflater.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.util.zip.Inflater.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: java.util.zip.Inflater.<clinit>():void");
    }

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
