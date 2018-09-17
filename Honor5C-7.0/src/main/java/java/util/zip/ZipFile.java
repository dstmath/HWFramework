package java.util.zip;

import dalvik.system.CloseGuard;
import java.io.Closeable;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.WeakHashMap;
import sun.util.logging.PlatformLogger;

public class ZipFile implements ZipConstants, Closeable {
    private static final int DEFLATED = 8;
    private static final int JZENTRY_COMMENT = 2;
    private static final int JZENTRY_EXTRA = 1;
    private static final int JZENTRY_NAME = 0;
    public static final int OPEN_DELETE = 4;
    public static final int OPEN_READ = 1;
    private static final int STORED = 0;
    private static final boolean usemmap = false;
    private volatile boolean closeRequested;
    private final File fileToRemoveOnClose;
    private final CloseGuard guard;
    private Deque<Inflater> inflaterCache;
    private long jzfile;
    private final boolean locsig;
    private final String name;
    private final Map<InputStream, Inflater> streams;
    private final int total;
    private ZipCoder zc;

    private class ZipFileInflaterInputStream extends InflaterInputStream {
        private volatile boolean closeRequested;
        private boolean eof;
        private final ZipFileInputStream zfin;

        ZipFileInflaterInputStream(ZipFileInputStream zfin, Inflater inf, int size) {
            super(zfin, inf, size);
            this.closeRequested = false;
            this.eof = false;
            this.zfin = zfin;
        }

        public void close() throws IOException {
            if (!this.closeRequested) {
                Inflater inf;
                this.closeRequested = true;
                super.close();
                synchronized (ZipFile.this.streams) {
                    inf = (Inflater) ZipFile.this.streams.remove(this);
                }
                if (inf != null) {
                    ZipFile.this.releaseInflater(inf);
                }
            }
        }

        protected void fill() throws IOException {
            if (this.eof) {
                throw new EOFException("Unexpected end of ZLIB input stream");
            }
            this.len = this.in.read(this.buf, ZipFile.JZENTRY_NAME, this.buf.length);
            if (this.len == -1) {
                this.buf[ZipFile.JZENTRY_NAME] = (byte) 0;
                this.len = ZipFile.OPEN_READ;
                this.eof = true;
            }
            this.inf.setInput(this.buf, ZipFile.JZENTRY_NAME, this.len);
        }

        public int available() throws IOException {
            if (this.closeRequested) {
                return ZipFile.JZENTRY_NAME;
            }
            long avail = this.zfin.size() - this.inf.getBytesWritten();
            return avail > 2147483647L ? PlatformLogger.OFF : (int) avail;
        }

        protected void finalize() throws Throwable {
            close();
        }
    }

    private class ZipFileInputStream extends InputStream {
        private volatile boolean closeRequested;
        protected long jzentry;
        private long pos;
        protected long rem;
        protected long size;

        ZipFileInputStream(long jzentry) {
            this.closeRequested = false;
            this.pos = 0;
            this.rem = ZipFile.getEntryCSize(jzentry);
            this.size = ZipFile.getEntrySize(jzentry);
            this.jzentry = jzentry;
        }

        public int read(byte[] b, int off, int len) throws IOException {
            ZipFile.this.ensureOpenOrZipException();
            if (this.rem == 0) {
                return -1;
            }
            if (len <= 0) {
                return ZipFile.JZENTRY_NAME;
            }
            if (((long) len) > this.rem) {
                len = (int) this.rem;
            }
            synchronized (ZipFile.this) {
                len = ZipFile.read(ZipFile.this.jzfile, this.jzentry, this.pos, b, off, len);
            }
            if (len > 0) {
                this.pos += (long) len;
                this.rem -= (long) len;
            }
            if (this.rem == 0) {
                close();
            }
            return len;
        }

        public int read() throws IOException {
            byte[] b = new byte[ZipFile.OPEN_READ];
            if (read(b, ZipFile.JZENTRY_NAME, ZipFile.OPEN_READ) == ZipFile.OPEN_READ) {
                return b[ZipFile.JZENTRY_NAME] & 255;
            }
            return -1;
        }

        public long skip(long n) {
            if (n > this.rem) {
                n = this.rem;
            }
            this.pos += n;
            this.rem -= n;
            if (this.rem == 0) {
                close();
            }
            return n;
        }

        public int available() {
            return this.rem > 2147483647L ? PlatformLogger.OFF : (int) this.rem;
        }

        public long size() {
            return this.size;
        }

        public void close() {
            if (!this.closeRequested) {
                this.closeRequested = true;
                this.rem = 0;
                synchronized (ZipFile.this) {
                    if (!(this.jzentry == 0 || ZipFile.this.jzfile == 0)) {
                        ZipFile.freeEntry(ZipFile.this.jzfile, this.jzentry);
                        this.jzentry = 0;
                    }
                }
                synchronized (ZipFile.this.streams) {
                    ZipFile.this.streams.remove(this);
                }
            }
        }

        protected void finalize() {
            close();
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.zip.ZipFile.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.util.zip.ZipFile.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: java.util.zip.ZipFile.<clinit>():void");
    }

