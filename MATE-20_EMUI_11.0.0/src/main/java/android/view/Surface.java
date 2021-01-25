package android.view;

import android.annotation.UnsupportedAppUsage;
import android.content.res.CompatibilityInfo;
import android.graphics.Canvas;
import android.graphics.ColorSpace;
import android.graphics.GraphicBuffer;
import android.graphics.Matrix;
import android.graphics.RecordingCanvas;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.RenderNode;
import android.graphics.SurfaceTexture;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemProperties;
import android.util.Log;
import dalvik.system.CloseGuard;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;

public class Surface implements Parcelable {
    public static final Parcelable.Creator<Surface> CREATOR = new Parcelable.Creator<Surface>() {
        /* class android.view.Surface.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public Surface createFromParcel(Parcel source) {
            try {
                Surface s = new Surface();
                s.readFromParcel(source);
                return s;
            } catch (Exception e) {
                Log.e(Surface.TAG, "Exception creating surface from parcel", e);
                return null;
            }
        }

        @Override // android.os.Parcelable.Creator
        public Surface[] newArray(int size) {
            return new Surface[size];
        }
    };
    private static final boolean DEBUG_JANK_FRAME = SystemProperties.getBoolean("persist.jank.gameskip", false);
    public static final int ROTATION_0 = 0;
    public static final int ROTATION_180 = 2;
    public static final int ROTATION_270 = 3;
    public static final int ROTATION_90 = 1;
    public static final int SCALING_MODE_FREEZE = 0;
    public static final int SCALING_MODE_NO_SCALE_CROP = 3;
    public static final int SCALING_MODE_SCALE_CROP = 2;
    public static final int SCALING_MODE_SCALE_TO_WINDOW = 1;
    private static final String TAG = "Surface";
    private final Canvas mCanvas = new CompatibleCanvas();
    private final CloseGuard mCloseGuard = CloseGuard.get();
    private Matrix mCompatibleMatrix;
    private int mGenerationId;
    private HwuiContext mHwuiContext;
    private boolean mIsAutoRefreshEnabled;
    private boolean mIsSharedBufferModeEnabled;
    private boolean mIsSingleBuffered;
    @UnsupportedAppUsage
    final Object mLock = new Object();
    @UnsupportedAppUsage
    private long mLockedObject;
    @UnsupportedAppUsage
    private String mName;
    @UnsupportedAppUsage
    long mNativeObject;

    @Retention(RetentionPolicy.SOURCE)
    public @interface Rotation {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface ScalingMode {
    }

    /* access modifiers changed from: private */
    public static native long nHwuiCreate(long j, long j2, boolean z);

    /* access modifiers changed from: private */
    public static native void nHwuiDestroy(long j);

    /* access modifiers changed from: private */
    public static native void nHwuiDraw(long j);

    /* access modifiers changed from: private */
    public static native void nHwuiSetSurface(long j, long j2);

    private static native void nativeAllocateBuffers(long j);

    private static native int nativeAttachAndQueueBuffer(long j, GraphicBuffer graphicBuffer);

    private static native int nativeAttachAndQueueBufferWithColorSpace(long j, GraphicBuffer graphicBuffer, int i);

    private static native long nativeCreateFromSurfaceControl(long j);

    private static native long nativeCreateFromSurfaceTexture(SurfaceTexture surfaceTexture) throws OutOfResourcesException;

    private static native int nativeForceScopedDisconnect(long j);

    private static native int nativeGetBufferCount(long j);

    private static native long nativeGetFromSurfaceControl(long j, long j2);

    private static native int nativeGetHeight(long j);

    private static native long nativeGetNextFrameNumber(long j);

    private static native int nativeGetWidth(long j);

    private static native boolean nativeIsConsumerRunningBehind(long j);

    private static native boolean nativeIsValid(long j);

    private static native long nativeLockCanvas(long j, Canvas canvas, Rect rect) throws OutOfResourcesException;

    private static native long nativeReadFromParcel(long j, Parcel parcel);

    @UnsupportedAppUsage
    private static native void nativeRelease(long j);

    private static native int nativeSetAutoRefreshEnabled(long j, boolean z);

    private static native void nativeSetBlurRegion(long j, Region region);

    private static native int nativeSetScalingMode(long j, int i);

    private static native int nativeSetSharedBufferModeEnabled(long j, boolean z);

    private static native void nativeSetViewParam(long j, ArrayList<Rect> arrayList, float[] fArr, int i);

    private static native void nativeSyncDynBufSize(long j, int i);

    private static native void nativeSyncFrameInfo(long j, long j2);

    private static native void nativeUnlockCanvasAndPost(long j, Canvas canvas);

    private static native void nativeWriteToParcel(long j, Parcel parcel);

    @UnsupportedAppUsage
    public Surface() {
    }

    public Surface(SurfaceControl from) {
        copyFrom(from);
    }

    public Surface(SurfaceTexture surfaceTexture) {
        if (surfaceTexture != null) {
            this.mIsSingleBuffered = surfaceTexture.isSingleBuffered();
            synchronized (this.mLock) {
                this.mName = surfaceTexture.toString();
                setNativeObjectLocked(nativeCreateFromSurfaceTexture(surfaceTexture));
            }
            return;
        }
        throw new IllegalArgumentException("surfaceTexture must not be null");
    }

    @UnsupportedAppUsage
    private Surface(long nativeObject) {
        synchronized (this.mLock) {
            setNativeObjectLocked(nativeObject);
        }
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        try {
            if (this.mCloseGuard != null) {
                this.mCloseGuard.warnIfOpen();
            }
            release();
        } finally {
            super.finalize();
        }
    }

    public void release() {
        synchronized (this.mLock) {
            if (this.mNativeObject != 0) {
                nativeRelease(this.mNativeObject);
                setNativeObjectLocked(0);
            }
            if (this.mHwuiContext != null) {
                this.mHwuiContext.destroy();
                this.mHwuiContext = null;
            }
        }
    }

    @UnsupportedAppUsage
    public void destroy() {
        release();
    }

    public void hwuiDestroy() {
        HwuiContext hwuiContext = this.mHwuiContext;
        if (hwuiContext != null) {
            hwuiContext.destroy();
            this.mHwuiContext = null;
        }
    }

    public boolean isValid() {
        synchronized (this.mLock) {
            if (this.mNativeObject == 0) {
                return false;
            }
            return nativeIsValid(this.mNativeObject);
        }
    }

    public int getGenerationId() {
        int i;
        synchronized (this.mLock) {
            i = this.mGenerationId;
        }
        return i;
    }

    @UnsupportedAppUsage
    public long getNextFrameNumber() {
        long nativeGetNextFrameNumber;
        synchronized (this.mLock) {
            checkNotReleasedLocked();
            nativeGetNextFrameNumber = nativeGetNextFrameNumber(this.mNativeObject);
        }
        return nativeGetNextFrameNumber;
    }

    public boolean isConsumerRunningBehind() {
        boolean nativeIsConsumerRunningBehind;
        synchronized (this.mLock) {
            checkNotReleasedLocked();
            nativeIsConsumerRunningBehind = nativeIsConsumerRunningBehind(this.mNativeObject);
        }
        return nativeIsConsumerRunningBehind;
    }

    public Canvas lockCanvas(Rect inOutDirty) throws OutOfResourcesException, IllegalArgumentException {
        Canvas canvas;
        synchronized (this.mLock) {
            checkNotReleasedLocked();
            if (this.mLockedObject == 0) {
                this.mLockedObject = nativeLockCanvas(this.mNativeObject, this.mCanvas, inOutDirty);
                canvas = this.mCanvas;
            } else {
                throw new IllegalArgumentException("Surface was already locked");
            }
        }
        return canvas;
    }

    public void unlockCanvasAndPost(Canvas canvas) {
        synchronized (this.mLock) {
            checkNotReleasedLocked();
            if (this.mHwuiContext != null) {
                this.mHwuiContext.unlockAndPost(canvas);
            } else {
                unlockSwCanvasAndPost(canvas);
            }
        }
    }

    private void unlockSwCanvasAndPost(Canvas canvas) {
        if (canvas == this.mCanvas) {
            if (this.mNativeObject != this.mLockedObject) {
                Log.w(TAG, "WARNING: Surface's mNativeObject (0x" + Long.toHexString(this.mNativeObject) + ") != mLockedObject (0x" + Long.toHexString(this.mLockedObject) + ")");
            }
            long j = this.mLockedObject;
            if (j != 0) {
                try {
                    nativeUnlockCanvasAndPost(j, canvas);
                } finally {
                    nativeRelease(this.mLockedObject);
                    this.mLockedObject = 0;
                }
            } else {
                throw new IllegalStateException("Surface was not locked");
            }
        } else {
            throw new IllegalArgumentException("canvas object must be the same instance that was previously returned by lockCanvas");
        }
    }

    public Canvas lockHardwareCanvas() {
        Canvas lockCanvas;
        synchronized (this.mLock) {
            checkNotReleasedLocked();
            if (this.mHwuiContext == null) {
                this.mHwuiContext = new HwuiContext(false);
            }
            lockCanvas = this.mHwuiContext.lockCanvas(nativeGetWidth(this.mNativeObject), nativeGetHeight(this.mNativeObject));
        }
        return lockCanvas;
    }

    public Canvas lockHardwareWideColorGamutCanvas() {
        Canvas lockCanvas;
        synchronized (this.mLock) {
            checkNotReleasedLocked();
            if (this.mHwuiContext != null && !this.mHwuiContext.isWideColorGamut()) {
                this.mHwuiContext.destroy();
                this.mHwuiContext = null;
            }
            if (this.mHwuiContext == null) {
                this.mHwuiContext = new HwuiContext(true);
            }
            lockCanvas = this.mHwuiContext.lockCanvas(nativeGetWidth(this.mNativeObject), nativeGetHeight(this.mNativeObject));
        }
        return lockCanvas;
    }

    @Deprecated
    public void unlockCanvas(Canvas canvas) {
        throw new UnsupportedOperationException();
    }

    /* access modifiers changed from: package-private */
    public void setCompatibilityTranslator(CompatibilityInfo.Translator translator) {
        if (translator != null) {
            float appScale = translator.applicationScale;
            this.mCompatibleMatrix = new Matrix();
            this.mCompatibleMatrix.setScale(appScale, appScale);
        }
    }

    @UnsupportedAppUsage
    public void copyFrom(SurfaceControl other) {
        if (other != null) {
            long surfaceControlPtr = other.mNativeObject;
            if (surfaceControlPtr != 0) {
                long newNativeObject = nativeGetFromSurfaceControl(this.mNativeObject, surfaceControlPtr);
                synchronized (this.mLock) {
                    if (newNativeObject != this.mNativeObject) {
                        if (this.mNativeObject != 0) {
                            nativeRelease(this.mNativeObject);
                        }
                        setNativeObjectLocked(newNativeObject);
                        return;
                    }
                    return;
                }
            }
            throw new NullPointerException("null SurfaceControl native object. Are you using a released SurfaceControl?");
        }
        throw new IllegalArgumentException("other must not be null");
    }

    public void createFrom(SurfaceControl other) {
        if (other != null) {
            long surfaceControlPtr = other.mNativeObject;
            if (surfaceControlPtr != 0) {
                long newNativeObject = nativeCreateFromSurfaceControl(surfaceControlPtr);
                synchronized (this.mLock) {
                    if (this.mNativeObject != 0) {
                        nativeRelease(this.mNativeObject);
                    }
                    setNativeObjectLocked(newNativeObject);
                }
                return;
            }
            throw new NullPointerException("null SurfaceControl native object. Are you using a released SurfaceControl?");
        }
        throw new IllegalArgumentException("other must not be null");
    }

    @UnsupportedAppUsage
    @Deprecated
    public void transferFrom(Surface other) {
        long newPtr;
        if (other == null) {
            throw new IllegalArgumentException("other must not be null");
        } else if (other != this) {
            synchronized (other.mLock) {
                newPtr = other.mNativeObject;
                other.setNativeObjectLocked(0);
            }
            synchronized (this.mLock) {
                if (this.mNativeObject != 0) {
                    nativeRelease(this.mNativeObject);
                }
                setNativeObjectLocked(newPtr);
            }
        }
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public void readFromParcel(Parcel source) {
        if (source != null) {
            synchronized (this.mLock) {
                this.mName = source.readString();
                this.mIsSingleBuffered = source.readInt() != 0;
                setNativeObjectLocked(nativeReadFromParcel(this.mNativeObject, source));
            }
            return;
        }
        throw new IllegalArgumentException("source must not be null");
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        if (dest != null) {
            synchronized (this.mLock) {
                dest.writeString(this.mName);
                dest.writeInt(this.mIsSingleBuffered ? 1 : 0);
                nativeWriteToParcel(this.mNativeObject, dest);
            }
            if ((flags & 1) != 0) {
                release();
                return;
            }
            return;
        }
        throw new IllegalArgumentException("dest must not be null");
    }

    public String toString() {
        String str;
        synchronized (this.mLock) {
            str = "Surface(name=" + this.mName + ")/@0x" + Integer.toHexString(System.identityHashCode(this));
        }
        return str;
    }

    private void setNativeObjectLocked(long ptr) {
        long j = this.mNativeObject;
        if (j != ptr) {
            if (j == 0 && ptr != 0) {
                this.mCloseGuard.open("release");
            } else if (this.mNativeObject != 0 && ptr == 0) {
                this.mCloseGuard.close();
            }
            this.mNativeObject = ptr;
            this.mGenerationId++;
            HwuiContext hwuiContext = this.mHwuiContext;
            if (hwuiContext != null) {
                hwuiContext.updateSurface();
            }
        }
    }

    private void checkNotReleasedLocked() {
        if (this.mNativeObject == 0) {
            throw new IllegalStateException("Surface has already been released.");
        }
    }

    public void allocateBuffers() {
        synchronized (this.mLock) {
            checkNotReleasedLocked();
            nativeAllocateBuffers(this.mNativeObject);
        }
    }

    /* access modifiers changed from: package-private */
    public void setScalingMode(int scalingMode) {
        synchronized (this.mLock) {
            checkNotReleasedLocked();
            if (nativeSetScalingMode(this.mNativeObject, scalingMode) != 0) {
                throw new IllegalArgumentException("Invalid scaling mode: " + scalingMode);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void forceScopedDisconnect() {
        synchronized (this.mLock) {
            checkNotReleasedLocked();
            if (nativeForceScopedDisconnect(this.mNativeObject) != 0) {
                throw new RuntimeException("Failed to disconnect Surface instance (bad object?)");
            }
        }
    }

    public void attachAndQueueBuffer(GraphicBuffer buffer) {
        synchronized (this.mLock) {
            checkNotReleasedLocked();
            if (nativeAttachAndQueueBuffer(this.mNativeObject, buffer) != 0) {
                throw new RuntimeException("Failed to attach and queue buffer to Surface (bad object?)");
            }
        }
    }

    public void attachAndQueueBufferWithColorSpace(GraphicBuffer buffer, ColorSpace colorSpace) {
        synchronized (this.mLock) {
            checkNotReleasedLocked();
            if (colorSpace == null) {
                colorSpace = ColorSpace.get(ColorSpace.Named.SRGB);
            }
            int err = nativeAttachAndQueueBufferWithColorSpace(this.mNativeObject, buffer, colorSpace.getId());
            if (err != 0) {
                throw new RuntimeException("Failed to attach and queue buffer to Surface (bad object?), native error: " + err);
            }
        }
    }

    public boolean isSingleBuffered() {
        return this.mIsSingleBuffered;
    }

    public void setSharedBufferModeEnabled(boolean enabled) {
        if (this.mIsSharedBufferModeEnabled == enabled) {
            return;
        }
        if (nativeSetSharedBufferModeEnabled(this.mNativeObject, enabled) == 0) {
            this.mIsSharedBufferModeEnabled = enabled;
            return;
        }
        throw new RuntimeException("Failed to set shared buffer mode on Surface (bad object?)");
    }

    public boolean isSharedBufferModeEnabled() {
        return this.mIsSharedBufferModeEnabled;
    }

    public void setAutoRefreshEnabled(boolean enabled) {
        if (this.mIsAutoRefreshEnabled == enabled) {
            return;
        }
        if (nativeSetAutoRefreshEnabled(this.mNativeObject, enabled) == 0) {
            this.mIsAutoRefreshEnabled = enabled;
            return;
        }
        throw new RuntimeException("Failed to set auto refresh on Surface (bad object?)");
    }

    public boolean isAutoRefreshEnabled() {
        return this.mIsAutoRefreshEnabled;
    }

    public static class OutOfResourcesException extends RuntimeException {
        public OutOfResourcesException() {
        }

        public OutOfResourcesException(String name) {
            super(name);
        }
    }

    public static String rotationToString(int rotation) {
        if (rotation == 0) {
            return "ROTATION_0";
        }
        if (rotation == 1) {
            return "ROTATION_90";
        }
        if (rotation == 2) {
            return "ROTATION_180";
        }
        if (rotation != 3) {
            return Integer.toString(rotation);
        }
        return "ROTATION_270";
    }

    private final class CompatibleCanvas extends Canvas {
        private Matrix mOrigMatrix;

        private CompatibleCanvas() {
            this.mOrigMatrix = null;
        }

        @Override // android.graphics.Canvas
        public void setMatrix(Matrix matrix) {
            Matrix matrix2;
            if (Surface.this.mCompatibleMatrix == null || (matrix2 = this.mOrigMatrix) == null || matrix2.equals(matrix)) {
                super.setMatrix(matrix);
                return;
            }
            Matrix m = new Matrix(Surface.this.mCompatibleMatrix);
            m.preConcat(matrix);
            super.setMatrix(m);
        }

        @Override // android.graphics.Canvas
        public void getMatrix(Matrix m) {
            super.getMatrix(m);
            if (this.mOrigMatrix == null) {
                this.mOrigMatrix = new Matrix();
            }
            this.mOrigMatrix.set(m);
        }
    }

    /* access modifiers changed from: private */
    public final class HwuiContext {
        private RecordingCanvas mCanvas;
        private long mHwuiRenderer;
        private final boolean mIsWideColorGamut;
        private final RenderNode mRenderNode = RenderNode.create("HwuiCanvas", null);

        HwuiContext(boolean isWideColorGamut) {
            this.mRenderNode.setClipToBounds(false);
            this.mRenderNode.setForceDarkAllowed(false);
            this.mIsWideColorGamut = isWideColorGamut;
            this.mHwuiRenderer = Surface.nHwuiCreate(this.mRenderNode.mNativeRenderNode, Surface.this.mNativeObject, isWideColorGamut);
        }

        /* access modifiers changed from: package-private */
        public Canvas lockCanvas(int width, int height) {
            if (this.mCanvas == null) {
                this.mCanvas = this.mRenderNode.beginRecording(width, height);
                return this.mCanvas;
            }
            throw new IllegalStateException("Surface was already locked!");
        }

        /* access modifiers changed from: package-private */
        public void unlockAndPost(Canvas canvas) {
            if (canvas == this.mCanvas) {
                this.mRenderNode.endRecording();
                this.mCanvas = null;
                Surface.nHwuiDraw(this.mHwuiRenderer);
                return;
            }
            throw new IllegalArgumentException("canvas object must be the same instance that was previously returned by lockCanvas");
        }

        /* access modifiers changed from: package-private */
        public void updateSurface() {
            Surface.nHwuiSetSurface(this.mHwuiRenderer, Surface.this.mNativeObject);
        }

        /* access modifiers changed from: package-private */
        public void destroy() {
            long j = this.mHwuiRenderer;
            if (j != 0) {
                Surface.nHwuiDestroy(j);
                this.mHwuiRenderer = 0;
            }
        }

        /* access modifiers changed from: package-private */
        public boolean isWideColorGamut() {
            return this.mIsWideColorGamut;
        }
    }

    public void syncFrameInfo(Choreographer choreographer) {
        if (DEBUG_JANK_FRAME) {
            nativeSyncFrameInfo(this.mNativeObject, choreographer.mFrameInfo.getIntendedVsync());
        }
    }

    @UnsupportedAppUsage
    public void setBlurRegion(Region region) {
        synchronized (this.mLock) {
            checkNotReleasedLocked();
            nativeSetBlurRegion(this.mNativeObject, region);
        }
    }

    @UnsupportedAppUsage
    public void setViewParam(ArrayList<Rect> viewRectList, float[] viewAlphaArray, int length) {
        synchronized (this.mLock) {
            checkNotReleasedLocked();
            nativeSetViewParam(this.mNativeObject, viewRectList, viewAlphaArray, length);
        }
    }

    @UnsupportedAppUsage
    public int getBufferCount() {
        return nativeGetBufferCount(this.mNativeObject);
    }

    @UnsupportedAppUsage
    public void syncDynamicBufSize(int size) {
        nativeSyncDynBufSize(this.mNativeObject, size);
    }
}
