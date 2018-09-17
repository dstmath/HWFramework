package android.view;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class GraphicBuffer implements Parcelable {
    public static final Creator<GraphicBuffer> CREATOR = null;
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.GraphicBuffer.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.view.GraphicBuffer.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.view.GraphicBuffer.<clinit>():void");
    }

    private static native long nCreateGraphicBuffer(int i, int i2, int i3, int i4);

    private static native void nDestroyGraphicBuffer(long j);

    private static native boolean nLockCanvas(long j, Canvas canvas, Rect rect);

    private static native long nReadGraphicBufferFromParcel(Parcel parcel);

    private static native boolean nUnlockCanvasAndPost(long j, Canvas canvas);

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
            this.mSaveCount = USAGE_SW_WRITE_NEVER;
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
        return USAGE_SW_WRITE_NEVER;
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
