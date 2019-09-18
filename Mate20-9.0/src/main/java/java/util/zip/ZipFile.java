package java.util.zip;

import dalvik.system.CloseGuard;
import java.io.Closeable;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Spliterators;
import java.util.WeakHashMap;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class ZipFile implements ZipConstants, Closeable {
    private static final int DEFLATED = 8;
    private static final int JZENTRY_COMMENT = 2;
    private static final int JZENTRY_EXTRA = 1;
    private static final int JZENTRY_NAME = 0;
    public static final int OPEN_DELETE = 4;
    public static final int OPEN_READ = 1;
    private static final int STORED = 0;
    private static final boolean usemmap = true;
    /* access modifiers changed from: private */
    public volatile boolean closeRequested;
    private final File fileToRemoveOnClose;
    private final CloseGuard guard;
    private Deque<Inflater> inflaterCache;
    /* access modifiers changed from: private */
    public long jzfile;
    private final boolean locsig;
    /* access modifiers changed from: private */
    public final String name;
    /* access modifiers changed from: private */
    public final Map<InputStream, Inflater> streams;
    /* access modifiers changed from: private */
    public final int total;
    private ZipCoder zc;

    private class ZipEntryIterator implements Enumeration<ZipEntry>, Iterator<ZipEntry> {
        private int i = 0;

        public ZipEntryIterator() {
            ZipFile.this.ensureOpen();
        }

        public boolean hasMoreElements() {
            return hasNext();
        }

        public boolean hasNext() {
            boolean z;
            synchronized (ZipFile.this) {
                ZipFile.this.ensureOpen();
                z = this.i < ZipFile.this.total;
            }
            return z;
        }

        public ZipEntry nextElement() {
            return next();
        }

        public ZipEntry next() {
            ZipEntry ze;
            String message;
            synchronized (ZipFile.this) {
                ZipFile.this.ensureOpen();
                if (this.i < ZipFile.this.total) {
                    long access$400 = ZipFile.this.jzfile;
                    int i2 = this.i;
                    this.i = i2 + 1;
                    long jzentry = ZipFile.getNextEntry(access$400, i2);
                    if (jzentry == 0) {
                        if (ZipFile.this.closeRequested) {
                            message = "ZipFile concurrently closed";
                        } else {
                            message = ZipFile.getZipMessage(ZipFile.this.jzfile);
                        }
                        throw new ZipError("jzentry == 0,\n jzfile = " + ZipFile.this.jzfile + ",\n total = " + ZipFile.this.total + ",\n name = " + ZipFile.this.name + ",\n i = " + this.i + ",\n message = " + message);
                    }
                    ze = ZipFile.this.getZipEntry(null, jzentry);
                    ZipFile.freeEntry(ZipFile.this.jzfile, jzentry);
                } else {
                    throw new NoSuchElementException();
                }
            }
            return ze;
        }
    }

    private class ZipFileInflaterInputStream extends InflaterInputStream {
        private volatile boolean closeRequested = false;
        private boolean eof = false;
        private final ZipFileInputStream zfin;

        ZipFileInflaterInputStream(ZipFileInputStream zfin2, Inflater inf, int size) {
            super(zfin2, inf, size);
            this.zfin = zfin2;
        }

        public void close() throws IOException {
            Inflater inf;
            if (!this.closeRequested) {
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

        /* access modifiers changed from: protected */
        public void fill() throws IOException {
            if (!this.eof) {
                this.len = this.in.read(this.buf, 0, this.buf.length);
                if (this.len == -1) {
                    this.buf[0] = 0;
                    this.len = 1;
                    this.eof = true;
                }
                this.inf.setInput(this.buf, 0, this.len);
                return;
            }
            throw new EOFException("Unexpected end of ZLIB input stream");
        }

        public int available() throws IOException {
            if (this.closeRequested) {
                return 0;
            }
            long avail = this.zfin.size() - this.inf.getBytesWritten();
            return avail > 2147483647L ? Integer.MAX_VALUE : (int) avail;
        }

        /* access modifiers changed from: protected */
        public void finalize() throws Throwable {
            close();
        }
    }

    private class ZipFileInputStream extends InputStream {
        protected long jzentry;
        private long pos = 0;
        protected long rem;
        protected long size;
        private volatile boolean zfisCloseRequested = false;

        ZipFileInputStream(long jzentry2) {
            this.rem = ZipFile.getEntryCSize(jzentry2);
            this.size = ZipFile.getEntrySize(jzentry2);
            this.jzentry = jzentry2;
        }

        /* JADX WARNING: Code restructure failed: missing block: B:20:0x0049, code lost:
            if (r1.rem != 0) goto L_0x004e;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:21:0x004b, code lost:
            close();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:22:0x004e, code lost:
            return r2;
         */
        public int read(byte[] b, int off, int len) throws IOException {
            int len2 = len;
            ZipFile.this.ensureOpenOrZipException();
            synchronized (ZipFile.this) {
                long rem2 = this.rem;
                long pos2 = this.pos;
                if (rem2 == 0) {
                    return -1;
                }
                if (len2 <= 0) {
                    return 0;
                }
                if (((long) len2) > rem2) {
                    len2 = (int) rem2;
                }
                int len3 = ZipFile.read(ZipFile.this.jzfile, this.jzentry, pos2, b, off, len2);
                if (len3 > 0) {
                    this.pos = ((long) len3) + pos2;
                    this.rem = rem2 - ((long) len3);
                }
            }
        }

        public int read() throws IOException {
            byte[] b = new byte[1];
            if (read(b, 0, 1) == 1) {
                return b[0] & Character.DIRECTIONALITY_UNDEFINED;
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
            return this.rem > 2147483647L ? Integer.MAX_VALUE : (int) this.rem;
        }

        public long size() {
            return this.size;
        }

        public void close() {
            if (!this.zfisCloseRequested) {
                this.zfisCloseRequested = true;
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

        /* access modifiers changed from: protected */
        public void finalize() {
            close();
        }
    }

    private static native void close(long j);

    /* access modifiers changed from: private */
    public static native void freeEntry(long j, long j2);

    private static native byte[] getCommentBytes(long j);

    private static native long getEntry(long j, byte[] bArr, boolean z);

    private static native byte[] getEntryBytes(long j, int i);

    /* access modifiers changed from: private */
    public static native long getEntryCSize(long j);

    private static native long getEntryCrc(long j);

    private static native int getEntryFlag(long j);

    private static native int getEntryMethod(long j);

    /* access modifiers changed from: private */
    public static native long getEntrySize(long j);

    private static native long getEntryTime(long j);

    private static native int getFileDescriptor(long j);

    /* access modifiers changed from: private */
    public static native long getNextEntry(long j, int i);

    private static native int getTotal(long j);

    /* access modifiers changed from: private */
    public static native String getZipMessage(long j);

    private static native long open(String str, int i, long j, boolean z) throws IOException;

    /* access modifiers changed from: private */
    public static native int read(long j, long j2, long j3, byte[] bArr, int i, int i2);

    private static native boolean startsWithLOC(long j);

    public ZipFile(String name2) throws IOException {
        this(new File(name2), 1);
    }

    public ZipFile(File file, int mode) throws IOException {
        this(file, mode, StandardCharsets.UTF_8);
    }

    public ZipFile(File file) throws ZipException, IOException {
        this(file, 1);
    }

    public ZipFile(File file, int mode, Charset charset) throws IOException {
        this.closeRequested = false;
        this.guard = CloseGuard.get();
        this.streams = new WeakHashMap();
        this.inflaterCache = new ArrayDeque();
        if ((mode & 1) == 0 || (mode & -6) != 0) {
            throw new IllegalArgumentException("Illegal mode: 0x" + Integer.toHexString(mode));
        }
        long length = file.length();
        if (length >= 22) {
            this.fileToRemoveOnClose = (mode & 4) != 0 ? file : null;
            String name2 = file.getPath();
            if (charset != null) {
                this.zc = ZipCoder.get(charset);
                this.jzfile = open(name2, mode, file.lastModified(), usemmap);
                this.name = name2;
                this.total = getTotal(this.jzfile);
                this.locsig = startsWithLOC(this.jzfile);
                Enumeration<? extends ZipEntry> entries = entries();
                this.guard.open("close");
                if (size() == 0 || !entries.hasMoreElements()) {
                    close();
                    throw new ZipException("No entries");
                }
                return;
            }
            throw new NullPointerException("charset is null");
        } else if (length != 0 || file.exists()) {
            throw new ZipException("File too short to be a zip file: " + file.length());
        } else {
            throw new FileNotFoundException("File doesn't exist: " + file);
        }
    }

    public ZipFile(String name2, Charset charset) throws IOException {
        this(new File(name2), 1, charset);
    }

    public ZipFile(File file, Charset charset) throws IOException {
        this(file, 1, charset);
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

    public ZipEntry getEntry(String name2) {
        if (name2 != null) {
            synchronized (this) {
                ensureOpen();
                long jzentry = getEntry(this.jzfile, this.zc.getBytes(name2), true);
                if (jzentry == 0) {
                    return null;
                }
                ZipEntry ze = getZipEntry(name2, jzentry);
                freeEntry(this.jzfile, jzentry);
                return ze;
            }
        }
        throw new NullPointerException("name");
    }

    public InputStream getInputStream(ZipEntry entry) throws IOException {
        long jzentry;
        if (entry != null) {
            synchronized (this) {
                ensureOpen();
                if (this.zc.isUTF8() || (entry.flag & 2048) == 0) {
                    jzentry = getEntry(this.jzfile, this.zc.getBytes(entry.name), true);
                } else {
                    jzentry = getEntry(this.jzfile, this.zc.getBytesUTF8(entry.name), true);
                }
                if (jzentry == 0) {
                    return null;
                }
                ZipFileInputStream in = new ZipFileInputStream(jzentry);
                int entryMethod = getEntryMethod(jzentry);
                if (entryMethod == 0) {
                    synchronized (this.streams) {
                        this.streams.put(in, null);
                    }
                    return in;
                } else if (entryMethod == 8) {
                    long size = getEntrySize(jzentry) + 2;
                    if (size > 65536) {
                        size = 65536;
                    }
                    if (size <= 0) {
                        size = 4096;
                    }
                    Inflater inf = getInflater();
                    InputStream is = new ZipFileInflaterInputStream(in, inf, (int) size);
                    synchronized (this.streams) {
                        this.streams.put(is, inf);
                    }
                    return is;
                } else {
                    throw new ZipException("invalid compression method");
                }
            }
        } else {
            throw new NullPointerException("entry");
        }
    }

    private Inflater getInflater() {
        Inflater inf;
        synchronized (this.inflaterCache) {
            do {
                Inflater poll = this.inflaterCache.poll();
                inf = poll;
                if (poll == null) {
                    return new Inflater(true);
                }
            } while (inf.ended());
            return inf;
        }
    }

    /* access modifiers changed from: private */
    public void releaseInflater(Inflater inf) {
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
        return new ZipEntryIterator();
    }

    public Stream<? extends ZipEntry> stream() {
        return StreamSupport.stream(Spliterators.spliterator(new ZipEntryIterator(), (long) size(), 1297), false);
    }

    /* access modifiers changed from: private */
    public ZipEntry getZipEntry(String name2, long jzentry) {
        ZipEntry e = new ZipEntry();
        e.flag = getEntryFlag(jzentry);
        if (name2 != null) {
            e.name = name2;
        } else {
            byte[] bname = getEntryBytes(jzentry, 0);
            if (this.zc.isUTF8() || (e.flag & 2048) == 0) {
                e.name = this.zc.toString(bname, bname.length);
            } else {
                e.name = this.zc.toStringUTF8(bname, bname.length);
            }
        }
        e.xdostime = getEntryTime(jzentry);
        e.crc = getEntryCrc(jzentry);
        e.size = getEntrySize(jzentry);
        e.csize = getEntryCSize(jzentry);
        e.method = getEntryMethod(jzentry);
        e.setExtra0(getEntryBytes(jzentry, 1), false);
        byte[] bcomm = getEntryBytes(jzentry, 2);
        if (bcomm == null) {
            e.comment = null;
        } else if (this.zc.isUTF8() || (e.flag & 2048) == 0) {
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

    public void close() throws IOException {
        if (!this.closeRequested) {
            this.guard.close();
            this.closeRequested = true;
            synchronized (this) {
                synchronized (this.streams) {
                    if (!this.streams.isEmpty()) {
                        Map<InputStream, Inflater> copy = new HashMap<>((Map<? extends InputStream, ? extends Inflater>) this.streams);
                        this.streams.clear();
                        for (Map.Entry<InputStream, Inflater> e : copy.entrySet()) {
                            e.getKey().close();
                            Inflater inf = e.getValue();
                            if (inf != null) {
                                inf.end();
                            }
                        }
                    }
                }
                synchronized (this.inflaterCache) {
                    while (true) {
                        Inflater poll = this.inflaterCache.poll();
                        Inflater inf2 = poll;
                        if (poll == null) {
                            break;
                        }
                        inf2.end();
                    }
                    while (true) {
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

    /* access modifiers changed from: protected */
    public void finalize() throws IOException {
        if (this.guard != null) {
            this.guard.warnIfOpen();
        }
        close();
    }

    /* access modifiers changed from: private */
    public void ensureOpen() {
        if (this.closeRequested) {
            throw new IllegalStateException("zip file closed");
        } else if (this.jzfile == 0) {
            throw new IllegalStateException("The object is not initialized.");
        }
    }

    /* access modifiers changed from: private */
    public void ensureOpenOrZipException() throws IOException {
        if (this.closeRequested) {
            throw new ZipException("ZipFile closed");
        }
    }

    public boolean startsWithLocHeader() {
        return this.locsig;
    }

    public int getFileDescriptor() {
        return getFileDescriptor(this.jzfile);
    }
}
