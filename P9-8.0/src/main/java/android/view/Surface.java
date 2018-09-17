package android.view;

import android.content.res.CompatibilityInfo.Translator;
import android.graphics.Canvas;
import android.graphics.GraphicBuffer;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.SystemProperties;
import android.util.Log;
import dalvik.system.CloseGuard;

public class Surface implements Parcelable {
    public static final String APP_INTELLIGENT = "com.huawei.intelligent";
    public static final String APP_LAUNCHER = "com.huawei.android.launcher";
    public static final Creator<Surface> CREATOR = new Creator<Surface>() {
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

        public Surface[] newArray(int size) {
            return new Surface[size];
        }
    };
    public static final int ROTATION_0 = 0;
    public static final int ROTATION_180 = 2;
    public static final int ROTATION_270 = 3;
    public static final int ROTATION_90 = 1;
    public static final int SCALING_MODE_FREEZE = 0;
    public static final int SCALING_MODE_NO_SCALE_CROP = 3;
    public static final int SCALING_MODE_SCALE_CROP = 2;
    public static final int SCALING_MODE_SCALE_TO_WINDOW = 1;
    private static final String TAG = "Surface";
    public static final String TOAST = "Toast";
    private static final boolean mbCheckGameJankFrame = SystemProperties.getBoolean("persist.jank.gameskip", false);
    public String mAppName;
    private final Canvas mCanvas = new CompatibleCanvas(this, null);
    private final CloseGuard mCloseGuard = CloseGuard.get();
    private Matrix mCompatibleMatrix;
    private int mGenerationId;
    private HwuiContext mHwuiContext;
    private boolean mIsSingleBuffered;
    final Object mLock = new Object();
    private long mLockedObject;
    private String mName;
    public long mNativeObject;
    public boolean mSurfaceControllerIsValid;

    private final class CompatibleCanvas extends Canvas {
        private Matrix mOrigMatrix;

        /* synthetic */ CompatibleCanvas(Surface this$0, CompatibleCanvas -this1) {
            this();
        }

        private CompatibleCanvas() {
            this.mOrigMatrix = null;
        }

        public void setMatrix(Matrix matrix) {
            if (Surface.this.mCompatibleMatrix == null || this.mOrigMatrix == null || this.mOrigMatrix.equals(matrix)) {
                super.setMatrix(matrix);
                return;
            }
            Matrix m = new Matrix(Surface.this.mCompatibleMatrix);
            m.preConcat(matrix);
            super.setMatrix(m);
        }

        public void getMatrix(Matrix m) {
            super.getMatrix(m);
            if (this.mOrigMatrix == null) {
                this.mOrigMatrix = new Matrix();
            }
            this.mOrigMatrix.set(m);
        }
    }

    private final class HwuiContext {
        private DisplayListCanvas mCanvas;
        private long mHwuiRenderer;
        private final RenderNode mRenderNode = RenderNode.create("HwuiCanvas", null);

        HwuiContext() {
            this.mRenderNode.setClipToBounds(false);
            this.mHwuiRenderer = Surface.nHwuiCreate(this.mRenderNode.mNativeRenderNode, Surface.this.mNativeObject);
        }

        Canvas lockCanvas(int width, int height) {
            if (this.mCanvas != null) {
                throw new IllegalStateException("Surface was already locked!");
            }
            this.mCanvas = this.mRenderNode.start(width, height);
            return this.mCanvas;
        }

        void unlockAndPost(Canvas canvas) {
            if (canvas != this.mCanvas) {
                throw new IllegalArgumentException("canvas object must be the same instance that was previously returned by lockCanvas");
            }
            this.mRenderNode.end(this.mCanvas);
            this.mCanvas = null;
            Surface.nHwuiDraw(this.mHwuiRenderer);
        }

        void updateSurface() {
            Surface.nHwuiSetSurface(this.mHwuiRenderer, Surface.this.mNativeObject);
        }

        void destroy() {
            if (this.mHwuiRenderer != 0) {
                Surface.nHwuiDestroy(this.mHwuiRenderer);
                this.mHwuiRenderer = 0;
            }
        }
    }

    public static class OutOfResourcesException extends RuntimeException {
        public OutOfResourcesException(String name) {
            super(name);
        }
    }

    private static native long nHwuiCreate(long j, long j2);

    private static native void nHwuiDestroy(long j);

    private static native void nHwuiDraw(long j);

    private static native void nHwuiSetSurface(long j, long j2);

    private static native void nativeAllocateBuffers(long j);

    private static native int nativeAttachAndQueueBuffer(long j, GraphicBuffer graphicBuffer);

    private static native long nativeCreateFromSurfaceControl(long j);

    private static native long nativeCreateFromSurfaceTexture(SurfaceTexture surfaceTexture) throws OutOfResourcesException;