    private static native void close(long j);

    private static native void freeEntry(long j, long j2);

    private static native byte[] getCommentBytes(long j);

    private static native long getEntry(long j, byte[] bArr, boolean z);

    private static native byte[] getEntryBytes(long j, int i);

    private static native long getEntryCSize(long j);

    private static native long getEntryCrc(long j);

    private static native int getEntryFlag(long j);

    private static native int getEntryMethod(long j);

    private static native long getEntrySize(long j);

    private static native long getEntryTime(long j);

    private static native long getNextEntry(long j, int i);

    private static native int getTotal(long j);

    private static native String getZipMessage(long j);

    private static native long open(String str, int i, long j, boolean z) throws IOException;

    private static native int read(long j, long j2, long j3, byte[] bArr, int i, int i2);

    private static native boolean startsWithLOC(long j);

    public ZipFile(String name) throws IOException {
        this(new File(name), (int) OPEN_READ);
    }

    public ZipFile(File file, int mode) throws IOException {
        this(file, mode, StandardCharsets.UTF_8);
    }

    public ZipFile(File file) throws ZipException, IOException {
        this(file, (int) OPEN_READ);
    }

    public ZipFile(File file, int mode, Charset charset) throws IOException {
        File file2 = null;
        this.closeRequested = false;
        this.guard = CloseGuard.get();
        this.streams = new WeakHashMap();
        this.inflaterCache = new ArrayDeque();
        if ((mode & OPEN_READ) == 0 || (mode & -6) != 0) {
            throw new IllegalArgumentException("Illegal mode: 0x" + Integer.toHexString(mode));
        } else if (file.length() < 22) {
            throw new ZipException("File too short to be a zip file: " + file.length());
        } else {
            String name = file.getPath();
            if ((mode & OPEN_DELETE) != 0) {
                file2 = file;
            }
            this.fileToRemoveOnClose = file2;
            if (charset == null) {
                throw new NullPointerException("charset is null");
            }
            this.zc = ZipCoder.get(charset);
            this.jzfile = open(name, mode, file.lastModified(), usemmap);
            this.name = name;
            this.total = getTotal(this.jzfile);
            this.locsig = startsWithLOC(this.jzfile);
            Enumeration<? extends ZipEntry> entries = entries();
            if (size() == 0 || !entries.hasMoreElements()) {
                close();
                throw new ZipException("No entries");
            } else {
                this.guard.open("close");
            }
        }
    }

    public ZipFile(String name, Charset charset) throws IOException {
        this(new File(name), OPEN_READ, charset);
    }

    public ZipFile(File file, Charset charset) throws IOException {
        this(file, OPEN_READ, charset);
    }

    public String getComment() {
        synchronized (this) {
            ensureOpen();
            byte[] bcomm = getCommentBytes(this.jzfile);
            if (bcomm == null) {
                return null;
            }
            String zipCoder = this.zc.toString(bcomm, bcomm.length);
            return zipCoder;
        }
    }

    public ZipEntry getEntry(String name) {
        if (name == null) {
            throw new NullPointerException("name");
        }
        synchronized (this) {
            ensureOpen();
            long jzentry = getEntry(this.jzfile, this.zc.getBytes(name), true);
            if (jzentry != 0) {
                ZipEntry ze = getZipEntry(name, jzentry);
                freeEntry(this.jzfile, jzentry);
                return ze;
            }
            return null;
        }
    }

    public InputStream getInputStream(ZipEntry entry) throws IOException {
        ZipFileInputStream zipFileInputStream;
        if (entry == null) {
            throw new NullPointerException("entry");
        }
        synchronized (this) {
            Throwable th;
            try {
                long jzentry;
                ensureOpen();
                if (this.zc.isUTF8() || (entry.flag & Modifier.STRICT) == 0) {
                    jzentry = getEntry(this.jzfile, this.zc.getBytes(entry.name), true);
                } else {
                    jzentry = getEntry(this.jzfile, this.zc.getBytesUTF8(entry.name), true);
                }
                if (jzentry == 0) {
                    return null;
                }
                ZipFileInputStream in = new ZipFileInputStream(jzentry);
                try {
                    switch (getEntryMethod(jzentry)) {
                        case JZENTRY_NAME /*0*/:
                            synchronized (this.streams) {
                                this.streams.put(in, null);
                                break;
                            }
                            return in;
                        case DEFLATED /*8*/:
                            long size = getEntrySize(jzentry) + 2;
                            if (size > 65536) {
                                size = 8192;
                            }
                            if (size <= 0) {
                                size = 4096;
                            }
                            Inflater inf = getInflater();
                            InputStream is = new ZipFileInflaterInputStream(in, inf, (int) size);
                            synchronized (this.streams) {
                                this.streams.put(is, inf);
                                break;
                            }
                            return is;
                        default:
                            throw new ZipException("invalid compression method");
                    }
                } catch (Throwable th2) {
                    th = th2;
                    zipFileInputStream = in;
                }
                th = th2;
                zipFileInputStream = in;
                throw th;
            } catch (Throwable th3) {
                th = th3;
            }
        }
    }

