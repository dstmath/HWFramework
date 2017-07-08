package android.util.jar;

import dalvik.system.CloseGuard;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.security.cert.Certificate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipEntry;
import javax.microedition.khronos.opengles.GL10;
import libcore.io.IoUtils;
import libcore.io.Streams;

public final class StrictJarFile {
    private boolean closed;
    private final CloseGuard guard;
    private final boolean isSigned;
    private final StrictJarManifest manifest;
    private final long nativeHandle;
    private final RandomAccessFile raf;
    private final StrictJarVerifier verifier;

    static final class EntryIterator implements Iterator<ZipEntry> {
        private final long iterationHandle;
        private ZipEntry nextEntry;

        EntryIterator(long nativeHandle, String prefix) throws IOException {
            this.iterationHandle = StrictJarFile.nativeStartIteration(nativeHandle, prefix);
        }

        public ZipEntry next() {
            if (this.nextEntry == null) {
                return StrictJarFile.nativeNextEntry(this.iterationHandle);
            }
            ZipEntry ze = this.nextEntry;
            this.nextEntry = null;
            return ze;
        }

        public boolean hasNext() {
            if (this.nextEntry != null) {
                return true;
            }
            ZipEntry ze = StrictJarFile.nativeNextEntry(this.iterationHandle);
            if (ze == null) {
                return false;
            }
            this.nextEntry = ze;
            return true;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    static final class JarFileInputStream extends FilterInputStream {
        private long count;
        private boolean done;
        private final VerifierEntry entry;

        JarFileInputStream(InputStream is, long size, VerifierEntry e) {
            super(is);
            this.done = false;
            this.entry = e;
            this.count = size;
        }

        public int read() throws IOException {
            if (this.done) {
                return -1;
            }
            if (this.count > 0) {
                int r = super.read();
                if (r != -1) {
                    this.entry.write(r);
                    this.count--;
                } else {
                    this.count = 0;
                }
                if (this.count == 0) {
                    this.done = true;
                    this.entry.verify();
                }
                return r;
            }
            this.done = true;
            this.entry.verify();
            return -1;
        }

        public int read(byte[] buffer, int byteOffset, int byteCount) throws IOException {
            if (this.done) {
                return -1;
            }
            if (this.count > 0) {
                int r = super.read(buffer, byteOffset, byteCount);
                if (r != -1) {
                    int size = r;
                    if (this.count < ((long) r)) {
                        size = (int) this.count;
                    }
                    this.entry.write(buffer, byteOffset, size);
                    this.count -= (long) size;
                } else {
                    this.count = 0;
                }
                if (this.count == 0) {
                    this.done = true;
                    this.entry.verify();
                }
                return r;
            }
            this.done = true;
            this.entry.verify();
            return -1;
        }

        public int available() throws IOException {
            if (this.done) {
                return 0;
            }
            return super.available();
        }

        public long skip(long byteCount) throws IOException {
            return Streams.skipByReading(this, byteCount);
        }
    }

    public static class RAFStream extends InputStream {
        private long endOffset;
        private long offset;
        private final RandomAccessFile sharedRaf;

        public RAFStream(RandomAccessFile raf, long initialOffset, long endOffset) {
            this.sharedRaf = raf;
            this.offset = initialOffset;
            this.endOffset = endOffset;
        }

        public RAFStream(RandomAccessFile raf, long initialOffset) throws IOException {
            this(raf, initialOffset, raf.length());
        }

        public int available() throws IOException {
            return this.offset < this.endOffset ? 1 : 0;
        }

        public int read() throws IOException {
            return Streams.readSingleByte(this);
        }

        public int read(byte[] buffer, int byteOffset, int byteCount) throws IOException {
            synchronized (this.sharedRaf) {
                long length = this.endOffset - this.offset;
                if (((long) byteCount) > length) {
                    byteCount = (int) length;
                }
                this.sharedRaf.seek(this.offset);
                int count = this.sharedRaf.read(buffer, byteOffset, byteCount);
                if (count > 0) {
                    this.offset += (long) count;
                    return count;
                }
                return -1;
            }
        }

        public long skip(long byteCount) throws IOException {
            if (byteCount > this.endOffset - this.offset) {
                byteCount = this.endOffset - this.offset;
            }
            this.offset += byteCount;
            return byteCount;
        }
    }

    public static class ZipInflaterInputStream extends InflaterInputStream {
        private long bytesRead;
        private final ZipEntry entry;

        public ZipInflaterInputStream(InputStream is, Inflater inf, int bsize, ZipEntry entry) {
            super(is, inf, bsize);
            this.bytesRead = 0;
            this.entry = entry;
        }

        public int read(byte[] buffer, int byteOffset, int byteCount) throws IOException {
            try {
                int i = super.read(buffer, byteOffset, byteCount);
                if (i != -1) {
                    this.bytesRead += (long) i;
                } else if (this.entry.getSize() != this.bytesRead) {
                    throw new IOException("Size mismatch on inflated file: " + this.bytesRead + " vs " + this.entry.getSize());
                }
                return i;
            } catch (IOException e) {
                throw new IOException("Error reading data for " + this.entry.getName() + " near offset " + this.bytesRead, e);
            }
        }

        public int available() throws IOException {
            int i = 0;
            if (this.closed) {
                return 0;
            }
            if (super.available() != 0) {
                i = (int) (this.entry.getSize() - this.bytesRead);
            }
            return i;
        }
    }

    private static native void nativeClose(long j);

    private static native ZipEntry nativeFindEntry(long j, String str);

    private static native ZipEntry nativeNextEntry(long j);

    private static native long nativeOpenJarFile(String str) throws IOException;

    private static native long nativeStartIteration(long j, String str);

    public StrictJarFile(String fileName) throws IOException, SecurityException {
        this(fileName, true, true);
    }

    public StrictJarFile(String fileName, boolean verify, boolean signatureSchemeRollbackProtectionsEnforced) throws IOException, SecurityException {
        this.guard = CloseGuard.get();
        this.nativeHandle = nativeOpenJarFile(fileName);
        this.raf = new RandomAccessFile(fileName, "r");
        if (verify) {
            try {
                boolean isSignedJar;
                HashMap<String, byte[]> metaEntries = getMetaEntries();
                this.manifest = new StrictJarManifest((byte[]) metaEntries.get("META-INF/MANIFEST.MF"), true);
                this.verifier = new StrictJarVerifier(fileName, this.manifest, metaEntries, signatureSchemeRollbackProtectionsEnforced);
                if (signatureSchemeRollbackProtectionsEnforced) {
                    for (String file : this.manifest.getEntries().keySet()) {
                        if (findEntry(file) == null) {
                            throw new SecurityException(fileName + ": File " + file + " in manifest does not exist");
                        }
                    }
                }
                if (this.verifier.readCertificates()) {
                    isSignedJar = this.verifier.isSignedJar();
                } else {
                    isSignedJar = false;
                }
                this.isSigned = isSignedJar;
            } catch (Exception e) {
                nativeClose(this.nativeHandle);
                IoUtils.closeQuietly(this.raf);
                throw e;
            }
        }
        this.isSigned = false;
        this.manifest = null;
        this.verifier = null;
        this.guard.open("close");
    }

    public StrictJarManifest getManifest() {
        return this.manifest;
    }

    public Iterator<ZipEntry> iterator() throws IOException {
        return new EntryIterator(this.nativeHandle, "");
    }

    public ZipEntry findEntry(String name) {
        return nativeFindEntry(this.nativeHandle, name);
    }

    public Certificate[][] getCertificateChains(ZipEntry ze) {
        if (this.isSigned) {
            return this.verifier.getCertificateChains(ze.getName());
        }
        return null;
    }

    @Deprecated
    public Certificate[] getCertificates(ZipEntry ze) {
        if (!this.isSigned) {
            return null;
        }
        Certificate[][] certChains = this.verifier.getCertificateChains(ze.getName());
        int count = 0;
        for (Certificate[] chain : certChains) {
            count += chain.length;
        }
        Certificate[] certs = new Certificate[count];
        int i = 0;
        for (Certificate[] chain2 : certChains) {
            System.arraycopy(chain2, 0, certs, i, chain2.length);
            i += chain2.length;
        }
        return certs;
    }

    public InputStream getInputStream(ZipEntry ze) {
        InputStream is = getZipInputStream(ze);
        if (!this.isSigned) {
            return is;
        }
        VerifierEntry entry = this.verifier.initEntry(ze.getName());
        if (entry == null) {
            return is;
        }
        return new JarFileInputStream(is, ze.getSize(), entry);
    }

    public void close() throws IOException {
        if (!this.closed) {
            this.guard.close();
            nativeClose(this.nativeHandle);
            IoUtils.closeQuietly(this.raf);
            this.closed = true;
        }
    }

    private InputStream getZipInputStream(ZipEntry ze) {
        if (ze.getMethod() == 0) {
            return new RAFStream(this.raf, ze.getDataOffset(), ze.getDataOffset() + ze.getSize());
        }
        return new ZipInflaterInputStream(new RAFStream(this.raf, ze.getDataOffset(), ze.getDataOffset() + ze.getCompressedSize()), new Inflater(true), Math.max(GL10.GL_STENCIL_BUFFER_BIT, (int) Math.min(ze.getSize(), 65535)), ze);
    }

    private HashMap<String, byte[]> getMetaEntries() throws IOException {
        HashMap<String, byte[]> metaEntries = new HashMap();
        Iterator<ZipEntry> entryIterator = new EntryIterator(this.nativeHandle, "META-INF/");
        while (entryIterator.hasNext()) {
            ZipEntry entry = (ZipEntry) entryIterator.next();
            metaEntries.put(entry.getName(), Streams.readFully(getInputStream(entry)));
        }
        return metaEntries;
    }
}
