package android.view;

import android.app.ActivityManager;
import android.app.ActivityThread;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.GraphicBuffer;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Region;
import android.hdm.HwDeviceManager;
import android.os.Debug;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Process;
import android.os.UserHandle;
import android.util.ArrayMap;
import android.util.Log;
import android.util.proto.ProtoOutputStream;
import android.view.Display;
import android.view.Surface;
import android.widget.Toast;
import com.android.internal.annotations.GuardedBy;
import com.huawei.pgmng.log.LogPower;
import dalvik.system.CloseGuard;
import java.io.Closeable;
import libcore.util.NativeAllocationRegistry;

public class SurfaceControl implements Parcelable {
    public static final int BUILT_IN_DISPLAY_ID_HDMI = 1;
    public static final int BUILT_IN_DISPLAY_ID_MAIN = 0;
    public static final Parcelable.Creator<SurfaceControl> CREATOR = new Parcelable.Creator<SurfaceControl>() {
        public SurfaceControl createFromParcel(Parcel in) {
            return new SurfaceControl(in);
        }

        public SurfaceControl[] newArray(int size) {
            return new SurfaceControl[size];
        }
    };
    public static final int CURSOR_WINDOW = 8192;
    public static final int FX_SURFACE_BLUR = 65536;
    public static final int FX_SURFACE_DIM = 131072;
    public static final int FX_SURFACE_MASK = 983040;
    public static final int FX_SURFACE_NORMAL = 0;
    public static final int HIDDEN = 4;
    public static final int MASK_SECURE_SCREENSHT = 8;
    public static final int MASK_SECURE_SHOT_SKIP_VASSIST = 2;
    public static final int NON_PREMULTIPLIED = 256;
    public static final int OPAQUE = 1024;
    public static final int POWER_MODE_DOZE = 1;
    public static final int POWER_MODE_DOZE_SUSPEND = 3;
    public static final int POWER_MODE_NORMAL = 2;
    public static final int POWER_MODE_OFF = 0;
    public static final int POWER_MODE_ON_SUSPEND = 4;
    public static final int PROTECTED_APP = 2048;
    public static final int SECURE = 128;
    public static final int SECURE_SCREENCAP = 32;
    public static final int SECURE_SCREENREC = 16;
    public static final int SECURE_SHOT_SKIP_VASSIST = 2;
    private static final int SURFACE_HIDDEN = 1;
    private static final int SURFACE_OPAQUE = 2;
    public static final int SURFACE_SECURE_SCREENCAP = 536870912;
    public static final int SURFACE_SECURE_SCREENREC = 268435456;
    private static final String TAG = "SurfaceControl";
    private static final String TAG_CTAIFS = "ctaifs";
    public static final int WINDOW_TYPE_DONT_SCREENSHOT = 441731;
    static Transaction sGlobalTransaction;
    private static boolean sHwInfo = true;
    static long sTransactionNestCount = 0;
    /* access modifiers changed from: private */
    public final CloseGuard mCloseGuard;
    /* access modifiers changed from: private */
    @GuardedBy("mSizeLock")
    public int mHeight;
    private final String mName;
    long mNativeObject;
    /* access modifiers changed from: private */
    public final Object mSizeLock;
    /* access modifiers changed from: private */
    @GuardedBy("mSizeLock")
    public int mWidth;

    public static class Builder {
        private int mFlags = 4;
        private int mFormat = -1;
        private int mHeight;
        private String mName;
        private int mOwnerUid = -1;
        private SurfaceControl mParent;
        private SurfaceSession mSession;
        private int mWidth;
        private int mWindowType = -1;

        public Builder(SurfaceSession session) {
            this.mSession = session;
        }

        public SurfaceControl build() {
            if (this.mWidth <= 0 || this.mHeight <= 0) {
                throw new IllegalArgumentException("width and height must be set");
            }
            SurfaceControl surfaceControl = new SurfaceControl(this.mSession, this.mName, this.mWidth, this.mHeight, this.mFormat, this.mFlags, this.mParent, this.mWindowType, this.mOwnerUid);
            return surfaceControl;
        }

        public Builder setName(String name) {
            this.mName = name;
            return this;
        }

        public Builder setSize(int width, int height) {
            if (width <= 0 || height <= 0) {
                throw new IllegalArgumentException("width and height must be positive");
            }
            this.mWidth = width;
            this.mHeight = height;
            return this;
        }

        public Builder setFormat(int format) {
            this.mFormat = format;
            return this;
        }

        public Builder setProtected(boolean protectedContent) {
            if (protectedContent) {
                this.mFlags |= 2048;
            } else {
                this.mFlags &= -2049;
            }
            return this;
        }

        public Builder setSecure(boolean secure) {
            if (secure) {
                this.mFlags |= 128;
            } else {
                this.mFlags &= -129;
            }
            return this;
        }

        public Builder setSecureScreenRecord(boolean secure) {
            if (secure) {
                this.mFlags |= 16;
            } else {
                this.mFlags &= -17;
            }
            return this;
        }

        public Builder setSecureScreenShot(boolean secure) {
            if (secure) {
                this.mFlags |= 32;
            } else {
                this.mFlags &= -33;
            }
            return this;
        }

        public Builder setOpaque(boolean opaque) {
            if (opaque) {
                this.mFlags |= 1024;
            } else {
                this.mFlags &= -1025;
            }
            return this;
        }

        public Builder setParent(SurfaceControl parent) {
            this.mParent = parent;
            return this;
        }

        public Builder setMetadata(int windowType, int ownerUid) {
            if (UserHandle.getAppId(Process.myUid()) == 1000) {
                this.mWindowType = windowType;
                this.mOwnerUid = ownerUid;
                return this;
            }
            throw new UnsupportedOperationException("It only makes sense to set Surface metadata from the WindowManager");
        }

        public Builder setColorLayer(boolean isColorLayer) {
            if (isColorLayer) {
                this.mFlags |= 131072;
            } else {
                this.mFlags &= -131073;
            }
            return this;
        }

        public Builder setFlags(int flags) {
            this.mFlags = flags;
            return this;
        }
    }

    public static final class PhysicalDisplayInfo {
        public long appVsyncOffsetNanos;
        public float density;
        public int height;
        public long presentationDeadlineNanos;
        public float refreshRate;
        public boolean secure;
        public int width;
        public float xDpi;
        public float yDpi;

        public PhysicalDisplayInfo() {
        }

        public PhysicalDisplayInfo(PhysicalDisplayInfo other) {
            copyFrom(other);
        }

        public boolean equals(Object o) {
            return (o instanceof PhysicalDisplayInfo) && equals((PhysicalDisplayInfo) o);
        }

        public boolean equals(PhysicalDisplayInfo other) {
            return other != null && this.width == other.width && this.height == other.height && this.refreshRate == other.refreshRate && this.density == other.density && this.xDpi == other.xDpi && this.yDpi == other.yDpi && this.secure == other.secure && this.appVsyncOffsetNanos == other.appVsyncOffsetNanos && this.presentationDeadlineNanos == other.presentationDeadlineNanos;
        }

