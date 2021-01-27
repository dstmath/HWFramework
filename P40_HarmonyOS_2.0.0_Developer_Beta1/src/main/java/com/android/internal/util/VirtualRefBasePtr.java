package com.android.internal.util;

public final class VirtualRefBasePtr {
    private long mNativePtr;

    private static native void nDecStrong(long j);

    private static native void nIncStrong(long j);

    public VirtualRefBasePtr(long ptr) {
        this.mNativePtr = ptr;
        nIncStrong(this.mNativePtr);
    }

    public long get() {
        return this.mNativePtr;
    }

    public void release() {
        long j = this.mNativePtr;
        if (j != 0) {
            nDecStrong(j);
            this.mNativePtr = 0;
        }
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        try {
            release();
        } finally {
            super.finalize();
        }
    }
}
