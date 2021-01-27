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
        /* class com.huawei.servicehost.ImageWrap.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ImageWrap createFromParcel(Parcel in) {
            long nativePtr = ImageWrap.nativeReadFromParcel(in);
            if (nativePtr != 0) {
                return new ImageWrap(nativePtr);
            }
            return null;
        }

        @Override // android.os.Parcelable.Creator
        public ImageWrap[] newArray(int size) {
            return new ImageWrap[size];
        }
    };
    private static final int HASH_MULTIPLIER = 31;
    private static final int HASH_SEED = 17;
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

    private ImageWrap() {
        this.mNativePtr = nativeInit();
    }

    private ImageWrap(long nativePtr) {
        this.mNativePtr = nativePtr;
    }

    private ImageWrap(ImageWrap val) {
    }

    /* access modifiers changed from: protected */
    @Override // java.lang.Object
    public void finalize() throws Throwable {
        try {
            release();
        } finally {
            super.finalize();
        }
    }

    public void swap(ImageWrap imageWrap) {
        long nativePtr = imageWrap.mNativePtr;
        imageWrap.mNativePtr = this.mNativePtr;
        this.mNativePtr = nativePtr;
    }

    public void release() {
        long j = this.mNativePtr;
        if (j != 0) {
            nativeDispose(j);
            this.mNativePtr = 0;
        }
    }

    public long getCapacity() {
        long j = this.mNativePtr;
        if (j != 0) {
            return nativeGetCapacity(j);
        }
        Log.e(TAG, "invalid ImageWrap.");
        return 0;
    }

    public ByteBuffer getData() {
        long j = this.mNativePtr;
        if (j != 0) {
            return nativeGetData(j);
        }
        Log.e(TAG, "invalid ImageWrap.");
        return null;
    }

    public void setData(ByteBuffer byteBuffer) {
        long j = this.mNativePtr;
        if (j != 0) {
            nativeSetData(j, byteBuffer);
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
            byte[] bytes = new byte[data.capacity()];
            data.rewind();
            data.get(bytes);
            out.write(bytes);
            try {
                out.close();
            } catch (IOException e) {
                Log.d(TAG, "writeDataToFile, IOException");
            }
        } catch (FileNotFoundException e2) {
            Log.d(TAG, "writeDataToFile, FileNotFoundException");
            if (out != null) {
                out.close();
            }
        } catch (IOException e3) {
            Log.d(TAG, "writeDataToFile, IOException");
            if (out != null) {
                out.close();
            }
        } catch (Throwable th) {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e4) {
                    Log.d(TAG, "writeDataToFile, IOException");
                }
            }
            throw th;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:24:0x0047 A[SYNTHETIC, Splitter:B:24:0x0047] */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x0053  */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x0062 A[SYNTHETIC, Splitter:B:34:0x0062] */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x006e  */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x0079 A[SYNTHETIC, Splitter:B:44:0x0079] */
    /* JADX WARNING: Removed duplicated region for block: B:49:0x0085 A[SYNTHETIC, Splitter:B:49:0x0085] */
    public ByteBuffer readDataFromFile(String file) {
        FileChannel channel;
        Throwable th;
        FileInputStream fileInputStream = null;
        MappedByteBuffer byteBuffer = null;
        try {
            fileInputStream = new FileInputStream(file);
            channel = fileInputStream.getChannel();
            try {
                byteBuffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
                try {
                    channel.close();
                } catch (IOException e) {
                    Log.e(TAG, "readDataFromFile, IOException when close channel");
                }
                try {
                    fileInputStream.close();
                } catch (IOException e2) {
                    Log.e(TAG, "readDataFromFile, IOException when close fileInputStream");
                }
            } catch (FileNotFoundException e3) {
                Log.e(TAG, "readDataFromFile, FileNotFoundException");
                if (channel != null) {
                    try {
                        channel.close();
                    } catch (IOException e4) {
                        Log.e(TAG, "readDataFromFile, IOException when close channel");
                    }
                }
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                setData(byteBuffer);
                return byteBuffer;
            } catch (IOException e5) {
                try {
                    Log.e(TAG, "readDataFromFile, IOException");
                    if (channel != null) {
                        try {
                            channel.close();
                        } catch (IOException e6) {
                            Log.e(TAG, "readDataFromFile, IOException when close channel");
                        }
                    }
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                    setData(byteBuffer);
                    return byteBuffer;
                } catch (Throwable th2) {
                    th = th2;
                    if (channel != null) {
                        try {
                            channel.close();
                        } catch (IOException e7) {
                            Log.e(TAG, "readDataFromFile, IOException when close channel");
                        }
                    }
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e8) {
                            Log.e(TAG, "readDataFromFile, IOException when close fileInputStream");
                        }
                    }
                    throw th;
                }
            }
        } catch (FileNotFoundException e9) {
            channel = null;
            Log.e(TAG, "readDataFromFile, FileNotFoundException");
            if (channel != null) {
            }
            if (fileInputStream != null) {
            }
            setData(byteBuffer);
            return byteBuffer;
        } catch (IOException e10) {
            channel = null;
            Log.e(TAG, "readDataFromFile, IOException");
            if (channel != null) {
            }
            if (fileInputStream != null) {
            }
            setData(byteBuffer);
            return byteBuffer;
        } catch (Throwable th3) {
            channel = null;
            th = th3;
            if (channel != null) {
            }
            if (fileInputStream != null) {
            }
            throw th;
        }
        setData(byteBuffer);
        return byteBuffer;
    }

    @Override // java.lang.Object
    public boolean equals(Object obj) {
        if (!(obj instanceof ImageWrap) || this.mNativePtr != ((ImageWrap) obj).mNativePtr) {
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