        public int hashCode() {
            return 0;
        }

        public void copyFrom(PhysicalDisplayInfo other) {
            this.width = other.width;
            this.height = other.height;
            this.refreshRate = other.refreshRate;
            this.density = other.density;
            this.xDpi = other.xDpi;
            this.yDpi = other.yDpi;
            this.secure = other.secure;
            this.appVsyncOffsetNanos = other.appVsyncOffsetNanos;
            this.presentationDeadlineNanos = other.presentationDeadlineNanos;
        }

        public String toString() {
            return "PhysicalDisplayInfo{" + this.width + " x " + this.height + ", " + this.refreshRate + " fps, density " + this.density + ", " + this.xDpi + " x " + this.yDpi + " dpi, secure " + this.secure + ", appVsyncOffset " + this.appVsyncOffsetNanos + ", bufferDeadline " + this.presentationDeadlineNanos + "}";
        }
    }

    public static class Transaction implements Closeable {
        public static final NativeAllocationRegistry sRegistry;
        Runnable mFreeNativeResources = sRegistry.registerNativeAllocation(this, this.mNativeObject);
        private long mNativeObject = SurfaceControl.nativeCreateTransaction();
        private final ArrayMap<SurfaceControl, Point> mResizedSurfaces = new ArrayMap<>();

        static {
            NativeAllocationRegistry nativeAllocationRegistry = new NativeAllocationRegistry(Transaction.class.getClassLoader(), SurfaceControl.nativeGetNativeTransactionFinalizer(), 512);
            sRegistry = nativeAllocationRegistry;
        }

        public void apply() {
            apply(false);
        }

        public void close() {
            this.mFreeNativeResources.run();
            this.mNativeObject = 0;
        }

        public void apply(boolean sync) {
            applyResizedSurfaces();
            SurfaceControl.nativeApplyTransaction(this.mNativeObject, sync);
        }

        private void applyResizedSurfaces() {
            for (int i = this.mResizedSurfaces.size() - 1; i >= 0; i--) {
                Point size = this.mResizedSurfaces.valueAt(i);
                SurfaceControl surfaceControl = this.mResizedSurfaces.keyAt(i);
                synchronized (surfaceControl.mSizeLock) {
                    int unused = surfaceControl.mWidth = size.x;
                    int unused2 = surfaceControl.mHeight = size.y;
                }
            }
            this.mResizedSurfaces.clear();
        }

        public Transaction show(SurfaceControl sc) {
            sc.checkNotReleased();
            SurfaceControl.nativeSetFlags(this.mNativeObject, sc.mNativeObject, 0, 1);
            return this;
        }

        public Transaction hide(SurfaceControl sc) {
            sc.checkNotReleased();
            SurfaceControl.nativeSetFlags(this.mNativeObject, sc.mNativeObject, 1, 1);
            return this;
        }

        public Transaction setPosition(SurfaceControl sc, float x, float y) {
            sc.checkNotReleased();
            SurfaceControl.nativeSetPosition(this.mNativeObject, sc.mNativeObject, x, y);
            return this;
        }

        public Transaction setSize(SurfaceControl sc, int w, int h) {
            sc.checkNotReleased();
            this.mResizedSurfaces.put(sc, new Point(w, h));
            SurfaceControl.nativeSetSize(this.mNativeObject, sc.mNativeObject, w, h);
            return this;
        }

        public void setSurfaceLowResolutionInfo(SurfaceControl sc, float scaleFactor, int mode) {
            sc.checkNotReleased();
            SurfaceControl.nativeSetSurfaceLowResolutionInfo(this.mNativeObject, sc.mNativeObject, scaleFactor, mode);
        }

        public Transaction setLayer(SurfaceControl sc, int z) {
            sc.checkNotReleased();
            SurfaceControl.nativeSetLayer(this.mNativeObject, sc.mNativeObject, z);
            return this;
        }

        public Transaction setRelativeLayer(SurfaceControl sc, SurfaceControl relativeTo, int z) {
            sc.checkNotReleased();
            SurfaceControl.nativeSetRelativeLayer(this.mNativeObject, sc.mNativeObject, relativeTo.getHandle(), z);
            return this;
        }

        public Transaction setTransparentRegionHint(SurfaceControl sc, Region transparentRegion) {
            sc.checkNotReleased();
            SurfaceControl.nativeSetTransparentRegionHint(this.mNativeObject, sc.mNativeObject, transparentRegion);
            return this;
        }

        public Transaction setAlpha(SurfaceControl sc, float alpha) {
            sc.checkNotReleased();
            SurfaceControl.nativeSetAlpha(this.mNativeObject, sc.mNativeObject, alpha);
            return this;
        }

        public Transaction setMatrix(SurfaceControl sc, float dsdx, float dtdx, float dtdy, float dsdy) {
            sc.checkNotReleased();
            if (dsdx == Float.POSITIVE_INFINITY || dsdy == Float.POSITIVE_INFINITY || dtdx == Float.POSITIVE_INFINITY || dtdy == Float.POSITIVE_INFINITY) {
                Log.i(SurfaceControl.TAG, "dsdx " + dsdx + " dsdy " + dsdy + " dtdx " + dtdx + " dtdy " + dtdy + " sc " + sc + " this " + this + " caller " + Debug.getCallers(6));
            }
            SurfaceControl.nativeSetMatrix(this.mNativeObject, sc.mNativeObject, dsdx, dtdx, dtdy, dsdy);
            return this;
        }

        public Transaction setMatrix(SurfaceControl sc, Matrix matrix, float[] float9) {
            matrix.getValues(float9);
            setMatrix(sc, float9[0], float9[3], float9[1], float9[4]);
            setPosition(sc, float9[2], float9[5]);
            return this;
        }

        public Transaction setWindowCrop(SurfaceControl sc, Rect crop) {
            SurfaceControl surfaceControl = sc;
            Rect rect = crop;
            sc.checkNotReleased();
            if (rect != null) {
                SurfaceControl.nativeSetWindowCrop(this.mNativeObject, surfaceControl.mNativeObject, rect.left, rect.top, rect.right, rect.bottom);
            } else {
                SurfaceControl.nativeSetWindowCrop(this.mNativeObject, surfaceControl.mNativeObject, 0, 0, 0, 0);
            }
            return this;
        }

        public Transaction setFinalCrop(SurfaceControl sc, Rect crop) {
            SurfaceControl surfaceControl = sc;
            Rect rect = crop;
            sc.checkNotReleased();
            if (rect != null) {
                SurfaceControl.nativeSetFinalCrop(this.mNativeObject, surfaceControl.mNativeObject, rect.left, rect.top, rect.right, rect.bottom);
            } else {
                SurfaceControl.nativeSetFinalCrop(this.mNativeObject, surfaceControl.mNativeObject, 0, 0, 0, 0);
            }
            return this;
        }

