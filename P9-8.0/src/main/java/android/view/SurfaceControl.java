package android.view;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityThread;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.GraphicBuffer;
import android.graphics.Rect;
import android.graphics.Region;
import android.hdm.HwDeviceManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.Process;
import android.rms.iaware.DataContract.Apps.LaunchMode;
import android.util.Log;
import android.view.Display.HdrCapabilities;
import android.view.Surface.OutOfResourcesException;
import android.widget.Toast;
import com.huawei.pgmng.log.LogPower;
import dalvik.system.CloseGuard;

public class SurfaceControl {
    public static final int BUILT_IN_DISPLAY_ID_HDMI = 1;
    public static final int BUILT_IN_DISPLAY_ID_MAIN = 0;
    public static final int CURSOR_WINDOW = 8192;
    public static final int FX_SURFACE_BLUR = 65536;
    public static final int FX_SURFACE_DIM = 131072;
    public static final int FX_SURFACE_MASK = 983040;
    public static final int FX_SURFACE_NORMAL = 0;
    public static final int HIDDEN = 4;
    public static final int MASK_SECURE_SCREENSHT = 4;
    public static final int NON_PREMULTIPLIED = 256;
    public static final int OPAQUE = 1024;
    public static final int POWER_MODE_DOZE = 1;
    public static final int POWER_MODE_DOZE_SUSPEND = 3;
    public static final int POWER_MODE_NORMAL = 2;
    public static final int POWER_MODE_OFF = 0;
    public static final int PROTECTED_APP = 2048;
    public static final int SECURE = 128;
    public static final int SECURE_SCREENREC = 1;
    public static final int SECURE_SCREENSHT = 2;
    private static final int SURFACE_HIDDEN = 1;
    private static final int SURFACE_OPAQUE = 2;
    private static final String TAG = "SurfaceControl";
    private static final String TAG_CTAIFS = "ctaifs";
    private static boolean sHwInfo = true;
    private final CloseGuard mCloseGuard;
    private final String mName;
    long mNativeObject;

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

        public PhysicalDisplayInfo(PhysicalDisplayInfo other) {
            copyFrom(other);
        }

