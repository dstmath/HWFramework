package sun.nio.ch;

import android.system.ErrnoException;
import android.system.OsConstants;
import dalvik.system.BlockGuard;
import dalvik.system.CloseGuard;
import java.io.Closeable;
import java.io.FileDescriptor;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.DirectByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.FileLockInterruptionException;
import java.nio.channels.NonReadableChannelException;
import java.nio.channels.NonWritableChannelException;
import java.nio.channels.OverlappingFileLockException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.WritableByteChannel;
import java.security.AccessController;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import libcore.io.Libcore;
import sun.misc.Cleaner;
import sun.security.action.GetPropertyAction;

public class FileChannelImpl extends FileChannel {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static final long MAPPED_TRANSFER_SIZE = 8388608;
    private static final int MAP_PV = 2;
    private static final int MAP_RO = 0;
    private static final int MAP_RW = 1;
    private static final int TRANSFER_SIZE = 8192;
    private static final long allocationGranularity = initIDs();
    private static volatile boolean fileSupported = true;
    private static boolean isSharedFileLockTable;
    private static volatile boolean pipeSupported = true;
    private static volatile boolean propertyChecked;
    private static volatile boolean transferSupported = true;
    private final boolean append;
    public final FileDescriptor fd;
    private volatile FileLockTable fileLockTable;
    private final CloseGuard guard = CloseGuard.get();
    private final FileDispatcher nd;
    private final Object parent;
    private final String path;
    private final Object positionLock = new Object();
    private final boolean readable;
    private final NativeThreadSet threads = new NativeThreadSet(2);
    private final boolean writable;

    private static class SimpleFileLockTable extends FileLockTable {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        private final List<FileLock> lockList = new ArrayList(2);

        static {
            Class<FileChannelImpl> cls = FileChannelImpl.class;
        }

        private void checkList(long position, long size) throws OverlappingFileLockException {
            for (FileLock fl : this.lockList) {
                if (fl.overlaps(position, size)) {
                    throw new OverlappingFileLockException();
                }
            }
        }

        public void add(FileLock fl) throws OverlappingFileLockException {
            synchronized (this.lockList) {
                checkList(fl.position(), fl.size());
                this.lockList.add(fl);
            }
        }

        public void remove(FileLock fl) {
            synchronized (this.lockList) {
                this.lockList.remove((Object) fl);
            }
        }

        public List<FileLock> removeAll() {
            List<FileLock> result;
            synchronized (this.lockList) {
                result = new ArrayList<>((Collection<? extends FileLock>) this.lockList);
                this.lockList.clear();
            }
            return result;
        }

        public void replace(FileLock fl1, FileLock fl2) {
            synchronized (this.lockList) {
                this.lockList.remove((Object) fl1);
                this.lockList.add(fl2);
            }
        }
    }

    private static class Unmapper implements Runnable {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        static volatile int count;
        private static final NativeDispatcher nd = new FileDispatcherImpl();
        static volatile long totalCapacity;
        static volatile long totalSize;
        private volatile long address;
        private final int cap;
        private final FileDescriptor fd;
        private final long size;

        static {
            Class<FileChannelImpl> cls = FileChannelImpl.class;
        }

        private Unmapper(long address2, long size2, int cap2, FileDescriptor fd2) {
            this.address = address2;
            this.size = size2;
            this.cap = cap2;
            this.fd = fd2;
            synchronized (Unmapper.class) {
                count++;
                totalSize += size2;
                totalCapacity += (long) cap2;
            }
        }

        public void run() {
            if (this.address != 0) {
                int unused = FileChannelImpl.unmap0(this.address, this.size);
                this.address = 0;
                if (this.fd.valid()) {
                    try {
                        nd.close(this.fd);
                    } catch (IOException e) {
                    }
                }
                synchronized (Unmapper.class) {
                    count--;
                    totalSize -= this.size;
                    totalCapacity -= (long) this.cap;
                }
            }
        }
    }

    private static native long initIDs();

    private native long map0(int i, long j, long j2) throws IOException;

    private native long position0(FileDescriptor fileDescriptor, long j);

    private native long transferTo0(FileDescriptor fileDescriptor, long j, long j2, FileDescriptor fileDescriptor2);

    /* access modifiers changed from: private */
    public static native int unmap0(long j, long j2);

    private FileChannelImpl(FileDescriptor fd2, String path2, boolean readable2, boolean writable2, boolean append2, Object parent2) {
        this.fd = fd2;
        this.readable = readable2;
        this.writable = writable2;
        this.append = append2;
        this.parent = parent2;
        this.path = path2;
        this.nd = new FileDispatcherImpl(append2);
        if (fd2 != null && fd2.valid()) {
            this.guard.open("close");
        }
    }

    public static FileChannel open(FileDescriptor fd2, String path2, boolean readable2, boolean writable2, Object parent2) {
        FileChannelImpl fileChannelImpl = new FileChannelImpl(fd2, path2, readable2, writable2, $assertionsDisabled, parent2);
        return fileChannelImpl;
    }

    public static FileChannel open(FileDescriptor fd2, String path2, boolean readable2, boolean writable2, boolean append2, Object parent2) {
        FileChannelImpl fileChannelImpl = new FileChannelImpl(fd2, path2, readable2, writable2, append2, parent2);
        return fileChannelImpl;
    }

    private void ensureOpen() throws IOException {
        if (!isOpen()) {
            throw new ClosedChannelException();
        }
    }

