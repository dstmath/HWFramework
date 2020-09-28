package android.util.apk;

import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import java.io.FileDescriptor;
import java.io.IOException;
import java.nio.DirectByteBuffer;
import java.security.DigestException;

/* access modifiers changed from: package-private */
public class MemoryMappedFileDataSource implements DataSource {
    private static final long MEMORY_PAGE_SIZE_BYTES = Os.sysconf(OsConstants._SC_PAGESIZE);
    private final FileDescriptor mFd;
    private final long mFilePosition;
    private final long mSize;

    MemoryMappedFileDataSource(FileDescriptor fd, long position, long size) {
        this.mFd = fd;
        this.mFilePosition = position;
        this.mSize = size;
    }

    @Override // android.util.apk.DataSource
    public long size() {
        return this.mSize;
    }

    /* JADX WARNING: Removed duplicated region for block: B:42:0x00a7 A[SYNTHETIC, Splitter:B:42:0x00a7] */
    @Override // android.util.apk.DataSource
    public void feedIntoDataDigester(DataDigester md, long offset, int size) throws IOException, DigestException {
        long mmapPtr;
        ErrnoException errnoException;
        long mmapRegionSize;
        long filePosition = this.mFilePosition + offset;
        long j = MEMORY_PAGE_SIZE_BYTES;
        long mmapFilePosition = j * (filePosition / j);
        int dataStartOffsetInMmapRegion = (int) (filePosition - mmapFilePosition);
        long mmapRegionSize2 = (long) (size + dataStartOffsetInMmapRegion);
        long mmapPtr2 = 0;
        try {
            try {
                mmapPtr = Os.mmap(0, mmapRegionSize2, OsConstants.PROT_READ, OsConstants.MAP_SHARED | OsConstants.MAP_POPULATE, this.mFd, mmapFilePosition);
                try {
                    try {
                        md.consume(new DirectByteBuffer(size, mmapPtr + ((long) dataStartOffsetInMmapRegion), this.mFd, (Runnable) null, true));
                        if (mmapPtr != 0) {
                            try {
                                Os.munmap(mmapPtr, mmapRegionSize2);
                            } catch (ErrnoException e) {
                            }
                        }
                    } catch (ErrnoException e2) {
                        e = e2;
                        mmapRegionSize = mmapRegionSize2;
                        mmapPtr2 = mmapPtr;
                        try {
                            throw new IOException("Failed to mmap " + mmapRegionSize + " bytes", e);
                        } catch (Throwable e3) {
                            errnoException = e3;
                            mmapPtr = mmapPtr2;
                            if (mmapPtr != 0) {
                            }
                            throw errnoException;
                        }
                    } catch (Throwable th) {
                        mmapRegionSize = mmapRegionSize2;
                        errnoException = th;
                        if (mmapPtr != 0) {
                        }
                        throw errnoException;
                    }
                } catch (ErrnoException e4) {
                    e = e4;
                    mmapRegionSize = mmapRegionSize2;
                    mmapPtr2 = mmapPtr;
                    throw new IOException("Failed to mmap " + mmapRegionSize + " bytes", e);
                } catch (Throwable th2) {
                    mmapRegionSize = mmapRegionSize2;
                    errnoException = th2;
                    if (mmapPtr != 0) {
                    }
                    throw errnoException;
                }
            } catch (ErrnoException e5) {
                e = e5;
                mmapRegionSize = mmapRegionSize2;
                throw new IOException("Failed to mmap " + mmapRegionSize + " bytes", e);
            } catch (Throwable th3) {
                mmapRegionSize = mmapRegionSize2;
                errnoException = th3;
                mmapPtr = 0;
                if (mmapPtr != 0) {
                    try {
                        Os.munmap(mmapPtr, mmapRegionSize);
                    } catch (ErrnoException e6) {
                    }
                }
                throw errnoException;
            }
        } catch (ErrnoException e7) {
            e = e7;
            mmapRegionSize = mmapRegionSize2;
            throw new IOException("Failed to mmap " + mmapRegionSize + " bytes", e);
        } catch (Throwable th4) {
            mmapRegionSize = mmapRegionSize2;
            errnoException = th4;
            mmapPtr = 0;
            if (mmapPtr != 0) {
            }
            throw errnoException;
        }
    }
}
