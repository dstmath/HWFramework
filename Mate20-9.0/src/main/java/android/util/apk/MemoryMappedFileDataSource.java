package android.util.apk;

import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import java.io.FileDescriptor;
import java.io.IOException;
import java.nio.DirectByteBuffer;
import java.security.DigestException;

class MemoryMappedFileDataSource implements DataSource {
    private static final long MEMORY_PAGE_SIZE_BYTES = Os.sysconf(OsConstants._SC_PAGESIZE);
    private final FileDescriptor mFd;
    private final long mFilePosition;
    private final long mSize;

    MemoryMappedFileDataSource(FileDescriptor fd, long position, long size) {
        this.mFd = fd;
        this.mFilePosition = position;
        this.mSize = size;
    }

    public long size() {
        return this.mSize;
    }

    /* JADX WARNING: Removed duplicated region for block: B:49:0x00c8 A[SYNTHETIC, Splitter:B:49:0x00c8] */
    public void feedIntoDataDigester(DataDigester md, long offset, int size) throws IOException, DigestException {
        long mmapPtr;
        long mmapRegionSize;
        ErrnoException errnoException;
        DirectByteBuffer directByteBuffer;
        long mmapPtr2;
        long filePosition = this.mFilePosition + offset;
        long mmapFilePosition = (filePosition / MEMORY_PAGE_SIZE_BYTES) * MEMORY_PAGE_SIZE_BYTES;
        int dataStartOffsetInMmapRegion = (int) (filePosition - mmapFilePosition);
        long mmapRegionSize2 = (long) (size + dataStartOffsetInMmapRegion);
        long mmapPtr3 = 0;
        try {
            long mmapRegionSize3 = mmapRegionSize2;
            try {
                long mmapPtr4 = Os.mmap(0, mmapRegionSize2, OsConstants.PROT_READ, OsConstants.MAP_SHARED | OsConstants.MAP_POPULATE, this.mFd, mmapFilePosition);
                try {
                    long j = mmapPtr4 + ((long) dataStartOffsetInMmapRegion);
                    FileDescriptor fileDescriptor = this.mFd;
                    directByteBuffer = directByteBuffer;
                    long j2 = filePosition;
                    mmapPtr2 = mmapPtr4;
                    try {
                        directByteBuffer = new DirectByteBuffer(size, j, fileDescriptor, null, true);
                    } catch (ErrnoException e) {
                        e = e;
                        DataDigester dataDigester = md;
                        mmapRegionSize = mmapRegionSize3;
                        mmapPtr3 = mmapPtr2;
                        try {
                            throw new IOException("Failed to mmap " + mmapRegionSize + " bytes", e);
                        } catch (Throwable e2) {
                            errnoException = e2;
                            mmapPtr = mmapPtr3;
                            if (mmapPtr != 0) {
                            }
                            throw errnoException;
                        }
                    } catch (Throwable th) {
                        th = th;
                        DataDigester dataDigester2 = md;
                        mmapRegionSize = mmapRegionSize3;
                        mmapPtr = mmapPtr2;
                        errnoException = th;
                        if (mmapPtr != 0) {
                        }
                        throw errnoException;
                    }
                } catch (ErrnoException e3) {
                    e = e3;
                    long j3 = filePosition;
                    mmapRegionSize = mmapRegionSize3;
                    DataDigester dataDigester3 = md;
                    mmapPtr3 = mmapPtr4;
                    throw new IOException("Failed to mmap " + mmapRegionSize + " bytes", e);
                } catch (Throwable th2) {
                    long j4 = filePosition;
                    mmapRegionSize = mmapRegionSize3;
                    DataDigester dataDigester4 = md;
                    mmapPtr = mmapPtr4;
                    errnoException = th2;
                    if (mmapPtr != 0) {
                    }
                    throw errnoException;
                }
            } catch (ErrnoException e4) {
                e = e4;
                long j5 = filePosition;
                mmapRegionSize = mmapRegionSize3;
                DataDigester dataDigester5 = md;
                throw new IOException("Failed to mmap " + mmapRegionSize + " bytes", e);
            } catch (Throwable th3) {
                long j6 = filePosition;
                mmapRegionSize = mmapRegionSize3;
                DataDigester dataDigester6 = md;
                errnoException = th3;
                mmapPtr = 0;
                if (mmapPtr != 0) {
                }
                throw errnoException;
            }
            try {
                md.consume(directByteBuffer);
                if (mmapPtr2 != 0) {
                    try {
                        Os.munmap(mmapPtr2, mmapRegionSize3);
                    } catch (ErrnoException e5) {
                    }
                }
            } catch (ErrnoException e6) {
                e = e6;
                mmapRegionSize = mmapRegionSize3;
                mmapPtr3 = mmapPtr2;
                throw new IOException("Failed to mmap " + mmapRegionSize + " bytes", e);
            } catch (Throwable th4) {
                th = th4;
                mmapRegionSize = mmapRegionSize3;
                mmapPtr = mmapPtr2;
                errnoException = th;
                if (mmapPtr != 0) {
                    try {
                        Os.munmap(mmapPtr, mmapRegionSize);
                    } catch (ErrnoException e7) {
                    }
                }
                throw errnoException;
            }
        } catch (ErrnoException e8) {
            e = e8;
            long j7 = filePosition;
            mmapRegionSize = mmapRegionSize2;
            DataDigester dataDigester7 = md;
            throw new IOException("Failed to mmap " + mmapRegionSize + " bytes", e);
        } catch (Throwable th5) {
            long j8 = filePosition;
            mmapRegionSize = mmapRegionSize2;
            DataDigester dataDigester8 = md;
            errnoException = th5;
            mmapPtr = 0;
            if (mmapPtr != 0) {
            }
            throw errnoException;
        }
    }
}
