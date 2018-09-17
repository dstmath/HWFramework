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
import java.util.Map.Entry;
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
            synchronized (ZipFile.this) {
                ZipFile.this.ensureOpen();
                if (this.i >= ZipFile.this.total) {
                    throw new NoSuchElementException();
                }
                long -get1 = ZipFile.this.jzfile;
                int i = this.i;
                this.i = i + 1;
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
    }

    private class ZipFileInflaterInputStream extends InflaterInputStream {
        private volatile boolean closeRequested = false;
        private boolean eof = false;
        private final ZipFileInputStream zfin;

        ZipFileInflaterInputStream(ZipFileInputStream zfin, Inflater inf, int size) {
            super(zfin, inf, size);
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
            this.len = this.in.read(this.buf, 0, this.buf.length);
            if (this.len == -1) {
                this.buf[0] = (byte) 0;
                this.len = 1;
                this.eof = true;
            }
            this.inf.setInput(this.buf, 0, this.len);
        }

        public int available() throws IOException {
            if (this.closeRequested) {
                return 0;
            }
            long avail = this.zfin.size() - this.inf.getBytesWritten();
            return avail > 2147483647L ? Integer.MAX_VALUE : (int) avail;
        }

        protected void finalize() throws Throwable {
            close();
        }
    }

    private class ZipFileInputStream extends InputStream {
        protected long jzentry;
        private long pos = 0;
        protected long rem;
        protected long size;
        private volatile boolean zfisCloseRequested = false;

        ZipFileInputStream(long jzentry) {
            this.rem = ZipFile.getEntryCSize(jzentry);
            this.size = ZipFile.getEntrySize(jzentry);
            this.jzentry = jzentry;
        }

        /* JADX WARNING: Missing block: B:22:0x0041, code:
            if (r12.rem != 0) goto L_0x0046;
     */
        /* JADX WARNING: Missing block: B:23:0x0043, code:
            close();
     */
        /* JADX WARNING: Missing block: B:24:0x0046, code:
            return r15;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public int read(byte[] b, int off, int len) throws IOException {
            ZipFile.this.ensureOpenOrZipException();
            synchronized (ZipFile.this) {
                long rem = this.rem;
                long pos = this.pos;
                if (rem == 0) {
                    return -1;
                } else if (len <= 0) {
                    return 0;
                } else {
                    if (((long) len) > rem) {
                        len = (int) rem;
                    }
                    len = ZipFile.read(ZipFile.this.jzfile, this.jzentry, pos, b, off, len);
                    if (len > 0) {
                        this.pos = ((long) len) + pos;
                        this.rem = rem - ((long) len);
                    }
                }
            }
        }

        public int read() throws IOException {
            byte[] b = new byte[1];
            if (read(b, 0, 1) == 1) {
                return b[0] & 255;
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

        protected void finalize() {
            close();
        }
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

    private static native int getFileDescriptor(long j);

    private static native long getNextEntry(long j, int i);

    private static native int getTotal(long j);

    private static native String getZipMessage(long j);

    private static native long open(String str, int i, long j, boolean z) throws IOException;

    private static native int read(long j, long j2, long j3, byte[] bArr, int i, int i2);

    private static native boolean startsWithLOC(long j);

    public ZipFile(String name) throws IOException {
        this(new File(name), 1);
    }

    public ZipFile(File file, int mode) throws IOException {
        this(file, mode, StandardCharsets.UTF_8);
    }

    public ZipFile(File file) throws ZipException, IOException {
        this(file, 1);
    }

    public ZipFile(File file, int mode, Charset charset) throws IOException {
        File file2 = null;
        this.closeRequested = false;
        this.guard = CloseGuard.get();
        this.streams = new WeakHashMap();
        this.inflaterCache = new ArrayDeque();
        if ((mode & 1) == 0 || (mode & -6) != 0) {
            throw new IllegalArgumentException("Illegal mode: 0x" + Integer.toHexString(mode));
        }
        long length = file.length();
        if (length >= 22) {
            if ((mode & 4) != 0) {
                file2 = file;
            }
            this.fileToRemoveOnClose = file2;
            String name = file.getPath();
            if (charset == null) {
                throw new NullPointerException("charset is null");
            }
            this.zc = ZipCoder.get(charset);
            this.jzfile = open(name, mode, file.lastModified(), usemmap);
            this.name = name;
            this.total = getTotal(this.jzfile);
            this.locsig = startsWithLOC(this.jzfile);
            Enumeration<? extends ZipEntry> entries = -wrap1();
            if (size() == 0 || (entries.hasMoreElements() ^ 1) != 0) {
                close();
                throw new ZipException("No entries");
            } else {
                this.guard.open("close");
            }
        } else if (length != 0 || (file.exists() ^ 1) == 0) {
            throw new ZipException("File too short to be a zip file: " + file.length());
        } else {
            throw new FileNotFoundException("File doesn't exist: " + file);
        }
    }

    public ZipFile(String name, Charset charset) throws IOException {
        this(new File(name), 1, charset);
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
        Throwable th;
        ZipFileInputStream zipFileInputStream;
        if (entry == null) {
            throw new NullPointerException("entry");
        }
        synchronized (this) {
            try {
                long jzentry;
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
                try {
                    switch (getEntryMethod(jzentry)) {
                        case 0:
                            synchronized (this.streams) {
                                this.streams.put(in, null);
                            }
                            return in;
                        case 8:
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
                            }
                            return is;
                        default:
                            throw new ZipException("invalid compression method");
                    }
                } catch (Throwable th2) {
                    th = th2;
                    zipFileInputStream = in;
                    throw th;
                }
                th = th2;
                zipFileInputStream = in;
            } catch (Throwable th3) {
                th = th3;
                throw th;
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

    /* renamed from: entries */
    public Enumeration<? extends ZipEntry> -wrap1() {
        return new ZipEntryIterator();
    }

    public Stream<? extends ZipEntry> stream() {
        return StreamSupport.stream(Spliterators.spliterator(new ZipEntryIterator(), (long) size(), 1297), false);
    }

    private ZipEntry getZipEntry(String name, long jzentry) {
        ZipEntry e = new ZipEntry();
        e.flag = getEntryFlag(jzentry);
        if (name != null) {
            e.name = name;
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
                        if (inf != null) {
                            inf.end();
                        }
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

    public int getFileDescriptor() {
        return getFileDescriptor(this.jzfile);
    }
}
