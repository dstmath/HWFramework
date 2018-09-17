package android.graphics;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class GraphicBuffer implements Parcelable {
    public static final Creator<GraphicBuffer> CREATOR = new Creator<GraphicBuffer>() {
        public GraphicBuffer createFromParcel(Parcel in) {
            int width = in.readInt();
            int height = in.readInt();
            int format = in.readInt();
            int usage = in.readInt();
            long nativeObject = GraphicBuffer.nReadGraphicBufferFromParcel(in);
            if (nativeObject != 0) {
                return new GraphicBuffer(width, height, format, usage, nativeObject, null);
            }
            return null;
        }

        public GraphicBuffer[] newArray(int size) {
            return new GraphicBuffer[size];
        }
    };
    public static final int USAGE_HW_2D = 1024;
    public static final int USAGE_HW_COMPOSER = 2048;
    public static final int USAGE_HW_MASK = 466688;
    public static final int USAGE_HW_RENDER = 512;
    public static final int USAGE_HW_TEXTURE = 256;
    public static final int USAGE_HW_VIDEO_ENCODER = 65536;
    public static final int USAGE_PROTECTED = 16384;
    public static final int USAGE_SOFTWARE_MASK = 255;
    public static final int USAGE_SW_READ_MASK = 15;
    public static final int USAGE_SW_READ_NEVER = 0;
    public static final int USAGE_SW_READ_OFTEN = 3;
    public static final int USAGE_SW_READ_RARELY = 2;
    public static final int USAGE_SW_WRITE_MASK = 240;
    public static final int USAGE_SW_WRITE_NEVER = 0;
    public static final int USAGE_SW_WRITE_OFTEN = 48;
    public static final int USAGE_SW_WRITE_RARELY = 32;
    private Canvas mCanvas;
    private boolean mDestroyed;
    private final int mFormat;
    private final int mHeight;
    private final long mNativeObject;
    private int mSaveCount;
    private final int mUsage;
    private final int mWidth;

    /* synthetic */ GraphicBuffer(int width, int height, int format, int usage, long nativeObject, GraphicBuffer -this5) {
        this(width, height, format, usage, nativeObject);
    }

    private static native long nCreateGraphicBuffer(int i, int i2, int i3, int i4);

    private static native void nDestroyGraphicBuffer(long j);

    private static native boolean nLockCanvas(long j, Canvas canvas, Rect rect);

    private static native long nReadGraphicBufferFromParcel(Parcel parcel);

    private static native boolean nUnlockCanvasAndPost(long j, Canvas canvas);

    private static native long nWrapGraphicBuffer(long j);

    private static native void nWriteGraphicBufferToParcel(long j, Parcel parcel);

    public static GraphicBuffer create(int width, int height, int format, int usage) {
        long nativeObject = nCreateGraphicBuffer(width, height, format, usage);
        if (nativeObject != 0) {
            return new GraphicBuffer(width, height, format, usage, nativeObject);
        }
        return null;
    }

    private GraphicBuffer(int width, int height, int format, int usage, long nativeObject) {
        this.mWidth = width;
        this.mHeight = height;
        this.mFormat = format;
        this.mUsage = usage;
        this.mNativeObject = nativeObject;
    }

    public static GraphicBuffer createFromExisting(int width, int height, int format, int usage, long unwrappedNativeObject) {
        long nativeObject = nWrapGraphicBuffer(unwrappedNativeObject);
        if (nativeObject != 0) {
            return new GraphicBuffer(width, height, format, usage, nativeObject);
        }
        return null;
    }

    public int getWidth() {
        return this.mWidth;
    }

    public int getHeight() {
        return this.mHeight;
    }

    public int getFormat() {
        return this.mFormat;
    }

    public int getUsage() {
        return this.mUsage;
    }

    public Canvas lockCanvas() {
        return lockCanvas(null);
    }

    public Canvas lockCanvas(Rect dirty) {
        if (this.mDestroyed) {
            return null;
        }
        if (this.mCanvas == null) {
            this.mCanvas = new Canvas();
        }
        if (!nLockCanvas(this.mNativeObject, this.mCanvas, dirty)) {
            return null;
        }
        this.mSaveCount = this.mCanvas.save();
        return this.mCanvas;
    }

    public void unlockCanvasAndPost(Canvas canvas) {
        if (!this.mDestroyed && this.mCanvas != null && canvas == this.mCanvas) {
            canvas.restoreToCount(this.mSaveCount);
            this.mSaveCount = 0;
            nUnlockCanvasAndPost(this.mNativeObject, this.mCanvas);
        }
    }

    public void destroy() {
        if (!this.mDestroyed) {
            this.mDestroyed = true;
            nDestroyGraphicBuffer(this.mNativeObject);
        }
    }

    public boolean isDestroyed() {
        return this.mDestroyed;
    }

    protected void finalize() throws Throwable {
        try {
            if (!this.mDestroyed) {
                nDestroyGraphicBuffer(this.mNativeObject);
            }
            super.finalize();
        } catch (Throwable th) {
            super.finalize();
        }
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        if (this.mDestroyed) {
            throw new IllegalStateException("This GraphicBuffer has been destroyed and cannot be written to a parcel.");
        }
        dest.writeInt(this.mWidth);
        dest.writeInt(this.mHeight);
        dest.writeInt(this.mFormat);
        dest.writeInt(this.mUsage);
        nWriteGraphicBufferToParcel(this.mNativeObject, dest);
    }
}
