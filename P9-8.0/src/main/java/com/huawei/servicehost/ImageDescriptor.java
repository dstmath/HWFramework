package com.huawei.servicehost;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.Log;

public class ImageDescriptor implements Parcelable {
    public static final Creator<ImageDescriptor> CREATOR = new Creator<ImageDescriptor>() {
        public ImageDescriptor createFromParcel(Parcel in) {
            long nativePtr = ImageDescriptor.nativeReadFromParcel(in);
            if (nativePtr != 0) {
                return new ImageDescriptor(nativePtr);
            }
            return null;
        }

        public ImageDescriptor[] newArray(int size) {
            return new ImageDescriptor[size];
        }
    };
    private static final boolean DEBUG = false;
    public static final int GRALLOC_USAGE_HW_COMPOSER = 2048;
    public static final int HAL_PIXEL_FORMAT_BLOB = 33;
    public static final int HAL_PIXEL_FORMAT_IMPLEMENTATION_DEFINED = 34;
    public static final int HAL_PIXEL_FORMAT_YCbCr_420_SP = 256;
    private static final String TAG = "ImageDescriptor";
    private long mNativePtr;

    private static native void nativeDispose(long j);

    private static native int nativeGetFormat(long j);

    private static native int nativeGetHeight(long j);

    private static native int nativeGetUsage(long j);

    private static native int nativeGetWidth(long j);

    private static native long nativeInit();

    private static native long nativeReadFromParcel(Parcel parcel);

    private static native void nativeSetFormat(long j, int i);

    private static native void nativeSetHeight(long j, int i);

    private static native void nativeSetUsage(long j, int i);

    private static native void nativeSetWidth(long j, int i);

    private static native void nativeWriteToParcel(long j, Parcel parcel);

    static {
        System.loadLibrary("ServiceHost_jni");
    }

    public int getWidth() {
        return nativeGetWidth(this.mNativePtr);
    }

    public int getHeight() {
        return nativeGetHeight(this.mNativePtr);
    }

    public int getFormat() {
        return nativeGetFormat(this.mNativePtr);
    }

    public int getUsage() {
        return nativeGetUsage(this.mNativePtr);
    }

    public void setWidth(int val) {
        nativeSetWidth(this.mNativePtr, val);
    }

    public void setHeight(int val) {
        nativeSetHeight(this.mNativePtr, val);
    }

    public void setFormat(int val) {
        nativeSetFormat(this.mNativePtr, val);
    }

    public void setUsage(int val) {
        nativeSetUsage(this.mNativePtr, val);
    }

    protected void finalize() throws Throwable {
        try {
            if (this.mNativePtr != 0) {
                nativeDispose(this.mNativePtr);
                this.mNativePtr = 0;
            }
            super.finalize();
        } catch (Throwable th) {
            super.finalize();
        }
    }

    public ImageDescriptor() {
        this.mNativePtr = nativeInit();
    }

    public ImageDescriptor(long nativePtr) {
        this.mNativePtr = nativePtr;
    }

    private ImageDescriptor(ImageDescriptor val) {
    }

    public boolean equals(Object o) {
        boolean z = DEBUG;
        if (!(o instanceof ImageDescriptor)) {
            return DEBUG;
        }
        if (this.mNativePtr == ((ImageDescriptor) o).mNativePtr) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        return (int) ((this.mNativePtr * 31) + 17);
    }

    public String toString() {
        return "";
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        if (this.mNativePtr == 0) {
            throw new IllegalStateException("This ImageDescriptor has been destroyed and cannot be written to a parcel.");
        }
        nativeWriteToParcel(this.mNativePtr, out);
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