    private static native int nativeForceScopedDisconnect(long j);

    private static native long nativeGetFromSurfaceControl(long j);

    private static native int nativeGetHeight(long j);

    private static native long nativeGetNextFrameNumber(long j);

    private static native int nativeGetWidth(long j);

    private static native boolean nativeIsConsumerRunningBehind(long j);

    private static native boolean nativeIsValid(long j);

    private static native long nativeLockCanvas(long j, Canvas canvas, Rect rect) throws OutOfResourcesException;

    private static native long nativeReadFromParcel(long j, Parcel parcel);

    private static native void nativeRelease(long j);

    private static native void nativeSetBuffersTransform(long j, long j2);

    private static native void nativeSetLowPowerDisplayLevel(int i);

    private static native void nativeSetRefreshDirty(long j, Rect rect);

    private static native void nativeSetSDRRatio(long j, float f);

    private static native int nativeSetScalingMode(long j, int i);

    private static native void nativeSyncFrameInfo(long j, long j2);

    private static native void nativeUnlockCanvasAndPost(long j, Canvas canvas);

    private static native void nativeWriteToParcel(long j, Parcel parcel);

    public Surface(SurfaceTexture surfaceTexture) {
        if (surfaceTexture == null) {
            throw new IllegalArgumentException("surfaceTexture must not be null");
        }
        this.mIsSingleBuffered = surfaceTexture.isSingleBuffered();
        synchronized (this.mLock) {
            this.mName = surfaceTexture.toString();
            setNativeObjectLocked(nativeCreateFromSurfaceTexture(surfaceTexture));
        }
    }

    private Surface(long nativeObject) {
        synchronized (this.mLock) {
            setNativeObjectLocked(nativeObject);
        }
    }