    /* access modifiers changed from: protected */
    public void implCloseChannel() throws IOException {
        this.guard.close();
        if (this.fileLockTable != null) {
            for (FileLock fl : this.fileLockTable.removeAll()) {
                synchronized (fl) {
                    if (fl.isValid()) {
                        this.nd.release(this.fd, fl.position(), fl.size());
                        ((FileLockImpl) fl).invalidate();
                    }
                }
            }
        }
        this.threads.signalAndWait();
        if (this.parent != null) {
            ((Closeable) this.parent).close();
        } else {
            this.nd.close(this.fd);
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

    public int read(ByteBuffer dst) throws IOException {
        ensureOpen();
        if (this.readable) {
            synchronized (this.positionLock) {
                int n = 0;
                int ti = -1;
                boolean z = true;
                try {
                    begin();
                    ti = this.threads.add();
                    if (!isOpen()) {
                        this.threads.remove(ti);
                        if (0 <= 0) {
                            z = false;
                        }
                        end(z);
                        return 0;
                    }
                    do {
                        n = IOUtil.read(this.fd, dst, -1, this.nd);
                        if (n != -3) {
                            break;
                        }
                    } while (isOpen());
                    int normalize = IOStatus.normalize(n);
                    this.threads.remove(ti);
                    if (n <= 0) {
                        z = false;
                    }
                    end(z);
                    return normalize;
                } catch (Throwable th) {
                    this.threads.remove(ti);
                    if (n <= 0) {
                        z = false;
                    }
                    end(z);
                    throw th;
                }
            }
        } else {
            throw new NonReadableChannelException();
        }
    }

    public long read(ByteBuffer[] dsts, int offset, int length) throws IOException {
        if (offset < 0 || length < 0 || offset > dsts.length - length) {
            throw new IndexOutOfBoundsException();
        }
        ensureOpen();
        if (this.readable) {
            synchronized (this.positionLock) {
                long n = 0;
                int ti = -1;
                boolean z = $assertionsDisabled;
                try {
                    begin();
                    ti = this.threads.add();
                    if (!isOpen()) {
                        this.threads.remove(ti);
                        if (0 > 0) {
                            z = true;
                        }
                        end(z);
                        return 0;
                    }
                    do {
                        n = IOUtil.read(this.fd, dsts, offset, length, this.nd);
                        if (n != -3) {
                            break;
                        }
                    } while (isOpen());
                    long normalize = IOStatus.normalize(n);
                    this.threads.remove(ti);
                    if (n > 0) {
                        z = true;
                    }
                    end(z);
                    return normalize;
                } catch (Throwable th) {
                    this.threads.remove(ti);
                    if (n > 0) {
                        z = true;
                    }
                    end(z);
                    throw th;
                }
            }
        } else {
            throw new NonReadableChannelException();
        }
    }

    public int write(ByteBuffer src) throws IOException {
        ensureOpen();
        if (this.writable) {
            synchronized (this.positionLock) {
                int n = 0;
                int ti = -1;
                boolean z = true;
                try {
                    begin();
                    ti = this.threads.add();
                    if (!isOpen()) {
                        this.threads.remove(ti);
                        if (0 <= 0) {
                            z = false;
                        }
                        end(z);
                        return 0;
                    }
                    do {
                        n = IOUtil.write(this.fd, src, -1, this.nd);
                        if (n != -3) {
                            break;
                        }
                    } while (isOpen());
                    int normalize = IOStatus.normalize(n);
                    this.threads.remove(ti);
                    if (n <= 0) {
                        z = false;
                    }
                    end(z);
                    return normalize;
                } catch (Throwable th) {
                    this.threads.remove(ti);
                    if (n <= 0) {
                        z = false;
                    }
                    end(z);
                    throw th;
                }
            }
        } else {
            throw new NonWritableChannelException();
        }
    }

    public long write(ByteBuffer[] srcs, int offset, int length) throws IOException {
        if (offset < 0 || length < 0 || offset > srcs.length - length) {
            throw new IndexOutOfBoundsException();
        }
        ensureOpen();
        if (this.writable) {
            synchronized (this.positionLock) {
                long n = 0;
                int ti = -1;
                boolean z = $assertionsDisabled;
                try {
                    begin();
                    ti = this.threads.add();
                    if (!isOpen()) {
                        this.threads.remove(ti);
                        if (0 > 0) {
                            z = true;
                        }
                        end(z);
                        return 0;
                    }
                    do {
                        n = IOUtil.write(this.fd, srcs, offset, length, this.nd);
                        if (n != -3) {
                            break;
                        }
                    } while (isOpen());
                    long normalize = IOStatus.normalize(n);
                    this.threads.remove(ti);
                    if (n > 0) {
                        z = true;
                    }
                    end(z);
                    return normalize;
                } catch (Throwable th) {
                    this.threads.remove(ti);
                    if (n > 0) {
                        z = true;
                    }
                    end(z);
                    throw th;
                }
            }
        } else {
            throw new NonWritableChannelException();
        }
    }

    public long position() throws IOException {
        long p;
        ensureOpen();
        synchronized (this.positionLock) {
            int ti = -1;
            boolean z = $assertionsDisabled;
            try {
                begin();
                ti = this.threads.add();
                if (!isOpen()) {
                    this.threads.remove(ti);
                    if (-1 > -1) {
                        z = true;
                    }
                    end(z);
                    return 0;
                }
                if (this.append) {
                    BlockGuard.getThreadPolicy().onWriteToDisk();
                }
                do {
                    p = this.append ? this.nd.size(this.fd) : position0(this.fd, -1);
                    if (p != -3) {
                        break;
                    }
                } while (isOpen());
                long normalize = IOStatus.normalize(p);
                this.threads.remove(ti);
                if (p > -1) {
                    z = true;
                }
                end(z);
                return normalize;
            } catch (Throwable th) {
                this.threads.remove(ti);
                if (-1 > -1) {
                    z = true;
                }
                end(z);
                throw th;
            }
        }
    }

    public FileChannel position(long newPosition) throws IOException {
        long p;
        ensureOpen();
        if (newPosition >= 0) {
            synchronized (this.positionLock) {
                int ti = -1;
                boolean z = $assertionsDisabled;
                try {
                    begin();
                    ti = this.threads.add();
                    if (!isOpen()) {
                        this.threads.remove(ti);
                        if (-1 > -1) {
                            z = true;
                        }
                        end(z);
                        return null;
                    }
                    BlockGuard.getThreadPolicy().onReadFromDisk();
                    do {
                        p = position0(this.fd, newPosition);
                        if (p == -3) {
                        }
                        break;
                    } while (isOpen());
                    break;
                    this.threads.remove(ti);
                    if (p > -1) {
                        z = true;
                    }
                    end(z);
                    return this;
                } catch (Throwable th) {
                    this.threads.remove(ti);
                    if (-1 > -1) {
                        z = true;
                    }
                    end(z);
                    throw th;
                }
            }
        } else {
            throw new IllegalArgumentException();
        }
    }

    public long size() throws IOException {
        ensureOpen();
        synchronized (this.positionLock) {
            long s = -1;
            int ti = -1;
            boolean z = $assertionsDisabled;
            try {
                begin();
                ti = this.threads.add();
                if (!isOpen()) {
                    this.threads.remove(ti);
                    if (-1 > -1) {
                        z = true;
                    }
                    end(z);
                    return -1;
                }
                do {
                    s = this.nd.size(this.fd);
                    if (s != -3) {
                        break;
                    }
                } while (isOpen());
                long normalize = IOStatus.normalize(s);
                this.threads.remove(ti);
                if (s > -1) {
                    z = true;
                }
                end(z);
                return normalize;
            } catch (Throwable th) {
                this.threads.remove(ti);
                if (s > -1) {
                    z = true;
                }
                end(z);
                throw th;
            }
        }
    }

    public FileChannel truncate(long newSize) throws IOException {
        long size;
        long p;
        long j = newSize;
        ensureOpen();
        if (j < 0) {
            throw new IllegalArgumentException("Negative size");
        } else if (this.writable) {
            synchronized (this.positionLock) {
                int rv = -1;
                int ti = -1;
                begin();
                ti = this.threads.add();
                if (!isOpen()) {
                    this.threads.remove(ti);
                    end(-1 > -1 ? true : $assertionsDisabled);
                    return null;
                }
                do {
                    try {
                        size = this.nd.size(this.fd);
                        if (size != -3) {
                            break;
                        }
                    } catch (Throwable th) {
                        this.threads.remove(ti);
                        end(rv > -1 ? true : $assertionsDisabled);
                        throw th;
                    }
                } while (isOpen());
                if (!isOpen()) {
                    this.threads.remove(ti);
                    end(-1 > -1 ? true : $assertionsDisabled);
                    return null;
                }
                do {
                    p = position0(this.fd, -1);
                    if (p != -3) {
                        break;
                    }
                } while (isOpen());
                if (!isOpen()) {
                    this.threads.remove(ti);
                    end(-1 > -1 ? true : $assertionsDisabled);
                    return null;
                }
                if (j < size) {
                    do {
                        rv = this.nd.truncate(this.fd, j);
                        if (rv != -3) {
                            break;
                        }
                    } while (isOpen());
                    if (!isOpen()) {
                        this.threads.remove(ti);
                        end(rv > -1 ? true : $assertionsDisabled);
                        return null;
                    }
                }
                if (p > j) {
                    p = j;
                }
                do {
                    rv = (int) position0(this.fd, p);
                    if (rv == -3) {
                    }
                    break;
                } while (isOpen());
                break;
                this.threads.remove(ti);
                end(rv > -1 ? true : $assertionsDisabled);
                return this;
            }
        } else {
            throw new NonWritableChannelException();
        }
    }

    public void force(boolean metaData) throws IOException {
        ensureOpen();
        int rv = -1;
        int ti = -1;
        boolean z = $assertionsDisabled;
        try {
            begin();
            ti = this.threads.add();
            if (isOpen()) {
                do {
                    rv = this.nd.force(this.fd, metaData);
                    if (rv != -3) {
                        break;
                    }
                } while (isOpen());
                this.threads.remove(ti);
                if (rv > -1) {
                    z = true;
                }
                end(z);
            }
        } finally {
            this.threads.remove(ti);
            if (rv > -1) {
                z = true;
            }
            end(z);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:58:0x00ae  */
    private long transferToDirectlyInternal(long position, int icount, WritableByteChannel target, FileDescriptor targetFD) throws IOException {
        int ti;
        WritableByteChannel writableByteChannel = target;
        long n = -1;
        boolean z = true;
        try {
            begin();
            ti = this.threads.add();
            try {
                if (!isOpen()) {
                    this.threads.remove(ti);
                    if (-1 <= -1) {
                        z = false;
                    }
                    end(z);
                    return -1;
                }
                BlockGuard.getThreadPolicy().onWriteToDisk();
                do {
                    try {
                        n = transferTo0(this.fd, position, (long) icount, targetFD);
                        if (n != -3) {
                            break;
                        }
                    } catch (Throwable th) {
                        th = th;
                        n = n;
                        this.threads.remove(ti);
                        if (n <= -1) {
                        }
                        end(z);
                        throw th;
                    }
                } while (isOpen());
                if (n == -6) {
                    if (writableByteChannel instanceof SinkChannelImpl) {
                        pipeSupported = $assertionsDisabled;
                    }
                    if (writableByteChannel instanceof FileChannelImpl) {
                        fileSupported = $assertionsDisabled;
                    }
                    this.threads.remove(ti);
                    if (n <= -1) {
                        z = false;
                    }
                    end(z);
                    return -6;
                } else if (n == -4) {
                    transferSupported = $assertionsDisabled;
                    this.threads.remove(ti);
                    if (n <= -1) {
                        z = false;
                    }
                    end(z);
                    return -4;
                } else {
                    long normalize = IOStatus.normalize(n);
                    this.threads.remove(ti);
                    if (n <= -1) {
                        z = false;
                    }
                    end(z);
                    return normalize;
                }
            } catch (Throwable th2) {
                th = th2;
                this.threads.remove(ti);
                if (n <= -1) {
                    z = false;
                }
                end(z);
                throw th;
            }
        } catch (Throwable th3) {
            th = th3;
            ti = -1;
            this.threads.remove(ti);
            if (n <= -1) {
            }
            end(z);
            throw th;
        }
    }

    private long transferToDirectly(long position, int icount, WritableByteChannel target) throws IOException {
        long transferToDirectlyInternal;
        WritableByteChannel writableByteChannel = target;
        if (!transferSupported) {
            return -4;
        }
        FileDescriptor targetFD = null;
        if (writableByteChannel instanceof FileChannelImpl) {
            if (!fileSupported) {
                return -6;
            }
            targetFD = ((FileChannelImpl) writableByteChannel).fd;
        } else if (writableByteChannel instanceof SelChImpl) {
            if ((writableByteChannel instanceof SinkChannelImpl) && !pipeSupported) {
                return -6;
            }
            if (!this.nd.canTransferToDirectly((SelectableChannel) writableByteChannel)) {
                return -6;
            }
            targetFD = ((SelChImpl) writableByteChannel).getFD();
        }
        FileDescriptor targetFD2 = targetFD;
        if (targetFD2 == null || IOUtil.fdVal(this.fd) == IOUtil.fdVal(targetFD2)) {
            return -4;
        }
        if (!this.nd.transferToDirectlyNeedsPositionLock()) {
            return transferToDirectlyInternal(position, icount, writableByteChannel, targetFD2);
        }
        synchronized (this.positionLock) {
            long pos = position();
            try {
                transferToDirectlyInternal = transferToDirectlyInternal(position, icount, writableByteChannel, targetFD2);
                position(pos);
            } catch (Throwable th) {
                position(pos);
                throw th;
            }
        }
        return transferToDirectlyInternal;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:?, code lost:
        unmap(r1);
     */
    private long transferToTrustedChannel(long position, long count, WritableByteChannel target) throws IOException {
        MappedByteBuffer dbb;
        boolean isSelChImpl = target instanceof SelChImpl;
        if (!(target instanceof FileChannelImpl) && !isSelChImpl) {
            return -4;
        }
        long position2 = position;
        long remaining = count;
        while (true) {
            if (remaining <= 0) {
                break;
            }
            try {
                dbb = map(FileChannel.MapMode.READ_ONLY, position2, Math.min(remaining, (long) MAPPED_TRANSFER_SIZE));
                int n = target.write(dbb);
                remaining -= (long) n;
                if (isSelChImpl) {
                    break;
                }
                position2 += (long) n;
                unmap(dbb);
            } catch (ClosedByInterruptException e) {
                try {
                    close();
                } catch (Throwable suppressed) {
                    e.addSuppressed(suppressed);
                }
                throw e;
            } catch (IOException ioe) {
                if (remaining == count) {
                    throw ioe;
                }
            } catch (Throwable th) {
                unmap(dbb);
                throw th;
            }
        }
        return count - remaining;
    }

    private long transferToArbitraryChannel(long position, int icount, WritableByteChannel target) throws IOException {
        ByteBuffer bb = Util.getTemporaryDirectBuffer(Math.min(icount, 8192));
        long tw = 0;
        long pos = position;
        try {
            Util.erase(bb);
            while (true) {
                if (tw >= ((long) icount)) {
                    break;
                }
                bb.limit(Math.min((int) (((long) icount) - tw), 8192));
                int nr = read(bb, pos);
                if (nr <= 0) {
                    break;
                }
                bb.flip();
                int nw = target.write(bb);
                tw += (long) nw;
                if (nw != nr) {
                    break;
                }
                pos += (long) nw;
                bb.clear();
            }
            return tw;
        } catch (IOException x) {
            if (0 > 0) {
                return 0;
            }
            throw x;
        } finally {
            Util.releaseTemporaryDirectBuffer(bb);
        }
    }

    public long transferTo(long position, long count, WritableByteChannel target) throws IOException {
        long j = position;
        long j2 = count;
        WritableByteChannel writableByteChannel = target;
        ensureOpen();
        if (!target.isOpen()) {
            throw new ClosedChannelException();
        } else if (!this.readable) {
            throw new NonReadableChannelException();
        } else if ((writableByteChannel instanceof FileChannelImpl) && !((FileChannelImpl) writableByteChannel).writable) {
            throw new NonWritableChannelException();
        } else if (j < 0 || j2 < 0) {
            throw new IllegalArgumentException();
        } else {
            long sz = size();
            if (j > sz) {
                return 0;
            }
            int icount = (int) Math.min(j2, 2147483647L);
            if (sz - j < ((long) icount)) {
                icount = (int) (sz - j);
            }
            int icount2 = icount;
            long transferToDirectly = transferToDirectly(j, icount2, writableByteChannel);
            long n = transferToDirectly;
            if (transferToDirectly >= 0) {
                return n;
            }
            int icount3 = icount2;
            long transferToTrustedChannel = transferToTrustedChannel(j, (long) icount2, writableByteChannel);
            long n2 = transferToTrustedChannel;
            if (transferToTrustedChannel >= 0) {
                return n2;
            }
            return transferToArbitraryChannel(j, icount3, writableByteChannel);
        }
    }

    private long transferFromFileChannel(FileChannelImpl src, long position, long count) throws IOException {
        long remaining;
        MappedByteBuffer bb;
        FileChannelImpl fileChannelImpl = src;
        if (fileChannelImpl.readable) {
            synchronized (fileChannelImpl.positionLock) {
                try {
                    long pos = src.position();
                    long remaining2 = Math.min(count, src.size() - pos);
                    long max = remaining2;
                    long position2 = position;
                    long n = remaining2;
                    long remaining3 = pos;
                    while (true) {
                        long p = remaining3;
                        if (n <= 0) {
                            break;
                        }
                        try {
                            long position3 = position2;
                            try {
                                remaining = n;
                                bb = fileChannelImpl.map(FileChannel.MapMode.READ_ONLY, p, Math.min(n, (long) MAPPED_TRANSFER_SIZE));
                            } catch (Throwable th) {
                                th = th;
                                throw th;
                            }
                            try {
                                long n2 = (long) write(bb, position3);
                                long p2 = p + n2;
                                long position4 = position3 + n2;
                                n = remaining - n2;
                                unmap(bb);
                                position2 = position4;
                                remaining3 = p2;
                                long position5 = count;
                            } catch (IOException ioe) {
                                IOException iOException = ioe;
                                if (remaining != max) {
                                    unmap(bb);
                                } else {
                                    throw ioe;
                                }
                            } catch (Throwable th2) {
                                th = th2;
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            long j = position2;
                            throw th;
                        }
                    }
                    remaining = n;
                    long j2 = position2;
                    long nwritten = max - remaining;
                    fileChannelImpl.position(pos + nwritten);
                    return nwritten;
                } catch (Throwable th4) {
                    th = th4;
                    long j3 = position;
                    throw th;
                }
            }
        } else {
            throw new NonReadableChannelException();
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:32:0x0065  */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x006a A[SYNTHETIC, Splitter:B:34:0x006a] */
    private long transferFromArbitraryChannel(ReadableByteChannel src, long position, long count) throws IOException {
        long j = count;
        ByteBuffer bb = Util.getTemporaryDirectBuffer((int) Math.min(j, 8192));
        long tw = 0;
        long pos = position;
        try {
            Util.erase(bb);
            while (true) {
                if (tw >= j) {
                    ReadableByteChannel readableByteChannel = src;
                    break;
                }
                bb.limit((int) Math.min(j - tw, 8192));
                try {
                    int nr = src.read(bb);
                    if (nr <= 0) {
                        break;
                    }
                    bb.flip();
                    try {
                        int nw = write(bb, pos);
                        tw += (long) nw;
                        if (nw != nr) {
                            break;
                        }
                        pos += (long) nw;
                        bb.clear();
                    } catch (IOException e) {
                        x = e;
                        if (tw > 0) {
                            Util.releaseTemporaryDirectBuffer(bb);
                            return tw;
                        }
                        try {
                            throw x;
                        } catch (Throwable th) {
                            x = th;
                        }
                    }
                } catch (IOException e2) {
                    x = e2;
                    if (tw > 0) {
                    }
                } catch (Throwable th2) {
                    x = th2;
                    Util.releaseTemporaryDirectBuffer(bb);
                    throw x;
                }
            }
            Util.releaseTemporaryDirectBuffer(bb);
            return tw;
        } catch (IOException e3) {
            x = e3;
            ReadableByteChannel readableByteChannel2 = src;
            if (tw > 0) {
            }
        } catch (Throwable th3) {
            x = th3;
            ReadableByteChannel readableByteChannel3 = src;
            Util.releaseTemporaryDirectBuffer(bb);
            throw x;
        }
    }

    public long transferFrom(ReadableByteChannel src, long position, long count) throws IOException {
        ensureOpen();
        if (!src.isOpen()) {
            throw new ClosedChannelException();
        } else if (!this.writable) {
            throw new NonWritableChannelException();
        } else if (position < 0 || count < 0) {
            throw new IllegalArgumentException();
        } else if (position > size()) {
            return 0;
        } else {
            if (!(src instanceof FileChannelImpl)) {
                return transferFromArbitraryChannel(src, position, count);
            }
            return transferFromFileChannel((FileChannelImpl) src, position, count);
        }
    }

    public int read(ByteBuffer dst, long position) throws IOException {
        int readInternal;
        if (dst == null) {
            throw new NullPointerException();
        } else if (position < 0) {
            throw new IllegalArgumentException("Negative position");
        } else if (this.readable) {
            ensureOpen();
            if (!this.nd.needsPositionLock()) {
                return readInternal(dst, position);
            }
            synchronized (this.positionLock) {
                readInternal = readInternal(dst, position);
            }
            return readInternal;
        } else {
            throw new NonReadableChannelException();
        }
    }

    private int readInternal(ByteBuffer dst, long position) throws IOException {
        int n = 0;
        int ti = -1;
        boolean z = $assertionsDisabled;
        try {
            begin();
            ti = this.threads.add();
            if (!isOpen()) {
                return -1;
            }
            do {
                n = IOUtil.read(this.fd, dst, position, this.nd);
                if (n != -3) {
                    break;
                }
            } while (isOpen());
            int normalize = IOStatus.normalize(n);
            this.threads.remove(ti);
            if (n > 0) {
                z = true;
            }
            end(z);
            return normalize;
        } finally {
            this.threads.remove(ti);
            if (n > 0) {
                z = true;
            }
            end(z);
        }
    }

    public int write(ByteBuffer src, long position) throws IOException {
        int writeInternal;
        if (src == null) {
            throw new NullPointerException();
        } else if (position < 0) {
            throw new IllegalArgumentException("Negative position");
        } else if (this.writable) {
            ensureOpen();
            if (!this.nd.needsPositionLock()) {
                return writeInternal(src, position);
            }
            synchronized (this.positionLock) {
                writeInternal = writeInternal(src, position);
            }
            return writeInternal;
        } else {
            throw new NonWritableChannelException();
        }
    }

    private int writeInternal(ByteBuffer src, long position) throws IOException {
        int n = 0;
        int ti = -1;
        boolean z = $assertionsDisabled;
        try {
            begin();
            ti = this.threads.add();
            if (!isOpen()) {
                return -1;
            }
            do {
                n = IOUtil.write(this.fd, src, position, this.nd);
                if (n != -3) {
                    break;
                }
            } while (isOpen());
            int normalize = IOStatus.normalize(n);
            this.threads.remove(ti);
            if (n > 0) {
                z = true;
            }
            end(z);
            return normalize;
        } finally {
            this.threads.remove(ti);
            if (n > 0) {
                z = true;
            }
            end(z);
        }
    }

    private static void unmap(MappedByteBuffer bb) {
        Cleaner cl = ((DirectBuffer) bb).cleaner();
        if (cl != null) {
            cl.clean();
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:123:0x018f A[Catch:{ IOException -> 0x01b0, all -> 0x01ad }] */
    /* JADX WARNING: Removed duplicated region for block: B:77:0x00dd  */
    /* JADX WARNING: Removed duplicated region for block: B:81:0x00f3  */
    /* JADX WARNING: Removed duplicated region for block: B:92:0x0122 A[SYNTHETIC, Splitter:B:92:0x0122] */
    public MappedByteBuffer map(FileChannel.MapMode mode, long position, long size) throws IOException {
        int ti;
        long filesize;
        long mapSize;
        int pagePosition;
        long addr;
        long addr2;
        boolean z;
        boolean z2;
        int rv;
        FileChannel.MapMode mapMode = mode;
        long j = size;
        ensureOpen();
        if (mapMode == null) {
            throw new NullPointerException("Mode is null");
        } else if (position < 0) {
            throw new IllegalArgumentException("Negative position");
        } else if (j < 0) {
            throw new IllegalArgumentException("Negative size");
        } else if (position + j < 0) {
            throw new IllegalArgumentException("Position + size overflow");
        } else if (j <= 2147483647L) {
            int imode = -1;
            if (mapMode == FileChannel.MapMode.READ_ONLY) {
                imode = 0;
            } else if (mapMode == FileChannel.MapMode.READ_WRITE) {
                imode = 1;
            } else if (mapMode == FileChannel.MapMode.PRIVATE) {
                imode = 2;
            }
            int imode2 = imode;
            if (mapMode != FileChannel.MapMode.READ_ONLY && !this.writable) {
                throw new NonWritableChannelException();
            } else if (this.readable) {
                long addr3 = -1;
                try {
                    begin();
                    int ti2 = this.threads.add();
                    if (!isOpen()) {
                        this.threads.remove(ti2);
                        end(IOStatus.checkAll(-1));
                        return null;
                    }
                    do {
                        try {
                            filesize = this.nd.size(this.fd);
                            if (filesize != -3) {
                                break;
                            }
                            try {
                            } catch (IOException e) {
                                IOException r = e;
                                if (OsConstants.S_ISREG(Libcore.os.fstat(this.fd).st_mode)) {
                                    throw r;
                                }
                            } catch (ErrnoException e2) {
                                e2.rethrowAsIOException();
                            } catch (Throwable th) {
                                y = th;
                                ti = ti2;
                                this.threads.remove(ti);
                                end(IOStatus.checkAll(addr3));
                                throw y;
                            }
                        } catch (Throwable th2) {
                            y = th2;
                            ti = ti2;
                            this.threads.remove(ti);
                            end(IOStatus.checkAll(addr3));
                            throw y;
                        }
                    } while (isOpen());
                    break;
                    if (!isOpen()) {
                        this.threads.remove(ti2);
                        end(IOStatus.checkAll(-1));
                        return null;
                    }
                    if (filesize < position + j) {
                        int rv2 = 0;
                        while (true) {
                            rv = rv2;
                            rv2 = this.nd.truncate(this.fd, position + j);
                            if (rv2 != -3) {
                                break;
                            } else if (!isOpen()) {
                                break;
                            }
                        }
                        if (!isOpen()) {
                            this.threads.remove(ti2);
                            end(IOStatus.checkAll(-1));
                            return null;
                        }
                    }
                    if (j != 0) {
                        addr3 = 0;
                        FileDescriptor dummy = new FileDescriptor();
                        if (this.writable) {
                            if (imode2 != 0) {
                                z2 = false;
                                DirectByteBuffer directByteBuffer = new DirectByteBuffer(0, 0, dummy, null, z2);
                                this.threads.remove(ti2);
                                end(IOStatus.checkAll(0));
                                return directByteBuffer;
                            }
                        }
                        z2 = true;
                        DirectByteBuffer directByteBuffer2 = new DirectByteBuffer(0, 0, dummy, null, z2);
                        this.threads.remove(ti2);
                        end(IOStatus.checkAll(0));
                        return directByteBuffer2;
                    }
                    int pagePosition2 = (int) (position % allocationGranularity);
                    long mapPosition = position - ((long) pagePosition2);
                    long mapSize2 = ((long) pagePosition2) + j;
                    try {
                        BlockGuard.getThreadPolicy().onReadFromDisk();
                        mapSize = mapSize2;
                        ti = ti2;
                        pagePosition = pagePosition2;
                        try {
                            addr = map0(imode2, mapPosition, mapSize);
                        } catch (OutOfMemoryError e3) {
                            e = e3;
                            OutOfMemoryError outOfMemoryError = e;
                            try {
                                System.gc();
                                Thread.sleep(100);
                            } catch (OutOfMemoryError y) {
                                long j2 = mapSize;
                                OutOfMemoryError outOfMemoryError2 = y;
                                throw new IOException("Map failed", y);
                            } catch (InterruptedException e4) {
                                InterruptedException interruptedException = e4;
                                Thread.currentThread().interrupt();
                            } catch (Throwable th3) {
                                y = th3;
                                this.threads.remove(ti);
                                end(IOStatus.checkAll(addr3));
                                throw y;
                            }
                            addr = map0(imode2, mapPosition, mapSize);
                            addr2 = addr;
                            FileDescriptor mfd = this.nd.duplicateForMapping(this.fd);
                            int isize = (int) j;
                            Unmapper unmapper = new Unmapper(addr2, mapSize, isize, mfd);
                            long j3 = addr2 + ((long) pagePosition);
                            if (this.writable) {
                            }
                            z = true;
                            DirectByteBuffer directByteBuffer3 = new DirectByteBuffer(isize, j3, mfd, unmapper, z);
                            this.threads.remove(ti);
                            end(IOStatus.checkAll(addr2));
                            return directByteBuffer3;
                        }
                    } catch (OutOfMemoryError e5) {
                        e = e5;
                        mapSize = mapSize2;
                        ti = ti2;
                        pagePosition = pagePosition2;
                        OutOfMemoryError outOfMemoryError3 = e;
                        System.gc();
                        Thread.sleep(100);
                        addr = map0(imode2, mapPosition, mapSize);
                        addr2 = addr;
                        FileDescriptor mfd2 = this.nd.duplicateForMapping(this.fd);
                        int isize2 = (int) j;
                        Unmapper unmapper2 = new Unmapper(addr2, mapSize, isize2, mfd2);
                        long j32 = addr2 + ((long) pagePosition);
                        if (this.writable) {
                        }
                        z = true;
                        DirectByteBuffer directByteBuffer32 = new DirectByteBuffer(isize2, j32, mfd2, unmapper2, z);
                        this.threads.remove(ti);
                        end(IOStatus.checkAll(addr2));
                        return directByteBuffer32;
                    }
                    addr2 = addr;
                    try {
                        FileDescriptor mfd22 = this.nd.duplicateForMapping(this.fd);
                        int isize22 = (int) j;
                        Unmapper unmapper22 = new Unmapper(addr2, mapSize, isize22, mfd22);
                        long j322 = addr2 + ((long) pagePosition);
                        if (this.writable) {
                            if (imode2 != 0) {
                                z = false;
                                DirectByteBuffer directByteBuffer322 = new DirectByteBuffer(isize22, j322, mfd22, unmapper22, z);
                                this.threads.remove(ti);
                                end(IOStatus.checkAll(addr2));
                                return directByteBuffer322;
                            }
                        }
                        z = true;
                        DirectByteBuffer directByteBuffer3222 = new DirectByteBuffer(isize22, j322, mfd22, unmapper22, z);
                        this.threads.remove(ti);
                        end(IOStatus.checkAll(addr2));
                        return directByteBuffer3222;
                    } catch (IOException ioe) {
                        unmap0(addr2, mapSize);
                        throw ioe;
                    } catch (Throwable th4) {
                        y = th4;
                        addr3 = addr2;
                        this.threads.remove(ti);
                        end(IOStatus.checkAll(addr3));
                        throw y;
                    }
                } catch (Throwable th5) {
                    y = th5;
                    ti = -1;
                    this.threads.remove(ti);
                    end(IOStatus.checkAll(addr3));
                    throw y;
                }
            } else {
                throw new NonReadableChannelException();
            }
        } else {
            throw new IllegalArgumentException("Size exceeds Integer.MAX_VALUE");
        }
        int i = rv;
        if (!isOpen()) {
        }
        if (j != 0) {
        }
    }

    private static boolean isSharedFileLockTable() {
        boolean z;
        if (!propertyChecked) {
            synchronized (FileChannelImpl.class) {
                if (!propertyChecked) {
                    String value = (String) AccessController.doPrivileged(new GetPropertyAction("sun.nio.ch.disableSystemWideOverlappingFileLockCheck"));
                    if (value != null) {
                        if (!value.equals("false")) {
                            z = $assertionsDisabled;
                            isSharedFileLockTable = z;
                            propertyChecked = true;
                        }
                    }
                    z = true;
                    isSharedFileLockTable = z;
                    propertyChecked = true;
                }
            }
        }
        return isSharedFileLockTable;
    }

    /* JADX INFO: finally extract failed */
    private FileLockTable fileLockTable() throws IOException {
        if (this.fileLockTable == null) {
            synchronized (this) {
                if (this.fileLockTable == null) {
                    if (isSharedFileLockTable()) {
                        int ti = this.threads.add();
                        try {
                            ensureOpen();
                            this.fileLockTable = FileLockTable.newSharedFileLockTable(this, this.fd);
                            this.threads.remove(ti);
                        } catch (Throwable th) {
                            this.threads.remove(ti);
                            throw th;
                        }
                    } else {
                        this.fileLockTable = new SimpleFileLockTable();
                    }
                }
            }
        }
        return this.fileLockTable;
    }

    /* JADX WARNING: Removed duplicated region for block: B:65:0x00d3  */
    public FileLock lock(long position, long size, boolean shared) throws IOException {
        FileLockImpl fli;
        boolean completed;
        FileLockTable flt;
        int ti;
        int n;
        FileLockImpl fli2;
        boolean completed2;
        ensureOpen();
        if (shared && !this.readable) {
            throw new NonReadableChannelException();
        } else if (shared || this.writable) {
            FileLockImpl fileLockImpl = new FileLockImpl((FileChannel) this, position, size, shared);
            FileLockImpl fli3 = fileLockImpl;
            FileLockTable flt2 = fileLockTable();
            flt2.add(fli3);
            try {
                begin();
                int ti2 = this.threads.add();
                if (!isOpen()) {
                    if (0 == 0) {
                        flt2.remove(fli3);
                    }
                    this.threads.remove(ti2);
                    try {
                        end($assertionsDisabled);
                        return null;
                    } catch (ClosedByInterruptException e) {
                        ClosedByInterruptException closedByInterruptException = e;
                        throw new FileLockInterruptionException();
                    }
                } else {
                    do {
                        try {
                            n = this.nd.lock(this.fd, true, position, size, shared);
                            if (n == 2) {
                                try {
                                } catch (Throwable th) {
                                    e = th;
                                    ti = ti2;
                                    flt = flt2;
                                    completed = false;
                                    fli = fli3;
                                    if (!completed) {
                                    }
                                    this.threads.remove(ti);
                                    try {
                                        end(completed);
                                        throw e;
                                    } catch (ClosedByInterruptException e2) {
                                        ClosedByInterruptException closedByInterruptException2 = e2;
                                        throw new FileLockInterruptionException();
                                    }
                                }
                            }
                            break;
                        } catch (Throwable th2) {
                            e = th2;
                            ti = ti2;
                            flt = flt2;
                            completed = false;
                            fli = fli3;
                            if (!completed) {
                            }
                            this.threads.remove(ti);
                            end(completed);
                            throw e;
                        }
                    } while (isOpen());
                    break;
                    if (isOpen()) {
                        if (n == 1) {
                            r1 = r1;
                            ti = ti2;
                            flt = flt2;
                            completed = false;
                            fli = fli3;
                            try {
                                FileLockImpl fileLockImpl2 = new FileLockImpl((FileChannel) this, position, size, (boolean) $assertionsDisabled);
                                FileLockImpl fli4 = fileLockImpl2;
                                flt.replace(fli, fli4);
                                fli3 = fli4;
                            } catch (Throwable th3) {
                                e = th3;
                                if (!completed) {
                                    flt.remove(fli);
                                }
                                this.threads.remove(ti);
                                end(completed);
                                throw e;
                            }
                        } else {
                            ti = ti2;
                            flt = flt2;
                            FileLockImpl fileLockImpl3 = fli3;
                        }
                        completed2 = true;
                        fli2 = fli3;
                    } else {
                        ti = ti2;
                        flt = flt2;
                        completed2 = false;
                        fli2 = fli3;
                    }
                    if (!completed2) {
                        flt.remove(fli2);
                    }
                    this.threads.remove(ti);
                    try {
                        end(completed2);
                        return fli2;
                    } catch (ClosedByInterruptException e3) {
                        ClosedByInterruptException closedByInterruptException3 = e3;
                        throw new FileLockInterruptionException();
                    }
                }
            } catch (Throwable th4) {
                e = th4;
                flt = flt2;
                completed = false;
                fli = fli3;
                ti = -1;
                if (!completed) {
                }
                this.threads.remove(ti);
                end(completed);
                throw e;
            }
        } else {
            throw new NonWritableChannelException();
        }
    }

    public FileLock tryLock(long position, long size, boolean shared) throws IOException {
        int ti;
        ensureOpen();
        if (shared && !this.readable) {
            throw new NonReadableChannelException();
        } else if (shared || this.writable) {
            FileLockImpl fileLockImpl = new FileLockImpl((FileChannel) this, position, size, shared);
            FileLockImpl fli = fileLockImpl;
            FileLockTable flt = fileLockTable();
            flt.add(fli);
            int ti2 = this.threads.add();
            try {
                ensureOpen();
                int result = this.nd.lock(this.fd, $assertionsDisabled, position, size, shared);
                if (result == -1) {
                    try {
                        flt.remove(fli);
                        this.threads.remove(ti2);
                        return null;
                    } catch (Throwable th) {
                        e = th;
                        FileLockTable fileLockTable2 = flt;
                        ti = ti2;
                        FileLockImpl fileLockImpl2 = fli;
                        this.threads.remove(ti);
                        throw e;
                    }
                } else if (result == 1) {
                    try {
                        r1 = r1;
                        FileLockTable flt2 = flt;
                        ti = ti2;
                        FileLockImpl fli2 = fli;
                        FileLockImpl fileLockImpl3 = new FileLockImpl((FileChannel) this, position, size, (boolean) $assertionsDisabled);
                        FileLockImpl fli22 = fileLockImpl3;
                        flt2.replace(fli2, fli22);
                        this.threads.remove(ti);
                        return fli22;
                    } catch (Throwable th2) {
                        e = th2;
                        FileLockTable fileLockTable3 = flt;
                        ti = ti2;
                        FileLockImpl fileLockImpl4 = fli;
                        this.threads.remove(ti);
                        throw e;
                    }
                } else {
                    FileLockImpl fli3 = fli;
                    this.threads.remove(ti2);
                    return fli3;
                }
            } catch (IOException e) {
                ti = ti2;
                flt.remove(fli);
                throw e;
            } catch (Throwable th3) {
                e = th3;
                this.threads.remove(ti);
                throw e;
            }
        } else {
            throw new NonWritableChannelException();
        }
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: package-private */
    public void release(FileLockImpl fli) throws IOException {
        int ti = this.threads.add();
        try {
            ensureOpen();
            this.nd.release(this.fd, fli.position(), fli.size());
            this.threads.remove(ti);
            this.fileLockTable.remove(fli);
        } catch (Throwable th) {
            this.threads.remove(ti);
            throw th;
        }
    }
}