        public boolean equals(Object o) {
            return o instanceof PhysicalDisplayInfo ? equals((PhysicalDisplayInfo) o) : false;
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
            return "PhysicalDisplayInfo{" + this.width + " x " + this.height + ", " + this.refreshRate + " fps, " + "density " + this.density + ", " + this.xDpi + " x " + this.yDpi + " dpi, secure " + this.secure + ", appVsyncOffset " + this.appVsyncOffsetNanos + ", bufferDeadline " + this.presentationDeadlineNanos + "}";
        }
    }

    private static native boolean nativeClearAnimationFrameStats();

    private static native boolean nativeClearContentFrameStats(long j);

    private static native void nativeCloseTransaction(boolean z);

    private static native long nativeCreate(SurfaceSession surfaceSession, String str, int i, int i2, int i3, int i4, long j, int i5, int i6) throws OutOfResourcesException;

    private static native IBinder nativeCreateDisplay(String str, boolean z);

    private static native void nativeDeferTransactionUntil(long j, IBinder iBinder, long j2);

    private static native void nativeDeferTransactionUntilSurface(long j, long j2, long j3);

    private static native void nativeDestroy(long j);

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

    private static native HdrCapabilities nativeGetHdrCapabilities(IBinder iBinder);

    private static native boolean nativeGetTransformToDisplayInverse(long j);

    private static native int nativeIsRogSupport();

    private static native void nativeOpenTransaction();

    private static native void nativeRelease(long j);

    private static native void nativeReparentChildren(long j, IBinder iBinder);

    private static native Bitmap nativeScreenshot(IBinder iBinder, Rect rect, int i, int i2, int i3, int i4, boolean z, boolean z2, int i5);

    private static native void nativeScreenshot(IBinder iBinder, Surface surface, Rect rect, int i, int i2, int i3, int i4, boolean z, boolean z2);

    private static native GraphicBuffer nativeScreenshotToBuffer(IBinder iBinder, Rect rect, int i, int i2, int i3, int i4, boolean z, boolean z2, int i5);

    private static native boolean nativeSetActiveColorMode(IBinder iBinder, int i);

    private static native boolean nativeSetActiveConfig(IBinder iBinder, int i);

    private static native void nativeSetAlpha(long j, float f);

    private static native void nativeSetAnimationTransaction();

    private static native void nativeSetBlurAlpha(long j, float f);

    private static native void nativeSetBlurBlank(long j, int i, int i2, int i3, int i4);

    private static native void nativeSetBlurRadius(long j, int i);

    private static native void nativeSetBlurRegion(long j, Region region);

    private static native void nativeSetBlurRound(long j, int i, int i2);

    private static native void nativeSetDisplayLayerStack(IBinder iBinder, int i);

    private static native void nativeSetDisplayPowerMode(IBinder iBinder, int i);

    private static native void nativeSetDisplayProjection(IBinder iBinder, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9);

    private static native void nativeSetDisplaySize(IBinder iBinder, int i, int i2);

    private static native void nativeSetDisplaySurface(IBinder iBinder, long j);

    private static native void nativeSetFinalCrop(long j, int i, int i2, int i3, int i4);

    private static native void nativeSetFlags(long j, int i, int i2);

    private static native void nativeSetGeometryAppliesWithResize(long j);

    private static native void nativeSetLayer(long j, int i);

    private static native void nativeSetLayerStack(long j, int i);

    private static native void nativeSetMatrix(long j, float f, float f2, float f3, float f4);

    private static native void nativeSetOverrideScalingMode(long j, int i);

    private static native void nativeSetPosition(long j, float f, float f2);

    private static native void nativeSetRelativeLayer(long j, IBinder iBinder, int i);

    private static native int nativeSetRogDisplayConfig(int i);

    private static native int nativeSetRogDisplayConfigFull(int i, int i2, int i3, int i4);

    private static native boolean nativeSetSecureScreenRecShotFlag(IBinder iBinder, int i, int i2);

    private static native void nativeSetSize(long j, int i, int i2);

    private static native void nativeSetSurfaceLowResolutionInfo(long j, float f, int i);

    private static native void nativeSetTransparentRegionHint(long j, Region region);

    private static native void nativeSetWindowCrop(long j, int i, int i2, int i3, int i4);

    private static native void nativeSeverChildren(long j);

    private static native void nativeUnfreezeDisplay();

    public SurfaceControl(SurfaceSession session, String name, int w, int h, int format, int flags, int windowType, int ownerUid) throws OutOfResourcesException {
        this(session, name, w, h, format, flags, null, windowType, ownerUid);
    }

    public SurfaceControl(SurfaceSession session, String name, int w, int h, int format, int flags) throws OutOfResourcesException {
        this(session, name, w, h, format, flags, null, -1, Binder.getCallingUid());
    }

    public SurfaceControl(SurfaceSession session, String name, int w, int h, int format, int flags, SurfaceControl parent, int windowType, int ownerUid) throws OutOfResourcesException {
        this.mCloseGuard = CloseGuard.get();
        if (session == null) {
            throw new IllegalArgumentException("session must not be null");
        } else if (name == null) {
            throw new IllegalArgumentException("name must not be null");
        } else {
            if ((flags & 4) == 0) {
                Log.w(TAG, "Surfaces should always be created with the HIDDEN flag set to ensure that they are not made visible prematurely before all of the surface's properties have been configured.  Set the other properties and make the surface visible within a transaction.  New surface name: " + name, new Throwable());
            }
            this.mName = name;
            this.mNativeObject = nativeCreate(session, name, w, h, format, flags, parent != null ? parent.mNativeObject : 0, windowType, ownerUid);
            if (this.mNativeObject == 0) {
                throw new OutOfResourcesException("Couldn't allocate SurfaceControl native object");
            }
            this.mCloseGuard.open("release");
        }
    }

    public SurfaceControl(SurfaceControl other) {
        this.mCloseGuard = CloseGuard.get();
        this.mName = other.mName;
        this.mNativeObject = other.mNativeObject;
        other.mCloseGuard.close();
        other.mNativeObject = 0;
        this.mCloseGuard.open("release");
    }

    protected void finalize() throws Throwable {
        try {
            if (this.mCloseGuard != null) {
                this.mCloseGuard.warnIfOpen();
            }
            if (this.mNativeObject != 0) {
                nativeRelease(this.mNativeObject);
            }
            super.finalize();
        } catch (Throwable th) {
            super.finalize();
        }
    }

    public String toString() {
        return "Surface(name=" + this.mName + ")";
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

    public void disconnect() {
        if (this.mNativeObject != 0) {
            nativeDisconnect(this.mNativeObject);
        }
    }

    private void checkNotReleased() {
        if (this.mNativeObject == 0) {
            throw new NullPointerException("mNativeObject is null. Have you called release() already?");
        }
    }

    public static void openTransaction() {
        nativeOpenTransaction();
    }

    public static void closeTransaction() {
        nativeCloseTransaction(false);
    }

    public static void closeTransactionSync() {
        nativeCloseTransaction(true);
    }

    public void deferTransactionUntil(IBinder handle, long frame) {
        if (frame > 0) {
            nativeDeferTransactionUntil(this.mNativeObject, handle, frame);
        }
    }

    public void deferTransactionUntil(Surface barrier, long frame) {
        if (frame > 0) {
            nativeDeferTransactionUntilSurface(this.mNativeObject, barrier.mNativeObject, frame);
        }
    }

    public void reparentChildren(IBinder newParentHandle) {
        nativeReparentChildren(this.mNativeObject, newParentHandle);
    }

    public void detachChildren() {
        nativeSeverChildren(this.mNativeObject);
    }

    public void setOverrideScalingMode(int scalingMode) {
        checkNotReleased();
        nativeSetOverrideScalingMode(this.mNativeObject, scalingMode);
    }

    public IBinder getHandle() {
        return nativeGetHandle(this.mNativeObject);
    }

    public static void setAnimationTransaction() {
        nativeSetAnimationTransaction();
    }

    public void setLayer(int zorder) {
        checkNotReleased();
        nativeSetLayer(this.mNativeObject, zorder);
    }

    public void setRelativeLayer(IBinder relativeTo, int zorder) {
        checkNotReleased();
        nativeSetRelativeLayer(this.mNativeObject, relativeTo, zorder);
    }

    public void setPosition(float x, float y) {
        checkNotReleased();
        nativeSetPosition(this.mNativeObject, x, y);
    }

    public void setGeometryAppliesWithResize() {
        checkNotReleased();
        nativeSetGeometryAppliesWithResize(this.mNativeObject);
    }

    public void setSize(int w, int h) {
        checkNotReleased();
        nativeSetSize(this.mNativeObject, w, h);
    }

    public void hide() {
        checkNotReleased();
        nativeSetFlags(this.mNativeObject, 1, 1);
    }

    public void show() {
        checkNotReleased();
        nativeSetFlags(this.mNativeObject, 0, 1);
    }

    public void setTransparentRegionHint(Region region) {
        checkNotReleased();
        nativeSetTransparentRegionHint(this.mNativeObject, region);
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
        nativeSetAlpha(this.mNativeObject, alpha);
    }

    public void setMatrix(float dsdx, float dtdx, float dtdy, float dsdy) {
        checkNotReleased();
        nativeSetMatrix(this.mNativeObject, dsdx, dtdx, dtdy, dsdy);
    }

    public void setWindowCrop(Rect crop) {
        checkNotReleased();
        if (crop != null) {
            nativeSetWindowCrop(this.mNativeObject, crop.left, crop.top, crop.right, crop.bottom);
        } else {
            nativeSetWindowCrop(this.mNativeObject, 0, 0, 0, 0);
        }
    }

    public void setFinalCrop(Rect crop) {
        checkNotReleased();
        if (crop != null) {
            nativeSetFinalCrop(this.mNativeObject, crop.left, crop.top, crop.right, crop.bottom);
        } else {
            nativeSetFinalCrop(this.mNativeObject, 0, 0, 0, 0);
        }
    }

    public void setLayerStack(int layerStack) {
        checkNotReleased();
        nativeSetLayerStack(this.mNativeObject, layerStack);
    }

    public void setOpaque(boolean isOpaque) {
        checkNotReleased();
        if (isOpaque) {
            nativeSetFlags(this.mNativeObject, 2, 2);
        } else {
            nativeSetFlags(this.mNativeObject, 0, 2);
        }
    }

    public void setSecure(boolean isSecure) {
        checkNotReleased();
        if (isSecure) {
            nativeSetFlags(this.mNativeObject, 128, 128);
        } else {
            nativeSetFlags(this.mNativeObject, 0, 128);
        }
    }

    public void setSecureScreenRecord(boolean isSecure) {
        checkNotReleased();
        if (isSecure) {
            nativeSetSecureScreenRecShotFlag(null, 1, 1);
        } else {
            nativeSetSecureScreenRecShotFlag(null, 0, 1);
        }
    }

    public void setSecureScreenShot(boolean isSecure) {
        checkNotReleased();
        if (isSecure) {
            nativeSetSecureScreenRecShotFlag(null, 2, 2);
        } else {
            nativeSetSecureScreenRecShotFlag(null, 0, 2);
        }
    }

    public static Bitmap screenshot_ext_hw(Rect sourceCrop, int width, int height, int minLayer, int maxLayer, boolean useIdentityTransform, int rotation) {
        nativeSetSecureScreenRecShotFlag(null, 4, 4);
        return screenshot(sourceCrop, width, height, minLayer, maxLayer, useIdentityTransform, rotation);
    }

    public static Bitmap screenshot_ext_hw(int width, int height) {
        nativeSetSecureScreenRecShotFlag(null, 4, 4);
        return screenshot(width, height);
    }

    public static void setDisplayPowerMode(IBinder displayToken, int mode) {
        if (displayToken == null) {
            throw new IllegalArgumentException("displayToken must not be null");
        }
        nativeSetDisplayPowerMode(displayToken, mode);
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
        if (displayToken == null) {
            throw new IllegalArgumentException("displayToken must not be null");
        } else if (layerStackRect == null) {
            throw new IllegalArgumentException("layerStackRect must not be null");
        } else if (displayRect == null) {
            throw new IllegalArgumentException("displayRect must not be null");
        } else {
            nativeSetDisplayProjection(displayToken, orientation, layerStackRect.left, layerStackRect.top, layerStackRect.right, layerStackRect.bottom, displayRect.left, displayRect.top, displayRect.right, displayRect.bottom);
        }
    }

    public static void setDisplayLayerStack(IBinder displayToken, int layerStack) {
        if (displayToken == null) {
            throw new IllegalArgumentException("displayToken must not be null");
        }
        nativeSetDisplayLayerStack(displayToken, layerStack);
    }

    public static void setDisplaySurface(IBinder displayToken, Surface surface) {
        if (displayToken == null) {
            throw new IllegalArgumentException("displayToken must not be null");
        } else if (surface != null) {
            synchronized (surface.mLock) {
                nativeSetDisplaySurface(displayToken, surface.mNativeObject);
            }
        } else {
            nativeSetDisplaySurface(displayToken, 0);
        }
    }

    public static void setDisplaySize(IBinder displayToken, int width, int height) {
        if (displayToken == null) {
            throw new IllegalArgumentException("displayToken must not be null");
        } else if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("width and height must be positive");
        } else {
            nativeSetDisplaySize(displayToken, width, height);
        }
    }

    public static HdrCapabilities getHdrCapabilities(IBinder displayToken) {
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
        if (displayToken == null) {
            throw new IllegalArgumentException("displayToken must not be null");
        }
        nativeDestroyDisplay(displayToken);
    }

    public static IBinder getBuiltInDisplay(int builtInDisplayId) {
        return nativeGetBuiltInDisplay(builtInDisplayId);
    }

    public static int setRogDisplayConfigFull(int width, int height, int density, int configmode) {
        return nativeSetRogDisplayConfigFull(width, height, density, configmode);
    }

    public static int setRogDisplayConfig(int configmode) {
        return nativeSetRogDisplayConfig(configmode);
    }

    public static void freezeDisplay() {
        nativeFreezeDisplay();
    }

    public static void unfreezeDisplay() {
        nativeUnfreezeDisplay();
    }

    public void setSurfaceLowResolutionInfo(float scaleFactor, int mode) {
        nativeSetSurfaceLowResolutionInfo(this.mNativeObject, scaleFactor, mode);
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
        nativeSetSecureScreenRecShotFlag(null, 4, 4);
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

    public static Bitmap screenshot(int width, int height) {
        if (showMDMToast()) {
            return null;
        }
        if (sHwInfo) {
            log();
        }
        IBinder displayToken = getBuiltInDisplay(0);
        LogPower.push(175);
        Bitmap bm = nativeScreenshot(displayToken, new Rect(), width, height, 0, 0, true, false, 0);
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
        } else if (consumer == null) {
            throw new IllegalArgumentException("consumer must not be null");
        } else {
            nativeScreenshot(display, consumer, sourceCrop, width, height, minLayer, maxLayer, allLayers, useIdentityTransform);
        }
    }

    public static void log() {
        String[] processAndAppName = getProcessAndAppName();
        if (processAndAppName[0] != null && processAndAppName[1] == null) {
            Log.i(TAG_CTAIFS, " <" + processAndAppName[0] + ">[" + processAndAppName[0] + "][" + processAndAppName[0] + "]:" + "[SurfaceControl.screenshot] " + processAndAppName[2]);
        } else if (processAndAppName[1] != null) {
            Log.i(TAG_CTAIFS, " <" + processAndAppName[1] + ">[" + processAndAppName[1] + "][" + processAndAppName[0] + "]:" + "[SurfaceControl.screenshot] " + processAndAppName[2]);
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
            ActivityManager manager = (ActivityManager) context.getSystemService(LaunchMode.ACTIVITY);
            PackageManager pm = context.getPackageManager();
            if (pm == null) {
                return processAndAppName;
            }
            for (RunningAppProcessInfo process : manager.getRunningAppProcesses()) {
                if (process.pid == pid) {
                    processAndAppName[0] = process.processName;
                    try {
                        processAndAppName[1] = pm.getApplicationLabel(pm.getApplicationInfo(process.processName, 128)).toString();
                    } catch (NameNotFoundException e) {
                    }
                }
            }
        }
        return processAndAppName;
    }

    public void setBlurRadius(int radius) {
        checkNotReleased();
        nativeSetBlurRadius(this.mNativeObject, radius);
    }

    public void setBlurRound(int rx, int ry) {
        checkNotReleased();
        nativeSetBlurRound(this.mNativeObject, rx, ry);
    }

    public void setBlurAlpha(float alpha) {
        checkNotReleased();
        nativeSetBlurAlpha(this.mNativeObject, alpha);
    }

    public void setBlurRegion(Region region) {
        checkNotReleased();
        nativeSetBlurRegion(this.mNativeObject, region);
    }

    public void setBlurBlank(Rect blank) {
        checkNotReleased();
        if (blank != null) {
            nativeSetBlurBlank(this.mNativeObject, blank.left, blank.top, blank.right, blank.bottom);
        } else {
            nativeSetBlurBlank(this.mNativeObject, 0, 0, 0, 0);
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
                    Toast.makeText(context, context.getResources().getString(33685934), 0).show();
                } catch (RuntimeException e) {
                    Log.e(TAG, "Caught a Runtime Exception in showMDMToast");
                }
            }
        }
        return true;
    }
}
