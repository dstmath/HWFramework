package android.os;

import android.annotation.UnsupportedAppUsage;
import android.os.Parcelable;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import dalvik.system.VMRuntime;
import java.io.Closeable;
import java.io.FileDescriptor;
import java.nio.ByteBuffer;
import java.nio.DirectByteBuffer;
import java.nio.NioUtils;
import sun.misc.Cleaner;

public final class SharedMemory implements Parcelable, Closeable {
    public static final Parcelable.Creator<SharedMemory> CREATOR = new Parcelable.Creator<SharedMemory>() {
        /* class android.os.SharedMemory.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public SharedMemory createFromParcel(Parcel source) {
            return new SharedMemory(source.readRawFileDescriptor());
        }

        @Override // android.os.Parcelable.Creator
        public SharedMemory[] newArray(int size) {
            return new SharedMemory[size];
        }
    };
    private static final int PROT_MASK = (((OsConstants.PROT_READ | OsConstants.PROT_WRITE) | OsConstants.PROT_EXEC) | OsConstants.PROT_NONE);
    private Cleaner mCleaner;
    private final FileDescriptor mFileDescriptor;
    private final MemoryRegistration mMemoryRegistration;
    private final int mSize;

    private static native FileDescriptor nCreate(String str, int i) throws ErrnoException;

    private static native int nGetSize(FileDescriptor fileDescriptor);

    private static native int nSetProt(FileDescriptor fileDescriptor, int i);

    private SharedMemory(FileDescriptor fd) {
        if (fd == null) {
            throw new IllegalArgumentException("Unable to create SharedMemory from a null FileDescriptor");
        } else if (fd.valid()) {
            this.mFileDescriptor = fd;
            this.mSize = nGetSize(this.mFileDescriptor);
            int i = this.mSize;
            if (i > 0) {
                this.mMemoryRegistration = new MemoryRegistration(i);
                FileDescriptor fileDescriptor = this.mFileDescriptor;
                this.mCleaner = Cleaner.create(fileDescriptor, new Closer(fileDescriptor, this.mMemoryRegistration));
                return;
            }
            throw new IllegalArgumentException("FileDescriptor is not a valid ashmem fd");
        } else {
            throw new IllegalArgumentException("Unable to create SharedMemory from closed FileDescriptor");
        }
    }

    public static SharedMemory create(String name, int size) throws ErrnoException {
        if (size > 0) {
            return new SharedMemory(nCreate(name, size));
        }
        throw new IllegalArgumentException("Size must be greater than zero");
    }

    private void checkOpen() {
        if (!this.mFileDescriptor.valid()) {
            throw new IllegalStateException("SharedMemory is closed");
        }
    }

    private static void validateProt(int prot) {
        if (((~PROT_MASK) & prot) != 0) {
            throw new IllegalArgumentException("Invalid prot value");
        }
    }

    public boolean setProtect(int prot) {
        checkOpen();
        validateProt(prot);
        return nSetProt(this.mFileDescriptor, prot) == 0;
    }

    public FileDescriptor getFileDescriptor() {
        return this.mFileDescriptor;
    }

    @UnsupportedAppUsage
    public int getFd() {
        return this.mFileDescriptor.getInt$();
    }

    public int getSize() {
        checkOpen();
        return this.mSize;
    }

    public ByteBuffer mapReadWrite() throws ErrnoException {
        return map(OsConstants.PROT_READ | OsConstants.PROT_WRITE, 0, this.mSize);
    }

    public ByteBuffer mapReadOnly() throws ErrnoException {
        return map(OsConstants.PROT_READ, 0, this.mSize);
    }

    public ByteBuffer map(int prot, int offset, int length) throws ErrnoException {
        checkOpen();
        validateProt(prot);
        if (offset < 0) {
            throw new IllegalArgumentException("Offset must be >= 0");
        } else if (length <= 0) {
            throw new IllegalArgumentException("Length must be > 0");
        } else if (offset + length <= this.mSize) {
            long address = Os.mmap(0, (long) length, prot, OsConstants.MAP_SHARED, this.mFileDescriptor, (long) offset);
            return new DirectByteBuffer(length, address, this.mFileDescriptor, new Unmapper(address, length, this.mMemoryRegistration.acquire()), (prot & OsConstants.PROT_WRITE) == 0);
        } else {
            throw new IllegalArgumentException("offset + length must not exceed getSize()");
        }
    }

    public static void unmap(ByteBuffer buffer) {
        if (buffer instanceof DirectByteBuffer) {
            NioUtils.freeDirectBuffer(buffer);
            return;
        }
        throw new IllegalArgumentException("ByteBuffer wasn't created by #map(int, int, int); can't unmap");
    }

    @Override // java.io.Closeable, java.lang.AutoCloseable
    public void close() {
        Cleaner cleaner = this.mCleaner;
        if (cleaner != null) {
            cleaner.clean();
            this.mCleaner = null;
        }
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 1;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        checkOpen();
        dest.writeFileDescriptor(this.mFileDescriptor);
    }

    private static final class Closer implements Runnable {
        private FileDescriptor mFd;
        private MemoryRegistration mMemoryReference;

        private Closer(FileDescriptor fd, MemoryRegistration memoryReference) {
            this.mFd = fd;
            this.mMemoryReference = memoryReference;
        }

        public void run() {
            try {
                Os.close(this.mFd);
            } catch (ErrnoException e) {
            }
            this.mMemoryReference.release();
            this.mMemoryReference = null;
        }
    }

    /* access modifiers changed from: private */
    public static final class Unmapper implements Runnable {
        private long mAddress;
        private MemoryRegistration mMemoryReference;
        private int mSize;

        private Unmapper(long address, int size, MemoryRegistration memoryReference) {
            this.mAddress = address;
            this.mSize = size;
            this.mMemoryReference = memoryReference;
        }

        public void run() {
            try {
                Os.munmap(this.mAddress, (long) this.mSize);
            } catch (ErrnoException e) {
            }
            this.mMemoryReference.release();
            this.mMemoryReference = null;
        }
    }

    /* access modifiers changed from: private */
    public static final class MemoryRegistration {
        private int mReferenceCount;
        private int mSize;

        private MemoryRegistration(int size) {
            this.mSize = size;
            this.mReferenceCount = 1;
            VMRuntime.getRuntime().registerNativeAllocation(this.mSize);
        }

        public synchronized MemoryRegistration acquire() {
            this.mReferenceCount++;
            return this;
        }

        public synchronized void release() {
            this.mReferenceCount--;
            if (this.mReferenceCount == 0) {
                VMRuntime.getRuntime().registerNativeFree(this.mSize);
            }
        }
    }
}
