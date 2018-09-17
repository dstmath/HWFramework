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
        if (this.mNativePtr != 0) {
            nDecStrong(this.mNativePtr);
            this.mNativePtr = 0;
        }
    }

    protected void finalize() throws Throwable {
        try {
            release();
        } finally {
            super.finalize();
        }
    }
}