    protected void finalize() throws Throwable {
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

    public void destroy() {
        release();
    }

    public boolean isValid() {
        synchronized (this.mLock) {
            if (this.mNativeObject == 0) {
                return false;
            }
            boolean nativeIsValid = nativeIsValid(this.mNativeObject);
            return nativeIsValid;
        }
    }

    public int getGenerationId() {
        int i;
        synchronized (this.mLock) {
            i = this.mGenerationId;
        }
        return i;
    }

    public long getNextFrameNumber() {
        long nativeGetNextFrameNumber;
        synchronized (this.mLock) {
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

    public void setRefreshDirty(Rect dirty) {
        synchronized (this.mLock) {
            checkNotReleasedLocked();
            nativeSetRefreshDirty(this.mNativeObject, dirty);
        }
    }

    public void setSDRRatio(float ratio) {
        synchronized (this.mLock) {
            if (isValid()) {
                nativeSetSDRRatio(this.mNativeObject, ratio);
            }
        }
    }

    public void syncFrameInfo(Choreographer choreographer) {
        if (mbCheckGameJankFrame) {
            nativeSyncFrameInfo(this.mNativeObject, choreographer.mFrameInfo.getIntendedVsync());
        }
    }

    public Canvas lockCanvas(Rect inOutDirty) throws OutOfResourcesException, IllegalArgumentException {
        Canvas canvas;
        synchronized (this.mLock) {
            checkNotReleasedLocked();
            if (this.mLockedObject != 0) {
                throw new IllegalArgumentException("Surface was already locked");
            }
            this.mLockedObject = nativeLockCanvas(this.mNativeObject, this.mCanvas, inOutDirty);
            canvas = this.mCanvas;
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
        if (canvas != this.mCanvas) {
            throw new IllegalArgumentException("canvas object must be the same instance that was previously returned by lockCanvas");
        }
        if (this.mNativeObject != this.mLockedObject) {
            Log.w(TAG, "WARNING: Surface's mNativeObject (0x" + Long.toHexString(this.mNativeObject) + ") != mLockedObject (0x" + Long.toHexString(this.mLockedObject) + ")");
        }
        if (this.mLockedObject == 0) {
            throw new IllegalStateException("Surface was not locked");
        }
        try {
            nativeUnlockCanvasAndPost(this.mLockedObject, canvas);
        } finally {
            nativeRelease(this.mLockedObject);
            this.mLockedObject = 0;
        }
    }

    public Canvas lockHardwareCanvas() {
        Canvas lockCanvas;
        synchronized (this.mLock) {
            checkNotReleasedLocked();
            if (this.mHwuiContext == null) {
                this.mHwuiContext = new HwuiContext();
            }
            lockCanvas = this.mHwuiContext.lockCanvas(nativeGetWidth(this.mNativeObject), nativeGetHeight(this.mNativeObject));
        }
        return lockCanvas;
    }

    @Deprecated
    public void unlockCanvas(Canvas canvas) {
        throw new UnsupportedOperationException();
    }

    void setCompatibilityTranslator(Translator translator) {
        if (translator != null) {
            float appScale = translator.applicationScale;
            this.mCompatibleMatrix = new Matrix();
            this.mCompatibleMatrix.setScale(appScale, appScale);
        }
    }

    public void copyFrom(SurfaceControl other) {
        if (other == null) {
            throw new IllegalArgumentException("other must not be null");
        }
        long surfaceControlPtr = other.mNativeObject;
        if (surfaceControlPtr == 0) {
            throw new NullPointerException("null SurfaceControl native object. Are you using a released SurfaceControl?");
        }
        long newNativeObject = nativeGetFromSurfaceControl(surfaceControlPtr);
        synchronized (this.mLock) {
            if (this.mNativeObject != 0) {
                nativeRelease(this.mNativeObject);
            }
            setNativeObjectLocked(newNativeObject);
        }
    }

    public void createFrom(SurfaceControl other) {
        if (other == null) {
            throw new IllegalArgumentException("other must not be null");
        }
        long surfaceControlPtr = other.mNativeObject;
        if (surfaceControlPtr == 0) {
            throw new NullPointerException("null SurfaceControl native object. Are you using a released SurfaceControl?");
        }
        long newNativeObject = nativeCreateFromSurfaceControl(surfaceControlPtr);
        synchronized (this.mLock) {
            if (this.mNativeObject != 0) {
                nativeRelease(this.mNativeObject);
            }
            setNativeObjectLocked(newNativeObject);
        }
    }

    @Deprecated
    public void transferFrom(Surface other) {
        if (other == null) {
            throw new IllegalArgumentException("other must not be null");
        } else if (other != this) {
            long newPtr;
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

    public static void setLowPowerDisplayLevel(int level) {
        nativeSetLowPowerDisplayLevel(level);
    }

    public int describeContents() {
        return 0;
    }

    public void readFromParcel(Parcel source) {
        boolean z = false;
        if (source == null) {
            throw new IllegalArgumentException("source must not be null");
        }
        synchronized (this.mLock) {
            this.mName = source.readString();
            if (source.readInt() != 0) {
                z = true;
            }
            this.mIsSingleBuffered = z;
            setNativeObjectLocked(nativeReadFromParcel(this.mNativeObject, source));
        }
    }

    public void writeToParcel(Parcel dest, int flags) {
        int i = 0;
        if (dest == null) {
            throw new IllegalArgumentException("dest must not be null");
        }
        synchronized (this.mLock) {
            dest.writeString(this.mName);
            if (this.mIsSingleBuffered) {
                i = 1;
            }
            dest.writeInt(i);
            nativeWriteToParcel(this.mNativeObject, dest);
        }
        if ((flags & 1) != 0) {
            release();
        }
    }

    public String toString() {
        String str;
        synchronized (this.mLock) {
            str = "Surface(name=" + this.mName + ")  (appName =" + this.mAppName + ") ( mSurfaceControllerIsValid =" + this.mSurfaceControllerIsValid + ") (mNativeObject  =" + Long.toHexString(this.mNativeObject) + ")/@0x" + Integer.toHexString(System.identityHashCode(this));
        }
        return str;
    }

    public void setSurfaceControllerState(boolean status) {
        this.mSurfaceControllerIsValid = status;
    }

    private void setNativeObjectLocked(long ptr) {
        if (this.mNativeObject != ptr) {
            if (this.mNativeObject == 0 && ptr != 0) {
                this.mCloseGuard.open("release");
            } else if (this.mNativeObject != 0 && ptr == 0) {
                this.mCloseGuard.close();
            }
            this.mNativeObject = ptr;
            if (this.mNativeObject == 0) {
                this.mSurfaceControllerIsValid = false;
            } else {
                this.mSurfaceControllerIsValid = true;
            }
            this.mGenerationId++;
            if (this.mHwuiContext != null) {
                this.mHwuiContext.updateSurface();
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

    void setScalingMode(int scalingMode) {
        synchronized (this.mLock) {
            checkNotReleasedLocked();
            if (nativeSetScalingMode(this.mNativeObject, scalingMode) != 0) {
                throw new IllegalArgumentException("Invalid scaling mode: " + scalingMode);
            }
        }
    }

    void forceScopedDisconnect() {
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

    public boolean isSingleBuffered() {
        return this.mIsSingleBuffered;
    }

    public static String rotationToString(int rotation) {
        switch (rotation) {
            case 0:
                return "ROTATION_0";
            case 1:
                return "ROATATION_90";
            case 2:
                return "ROATATION_180";
            case 3:
                return "ROATATION_270";
            default:
                throw new IllegalArgumentException("Invalid rotation: " + rotation);
        }
    }
}
