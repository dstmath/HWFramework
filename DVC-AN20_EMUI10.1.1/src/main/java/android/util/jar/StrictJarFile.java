package android.util.jar;

import android.os.SystemProperties;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.util.jar.StrictJarVerifier;
import dalvik.system.CloseGuard;
import java.io.FileDescriptor;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.Certificate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipEntry;
import libcore.io.IoBridge;
import libcore.io.IoUtils;
import libcore.io.Streams;

public final class StrictJarFile {
    static final boolean SIGNATURE_PROTECTIONS_ENFORCE = SystemProperties.getBoolean("persist.sys.signature_protections_enforce", false);
    private boolean closed;
    private final FileDescriptor fd;
    private final CloseGuard guard;
    private final boolean isSigned;
    private final StrictJarManifest manifest;
    private final long nativeHandle;
    private final StrictJarVerifier verifier;

    private static native void nativeClose(long j);

    private static native ZipEntry nativeFindEntry(long j, String str);

    /* access modifiers changed from: private */
    public static native ZipEntry nativeNextEntry(long j);

    private static native long nativeOpenJarFile(String str, int i) throws IOException;

    /* access modifiers changed from: private */
    public static native long nativeStartIteration(long j, String str);

    public StrictJarFile(String fileName) throws IOException, SecurityException {
        this(fileName, true, true);
    }

    public StrictJarFile(FileDescriptor fd2) throws IOException, SecurityException {
        this(fd2, true, true);
    }

    public StrictJarFile(FileDescriptor fd2, boolean verify, boolean signatureSchemeRollbackProtectionsEnforced) throws IOException, SecurityException {
        this("[fd:" + fd2.getInt$() + "]", fd2, verify, signatureSchemeRollbackProtectionsEnforced);
    }

    public StrictJarFile(String fileName, boolean verify, boolean signatureSchemeRollbackProtectionsEnforced) throws IOException, SecurityException {
        this(fileName, IoBridge.open(fileName, OsConstants.O_RDONLY), verify, signatureSchemeRollbackProtectionsEnforced);
    }

    private StrictJarFile(String name, FileDescriptor fd2, boolean verify, boolean signatureSchemeRollbackProtectionsEnforced) throws IOException, SecurityException {
        this.guard = CloseGuard.get();
        this.nativeHandle = nativeOpenJarFile(name, fd2.getInt$());
        this.fd = fd2;
        boolean z = false;
        if (verify) {
            try {
                HashMap<String, byte[]> metaEntries = getMetaEntries();
                this.manifest = new StrictJarManifest(metaEntries.get("META-INF/MANIFEST.MF"), true);
                this.verifier = new StrictJarVerifier(name, this.manifest, metaEntries, signatureSchemeRollbackProtectionsEnforced);
                if (signatureSchemeRollbackProtectionsEnforced || SIGNATURE_PROTECTIONS_ENFORCE) {
                    for (String file : this.manifest.getEntries().keySet()) {
                        if (findEntry(file) == null) {
                            throw new SecurityException(name + ": File " + file + " in manifest does not exist");
                        }
                    }
                }
                if (this.verifier.readCertificates() && this.verifier.isSignedJar()) {
                    z = true;
                }
                this.isSigned = z;
            } catch (IOException | SecurityException e) {
                nativeClose(this.nativeHandle);
                IoUtils.closeQuietly(fd2);
                this.closed = true;
                throw e;
            }
        } else {
            this.isSigned = false;
            this.manifest = null;
            this.verifier = null;
        }
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
        StrictJarVerifier.VerifierEntry entry;
        InputStream is = getZipInputStream(ze);
        if (!this.isSigned || (entry = this.verifier.initEntry(ze.getName())) == null) {
            return is;
        }
        return new JarFileInputStream(is, ze.getSize(), entry);
    }

    public void close() throws IOException {
        if (!this.closed) {
            CloseGuard closeGuard = this.guard;
            if (closeGuard != null) {
                closeGuard.close();
            }
            nativeClose(this.nativeHandle);
            IoUtils.closeQuietly(this.fd);
            this.closed = true;
        }
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        try {
            if (this.guard != null) {
                this.guard.warnIfOpen();
            }
            close();
        } finally {
            super.finalize();
        }
    }

    private InputStream getZipInputStream(ZipEntry ze) {
        if (ze.getMethod() == 0) {
            return new FDStream(this.fd, ze.getDataOffset(), ze.getDataOffset() + ze.getSize());
        }
        return new ZipInflaterInputStream(new FDStream(this.fd, ze.getDataOffset(), ze.getDataOffset() + ze.getCompressedSize()), new Inflater(true), Math.max(1024, (int) Math.min(ze.getSize(), 65535L)), ze);
    }

