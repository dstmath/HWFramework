package ohos.utils;

import ohos.hiviewdfx.HiLogLabel;

public class Ashmem {
    public static final int PROT_EXEC = 4;
    public static final int PROT_NONE = 0;
    public static final int PROT_READ = 1;
    public static final int PROT_WRITE = 2;
    private static final HiLogLabel TAG = new HiLogLabel(3, 218119424, "UtilsAshmem");
    private long mNativeId;
    private boolean mOwnsNativeObject;

    private static native void nativeCloseAshmem(long j);

    private static native long nativeCreateAshmem(String str, int i);

    private static native long nativeCreateAshmemFromExisting(long j);

    private static native int nativeGetAshmemSize(long j);

    private static native boolean nativeMapAshmem(long j, int i);

    private static native byte[] nativeReadFromAshmem(long j, int i, int i2);

    private static native void nativeRelease(long j);

    private static native boolean nativeSetProtection(long j, int i);

    private static native void nativeUnmapAshmem(long j);

    private static native boolean nativeWriteToAshmem(long j, byte[] bArr, int i, int i2);

    private Ashmem(long j) {
        this.mNativeId = j;
    }

    public static Ashmem createAshmem(String str, int i) {
        long nativeCreateAshmem = nativeCreateAshmem(str, i);
        if (nativeCreateAshmem > 0) {
            return new Ashmem(nativeCreateAshmem);
        }
        return null;
    }

    public static Ashmem createAshmemFromExisting(long j) {
        if (j <= 0) {
            return null;
        }
        long nativeCreateAshmemFromExisting = nativeCreateAshmemFromExisting(j);
        if (nativeCreateAshmemFromExisting > 0) {
            return new Ashmem(nativeCreateAshmemFromExisting);
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        try {
            if (this.mNativeId > 0) {
                nativeCloseAshmem(this.mNativeId);
                nativeRelease(this.mNativeId);
                this.mNativeId = -1;
            }
        } finally {
            super.finalize();
        }
    }

    public void closeAshmem() {
        long j = this.mNativeId;
        if (j > 0) {
            nativeCloseAshmem(j);
        }
    }

    public boolean mapAshmem(int i) {
        return nativeMapAshmem(this.mNativeId, i);
    }

    public boolean mapReadAndWriteAShmem() {
        return mapAshmem(3);
    }

    public boolean mapReadOnlyAShmem() {
        return mapAshmem(1);
    }

    public void unmapAShmem() {
        nativeUnmapAshmem(this.mNativeId);
    }

    public boolean setProtection(int i) {
        return nativeSetProtection(this.mNativeId, i);
    }

    public int getAShmemSize() {
        return nativeGetAshmemSize(this.mNativeId);
    }

    public boolean writeToAShmem(byte[] bArr, int i, int i2) {
        if (bArr == null || i < 0 || i2 < 0) {
            return false;
        }
        return nativeWriteToAshmem(this.mNativeId, bArr, i, i2);
    }

    public byte[] readFromAShmem(int i, int i2) {
        if (i < 0 || i2 < 0) {
            return null;
        }
        return nativeReadFromAshmem(this.mNativeId, i, i2);
    }

    public long getAshmemIdentity() {
        return this.mNativeId;
    }
}
