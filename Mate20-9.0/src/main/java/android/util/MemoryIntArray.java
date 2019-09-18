package android.util;

import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import dalvik.system.CloseGuard;
import java.io.Closeable;
import java.io.IOException;
import java.util.UUID;
import libcore.io.IoUtils;

public final class MemoryIntArray implements Parcelable, Closeable {
    public static final Parcelable.Creator<MemoryIntArray> CREATOR = new Parcelable.Creator<MemoryIntArray>() {
        public MemoryIntArray createFromParcel(Parcel parcel) {
            try {
                return new MemoryIntArray(parcel);
            } catch (IOException e) {
                throw new IllegalArgumentException("Error unparceling MemoryIntArray");
            }
        }

        public MemoryIntArray[] newArray(int size) {
            return new MemoryIntArray[size];
        }
    };
    private static final int MAX_SIZE = 1024;
    private static final String TAG = "MemoryIntArray";
    private final CloseGuard mCloseGuard;
    private int mFd;
    private final boolean mIsOwner;
    private final long mMemoryAddr;

    private native void nativeClose(int i, long j, boolean z);

    private native int nativeCreate(String str, int i);

    private native int nativeGet(int i, long j, int i2);

    private native long nativeOpen(int i, boolean z);

    private native void nativeSet(int i, long j, int i2, int i3);

    private native int nativeSize(int i);

    public MemoryIntArray(int size) throws IOException {
        this.mCloseGuard = CloseGuard.get();
        this.mFd = -1;
        if (size <= 1024) {
            this.mIsOwner = true;
            this.mFd = nativeCreate(UUID.randomUUID().toString(), size);
            this.mMemoryAddr = nativeOpen(this.mFd, this.mIsOwner);
            this.mCloseGuard.open("close");
            return;
        }
        throw new IllegalArgumentException("Max size is 1024");
    }

    private MemoryIntArray(Parcel parcel) throws IOException {
        this.mCloseGuard = CloseGuard.get();
        this.mFd = -1;
        this.mIsOwner = false;
        ParcelFileDescriptor pfd = (ParcelFileDescriptor) parcel.readParcelable(null);
        if (pfd != null) {
            this.mFd = pfd.detachFd();
            this.mMemoryAddr = nativeOpen(this.mFd, this.mIsOwner);
            this.mCloseGuard.open("close");
            return;
        }
        throw new IOException("No backing file descriptor");
    }

    public boolean isWritable() {
        enforceNotClosed();
        return this.mIsOwner;
    }

    public int get(int index) throws IOException {
        enforceNotClosed();
        enforceValidIndex(index);
        return nativeGet(this.mFd, this.mMemoryAddr, index);
    }

    public void set(int index, int value) throws IOException {
        enforceNotClosed();
        enforceWritable();
        enforceValidIndex(index);
        nativeSet(this.mFd, this.mMemoryAddr, index, value);
    }

    public int size() throws IOException {
        enforceNotClosed();
        return nativeSize(this.mFd);
    }

    public void close() throws IOException {
        if (!isClosed()) {
            nativeClose(this.mFd, this.mMemoryAddr, this.mIsOwner);
            this.mFd = -1;
            this.mCloseGuard.close();
        }
    }

    public boolean isClosed() {
        return this.mFd == -1;
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        try {
            if (this.mCloseGuard != null) {
                this.mCloseGuard.warnIfOpen();
            }
            IoUtils.closeQuietly(this);
        } finally {
            super.finalize();
        }
    }

    public int describeContents() {
        return 1;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        ParcelFileDescriptor pfd = ParcelFileDescriptor.adoptFd(this.mFd);
        try {
            parcel.writeParcelable(pfd, flags & -2);
        } finally {
            pfd.detachFd();
        }
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        if (this.mFd == ((MemoryIntArray) obj).mFd) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        return this.mFd;
    }

    private void enforceNotClosed() {
        if (isClosed()) {
            throw new IllegalStateException("cannot interact with a closed instance");
        }
    }

    private void enforceValidIndex(int index) throws IOException {
        int size = size();
        if (index < 0 || index > size - 1) {
            throw new IndexOutOfBoundsException(index + " not between 0 and " + (size - 1));
        }
    }

    private void enforceWritable() {
        if (!isWritable()) {
            throw new UnsupportedOperationException("array is not writable");
        }
    }

    public static int getMaxSize() {
        return 1024;
    }
}