        public Transaction setLayerStack(SurfaceControl sc, int layerStack) {
            sc.checkNotReleased();
            SurfaceControl.nativeSetLayerStack(this.mNativeObject, sc.mNativeObject, layerStack);
            return this;
        }

        public Transaction deferTransactionUntil(SurfaceControl sc, IBinder handle, long frameNumber) {
            if (frameNumber < 0) {
                return this;
            }
            sc.checkNotReleased();
            SurfaceControl.nativeDeferTransactionUntil(this.mNativeObject, sc.mNativeObject, handle, frameNumber);
            return this;
        }

        public Transaction deferTransactionUntilSurface(SurfaceControl sc, Surface barrierSurface, long frameNumber) {
            if (frameNumber < 0) {
                return this;
            }
            sc.checkNotReleased();
            SurfaceControl.nativeDeferTransactionUntilSurface(this.mNativeObject, sc.mNativeObject, barrierSurface.mNativeObject, frameNumber);
            return this;
        }

        public Transaction reparentChildren(SurfaceControl sc, IBinder newParentHandle) {
            sc.checkNotReleased();
            SurfaceControl.nativeReparentChildren(this.mNativeObject, sc.mNativeObject, newParentHandle);
            return this;
        }

        public Transaction reparent(SurfaceControl sc, IBinder newParentHandle) {
            sc.checkNotReleased();
            SurfaceControl.nativeReparent(this.mNativeObject, sc.mNativeObject, newParentHandle);
            return this;
        }

        public Transaction detachChildren(SurfaceControl sc) {
            sc.checkNotReleased();
            SurfaceControl.nativeSeverChildren(this.mNativeObject, sc.mNativeObject);
            return this;
        }

        public Transaction setOverrideScalingMode(SurfaceControl sc, int overrideScalingMode) {
            sc.checkNotReleased();
            SurfaceControl.nativeSetOverrideScalingMode(this.mNativeObject, sc.mNativeObject, overrideScalingMode);
            return this;
        }

        public Transaction setColor(SurfaceControl sc, float[] color) {
            sc.checkNotReleased();
            SurfaceControl.nativeSetColor(this.mNativeObject, sc.mNativeObject, color);
            return this;
        }

        public Transaction setGeometryAppliesWithResize(SurfaceControl sc) {
            sc.checkNotReleased();
            SurfaceControl.nativeSetGeometryAppliesWithResize(this.mNativeObject, sc.mNativeObject);
            return this;
        }

        public Transaction setSecure(SurfaceControl sc, boolean isSecure) {
            sc.checkNotReleased();
            if (isSecure) {
                SurfaceControl.nativeSetFlags(this.mNativeObject, sc.mNativeObject, 128, 128);
            } else {
                SurfaceControl.nativeSetFlags(this.mNativeObject, sc.mNativeObject, 0, 128);
            }
            return this;
        }

        public Transaction setSecureRecordScreen(SurfaceControl sc, boolean isSecure) {
            SurfaceControl surfaceControl = sc;
            boolean z = isSecure;
            sc.checkNotReleased();
            Log.i(SurfaceControl.TAG, "setSecureRecordScreen " + z);
            if (z) {
                SurfaceControl.nativeSetFlags(this.mNativeObject, surfaceControl.mNativeObject, 16, 16);
            } else {
                SurfaceControl.nativeSetFlags(this.mNativeObject, surfaceControl.mNativeObject, 0, 16);
            }
            return this;
        }

        public Transaction setSecureCaptureScreen(SurfaceControl sc, boolean isSecure) {
            SurfaceControl surfaceControl = sc;
            boolean z = isSecure;
            sc.checkNotReleased();
            Log.i(SurfaceControl.TAG, "setSecureCaptureScreen " + z);
            if (z) {
                SurfaceControl.nativeSetFlags(this.mNativeObject, surfaceControl.mNativeObject, 32, 32);
            } else {
                SurfaceControl.nativeSetFlags(this.mNativeObject, surfaceControl.mNativeObject, 0, 32);
            }
            return this;
        }

        public Transaction setOpaque(SurfaceControl sc, boolean isOpaque) {
            sc.checkNotReleased();
            if (isOpaque) {
                SurfaceControl.nativeSetFlags(this.mNativeObject, sc.mNativeObject, 2, 2);
            } else {
                SurfaceControl.nativeSetFlags(this.mNativeObject, sc.mNativeObject, 0, 2);
            }
            return this;
        }

        public Transaction destroy(SurfaceControl sc) {
            sc.checkNotReleased();
            sc.mCloseGuard.close();
            SurfaceControl.nativeDestroy(this.mNativeObject, sc.mNativeObject);
            return this;
        }

        public Transaction setDisplaySurface(IBinder displayToken, Surface surface) {
            if (displayToken != null) {
                if (surface != null) {
                    synchronized (surface.mLock) {
                        SurfaceControl.nativeSetDisplaySurface(this.mNativeObject, displayToken, surface.mNativeObject);
                    }
                } else {
                    SurfaceControl.nativeSetDisplaySurface(this.mNativeObject, displayToken, 0);
                }
                return this;
            }
            throw new IllegalArgumentException("displayToken must not be null");
        }

        public Transaction setDisplayLayerStack(IBinder displayToken, int layerStack) {
            if (displayToken != null) {
                SurfaceControl.nativeSetDisplayLayerStack(this.mNativeObject, displayToken, layerStack);
                return this;
            }
            throw new IllegalArgumentException("displayToken must not be null");
        }

        public Transaction setDisplayProjection(IBinder displayToken, int orientation, Rect layerStackRect, Rect displayRect) {
            Rect rect = layerStackRect;
            Rect rect2 = displayRect;
            if (displayToken == null) {
                throw new IllegalArgumentException("displayToken must not be null");
            } else if (rect == null) {
                throw new IllegalArgumentException("layerStackRect must not be null");
            } else if (rect2 != null) {
                SurfaceControl.nativeSetDisplayProjection(this.mNativeObject, displayToken, orientation, rect.left, rect.top, rect.right, rect.bottom, rect2.left, rect2.top, rect2.right, rect2.bottom);
                return this;
            } else {
                throw new IllegalArgumentException("displayRect must not be null");
            }
        }

        public Transaction setDisplaySize(IBinder displayToken, int width, int height) {
            if (displayToken == null) {
                throw new IllegalArgumentException("displayToken must not be null");
            } else if (width <= 0 || height <= 0) {
                throw new IllegalArgumentException("width and height must be positive");
            } else {
                SurfaceControl.nativeSetDisplaySize(this.mNativeObject, displayToken, width, height);
                return this;
            }
        }

        public Transaction setAnimationTransaction() {
            SurfaceControl.nativeSetAnimationTransaction(this.mNativeObject);
            return this;
        }