    private Inflater getInflater() {
        synchronized (this.inflaterCache) {
            Inflater inf;
            do {
                inf = (Inflater) this.inflaterCache.poll();
                if (inf == null) {
                    return new Inflater(true);
                }
            } while (inf.ended());
            return inf;
        }
    }

    private void releaseInflater(Inflater inf) {
        if (!inf.ended()) {
            inf.reset();
            synchronized (this.inflaterCache) {
                this.inflaterCache.add(inf);
            }
        }
    }

    public String getName() {
        return this.name;
    }

    public Enumeration<? extends ZipEntry> entries() {
        ensureOpen();
        return new Enumeration<ZipEntry>() {
            private int i;

            {
                this.i = ZipFile.JZENTRY_NAME;
            }

            public boolean hasMoreElements() {
                boolean z;
                synchronized (ZipFile.this) {
                    ZipFile.this.ensureOpen();
                    z = this.i < ZipFile.this.total;
                }
                return z;
            }

            public ZipEntry nextElement() throws NoSuchElementException {
                ZipEntry ze;
                synchronized (ZipFile.this) {
                    ZipFile.this.ensureOpen();
                    if (this.i >= ZipFile.this.total) {
                        throw new NoSuchElementException();
                    }
                    long -get1 = ZipFile.this.jzfile;
                    int i = this.i;
                    this.i = i + ZipFile.OPEN_READ;
                    long jzentry = ZipFile.getNextEntry(-get1, i);
                    if (jzentry == 0) {
                        String message;
                        if (ZipFile.this.closeRequested) {
                            message = "ZipFile concurrently closed";
                        } else {
                            message = ZipFile.getZipMessage(ZipFile.this.jzfile);
                        }
                        throw new ZipError("jzentry == 0,\n jzfile = " + ZipFile.this.jzfile + ",\n total = " + ZipFile.this.total + ",\n name = " + ZipFile.this.name + ",\n i = " + this.i + ",\n message = " + message);
                    }
                    ze = ZipFile.this.getZipEntry(null, jzentry);
                    ZipFile.freeEntry(ZipFile.this.jzfile, jzentry);
                }
                return ze;
            }
        };
    }

    private ZipEntry getZipEntry(String name, long jzentry) {
        ZipEntry e = new ZipEntry();
        e.flag = getEntryFlag(jzentry);
        if (name != null) {
            e.name = name;
        } else {
            byte[] bname = getEntryBytes(jzentry, JZENTRY_NAME);
            if (this.zc.isUTF8() || (e.flag & Modifier.STRICT) == 0) {
                e.name = this.zc.toString(bname, bname.length);
            } else {
                e.name = this.zc.toStringUTF8(bname, bname.length);
            }
        }
        e.time = getEntryTime(jzentry);
        e.crc = getEntryCrc(jzentry);
        e.size = getEntrySize(jzentry);
        e.csize = getEntryCSize(jzentry);
        e.method = getEntryMethod(jzentry);
        e.extra = getEntryBytes(jzentry, OPEN_READ);
        byte[] bcomm = getEntryBytes(jzentry, JZENTRY_COMMENT);
        if (bcomm == null) {
            e.comment = null;
        } else if (this.zc.isUTF8() || (e.flag & Modifier.STRICT) == 0) {
            e.comment = this.zc.toString(bcomm, bcomm.length);
        } else {
            e.comment = this.zc.toStringUTF8(bcomm, bcomm.length);
        }
        return e;
    }

    public int size() {
        ensureOpen();
        return this.total;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void close() throws IOException {
        if (!this.closeRequested) {
            this.guard.close();
            this.closeRequested = true;
            synchronized (this) {
                Inflater inf;
                synchronized (this.streams) {
                    if (!this.streams.isEmpty()) {
                        Map<InputStream, Inflater> copy = new HashMap(this.streams);
                        this.streams.clear();
                        for (Entry<InputStream, Inflater> e : copy.entrySet()) {
                            ((InputStream) e.getKey()).close();
                            inf = (Inflater) e.getValue();
                            if (inf != null) {
                                inf.end();
                            }
                        }
                    }
                }
                synchronized (this.inflaterCache) {
                    while (true) {
                        inf = (Inflater) this.inflaterCache.poll();
                        if (inf == null) {
                            break;
                        }
                        inf.end();
                    }
                }
                if (this.jzfile != 0) {
                    long zf = this.jzfile;
                    this.jzfile = 0;
                    close(zf);
                }
                if (this.fileToRemoveOnClose != null) {
                    this.fileToRemoveOnClose.delete();
                }
            }
        }
    }

    protected void finalize() throws IOException {
        if (this.guard != null) {
            this.guard.warnIfOpen();
        }
        close();
    }

    private void ensureOpen() {
        if (this.closeRequested) {
            throw new IllegalStateException("zip file closed");
        } else if (this.jzfile == 0) {
            throw new IllegalStateException("The object is not initialized.");
        }
    }

    private void ensureOpenOrZipException() throws IOException {
        if (this.closeRequested) {
            throw new ZipException("ZipFile closed");
        }
    }

    public boolean startsWithLocHeader() {
        return this.locsig;
    }
}
