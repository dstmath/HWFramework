package android.hardware;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import dalvik.system.CloseGuard;
import libcore.util.NativeAllocationRegistry;

public final class HardwareBuffer implements Parcelable, AutoCloseable {
    public static final int BLOB = 33;
    public static final Creator<HardwareBuffer> CREATOR = new Creator<HardwareBuffer>() {
        public HardwareBuffer createFromParcel(Parcel in) {
            long nativeObject = HardwareBuffer.nReadHardwareBufferFromParcel(in);
            if (nativeObject != 0) {
                return new HardwareBuffer(nativeObject, null);
            }
            return null;
        }

        public HardwareBuffer[] newArray(int size) {
            return new HardwareBuffer[size];
        }
    };
    private static final long NATIVE_HARDWARE_BUFFER_SIZE = 232;
    public static final int RGBA_1010102 = 43;
    public static final int RGBA_8888 = 1;
    public static final int RGBA_FP16 = 22;
    public static final int RGBX_8888 = 2;
    public static final int RGB_565 = 4;
    public static final int RGB_888 = 3;
    public static final long USAGE_CPU_READ_OFTEN = 3;
    public static final long USAGE_CPU_READ_RARELY = 2;
    public static final long USAGE_CPU_WRITE_OFTEN = 48;
    public static final long USAGE_CPU_WRITE_RARELY = 32;
    public static final long USAGE_GPU_COLOR_OUTPUT = 512;
    public static final long USAGE_GPU_DATA_BUFFER = 16777216;
    public static final long USAGE_GPU_SAMPLED_IMAGE = 256;
    public static final long USAGE_PROTECTED_CONTENT = 16384;
    public static final long USAGE_SENSOR_DIRECT_DATA = 8388608;
    public static final long USAGE_VIDEO_ENCODE = 65536;
    private Runnable mCleaner;
    private final CloseGuard mCloseGuard;
    private long mNativeObject;

    /* synthetic */ HardwareBuffer(long nativeObject, HardwareBuffer -this1) {
        this(nativeObject);
    }

    private static native long nCreateHardwareBuffer(int i, int i2, int i3, int i4, long j);

    private static native int nGetFormat(long j);

    private static native int nGetHeight(long j);

    private static native int nGetLayers(long j);

    private static native long nGetNativeFinalizer();

    private static native long nGetUsage(long j);

    private static native int nGetWidth(long j);

    private static native long nReadHardwareBufferFromParcel(Parcel parcel);

    private static native void nWriteHardwareBufferToParcel(long j, Parcel parcel);

    public static HardwareBuffer create(int width, int height, int format, int layers, long usage) {
        if (!isSupportedFormat(format)) {
            throw new IllegalArgumentException("Invalid pixel format " + format);
        } else if (width <= 0) {
            throw new IllegalArgumentException("Invalid width " + width);
        } else if (height <= 0) {
            throw new IllegalArgumentException("Invalid height " + height);
        } else if (layers <= 0) {
            throw new IllegalArgumentException("Invalid layer count " + layers);
        } else if (format != 33 || height == 1) {
            long nativeObject = nCreateHardwareBuffer(width, height, format, layers, usage);
            if (nativeObject != 0) {
                return new HardwareBuffer(nativeObject);
            }
            throw new IllegalArgumentException("Unable to create a HardwareBuffer, either the dimensions passed were too large, too many image layers were requested, or an invalid set of usage flags was passed");
        } else {
            throw new IllegalArgumentException("Height must be 1 when using the BLOB format");
        }
    }

    private HardwareBuffer(long nativeObject) {
        this.mCloseGuard = CloseGuard.get();
        this.mNativeObject = nativeObject;
        this.mCleaner = new NativeAllocationRegistry(HardwareBuffer.class.getClassLoader(), nGetNativeFinalizer(), NATIVE_HARDWARE_BUFFER_SIZE).registerNativeAllocation(this, this.mNativeObject);
        this.mCloseGuard.open("close");
    }

    protected void finalize() throws Throwable {
        try {
            this.mCloseGuard.warnIfOpen();
            close();
        } finally {
            super.finalize();
        }
    }

    public int getWidth() {
        if (!isClosed()) {
            return nGetWidth(this.mNativeObject);
        }
        throw new IllegalStateException("This HardwareBuffer has been closed and its width cannot be obtained.");
    }

    public int getHeight() {
        if (!isClosed()) {
            return nGetHeight(this.mNativeObject);
        }
        throw new IllegalStateException("This HardwareBuffer has been closed and its height cannot be obtained.");
    }

    public int getFormat() {
        if (!isClosed()) {
            return nGetFormat(this.mNativeObject);
        }
        throw new IllegalStateException("This HardwareBuffer has been closed and its format cannot be obtained.");
    }

    public int getLayers() {
        if (!isClosed()) {
            return nGetLayers(this.mNativeObject);
        }
        throw new IllegalStateException("This HardwareBuffer has been closed and its layer count cannot be obtained.");
    }

    public long getUsage() {
        if (!isClosed()) {
            return nGetUsage(this.mNativeObject);
        }
        throw new IllegalStateException("This HardwareBuffer has been closed and its usage cannot be obtained.");
    }

    @Deprecated
    public void destroy() {
        close();
    }

    @Deprecated
    public boolean isDestroyed() {
        return isClosed();
    }

    public void close() {
        if (!isClosed()) {
            this.mCloseGuard.close();
            this.mNativeObject = 0;
            this.mCleaner.run();
            this.mCleaner = null;
        }
    }

    public boolean isClosed() {
        return this.mNativeObject == 0;
    }

    public int describeContents() {
        return 1;
    }

    public void writeToParcel(Parcel dest, int flags) {
        if (isClosed()) {
            throw new IllegalStateException("This HardwareBuffer has been closed and cannot be written to a parcel.");
        }
        nWriteHardwareBufferToParcel(this.mNativeObject, dest);
    }

    private static boolean isSupportedFormat(int format) {
        switch (format) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 22:
            case 33:
            case 43:
                return true;
            default:
                return false;
        }
    }
}
