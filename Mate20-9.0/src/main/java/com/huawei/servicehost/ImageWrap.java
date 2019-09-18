package com.huawei.servicehost;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class ImageWrap implements Parcelable {
    public static final Parcelable.Creator<ImageWrap> CREATOR = new Parcelable.Creator<ImageWrap>() {
        public ImageWrap createFromParcel(Parcel in) {
            long nativePtr = ImageWrap.nativeReadFromParcel(in);
            if (nativePtr != 0) {
                return new ImageWrap(nativePtr);
            }
            return null;
        }

        public ImageWrap[] newArray(int size) {
            return new ImageWrap[size];
        }
    };
    private static final boolean DEBUG = false;
    private static final String TAG = "ImageWrap";
    private long mNativePtr;

    private static native void nativeDispose(long j);

    private static native long nativeGetCapacity(long j);

    private static native ByteBuffer nativeGetData(long j);

    private static native long nativeInit();

    /* access modifiers changed from: private */
    public static native long nativeReadFromParcel(Parcel parcel);

    private static native void nativeSetData(long j, ByteBuffer byteBuffer);

    private static native void nativeWriteToParcel(long j, Parcel parcel);

    static {
        System.loadLibrary("ServiceHost_jni");
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        try {
            release();
        } finally {
            super.finalize();
        }
    }

    private ImageWrap() {
        this.mNativePtr = nativeInit();
    }

    private ImageWrap(long nativePtr) {
        this.mNativePtr = nativePtr;
    }

    private ImageWrap(ImageWrap val) {
    }

    public void swap(ImageWrap val) {
        long nPtr = val.mNativePtr;
        val.mNativePtr = this.mNativePtr;
        this.mNativePtr = nPtr;
    }

    public void release() {
        if (this.mNativePtr != 0) {
            nativeDispose(this.mNativePtr);
            this.mNativePtr = 0;
        }
    }

    public long getCapacity() {
        if (this.mNativePtr != 0) {
            return nativeGetCapacity(this.mNativePtr);
        }
        Log.e(TAG, "invalid ImageWrap.");
        return 0;
    }

    public ByteBuffer getData() {
        if (this.mNativePtr != 0) {
            return nativeGetData(this.mNativePtr);
        }
        Log.e(TAG, "invalid ImageWrap.");
        return null;
    }

    public void setData(ByteBuffer byteBuffer) {
        if (this.mNativePtr != 0) {
            nativeSetData(this.mNativePtr, byteBuffer);
        }
    }

    public void writeDataToFile(String file) {
        ByteBuffer data = getData();
        if (data == null) {
            Log.e(TAG, "getData failed.");
            return;
        }
        BufferedOutputStream out = null;
        try {
            out = new BufferedOutputStream(new FileOutputStream(file));
            byte[] b = new byte[data.capacity()];
            data.rewind();
            data.get(b);
            out.write(b);
            try {
                out.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
            if (out != null) {
                out.close();
            }
        } catch (Exception e2) {
            e2.printStackTrace();
            if (out != null) {
                out.close();
            }
        } catch (Throwable th) {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e12) {
                    e12.printStackTrace();
                }
            }
            throw th;
        }
    }

    public ByteBuffer readDataFromFile(String file) {
        FileInputStream fileInputStream = null;
        MappedByteBuffer byteBuffer = null;
        FileChannel channel = null;
        try {
            FileInputStream fileInputStream2 = new FileInputStream(file);
            FileChannel channel2 = fileInputStream2.getChannel();
            byteBuffer = channel2.map(FileChannel.MapMode.READ_ONLY, 0, channel2.size());
            if (channel2 != null) {
                try {
                    channel2.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            fileInputStream2.close();
        } catch (FileNotFoundException e2) {
            e2.printStackTrace();
            if (channel != null) {
                channel.close();
            }
            if (fileInputStream != null) {
                fileInputStream.close();
            }
        } catch (IOException e3) {
            e3.printStackTrace();
            if (channel != null) {
                channel.close();
            }
            if (fileInputStream != null) {
                fileInputStream.close();
            }
        } catch (Throwable th) {
            if (channel != null) {
                try {
                    channel.close();
                } catch (Exception e4) {
                    e4.printStackTrace();
                    throw th;
                }
            }
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            throw th;
        }
        setData(byteBuffer);
        return byteBuffer;
    }

    public boolean equals(Object o) {
        boolean z = o instanceof ImageWrap;
        boolean z2 = DEBUG;
        if (!z) {
            return DEBUG;
        }
        if (this.mNativePtr == ((ImageWrap) o).mNativePtr) {
            z2 = true;
        }
        return z2;
    }

    public int hashCode() {
        return (int) (((long) 17) + (31 * this.mNativePtr));
    }

    public String toString() {
        return "";
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        if (this.mNativePtr != 0) {
            nativeWriteToParcel(this.mNativePtr, out);
            return;
        }
        throw new IllegalStateException("This ImageWrap has been destroyed and cannot be written to a parcel.");
    }

    public void readFromParcel(Parcel in) {
        long nativePtr = nativeReadFromParcel(in);
        if (nativePtr != 0) {
            this.mNativePtr = nativePtr;
        } else {
            Log.e(TAG, "ImageWrap readFromParcel failed.");
            throw new IllegalStateException("ImageWrap readFromParcel failed.");
        }
    }
}
