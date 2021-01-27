package ohos.agp.render;

import ohos.aafwk.utils.log.LogDomain;
import ohos.agp.utils.MemoryCleanerRegistry;
import ohos.agp.utils.NativeMemoryCleanerHelper;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class Picture {
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) LogDomain.END, "AGP_RenderPicture");
    private int mHeight;
    protected long mNativePictureHandle;
    private RecordingCanvas mRecordingCanvas;
    private int mWidth;

    private native long nativeBeginRecording(long j, int i, int i2);

    private native long nativeCreatePictureHandle();

    private native void nativeEndRecording(long j);

    public Picture() {
        this.mNativePictureHandle = 0;
        this.mWidth = 0;
        this.mHeight = 0;
        this.mNativePictureHandle = nativeCreatePictureHandle();
        MemoryCleanerRegistry.getInstance().register(this, new PictureCleaner(this.mNativePictureHandle));
    }

    protected static class PictureCleaner extends NativeMemoryCleanerHelper {
        private native void nativePictureRelease(long j);

        public PictureCleaner(long j) {
            super(j);
        }

        /* access modifiers changed from: protected */
        @Override // ohos.agp.utils.NativeMemoryCleanerHelper
        public void releaseNativeMemory(long j) {
            if (j != 0) {
                nativePictureRelease(j);
            }
        }
    }

    public long getNativeHandle() {
        return this.mNativePictureHandle;
    }

    public Canvas beginRecording(int i, int i2) {
        if (this.mRecordingCanvas != null) {
            HiLog.error(TAG, "Picture already recording, must call endRecording() method", new Object[0]);
            return null;
        }
        this.mWidth = i;
        this.mHeight = i2;
        this.mRecordingCanvas = new RecordingCanvas(this, nativeBeginRecording(this.mNativePictureHandle, i, i2));
        return this.mRecordingCanvas;
    }

    public void endRecording() {
        if (this.mRecordingCanvas != null) {
            this.mRecordingCanvas = null;
            nativeEndRecording(this.mNativePictureHandle);
        }
    }

    public int getWidth() {
        return this.mWidth;
    }

    public int getHeight() {
        return this.mHeight;
    }

    /* access modifiers changed from: private */
    public static class RecordingCanvas extends Canvas {
        private final Picture mPicture;

        public RecordingCanvas(Picture picture, long j) {
            super(j);
            this.mPicture = picture;
        }

        @Override // ohos.agp.render.Canvas
        public void drawPicture(Picture picture) {
            if (this.mPicture == picture) {
                HiLog.error(Picture.TAG, "Cannot draw a picture into its recording canvas", new Object[0]);
            } else {
                super.drawPicture(picture);
            }
        }
    }
}