    /* access modifiers changed from: package-private */
    public static final class EntryIterator implements Iterator<ZipEntry> {
        private final long iterationHandle;
        private ZipEntry nextEntry;

        EntryIterator(long nativeHandle, String prefix) throws IOException {
            this.iterationHandle = StrictJarFile.nativeStartIteration(nativeHandle, prefix);
        }

        @Override // java.util.Iterator
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

    private HashMap<String, byte[]> getMetaEntries() throws IOException {
        HashMap<String, byte[]> metaEntries = new HashMap<>();
        Iterator<ZipEntry> entryIterator = new EntryIterator(this.nativeHandle, "META-INF/");
        while (entryIterator.hasNext()) {
            ZipEntry entry = entryIterator.next();
            metaEntries.put(entry.getName(), Streams.readFully(getInputStream(entry)));
        }
        return metaEntries;
    }

    /* access modifiers changed from: package-private */
    public static final class JarFileInputStream extends FilterInputStream {
        private long count;
        private boolean done = false;
        private final StrictJarVerifier.VerifierEntry entry;

        JarFileInputStream(InputStream is, long size, StrictJarVerifier.VerifierEntry e) {
            super(is);
            this.entry = e;
            this.count = size;
        }

        @Override // java.io.FilterInputStream, java.io.InputStream
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

        @Override // java.io.FilterInputStream, java.io.InputStream
        public int read(byte[] buffer, int byteOffset, int byteCount) throws IOException {
            if (this.done) {
                return -1;
            }
            if (this.count > 0) {
                int r = super.read(buffer, byteOffset, byteCount);
                if (r != -1) {
                    int size = r;
                    long j = this.count;
                    if (j < ((long) size)) {
                        size = (int) j;
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

        @Override // java.io.FilterInputStream, java.io.InputStream
        public int available() throws IOException {
            if (this.done) {
                return 0;
            }
            return super.available();
        }

        @Override // java.io.FilterInputStream, java.io.InputStream
        public long skip(long byteCount) throws IOException {
            return Streams.skipByReading(this, byteCount);
        }
    }

    public static class ZipInflaterInputStream extends InflaterInputStream {
        private long bytesRead = 0;
        private boolean closed;
        private final ZipEntry entry;

        public ZipInflaterInputStream(InputStream is, Inflater inf, int bsize, ZipEntry entry2) {
            super(is, inf, bsize);
            this.entry = entry2;
        }

        @Override // java.io.FilterInputStream, java.util.zip.InflaterInputStream, java.io.InputStream
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

        @Override // java.io.FilterInputStream, java.util.zip.InflaterInputStream, java.io.InputStream
        public int available() throws IOException {
            if (!this.closed && super.available() != 0) {
                return (int) (this.entry.getSize() - this.bytesRead);
            }
            return 0;
        }

        @Override // java.io.FilterInputStream, java.util.zip.InflaterInputStream, java.io.Closeable, java.lang.AutoCloseable, java.io.InputStream
        public void close() throws IOException {
            super.close();
            this.closed = true;
        }
    }

    public static class FDStream extends InputStream {
        private long endOffset;
        private final FileDescriptor fd;
        private long offset;

        public FDStream(FileDescriptor fd2, long initialOffset, long endOffset2) {
            this.fd = fd2;
            this.offset = initialOffset;
            this.endOffset = endOffset2;
        }

        @Override // java.io.InputStream
        public int available() throws IOException {
            return this.offset < this.endOffset ? 1 : 0;
        }

        @Override // java.io.InputStream
        public int read() throws IOException {
            return Streams.readSingleByte(this);
        }

        @Override // java.io.InputStream
        public int read(byte[] buffer, int byteOffset, int byteCount) throws IOException {
            synchronized (this.fd) {
                long length = this.endOffset - this.offset;
                if (((long) byteCount) > length) {
                    byteCount = (int) length;
                }
                try {
                    Os.lseek(this.fd, this.offset, OsConstants.SEEK_SET);
                    int count = IoBridge.read(this.fd, buffer, byteOffset, byteCount);
                    if (count <= 0) {
                        return -1;
                    }
                    this.offset += (long) count;
                    return count;
                } catch (ErrnoException e) {
                    throw new IOException(e);
                }
            }
        }

        @Override // java.io.InputStream
        public long skip(long byteCount) throws IOException {
            long j = this.endOffset;
            long j2 = this.offset;
            if (byteCount > j - j2) {
                byteCount = j - j2;
            }
            this.offset += byteCount;
            return byteCount;
        }
    }
}
