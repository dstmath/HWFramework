package android.os;

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
        public SharedMemory createFromParcel(Parcel source) {
            return new SharedMemory(source.readRawFileDescriptor());
        }

        public SharedMemory[] newArray(int size) {
            return new SharedMemory[size];
        }
    };
    private static final int PROT_MASK = (((OsConstants.PROT_READ | OsConstants.PROT_WRITE) | OsConstants.PROT_EXEC) | OsConstants.PROT_NONE);
    private Cleaner mCleaner;
    private final FileDescriptor mFileDescriptor;
    private final MemoryRegistration mMemoryRegistration;
    private final int mSize;

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

    private static final class MemoryRegistration {
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

    private static final class Unmapper implements Runnable {
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

    private static native FileDescriptor nCreate(String str, int i) throws ErrnoException;

    private static native int nGetSize(FileDescriptor fileDescriptor);

    private static native int nSetProt(FileDescriptor fileDescriptor, int i);

    private SharedMemory(FileDescriptor fd) {
        if (fd == null) {
            throw new IllegalArgumentException("Unable to create SharedMemory from a null FileDescriptor");
        } else if (fd.valid()) {
            this.mFileDescriptor = fd;
            this.mSize = nGetSize(this.mFileDescriptor);
            if (this.mSize > 0) {
                this.mMemoryRegistration = new MemoryRegistration(this.mSize);
                this.mCleaner = Cleaner.create(this.mFileDescriptor, new Closer(this.mFileDescriptor, this.mMemoryRegistration));
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
        int i = offset;
        int i2 = length;
        checkOpen();
        validateProt(prot);
        if (i < 0) {
            throw new IllegalArgumentException("Offset must be >= 0");
        } else if (i2 <= 0) {
            throw new IllegalArgumentException("Length must be > 0");
        } else if (i + i2 <= this.mSize) {
            long address = Os.mmap(0, (long) i2, prot, OsConstants.MAP_SHARED, this.mFileDescriptor, (long) i);
            boolean readOnly = (prot & OsConstants.PROT_WRITE) == 0;
            Unmapper unmapper = new Unmapper(address, i2, this.mMemoryRegistration.acquire());
            DirectByteBuffer directByteBuffer = new DirectByteBuffer(i2, address, this.mFileDescriptor, unmapper, readOnly);
            return directByteBuffer;
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

    public void close() {
        if (this.mCleaner != null) {
            this.mCleaner.clean();
            this.mCleaner = null;
        }
    }

    public int describeContents() {
        return 1;
    }

    public void writeToParcel(Parcel dest, int flags) {
        checkOpen();
        dest.writeFileDescriptor(this.mFileDescriptor);
    }
}
