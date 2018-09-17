package android.util;

import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.io.Closeable;
import java.io.IOException;
import java.util.UUID;
import libcore.io.IoUtils;

public final class MemoryIntArray implements Parcelable, Closeable {
    public static final Creator<MemoryIntArray> CREATOR = null;
    private static final int MAX_SIZE = 1024;
    private static final String TAG = "MemoryIntArray";
    private int mFd;
    private final boolean mIsOwner;
    private final long mMemoryAddr;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.util.MemoryIntArray.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.util.MemoryIntArray.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.util.MemoryIntArray.<clinit>():void");
    }

    private native void nativeClose(int i, long j, boolean z);

    private native int nativeCreate(String str, int i);

    private native int nativeGet(int i, long j, int i2);

    private native long nativeOpen(int i, boolean z);

    private native void nativeSet(int i, long j, int i2, int i3);

    private native int nativeSize(int i);

    public MemoryIntArray(int size) throws IOException {
        if (size > MAX_SIZE) {
            throw new IllegalArgumentException("Max size is 1024");
        }
        this.mIsOwner = true;
        this.mFd = nativeCreate(UUID.randomUUID().toString(), size);
        this.mMemoryAddr = nativeOpen(this.mFd, this.mIsOwner);
    }

    private MemoryIntArray(Parcel parcel) throws IOException {
        this.mIsOwner = false;
        ParcelFileDescriptor pfd = (ParcelFileDescriptor) parcel.readParcelable(null);
        if (pfd == null) {
            throw new IOException("No backing file descriptor");
        }
        this.mFd = pfd.detachFd();
        this.mMemoryAddr = nativeOpen(this.mFd, this.mIsOwner);
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
        }
    }

    public boolean isClosed() {
        return this.mFd == -1;
    }

    protected void finalize() throws Throwable {
        IoUtils.closeQuietly(this);
        super.finalize();
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
        boolean z = true;
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        if (this.mFd != ((MemoryIntArray) obj).mFd) {
            z = false;
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
        return MAX_SIZE;
    }
}
