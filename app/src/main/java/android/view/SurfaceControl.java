package android.view;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityThread;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.Region;
import android.os.IBinder;
import android.os.Process;
import android.util.Log;
import android.view.Display.HdrCapabilities;
import android.view.Surface.OutOfResourcesException;
import androidhwext.R;
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
    public static final int NON_PREMULTIPLIED = 256;
    public static final int OPAQUE = 1024;
    public static final int POWER_MODE_DOZE = 1;
    public static final int POWER_MODE_DOZE_SUSPEND = 3;
    public static final int POWER_MODE_NORMAL = 2;
    public static final int POWER_MODE_OFF = 0;
    public static final int PROTECTED_APP = 2048;
    public static final int SECURE = 128;
    private static final int SURFACE_HIDDEN = 1;
    private static final int SURFACE_OPAQUE = 2;
    private static final String TAG = "SurfaceControl";
    private static final String TAG_CTAIFS = "ctaifs";
    private static boolean sHwInfo;
    private final CloseGuard mCloseGuard;
    private final String mName;
    long mNativeObject;

    public static final class PhysicalDisplayInfo {
        public long appVsyncOffsetNanos;
        public int colorTransform;
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
            return other != null && this.width == other.width && this.height == other.height && this.refreshRate == other.refreshRate && this.density == other.density && this.xDpi == other.xDpi && this.yDpi == other.yDpi && this.secure == other.secure && this.appVsyncOffsetNanos == other.appVsyncOffsetNanos && this.presentationDeadlineNanos == other.presentationDeadlineNanos && this.colorTransform == other.colorTransform;
        }

        public int hashCode() {
            return SurfaceControl.POWER_MODE_OFF;
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
            this.colorTransform = other.colorTransform;
        }

        public String toString() {
            return "PhysicalDisplayInfo{" + this.width + " x " + this.height + ", " + this.refreshRate + " fps, " + "density " + this.density + ", " + this.xDpi + " x " + this.yDpi + " dpi, secure " + this.secure + ", appVsyncOffset " + this.appVsyncOffsetNanos + ", bufferDeadline " + this.presentationDeadlineNanos + ", colorTransform " + this.colorTransform + "}";
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.SurfaceControl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.view.SurfaceControl.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.SurfaceControl.<clinit>():void");
    }

    private static native boolean nativeClearAnimationFrameStats();

    private static native boolean nativeClearContentFrameStats(long j);

    private static native void nativeCloseTransaction(boolean z);

    private static native long nativeCreate(SurfaceSession surfaceSession, String str, int i, int i2, int i3, int i4) throws OutOfResourcesException;

    private static native IBinder nativeCreateDisplay(String str, boolean z);

    private static native void nativeDeferTransactionUntil(long j, IBinder iBinder, long j2);

    private static native void nativeDestroy(long j);

    private static native void nativeDestroyDisplay(IBinder iBinder);

    private static native void nativeDisconnect(long j);

    private static native void nativeFreezeDisplay();

    private static native int nativeGetActiveConfig(IBinder iBinder);

    private static native boolean nativeGetAnimationFrameStats(WindowAnimationFrameStats windowAnimationFrameStats);

    private static native IBinder nativeGetBuiltInDisplay(int i);

    private static native boolean nativeGetContentFrameStats(long j, WindowContentFrameStats windowContentFrameStats);

    private static native PhysicalDisplayInfo[] nativeGetDisplayConfigs(IBinder iBinder);

    private static native IBinder nativeGetHandle(long j);

    private static native HdrCapabilities nativeGetHdrCapabilities(IBinder iBinder);

    private static native int nativeIsRogSupport();

    private static native void nativeOpenTransaction();

    private static native void nativeRelease(long j);

    private static native Bitmap nativeScreenshot(IBinder iBinder, Rect rect, int i, int i2, int i3, int i4, boolean z, boolean z2, int i5);

    private static native void nativeScreenshot(IBinder iBinder, Surface surface, Rect rect, int i, int i2, int i3, int i4, boolean z, boolean z2);

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

    private static native void nativeSetLayer(long j, int i);

    private static native void nativeSetLayerStack(long j, int i);

    private static native void nativeSetMatrix(long j, float f, float f2, float f3, float f4);

    private static native void nativeSetOverrideScalingMode(long j, int i);

    private static native void nativeSetPosition(long j, float f, float f2);

    private static native void nativeSetPositionAppliesWithResize(long j);

    private static native void nativeSetSize(long j, int i, int i2);

    private static native void nativeSetSurfaceLowResolutionInfo(long j, float f, int i);

    private static native void nativeSetTransparentRegionHint(long j, Region region);

    private static native void nativeSetWindowCrop(long j, int i, int i2, int i3, int i4);

    private static native void nativeUnfreezeDisplay();

    public SurfaceControl(SurfaceSession session, String name, int w, int h, int format, int flags) throws OutOfResourcesException {
        this.mCloseGuard = CloseGuard.get();
        if (session == null) {
            throw new IllegalArgumentException("session must not be null");
        } else if (name == null) {
            throw new IllegalArgumentException("name must not be null");
        } else {
            if ((flags & HIDDEN) == 0) {
                Log.w(TAG, "Surfaces should always be created with the HIDDEN flag set to ensure that they are not made visible prematurely before all of the surface's properties have been configured.  Set the other properties and make the surface visible within a transaction.  New surface name: " + name, new Throwable());
            }
            this.mName = name;
            this.mNativeObject = nativeCreate(session, name, w, h, format, flags);
            if (this.mNativeObject == 0) {
                throw new OutOfResourcesException("Couldn't allocate SurfaceControl native object");
            }
            this.mCloseGuard.open("release");
        }
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
        nativeDeferTransactionUntil(this.mNativeObject, handle, frame);
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

    public void setPosition(float x, float y) {
        checkNotReleased();
        nativeSetPosition(this.mNativeObject, x, y);
    }

    public void setPositionAppliesWithResize() {
        checkNotReleased();
        nativeSetPositionAppliesWithResize(this.mNativeObject);
    }

    public void setSize(int w, int h) {
        checkNotReleased();
        nativeSetSize(this.mNativeObject, w, h);
    }

    public void hide() {
        checkNotReleased();
        nativeSetFlags(this.mNativeObject, SURFACE_HIDDEN, SURFACE_HIDDEN);
    }

    public void show() {
        checkNotReleased();
        nativeSetFlags(this.mNativeObject, POWER_MODE_OFF, SURFACE_HIDDEN);
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

    public void setMatrix(float dsdx, float dtdx, float dsdy, float dtdy) {
        checkNotReleased();
        nativeSetMatrix(this.mNativeObject, dsdx, dtdx, dsdy, dtdy);
    }

    public void setWindowCrop(Rect crop) {
        checkNotReleased();
        if (crop != null) {
            nativeSetWindowCrop(this.mNativeObject, crop.left, crop.top, crop.right, crop.bottom);
        } else {
            nativeSetWindowCrop(this.mNativeObject, POWER_MODE_OFF, POWER_MODE_OFF, POWER_MODE_OFF, POWER_MODE_OFF);
        }
    }

    public void setFinalCrop(Rect crop) {
        checkNotReleased();
        if (crop != null) {
            nativeSetFinalCrop(this.mNativeObject, crop.left, crop.top, crop.right, crop.bottom);
        } else {
            nativeSetFinalCrop(this.mNativeObject, POWER_MODE_OFF, POWER_MODE_OFF, POWER_MODE_OFF, POWER_MODE_OFF);
        }
    }

    public void setLayerStack(int layerStack) {
        checkNotReleased();
        nativeSetLayerStack(this.mNativeObject, layerStack);
    }

    public void setOpaque(boolean isOpaque) {
        checkNotReleased();
        if (isOpaque) {
            nativeSetFlags(this.mNativeObject, SURFACE_OPAQUE, SURFACE_OPAQUE);
        } else {
            nativeSetFlags(this.mNativeObject, POWER_MODE_OFF, SURFACE_OPAQUE);
        }
    }

    public void setSecure(boolean isSecure) {
        checkNotReleased();
        if (isSecure) {
            nativeSetFlags(this.mNativeObject, SECURE, SECURE);
        } else {
            nativeSetFlags(this.mNativeObject, POWER_MODE_OFF, SECURE);
        }
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

    @Deprecated
    public static int setRogDisplayConfigFull(int width, int height, int density, int configmode) {
        return POWER_MODE_OFF;
    }

    @Deprecated
    public static int setRogDisplayConfig(int configmode) {
        return POWER_MODE_OFF;
    }

    @Deprecated
    public static void freezeDisplay() {
        nativeFreezeDisplay();
    }

    @Deprecated
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
        screenshot(display, consumer, new Rect(), width, height, POWER_MODE_OFF, POWER_MODE_OFF, true, false);
    }

    public static void screenshot(IBinder display, Surface consumer) {
        screenshot(display, consumer, new Rect(), POWER_MODE_OFF, POWER_MODE_OFF, POWER_MODE_OFF, POWER_MODE_OFF, true, false);
    }

    public static Bitmap screenshot(Rect sourceCrop, int width, int height, int minLayer, int maxLayer, boolean useIdentityTransform, int rotation) {
        if (sHwInfo) {
            log();
        }
        return nativeScreenshot(getBuiltInDisplay(POWER_MODE_OFF), sourceCrop, width, height, minLayer, maxLayer, false, useIdentityTransform, rotation);
    }

    public static Bitmap screenshot(int width, int height) {
        if (sHwInfo) {
            log();
        }
        IBinder displayToken = getBuiltInDisplay(POWER_MODE_OFF);
        LogPower.push(LogPower.SCREEN_SHOT_START);
        Bitmap bm = nativeScreenshot(displayToken, new Rect(), width, height, (int) POWER_MODE_OFF, POWER_MODE_OFF, true, false, POWER_MODE_OFF);
        LogPower.push(LogPower.SCREEN_SHOT_END);
        return bm;
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
        if (processAndAppName[POWER_MODE_OFF] != null && processAndAppName[SURFACE_HIDDEN] == null) {
            Log.i(TAG_CTAIFS, " <" + processAndAppName[POWER_MODE_OFF] + ">[" + processAndAppName[POWER_MODE_OFF] + "][" + processAndAppName[POWER_MODE_OFF] + "]:" + "[SurfaceControl.screenshot]" + processAndAppName[SURFACE_OPAQUE]);
        } else if (processAndAppName[SURFACE_HIDDEN] != null) {
            Log.i(TAG_CTAIFS, " <" + processAndAppName[SURFACE_HIDDEN] + ">[" + processAndAppName[SURFACE_HIDDEN] + "][" + processAndAppName[POWER_MODE_OFF] + "]:" + "[SurfaceControl.screenshot]" + processAndAppName[SURFACE_OPAQUE]);
        }
    }

    public static String[] getProcessAndAppName() {
        String[] processAndAppName = new String[POWER_MODE_DOZE_SUSPEND];
        if (ActivityThread.currentProcessName() != null) {
            Context context = ActivityThread.currentActivityThread().getSystemContext();
            if (context == null) {
                return processAndAppName;
            }
            processAndAppName[SURFACE_OPAQUE] = context.getString(R.string.background_screenshot);
            int pid = Process.myPid();
            ActivityManager manager = (ActivityManager) context.getSystemService("activity");
            PackageManager pm = context.getPackageManager();
            if (pm == null) {
                return processAndAppName;
            }
            for (RunningAppProcessInfo process : manager.getRunningAppProcesses()) {
                if (process.pid == pid) {
                    processAndAppName[POWER_MODE_OFF] = process.processName;
                    try {
                        processAndAppName[SURFACE_HIDDEN] = pm.getApplicationLabel(pm.getApplicationInfo(process.processName, SECURE)).toString();
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
            nativeSetBlurBlank(this.mNativeObject, POWER_MODE_OFF, POWER_MODE_OFF, POWER_MODE_OFF, POWER_MODE_OFF);
        }
    }
}
