package com.huawei.servicehost;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class ImageDescriptor implements Parcelable {
    public static final Parcelable.Creator<ImageDescriptor> CREATOR = new Parcelable.Creator<ImageDescriptor>() {
        /* class com.huawei.servicehost.ImageDescriptor.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ImageDescriptor createFromParcel(Parcel in) {
            long nativePtr = ImageDescriptor.nativeReadFromParcel(in);
            if (nativePtr != 0) {
                return new ImageDescriptor(nativePtr);
            }
            return null;
        }

        @Override // android.os.Parcelable.Creator
        public ImageDescriptor[] newArray(int size) {
            return new ImageDescriptor[size];
        }
    };
    public static final int GRALLOC_USAGE_EXTERNAL_DISP = 8192;
    public static final int GRALLOC_USAGE_HW_COMPOSER = 2048;
    public static final int GRALLOC_USAGE_HW_TEXTURE = 256;
    public static final int GRALLOC_USAGE_HW_VIDEO_ENCODER = 65536;
    public static final int HAL_PIXEL_FORMAT_BLOB = 33;
    public static final int HAL_PIXEL_FORMAT_IMPLEMENTATION_DEFINED = 34;
    public static final int HAL_PIXEL_FORMAT_YCBCR_420_SP = 256;
    private static final int HASH_MULTIPLIER = 31;
    private static final int HASH_SEED = 17;
    private static final String TAG = "ImageDescriptor";
    private long mNativePtr;

    private static native void nativeDispose(long j);

    private static native int nativeGetFormat(long j);

    private static native int nativeGetHeight(long j);

    private static native int nativeGetUsage(long j);

    private static native int nativeGetWidth(long j);

    private static native long nativeInit();

    /* access modifiers changed from: private */
    public static native long nativeReadFromParcel(Parcel parcel);

    private static native void nativeSetFormat(long j, int i);

    private static native void nativeSetHeight(long j, int i);

    private static native void nativeSetUsage(long j, int i);

    private static native void nativeSetWidth(long j, int i);

    private static native void nativeWriteToParcel(long j, Parcel parcel);

    static {
        System.loadLibrary("ServiceHost_jni");
    }

    private ImageDescriptor(ImageDescriptor val) {
    }

    public ImageDescriptor() {
        this.mNativePtr = nativeInit();
    }

    public ImageDescriptor(long nativePtr) {
        this.mNativePtr = nativePtr;
    }

    public int getWidth() {
        return nativeGetWidth(this.mNativePtr);
    }

    public void setWidth(int val) {
        nativeSetWidth(this.mNativePtr, val);
    }

    public int getHeight() {
        return nativeGetHeight(this.mNativePtr);
    }

    public void setHeight(int val) {
        nativeSetHeight(this.mNativePtr, val);
    }

    public int getFormat() {
        return nativeGetFormat(this.mNativePtr);
    }

    public void setFormat(int val) {
        nativeSetFormat(this.mNativePtr, val);
    }

    public int getUsage() {
        return nativeGetUsage(this.mNativePtr);
    }

    public void setUsage(int val) {
        nativeSetUsage(this.mNativePtr, val);
    }

    /* access modifiers changed from: protected */
    @Override // java.lang.Object
    public void finalize() throws Throwable {
        try {
            if (this.mNativePtr != 0) {
                nativeDispose(this.mNativePtr);
                this.mNativePtr = 0;
            }
        } finally {
            super.finalize();
        }
    }

    @Override // java.lang.Object
    public boolean equals(Object obj) {
        if (!(obj instanceof ImageDescriptor) || this.mNativePtr != ((ImageDescriptor) obj).mNativePtr) {
            return false;
        }
        return true;
    }

    @Override // java.lang.Object
    public int hashCode() {
        return (int) (((long) 17) + (this.mNativePtr * 31));
    }

    @Override // java.lang.Object
    public String toString() {
        return "";
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        long j = this.mNativePtr;
        if (j != 0) {
            nativeWriteToParcel(j, out);
            return;
        }
        throw new IllegalStateException("This ImageDescriptor has been destroyed and cannot be written to a parcel.");
    }

    public void readFromParcel(Parcel in) {
        long nativePtr = nativeReadFromParcel(in);
        if (nativePtr != 0) {
            this.mNativePtr = nativePtr;
        } else {
            Log.e(TAG, "ImageDescriptor readFromParcel failed.");
            throw new IllegalStateException("ImageDescriptor readFromParcel failed.");
        }
    }
}
