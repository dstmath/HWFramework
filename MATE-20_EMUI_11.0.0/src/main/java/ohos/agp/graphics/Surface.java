package ohos.agp.graphics;

import ohos.aafwk.utils.log.LogDomain;
import ohos.agp.render.Canvas;
import ohos.agp.utils.MemoryCleaner;
import ohos.agp.utils.MemoryCleanerRegistry;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public class Surface implements Sequenceable {
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) LogDomain.END, "Surface");
    private Canvas mCanvas = null;
    private long mNativePtr = 0;
    private int mNumber = 0;

    private native boolean nativeBindToTextureHolder(long j, TextureHolder textureHolder);

    private native void nativeClearRecordOp(long j);

    private native long nativeGetSurfaceHandle();

    private native boolean nativeMarshalling(long j, Parcel parcel);

    private native boolean nativeShowRawImage(long j, byte[] bArr, int i, int i2, int i3);

    private native void nativeSyncCanvasDrawCalls(long j, long j2);

    private native boolean nativeUnMarshalling(long j, Parcel parcel);

    public void release() {
    }

    static {
        System.loadLibrary("agp.z");
    }

    public enum PixelFormat {
        PIXEL_FORMAT_YCBCR_422_I(0),
        PIXEL_FORMAT_YCRCB_420_SP(1),
        PIXEL_FORMAT_YV12(2);
        
        final int enumInt;

        private PixelFormat(int i) {
            this.enumInt = i;
        }

        public int value() {
            return this.enumInt;
        }
    }

    /* access modifiers changed from: protected */
    public static class SurfaceCleaner implements MemoryCleaner {
        private long mNativePtr;

        private native void nativeRelease(long j);

        SurfaceCleaner(long j) {
            this.mNativePtr = j;
        }

        @Override // ohos.agp.utils.MemoryCleaner
        public void run() {
            long j = this.mNativePtr;
            if (j != 0) {
                nativeRelease(j);
                this.mNativePtr = 0;
            }
        }
    }

    public Surface() {
        createNativePtr();
    }

    private Surface(long j) {
        this.mNativePtr = j;
    }

    public Canvas acquireCanvas() {
        Canvas canvas = this.mCanvas;
        if (canvas == null) {
            this.mCanvas = new Canvas();
            this.mNumber++;
            return this.mCanvas;
        }
        if (this.mNumber == 0) {
            nativeClearRecordOp(canvas.getNativePtr());
        } else {
            HiLog.error(TAG, "LockCanvas mNumber is error.", new Object[0]);
        }
        this.mNumber++;
        return this.mCanvas;
    }

    public void syncCanvasDrawCalls() {
        int i = this.mNumber;
        if (i > 0) {
            this.mNumber = i - 1;
        }
        Canvas canvas = this.mCanvas;
        if (canvas != null) {
            nativeSyncCanvasDrawCalls(this.mNativePtr, canvas.getNativePtr());
        }
    }

    public boolean bindToTextureHolder(TextureHolder textureHolder) {
        return nativeBindToTextureHolder(this.mNativePtr, textureHolder);
    }

    public boolean marshalling(Parcel parcel) {
        if (parcel != null) {
            return nativeMarshalling(this.mNativePtr, parcel);
        }
        HiLog.error(TAG, "marshalling out is null", new Object[0]);
        return false;
    }

    public boolean unmarshalling(Parcel parcel) {
        if (parcel != null) {
            return nativeUnMarshalling(this.mNativePtr, parcel);
        }
        HiLog.error(TAG, "unmarshalling in is null.", new Object[0]);
        return false;
    }

    public boolean showRawImage(byte[] bArr, PixelFormat pixelFormat, int i, int i2) {
        return nativeShowRawImage(this.mNativePtr, bArr, pixelFormat.value(), i, i2);
    }

    private void createNativePtr() {
        if (this.mNativePtr == 0) {
            this.mNativePtr = nativeGetSurfaceHandle();
            MemoryCleanerRegistry.getInstance().register(this, new SurfaceCleaner(this.mNativePtr));
        }
    }
}