        public Transaction setEarlyWakeup() {
            SurfaceControl.nativeSetEarlyWakeup(this.mNativeObject);
            return this;
        }

        public Transaction merge(Transaction other) {
            this.mResizedSurfaces.putAll(other.mResizedSurfaces);
            other.mResizedSurfaces.clear();
            SurfaceControl.nativeMergeTransaction(this.mNativeObject, other.mNativeObject);
            return this;
        }

        public Transaction setBlurRadius(SurfaceControl sc, int radius) {
            sc.checkNotReleased();
            SurfaceControl.nativeSetBlurRadius(this.mNativeObject, sc.mNativeObject, radius);
            return this;
        }

        public Transaction setBlurRound(SurfaceControl sc, int rx, int ry) {
            sc.checkNotReleased();
            SurfaceControl.nativeSetBlurRound(this.mNativeObject, sc.mNativeObject, rx, ry);
            return this;
        }

        public Transaction setBlurAlpha(SurfaceControl sc, float alpha) {
            sc.checkNotReleased();
            SurfaceControl.nativeSetBlurAlpha(this.mNativeObject, sc.mNativeObject, alpha);
            return this;
        }

        public Transaction setBlurRegion(SurfaceControl sc, Region region) {
            sc.checkNotReleased();
            SurfaceControl.nativeSetBlurRegion(this.mNativeObject, sc.mNativeObject, region);
            return this;
        }

        public Transaction setBlurBlank(SurfaceControl sc, Rect blank) {
            SurfaceControl surfaceControl = sc;
            Rect rect = blank;
            sc.checkNotReleased();
            if (rect != null) {
                SurfaceControl.nativeSetBlurBlank(this.mNativeObject, surfaceControl.mNativeObject, rect.left, rect.top, rect.right, rect.bottom);
            } else {
                SurfaceControl.nativeSetBlurBlank(this.mNativeObject, surfaceControl.mNativeObject, 0, 0, 0, 0);
            }
            return this;
        }

        public Transaction setWindowClipFlag(SurfaceControl sc, int flag) {
            sc.checkNotReleased();
            SurfaceControl.nativeSetWindowClipFlag(this.mNativeObject, sc.mNativeObject, flag);
            return this;
        }

        public Transaction setWindowClipRound(SurfaceControl sc, float rx, float ry) {
            sc.checkNotReleased();
            SurfaceControl.nativeSetWindowClipRound(this.mNativeObject, sc.mNativeObject, rx, ry);
            return this;
        }

        public Transaction setWindowClipIcon(SurfaceControl sc, int iconViewWidth, int iconViewHeight, byte[] iconPixels, int byteCount, int width, int height) {
            sc.checkNotReleased();
            SurfaceControl.nativeSetWindowClipIcon(this.mNativeObject, sc.mNativeObject, iconViewWidth, iconViewHeight, iconPixels, byteCount, width, height);
            return this;
        }
    }

    /* access modifiers changed from: private */
    public static native void nativeApplyTransaction(long j, boolean z);

    private static native GraphicBuffer nativeCaptureLayers(IBinder iBinder, Rect rect, float f);

    private static native boolean nativeClearAnimationFrameStats();

    private static native boolean nativeClearContentFrameStats(long j);

    private static native long nativeCreate(SurfaceSession surfaceSession, String str, int i, int i2, int i3, int i4, long j, int i5, int i6) throws Surface.OutOfResourcesException;

    private static native IBinder nativeCreateDisplay(String str, boolean z);

    /* access modifiers changed from: private */
    public static native long nativeCreateTransaction();

    /* access modifiers changed from: private */
    public static native void nativeDeferTransactionUntil(long j, long j2, IBinder iBinder, long j3);

    /* access modifiers changed from: private */
    public static native void nativeDeferTransactionUntilSurface(long j, long j2, long j3, long j4);

    private static native void nativeDestroy(long j);

    /* access modifiers changed from: private */
    public static native void nativeDestroy(long j, long j2);

    private static native void nativeDestroyDisplay(IBinder iBinder);

    private static native void nativeDisconnect(long j);

    private static native void nativeFreezeDisplay();

    private static native int nativeGetActiveColorMode(IBinder iBinder);

    private static native int nativeGetActiveConfig(IBinder iBinder);

    private static native boolean nativeGetAnimationFrameStats(WindowAnimationFrameStats windowAnimationFrameStats);

    private static native IBinder nativeGetBuiltInDisplay(int i);

    private static native boolean nativeGetContentFrameStats(long j, WindowContentFrameStats windowContentFrameStats);

    private static native int[] nativeGetDisplayColorModes(IBinder iBinder);

    private static native PhysicalDisplayInfo[] nativeGetDisplayConfigs(IBinder iBinder);

    private static native IBinder nativeGetHandle(long j);

    private static native Display.HdrCapabilities nativeGetHdrCapabilities(IBinder iBinder);

    /* access modifiers changed from: private */
    public static native long nativeGetNativeTransactionFinalizer();

    private static native boolean nativeGetTransformToDisplayInverse(long j);

    private static native int nativeIsRogSupport();

    /* access modifiers changed from: private */
    public static native void nativeMergeTransaction(long j, long j2);

    private static native long nativeReadFromParcel(Parcel parcel);

    private static native void nativeRelease(long j);

    /* access modifiers changed from: private */
    public static native void nativeReparent(long j, long j2, IBinder iBinder);

    /* access modifiers changed from: private */
    public static native void nativeReparentChildren(long j, long j2, IBinder iBinder);

    private static native Bitmap nativeScreenshot(IBinder iBinder, Rect rect, int i, int i2, int i3, int i4, boolean z, boolean z2, int i5);

    private static native void nativeScreenshot(IBinder iBinder, Surface surface, Rect rect, int i, int i2, int i3, int i4, boolean z, boolean z2);

    private static native GraphicBuffer nativeScreenshotToBuffer(IBinder iBinder, Rect rect, int i, int i2, int i3, int i4, boolean z, boolean z2, int i5);

    private static native boolean nativeSetActiveColorMode(IBinder iBinder, int i);

    private static native boolean nativeSetActiveConfig(IBinder iBinder, int i);

    /* access modifiers changed from: private */
    public static native void nativeSetAlpha(long j, long j2, float f);

    /* access modifiers changed from: private */
    public static native void nativeSetAnimationTransaction(long j);

    /* access modifiers changed from: private */
    public static native void nativeSetBlurAlpha(long j, long j2, float f);

    /* access modifiers changed from: private */
    public static native void nativeSetBlurBlank(long j, long j2, int i, int i2, int i3, int i4);

    /* access modifiers changed from: private */
    public static native void nativeSetBlurRadius(long j, long j2, int i);

    /* access modifiers changed from: private */
    public static native void nativeSetBlurRegion(long j, long j2, Region region);

    /* access modifiers changed from: private */
    public static native void nativeSetBlurRound(long j, long j2, int i, int i2);

    /* access modifiers changed from: private */
    public static native void nativeSetColor(long j, long j2, float[] fArr);

    /* access modifiers changed from: private */
    public static native void nativeSetDisplayLayerStack(long j, IBinder iBinder, int i);

    private static native void nativeSetDisplayPowerMode(IBinder iBinder, int i);

    /* access modifiers changed from: private */
    public static native void nativeSetDisplayProjection(long j, IBinder iBinder, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9);

    /* access modifiers changed from: private */
    public static native void nativeSetDisplaySize(long j, IBinder iBinder, int i, int i2);

    /* access modifiers changed from: private */
    public static native void nativeSetDisplaySurface(long j, IBinder iBinder, long j2);

    /* access modifiers changed from: private */
    public static native void nativeSetEarlyWakeup(long j);

    /* access modifiers changed from: private */
    public static native void nativeSetFinalCrop(long j, long j2, int i, int i2, int i3, int i4);

    /* access modifiers changed from: private */
    public static native void nativeSetFlags(long j, long j2, int i, int i2);

    /* access modifiers changed from: private */
    public static native void nativeSetGeometryAppliesWithResize(long j, long j2);

    /* access modifiers changed from: private */
    public static native void nativeSetLayer(long j, long j2, int i);

    /* access modifiers changed from: private */
    public static native void nativeSetLayerStack(long j, long j2, int i);

    /* access modifiers changed from: private */
    public static native void nativeSetMatrix(long j, long j2, float f, float f2, float f3, float f4);

    /* access modifiers changed from: private */
    public static native void nativeSetOverrideScalingMode(long j, long j2, int i);

    /* access modifiers changed from: private */
    public static native void nativeSetPosition(long j, long j2, float f, float f2);

    /* access modifiers changed from: private */
    public static native void nativeSetRelativeLayer(long j, long j2, IBinder iBinder, int i);

    private static native int nativeSetRogDisplayConfigFull(int i, int i2, int i3, int i4);

    private static native boolean nativeSetSecureScreenRecShotFlag(IBinder iBinder, int i, int i2);

    /* access modifiers changed from: private */
    public static native void nativeSetSize(long j, long j2, int i, int i2);

    /* access modifiers changed from: private */
    public static native void nativeSetSurfaceLowResolutionInfo(long j, long j2, float f, int i);

    /* access modifiers changed from: private */
    public static native void nativeSetTransparentRegionHint(long j, long j2, Region region);

    /* access modifiers changed from: private */
    public static native void nativeSetWindowClipFlag(long j, long j2, int i);

    /* access modifiers changed from: private */
    public static native void nativeSetWindowClipIcon(long j, long j2, int i, int i2, byte[] bArr, int i3, int i4, int i5);

    /* access modifiers changed from: private */
    public static native void nativeSetWindowClipRound(long j, long j2, float f, float f2);

    /* access modifiers changed from: private */
    public static native void nativeSetWindowCrop(long j, long j2, int i, int i2, int i3, int i4);

    /* access modifiers changed from: private */
    public static native void nativeSeverChildren(long j, long j2);

    private static native void nativeUnfreezeDisplay();

    private static native void nativeWriteToParcel(long j, Parcel parcel);

    private static native boolean nativehwIsClientValid(long j);

    private SurfaceControl(SurfaceSession session, String name, int w, int h, int format, int flags, SurfaceControl parent, int windowType, int ownerUid) throws Surface.OutOfResourcesException, IllegalArgumentException {
        long j;
        String str = name;
        SurfaceControl surfaceControl = parent;
        this.mCloseGuard = CloseGuard.get();
        this.mSizeLock = new Object();
        if (session == null) {
            int i = w;
            throw new IllegalArgumentException("session must not be null");
        } else if (str != null) {
            if ((flags & 4) == 0) {
                Log.w(TAG, "Surfaces should always be created with the HIDDEN flag set to ensure that they are not made visible prematurely before all of the surface's properties have been configured.  Set the other properties and make the surface visible within a transaction.  New surface name: " + str, new Throwable());
            }
            this.mName = str;
            int i2 = w;
            this.mWidth = i2;
            int i3 = h;
            this.mHeight = i3;
            if (surfaceControl != null) {
                j = surfaceControl.mNativeObject;
            } else {
                j = 0;
            }
            this.mNativeObject = nativeCreate(session, str, i2, i3, format, flags, j, windowType, ownerUid);
            if (this.mNativeObject != 0) {
                this.mCloseGuard.open("release");
                return;
            }
            throw new Surface.OutOfResourcesException("Couldn't allocate SurfaceControl native object");
        } else {
            int i4 = w;
            throw new IllegalArgumentException("name must not be null");
        }
    }

    public SurfaceControl(SurfaceControl other) {
        this.mCloseGuard = CloseGuard.get();
        this.mSizeLock = new Object();
        this.mName = other.mName;
        this.mWidth = other.mWidth;
        this.mHeight = other.mHeight;
        this.mNativeObject = other.mNativeObject;
        other.mCloseGuard.close();
        other.mNativeObject = 0;
        this.mCloseGuard.open("release");
    }

    private SurfaceControl(Parcel in) {
        this.mCloseGuard = CloseGuard.get();
        this.mSizeLock = new Object();
        this.mName = in.readString();
        this.mWidth = in.readInt();
        this.mHeight = in.readInt();
        this.mNativeObject = nativeReadFromParcel(in);
        if (this.mNativeObject != 0) {
            this.mCloseGuard.open("release");
            return;
        }
        throw new IllegalArgumentException("Couldn't read SurfaceControl from parcel=" + in);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mName);
        dest.writeInt(this.mWidth);
        dest.writeInt(this.mHeight);
        nativeWriteToParcel(this.mNativeObject, dest);
    }

    public void writeToProto(ProtoOutputStream proto, long fieldId) {
        long token = proto.start(fieldId);
        proto.write(1120986464257L, System.identityHashCode(this));
        proto.write(1138166333442L, this.mName);
        proto.end(token);
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        try {
            if (this.mCloseGuard != null) {
                this.mCloseGuard.warnIfOpen();
            }
            if (this.mNativeObject != 0) {
                nativeRelease(this.mNativeObject);
            }
        } finally {
            super.finalize();
        }
    }

    public void release() {
        if (this.mNativeObject != 0) {
            nativeRelease(this.mNativeObject);
            this.mNativeObject = 0;
        }
        this.mCloseGuard.close();
    }

    public void destroy() {
        if (this.mNativeObject != 0) {
            nativeDestroy(this.mNativeObject);
            this.mNativeObject = 0;
        }
        this.mCloseGuard.close();
    }

    public boolean hwIsNativeClientValid() {
        if (this.mNativeObject == 0) {
            return false;
        }
        Log.d(TAG, "SurfaceControl#hwIsNativeClientValid   mNativeObject = " + this.mNativeObject);
        return nativehwIsClientValid(this.mNativeObject);
    }

    public void disconnect() {
        if (this.mNativeObject != 0) {
            nativeDisconnect(this.mNativeObject);
        }
    }

    /* access modifiers changed from: private */
    public void checkNotReleased() {
        if (this.mNativeObject == 0) {
            throw new NullPointerException("mNativeObject is null. Have you called release() already?");
        }
    }

    public static void openTransaction() {
        synchronized (SurfaceControl.class) {
            if (sGlobalTransaction == null) {
                sGlobalTransaction = new Transaction();
            }
            synchronized (SurfaceControl.class) {
                sTransactionNestCount++;
            }
        }
    }

    private static void closeTransaction(boolean sync) {
        synchronized (SurfaceControl.class) {
            if (sTransactionNestCount == 0) {
                Log.e(TAG, "Call to SurfaceControl.closeTransaction without matching openTransaction");
            } else {
                long j = sTransactionNestCount - 1;
                sTransactionNestCount = j;
                if (j > 0) {
                    return;
                }
            }
            sGlobalTransaction.apply(sync);
        }
    }

    @Deprecated
    public static void mergeToGlobalTransaction(Transaction t) {
        synchronized (SurfaceControl.class) {
            sGlobalTransaction.merge(t);
        }
    }

    public static void closeTransaction() {
        closeTransaction(false);
    }

    public static void closeTransactionSync() {
        closeTransaction(true);
    }

    public void deferTransactionUntil(IBinder handle, long frame) {
        synchronized (SurfaceControl.class) {
            sGlobalTransaction.deferTransactionUntil(this, handle, frame);
        }
    }

    public void deferTransactionUntil(Surface barrier, long frame) {
        synchronized (SurfaceControl.class) {
            sGlobalTransaction.deferTransactionUntilSurface(this, barrier, frame);
        }
    }

    public void reparentChildren(IBinder newParentHandle) {
        synchronized (SurfaceControl.class) {
            sGlobalTransaction.reparentChildren(this, newParentHandle);
        }
    }

    public void reparent(IBinder newParentHandle) {
        synchronized (SurfaceControl.class) {
            sGlobalTransaction.reparent(this, newParentHandle);
        }
    }

    public void detachChildren() {
        synchronized (SurfaceControl.class) {
            sGlobalTransaction.detachChildren(this);
        }
    }

    public void setOverrideScalingMode(int scalingMode) {
        checkNotReleased();
        synchronized (SurfaceControl.class) {
            sGlobalTransaction.setOverrideScalingMode(this, scalingMode);
        }
    }

    public IBinder getHandle() {
        return nativeGetHandle(this.mNativeObject);
    }

    public static void setAnimationTransaction() {
        synchronized (SurfaceControl.class) {
            sGlobalTransaction.setAnimationTransaction();
        }
    }

    public void setLayer(int zorder) {
        checkNotReleased();
        synchronized (SurfaceControl.class) {
            sGlobalTransaction.setLayer(this, zorder);
        }
    }

    public void setRelativeLayer(SurfaceControl relativeTo, int zorder) {
        checkNotReleased();
        synchronized (SurfaceControl.class) {
            sGlobalTransaction.setRelativeLayer(this, relativeTo, zorder);
        }
    }

    public void setPosition(float x, float y) {
        checkNotReleased();
        synchronized (SurfaceControl.class) {
            sGlobalTransaction.setPosition(this, x, y);
        }
    }

    public void setGeometryAppliesWithResize() {
        checkNotReleased();
        synchronized (SurfaceControl.class) {
            sGlobalTransaction.setGeometryAppliesWithResize(this);
        }
    }

    public void setSize(int w, int h) {
        checkNotReleased();
        synchronized (SurfaceControl.class) {
            sGlobalTransaction.setSize(this, w, h);
        }
    }

    public void hide() {
        checkNotReleased();
        synchronized (SurfaceControl.class) {
            sGlobalTransaction.hide(this);
        }
    }

    public void show() {
        checkNotReleased();
        synchronized (SurfaceControl.class) {
            sGlobalTransaction.show(this);
        }
    }

    public void setTransparentRegionHint(Region region) {
        checkNotReleased();
        synchronized (SurfaceControl.class) {
            sGlobalTransaction.setTransparentRegionHint(this, region);
        }
    }

    public boolean clearContentFrameStats() {
        checkNotReleased();
        return nativeClearContentFrameStats(this.mNativeObject);
    }

    public boolean getContentFrameStats(WindowContentFrameStats outStats) {
        checkNotReleased();
        return nativeGetContentFrameStats(this.mNativeObject, outStats);
    }

    public static boolean clearAnimationFrameStats() {
        return nativeClearAnimationFrameStats();
    }

    public static boolean getAnimationFrameStats(WindowAnimationFrameStats outStats) {
        return nativeGetAnimationFrameStats(outStats);
    }

    public void setAlpha(float alpha) {
        checkNotReleased();
        synchronized (SurfaceControl.class) {
            sGlobalTransaction.setAlpha(this, alpha);
        }
    }

    public void setColor(float[] color) {
        checkNotReleased();
        synchronized (SurfaceControl.class) {
            sGlobalTransaction.setColor(this, color);
        }
    }

    public void setMatrix(float dsdx, float dtdx, float dtdy, float dsdy) {
        checkNotReleased();
        synchronized (SurfaceControl.class) {
            sGlobalTransaction.setMatrix(this, dsdx, dtdx, dtdy, dsdy);
        }
    }

    public void setMatrix(Matrix matrix, float[] float9) {
        checkNotReleased();
        matrix.getValues(float9);
        synchronized (SurfaceControl.class) {
            sGlobalTransaction.setMatrix(this, float9[0], float9[3], float9[1], float9[4]);
            sGlobalTransaction.setPosition(this, float9[2], float9[5]);
        }
    }

    public void setWindowCrop(Rect crop) {
        checkNotReleased();
        synchronized (SurfaceControl.class) {
            sGlobalTransaction.setWindowCrop(this, crop);
        }
    }

    public void setFinalCrop(Rect crop) {
        checkNotReleased();
        synchronized (SurfaceControl.class) {
            sGlobalTransaction.setFinalCrop(this, crop);
        }
    }

    public void setLayerStack(int layerStack) {
        checkNotReleased();
        synchronized (SurfaceControl.class) {
            sGlobalTransaction.setLayerStack(this, layerStack);
        }
    }

    public void setOpaque(boolean isOpaque) {
        checkNotReleased();
        synchronized (SurfaceControl.class) {
            sGlobalTransaction.setOpaque(this, isOpaque);
        }
    }

    public void setSecure(boolean isSecure) {
        checkNotReleased();
        synchronized (SurfaceControl.class) {
            sGlobalTransaction.setSecure(this, isSecure);
        }
    }

    public int getWidth() {
        int i;
        synchronized (this.mSizeLock) {
            i = this.mWidth;
        }
        return i;
    }

    public int getHeight() {
        int i;
        synchronized (this.mSizeLock) {
            i = this.mHeight;
        }
        return i;
    }

    public String toString() {
        return "Surface(name=" + this.mName + ")/@0x" + Integer.toHexString(System.identityHashCode(this));
    }

    public void setSecureScreenRecord(boolean isSecure) {
        checkNotReleased();
        synchronized (SurfaceControl.class) {
            sGlobalTransaction.setSecureRecordScreen(this, isSecure);
        }
    }

    public void setSecureScreenShot(boolean isSecure) {
        checkNotReleased();
        synchronized (SurfaceControl.class) {
            sGlobalTransaction.setSecureCaptureScreen(this, isSecure);
        }
    }

    public static Bitmap screenshot_ext_hw(Rect sourceCrop, int width, int height, int minLayer, int maxLayer, boolean useIdentityTransform, int rotation) {
        nativeSetSecureScreenRecShotFlag(null, 8, 8);
        Bitmap bitmap = screenshot(sourceCrop, width, height, minLayer, maxLayer, useIdentityTransform, rotation);
        setScreenshotSkipVAssist(false);
        return bitmap;
    }

    public static Bitmap screenshot_ext_hw(Rect sourceCrop, int width, int height, int rotation) {
        nativeSetSecureScreenRecShotFlag(null, 8, 8);
        Bitmap bitmap = screenshot(sourceCrop, width, height, rotation);
        setScreenshotSkipVAssist(false);
        return bitmap;
    }

    public static void setScreenshotSkipVAssist(boolean isSkip) {
        Log.i(TAG, "setScreenshotSkipVAssist isSkip:" + isSkip);
        if (isSkip) {
            nativeSetSecureScreenRecShotFlag(null, 2, 2);
        } else {
            nativeSetSecureScreenRecShotFlag(null, 0, 2);
        }
    }

    public static void setDisplayPowerMode(IBinder displayToken, int mode) {
        if (displayToken != null) {
            nativeSetDisplayPowerMode(displayToken, mode);
            return;
        }
        throw new IllegalArgumentException("displayToken must not be null");
    }

    public static PhysicalDisplayInfo[] getDisplayConfigs(IBinder displayToken) {
        if (displayToken != null) {
            return nativeGetDisplayConfigs(displayToken);
        }
        throw new IllegalArgumentException("displayToken must not be null");
    }

    public static int getActiveConfig(IBinder displayToken) {
        if (displayToken != null) {
            return nativeGetActiveConfig(displayToken);
        }
        throw new IllegalArgumentException("displayToken must not be null");
    }

    public static boolean setActiveConfig(IBinder displayToken, int id) {
        if (displayToken != null) {
            return nativeSetActiveConfig(displayToken, id);
        }
        throw new IllegalArgumentException("displayToken must not be null");
    }

    public static int[] getDisplayColorModes(IBinder displayToken) {
        if (displayToken != null) {
            return nativeGetDisplayColorModes(displayToken);
        }
        throw new IllegalArgumentException("displayToken must not be null");
    }

    public static int getActiveColorMode(IBinder displayToken) {
        if (displayToken != null) {
            return nativeGetActiveColorMode(displayToken);
        }
        throw new IllegalArgumentException("displayToken must not be null");
    }

    public static boolean setActiveColorMode(IBinder displayToken, int colorMode) {
        if (displayToken != null) {
            return nativeSetActiveColorMode(displayToken, colorMode);
        }
        throw new IllegalArgumentException("displayToken must not be null");
    }

    public static void setDisplayProjection(IBinder displayToken, int orientation, Rect layerStackRect, Rect displayRect) {
        synchronized (SurfaceControl.class) {
            sGlobalTransaction.setDisplayProjection(displayToken, orientation, layerStackRect, displayRect);
        }
    }

    public static void setDisplayLayerStack(IBinder displayToken, int layerStack) {
        synchronized (SurfaceControl.class) {
            sGlobalTransaction.setDisplayLayerStack(displayToken, layerStack);
        }
    }

    public static void setDisplaySurface(IBinder displayToken, Surface surface) {
        synchronized (SurfaceControl.class) {
            sGlobalTransaction.setDisplaySurface(displayToken, surface);
        }
    }

    public static void setDisplaySize(IBinder displayToken, int width, int height) {
        synchronized (SurfaceControl.class) {
            sGlobalTransaction.setDisplaySize(displayToken, width, height);
        }
    }

    public static Display.HdrCapabilities getHdrCapabilities(IBinder displayToken) {
        if (displayToken != null) {
            return nativeGetHdrCapabilities(displayToken);
        }
        throw new IllegalArgumentException("displayToken must not be null");
    }

    public static IBinder createDisplay(String name, boolean secure) {
        if (name != null) {
            return nativeCreateDisplay(name, secure);
        }
        throw new IllegalArgumentException("name must not be null");
    }

    public static void destroyDisplay(IBinder displayToken) {
        if (displayToken != null) {
            nativeDestroyDisplay(displayToken);
            return;
        }
        throw new IllegalArgumentException("displayToken must not be null");
    }

    public static IBinder getBuiltInDisplay(int builtInDisplayId) {
        return nativeGetBuiltInDisplay(builtInDisplayId);
    }

    public static int setRogDisplayConfigFull(int width, int height, int density, int configmode) {
        return nativeSetRogDisplayConfigFull(width, height, density, configmode);
    }

    public static void freezeDisplay() {
        nativeFreezeDisplay();
    }

    public static void unfreezeDisplay() {
        nativeUnfreezeDisplay();
    }

    public void setSurfaceLowResolutionInfo(float ratio, int mode) {
        checkNotReleased();
        synchronized (SurfaceControl.class) {
            sGlobalTransaction.setSurfaceLowResolutionInfo(this, ratio, mode);
        }
    }

    public static int isRogSupport() {
        return nativeIsRogSupport();
    }

    public static void screenshot(IBinder display, Surface consumer, int width, int height, int minLayer, int maxLayer, boolean useIdentityTransform) {
        screenshot(display, consumer, new Rect(), width, height, minLayer, maxLayer, false, useIdentityTransform);
    }

    public static void screenshot(IBinder display, Surface consumer, int width, int height) {
        screenshot(display, consumer, new Rect(), width, height, 0, 0, true, false);
    }

    public static void screenshot(IBinder display, Surface consumer) {
        screenshot(display, consumer, new Rect(), 0, 0, 0, 0, true, false);
    }

    public static void screenshot_ext_hw(IBinder display, Surface consumer) {
        nativeSetSecureScreenRecShotFlag(null, 8, 8);
        screenshot(display, consumer, new Rect(), 0, 0, 0, 0, true, false);
    }

    public static Bitmap screenshot(Rect sourceCrop, int width, int height, int minLayer, int maxLayer, boolean useIdentityTransform, int rotation) {
        if (showMDMToast()) {
            return null;
        }
        if (sHwInfo) {
            log();
        }
        return nativeScreenshot(getBuiltInDisplay(0), sourceCrop, width, height, minLayer, maxLayer, false, useIdentityTransform, rotation);
    }

    public static GraphicBuffer screenshotToBuffer(Rect sourceCrop, int width, int height, int minLayer, int maxLayer, boolean useIdentityTransform, int rotation) {
        return nativeScreenshotToBuffer(getBuiltInDisplay(0), sourceCrop, width, height, minLayer, maxLayer, false, useIdentityTransform, rotation);
    }

    public static Bitmap screenshot(Rect sourceCrop, int width, int height, int rotation) {
        if (showMDMToast()) {
            return null;
        }
        if (sHwInfo) {
            log();
        }
        IBinder displayToken = getBuiltInDisplay(0);
        int i = 3;
        if (rotation == 1 || rotation == 3) {
            if (rotation != 1) {
                i = 1;
            }
            rotation = i;
        }
        rotateCropForSF(sourceCrop, rotation);
        LogPower.push(175);
        Bitmap bm = nativeScreenshot(displayToken, sourceCrop, width, height, 0, 0, true, false, rotation);
        LogPower.push(176);
        return bm;
    }

    public static Bitmap screenshot(IBinder displayToken, int width, int height) {
        if (showMDMToast()) {
            return null;
        }
        if (sHwInfo) {
            log();
        }
        nativeSetSecureScreenRecShotFlag(displayToken, 8, 8);
        LogPower.push(175);
        Bitmap bm = nativeScreenshot(displayToken, new Rect(), width, height, 0, 0, true, false, 0);
        LogPower.push(176);
        return bm;
    }

    public static GraphicBuffer screenshotToBufferForExternalDisplay(IBinder displayToken, Rect sourceCrop, int width, int height, int minLayer, int maxLayer, boolean useIdentityTransform, int rotation) {
        return nativeScreenshotToBuffer(displayToken, sourceCrop, width, height, minLayer, maxLayer, false, useIdentityTransform, rotation);
    }

    private static void screenshot(IBinder display, Surface consumer, Rect sourceCrop, int width, int height, int minLayer, int maxLayer, boolean allLayers, boolean useIdentityTransform) {
        if (sHwInfo) {
            log();
        }
        if (display == null) {
            throw new IllegalArgumentException("displayToken must not be null");
        } else if (consumer != null) {
            nativeScreenshot(display, consumer, sourceCrop, width, height, minLayer, maxLayer, allLayers, useIdentityTransform);
        } else {
            throw new IllegalArgumentException("consumer must not be null");
        }
    }

    private static void rotateCropForSF(Rect crop, int rot) {
        if (rot == 1 || rot == 3) {
            int tmp = crop.top;
            crop.top = crop.left;
            crop.left = tmp;
            int tmp2 = crop.right;
            crop.right = crop.bottom;
            crop.bottom = tmp2;
        }
    }

    public static GraphicBuffer captureLayers(IBinder layerHandleToken, Rect sourceCrop, float frameScale) {
        return nativeCaptureLayers(layerHandleToken, sourceCrop, frameScale);
    }

    public static void log() {
        String[] processAndAppName = getProcessAndAppName();
        if (processAndAppName[0] != null && processAndAppName[1] == null) {
            Log.i(TAG_CTAIFS, " <" + processAndAppName[0] + ">[" + processAndAppName[0] + "][" + processAndAppName[0] + "]:[SurfaceControl.screenshot] " + processAndAppName[2]);
        } else if (processAndAppName[1] != null) {
            Log.i(TAG_CTAIFS, " <" + processAndAppName[1] + ">[" + processAndAppName[1] + "][" + processAndAppName[0] + "]:[SurfaceControl.screenshot] " + processAndAppName[2]);
        }
    }

    public static String[] getProcessAndAppName() {
        String[] processAndAppName = new String[3];
        if (ActivityThread.currentProcessName() != null) {
            Context context = ActivityThread.currentActivityThread().getSystemContext();
            if (context == null) {
                return processAndAppName;
            }
            processAndAppName[2] = context.getString(33685929);
            int pid = Process.myPid();
            ActivityManager manager = (ActivityManager) context.getSystemService("activity");
            PackageManager pm = context.getPackageManager();
            if (pm == null) {
                return processAndAppName;
            }
            for (ActivityManager.RunningAppProcessInfo process : manager.getRunningAppProcesses()) {
                if (process.pid == pid) {
                    processAndAppName[0] = process.processName;
                    try {
                        processAndAppName[1] = pm.getApplicationLabel(pm.getApplicationInfo(process.processName, 128)).toString();
                    } catch (PackageManager.NameNotFoundException e) {
                    }
                }
            }
        }
        return processAndAppName;
    }

    public void setBlurRadius(int radius) {
        checkNotReleased();
        synchronized (SurfaceControl.class) {
            sGlobalTransaction.setBlurRadius(this, radius);
        }
    }

    public void setBlurRound(int rx, int ry) {
        checkNotReleased();
        synchronized (SurfaceControl.class) {
            sGlobalTransaction.setBlurRound(this, rx, ry);
        }
    }

    public void setBlurAlpha(float alpha) {
        checkNotReleased();
        synchronized (SurfaceControl.class) {
            sGlobalTransaction.setBlurAlpha(this, alpha);
        }
    }

    public void setBlurRegion(Region region) {
        checkNotReleased();
        synchronized (SurfaceControl.class) {
            sGlobalTransaction.setBlurRegion(this, region);
        }
    }

    public void setBlurBlank(Rect blank) {
        checkNotReleased();
        synchronized (SurfaceControl.class) {
            sGlobalTransaction.setBlurBlank(this, blank);
        }
    }

    public void setWindowClipFlag(int flag) {
        checkNotReleased();
        synchronized (SurfaceControl.class) {
            sGlobalTransaction.setWindowClipFlag(this, flag);
        }
    }

    public void setWindowClipRound(float rx, float ry) {
        checkNotReleased();
        synchronized (SurfaceControl.class) {
            sGlobalTransaction.setWindowClipRound(this, rx, ry);
        }
    }

    public void setWindowClipIcon(int iconViewWidth, int iconViewHeight, byte[] iconPixels, int byteCount, int width, int height) {
        checkNotReleased();
        synchronized (SurfaceControl.class) {
            sGlobalTransaction.setWindowClipIcon(this, iconViewWidth, iconViewHeight, iconPixels, byteCount, width, height);
        }
    }

    public static boolean showMDMToast() {
        if (!HwDeviceManager.mdmDisallowOp(20, null)) {
            return false;
        }
        ActivityThread activityThread = ActivityThread.currentActivityThread();
        if (activityThread != null) {
            Context context = activityThread.getApplication();
            if (context != null) {
                try {
                    Toast.makeText(context, (CharSequence) context.getResources().getString(33685934), 0).show();
                } catch (RuntimeException e) {
                    Log.e(TAG, "Caught a Runtime Exception in showMDMToast");
                }
            }
        }
        return true;
    }
}
