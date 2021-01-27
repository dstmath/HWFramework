package com.huawei.hwaps;

import android.app.ActivityThread;
import android.app.Application;
import android.aps.IApsManager;
import android.common.HwFrameworkFactory;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.graphics.RenderNode;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.HwLog;
import android.util.Log;
import android.util.Slog;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewRootImpl;
import android.view.WindowManager;
import android.view.WindowManagerGlobal;
import com.android.server.LocalServices;
import com.huawei.android.content.pm.PackageManagerExt;
import huawei.android.provider.HwSettings;
import java.util.Arrays;
import java.util.Locale;

public class HwApsImpl implements IHwApsImpl {
    private static final int MIN_TIME_INTERNAL = 60000;
    private static final String[] ROG_KILL_APPS = {PackageManagerExt.HW_LAUNCHER_PACKAGE_NAME};
    private static final String[] ROG_NOT_KILL_APPS = {"com.android.deskclock", "com.huawei.aod", "com.huawei.android.thememanager", "com.huawei.security:SERVICE"};
    private static final String TAG = "HwApsImpl";
    private static HwApsImpl sInstance = null;
    private static boolean sLibraryLoadedOk;
    private View mBeingInvalidatedChild = null;
    private int mEpsTvCount = 0;
    private long mEpsTvDirty = 0;
    private int mEpsUiCount = 0;
    private long mEpsUiDirty = 0;
    private IEventAnalyzed mEventAnalyzed = null;
    private boolean mIsApsInitialized = false;
    private boolean mIsSupportApsDropEmptyFrame = false;
    private long mLastTime = System.currentTimeMillis();
    private int mWindowHeight = 0;
    private int mWindowWidth = 0;

    private native boolean nIsApsFeatureSupport(int i);

    private native boolean nIsChipsetSupportRog();

    private native boolean nIsDumpPartialUpdateOn();

    private native boolean nIsSupportApsPartialUpdate();

    private native void nSetSfrWorkPid(int i);

    private native void nativeFpsControllerRelease(long j);

    private native void nativeFpsRequestRelease(long j);

    private native int nativeGetCurFps(long j);

    private native int nativeGetTargetFps(long j);

    private native long nativeInitFpsController();

    private native long nativeInitFpsRequest(long j);

    private native void nativePowerCtroll(long j);

    private native void nativeSetUiFrameState(boolean z);

    private native void nativeStart(long j, int i);

    private native void nativeStartFeedback(long j, int i);

    private native void nativeStop(long j);

    static {
        sLibraryLoadedOk = false;
        try {
            System.loadLibrary("hwapsimpl_jni");
            sLibraryLoadedOk = true;
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "Load hwapsimpl_jni so failed, not found.");
        }
    }

    private HwApsImpl() {
        if (isSupportAps()) {
            this.mEventAnalyzed = HwapsWrapper.getEventAnalyzed();
            this.mIsSupportApsDropEmptyFrame = isApsFeatureSupport(PackageManagerExt.MATCH_STATIC_SHARED_LIBRARIES);
        }
    }

    public static HwApsImpl getDefault() {
        HwApsImpl hwApsImpl;
        synchronized (HwApsImpl.class) {
            if (sInstance == null) {
                sInstance = new HwApsImpl();
                Log.i(TAG, "APS: new HwApsImpl created");
            }
            hwApsImpl = sInstance;
        }
        return hwApsImpl;
    }

    public void adaptPowerSave(Context context, MotionEvent event) {
        if (this.mEventAnalyzed != null && isSupportFps()) {
            this.mEventAnalyzed.processAnalyze(event.getAction());
        }
    }

    public void initAps(Context context, int screenWidth, int myPid) {
        if (!this.mIsApsInitialized) {
            IEventAnalyzed iEventAnalyzed = this.mEventAnalyzed;
            if (iEventAnalyzed != null) {
                iEventAnalyzed.initAps(context, myPid);
                if (sLibraryLoadedOk) {
                    nSetSfrWorkPid(myPid);
                }
                this.mWindowWidth = context.getResources().getDisplayMetrics().widthPixels;
                this.mWindowHeight = context.getResources().getDisplayMetrics().heightPixels;
            }
            this.mIsApsInitialized = true;
        }
    }

    public final boolean isSupportAps() {
        return isApsFeatureSupport(268435455);
    }

    public void powerCtroll() {
        IFpsController fpsController;
        if (isSupportFps() && (fpsController = HwapsWrapper.getFpsController()) != null) {
            fpsController.powerCtroll();
        }
    }

    public void setApsOnPause() {
        if (isSupportFps()) {
            this.mEventAnalyzed.setHasOnPaused(true);
        }
    }

    public void processApsPointerEvent(Context context, String pkgName, int screenWidth, int myPid, MotionEvent event) {
        if (isSupportFps()) {
            initAps(context, screenWidth, myPid);
            adaptPowerSave(context, event);
        }
    }

    public int getCustScreenDimDurationLocked(int screenOffTimeout) {
        IEventAnalyzed iEventAnalyzed = this.mEventAnalyzed;
        if (iEventAnalyzed != null) {
            return iEventAnalyzed.getCustScreenDimDurationLocked(screenOffTimeout);
        }
        Log.w(TAG, "APS: Screen Dim: getCustScreenDimDuration eventAnalyzed null");
        return -1;
    }

    public void setLowResolutionMode(Context context, boolean isEnableLowResolutionMode) {
        Log.i("sdr", "APS: SDR: HwApsImpl.setLowResolutionMod, enableLowResolutionMode = " + isEnableLowResolutionMode);
        IApsManager apsManager = HwFrameworkFactory.getApsManager();
        if (apsManager != null) {
            apsManager.setLowResolutionMode(isEnableLowResolutionMode ? 1 : 0);
        }
    }

    public boolean isLowResolutionSupported() {
        return isApsFeatureSupport(32768);
    }

    public boolean isSupportApsPartialUpdate() {
        if (sLibraryLoadedOk) {
            return nIsSupportApsPartialUpdate();
        }
        return false;
    }

    private boolean isDumpPartialUpdateOn() {
        if (sLibraryLoadedOk) {
            return nIsDumpPartialUpdateOn();
        }
        return false;
    }

    private boolean isSupportFps() {
        return isApsFeatureSupport(1);
    }

    public void savePartialUpdateDirty(Rect currentDirtyRect, int left, int top, int right, int bottom, Context context, String name) {
        if (isSupportApsPartialUpdate()) {
            currentDirtyRect.union(left, top, right, bottom);
            if (isDumpPartialUpdateOn()) {
                Slog.i("partial", "APS: PU, invalidate, pkgname=" + context.getPackageName() + ", union dirty = (" + left + "," + top + "," + right + "," + bottom + "), now dirty = " + currentDirtyRect + ", classname = " + name);
            }
        }
    }

    private void epsCollectData(Rect currentDirtyRect, String name) {
        if (name != null) {
            int width = currentDirtyRect.width();
            int height = currentDirtyRect.height();
            if (width > 0 && height > 0) {
                if (width > this.mWindowWidth) {
                    width = this.mWindowWidth;
                }
                if (height > this.mWindowHeight) {
                    height = this.mWindowHeight;
                }
                if (name.contains("Texture")) {
                    this.mEpsTvCount++;
                    this.mEpsTvDirty += (long) (width * height);
                } else {
                    this.mEpsUiCount++;
                    this.mEpsUiDirty += (long) (width * height);
                }
                long now = System.currentTimeMillis();
                if (now - this.mLastTime > 60000) {
                    HwLog.dubaie("DUBAI_TAG_EPS_VIEW_INFO", "tcount=" + this.mEpsTvCount + " ucount=" + this.mEpsUiCount + " tdirty=" + this.mEpsTvDirty + " udirty=" + this.mEpsUiDirty);
                    this.mLastTime = now;
                    this.mEpsTvCount = 0;
                    this.mEpsUiCount = 0;
                    this.mEpsTvDirty = 0;
                    this.mEpsUiDirty = 0;
                }
            }
        }
    }

    public void setPartialDirtyToNative(Rect currentDirtyRect, RenderNode renderNode, int inWidth, int inHeight, Context context, String name) {
        epsCollectData(currentDirtyRect, name);
        if (isSupportApsPartialUpdate()) {
            renderNode.setDirtyLeftTopRightBottom(currentDirtyRect.left, currentDirtyRect.top, currentDirtyRect.right, currentDirtyRect.bottom);
            if (isDumpPartialUpdateOn()) {
                StringBuilder sb = new StringBuilder();
                sb.append("APS: PU, setDirty, pkgname=");
                sb.append(context.getPackageName());
                sb.append(", currentDirty = ");
                sb.append(currentDirtyRect.isEmpty() ? new Rect(0, 0, inWidth, inHeight) : currentDirtyRect);
                sb.append(", classname = ");
                sb.append(name);
                Slog.i("partial", sb.toString());
            }
            if (!currentDirtyRect.isEmpty()) {
                currentDirtyRect.setEmpty();
            }
        }
    }

    public boolean isDropEmptyFrame(View descendant) {
        if (!this.mIsSupportApsDropEmptyFrame || this.mBeingInvalidatedChild != descendant) {
            return false;
        }
        return true;
    }

    public boolean isNonEmptyFrameCase(ViewGroup sourceView, View child) {
        ViewRootImpl impl = child != null ? child.getViewRootImpl() : null;
        if (impl == null || !this.mIsSupportApsDropEmptyFrame || Thread.currentThread() != impl.mThread) {
            return true;
        }
        this.mBeingInvalidatedChild = child;
        if (sourceView != null && (sourceView instanceof ViewGroup)) {
            sourceView.onDescendantInvalidated(child, child);
            ViewParent grandParent = sourceView.getParent();
            if (grandParent != null && (grandParent instanceof ViewGroup)) {
                grandParent.onDescendantInvalidated(child, child);
            }
        }
        impl.onDescendantInvalidated(child, child);
        this.mBeingInvalidatedChild = null;
        return false;
    }

    public boolean isValidSdrRatio(float resolutionRatio) {
        return resolutionRatio > 0.0f && resolutionRatio != 1.0f;
    }

    public float getResolutionRatioByPkgName(String appPkgName, String owningPkgName) {
        String pkgName = appPkgName;
        if (pkgName == null || pkgName.isEmpty()) {
            pkgName = owningPkgName;
        }
        return getResolutionRatioByPkgName(pkgName);
    }

    public float getResolutionRatioByPkgName(String packageName) {
        IApsManager apsManager = (IApsManager) LocalServices.getService(IApsManager.class);
        if (apsManager != null) {
            return apsManager.getResolution(packageName);
        }
        return 1.0f;
    }

    public boolean checkAndApplyToDmByRatio(float resolutionRatio, DisplayMetrics inoutDm) {
        if (!isValidSdrRatio(resolutionRatio)) {
            return false;
        }
        if (Float.compare(inoutDm.density, inoutDm.noncompatDensity) == 0) {
            inoutDm.widthPixels = (int) ((((float) inoutDm.noncompatWidthPixels) * resolutionRatio) + 0.5f);
            inoutDm.heightPixels = (int) ((((float) inoutDm.noncompatHeightPixels) * resolutionRatio) + 0.5f);
        }
        inoutDm.density = inoutDm.noncompatDensity * resolutionRatio;
        inoutDm.densityDpi = (int) ((inoutDm.density * 160.0f) + 0.5f);
        inoutDm.scaledDensity = inoutDm.noncompatScaledDensity * resolutionRatio;
        inoutDm.xdpi = inoutDm.noncompatXdpi * resolutionRatio;
        inoutDm.ydpi = inoutDm.noncompatYdpi * resolutionRatio;
        return true;
    }

    public void applyToConfigurationByResolutionRatio(boolean isSupportsScreen, float resolutionRatio, Configuration inoutConfig) {
        if (!isSupportsScreen && isValidSdrRatio(resolutionRatio) && inoutConfig.densityDpi == DisplayMetrics.DENSITY_DEVICE) {
            inoutConfig.densityDpi = (int) ((((float) DisplayMetrics.DENSITY_DEVICE) * resolutionRatio) + 0.5f);
        }
    }

    private float getRogRatio() {
        float ratio = Float.parseFloat(SystemProperties.get("persist.sys.rog.currentratio", "-1"));
        if (Float.compare(ratio, -1.0f) != 0) {
            return ratio;
        }
        int sfDensity = SystemProperties.getInt("ro.sf.lcd_density", 0);
        return (((float) sfDensity) * 1.0f) / ((float) SystemProperties.getInt("ro.sf.real_lcd_density", sfDensity));
    }

    public void adjustPmDisplayMetricsInRog(DisplayMetrics inoutMetrics) {
        if (inoutMetrics != null) {
            float ratio = getRogRatio();
            if (Float.compare(ratio, 1.0f) == 0) {
                Log.i(TAG, "APS: ROG: HwApsImpl, adjust PMS display metrics: Not in rog mode.");
                return;
            }
            inoutMetrics.widthPixels = (int) ((((float) inoutMetrics.widthPixels) * ratio) + 0.5f);
            inoutMetrics.heightPixels = (int) ((((float) inoutMetrics.heightPixels) * ratio) + 0.5f);
            inoutMetrics.density *= ratio;
            inoutMetrics.densityDpi = (int) ((((float) inoutMetrics.densityDpi) * ratio) + 0.5f);
            inoutMetrics.scaledDensity *= ratio;
            inoutMetrics.xdpi *= ratio;
            inoutMetrics.ydpi *= ratio;
            inoutMetrics.noncompatWidthPixels = (int) ((((float) inoutMetrics.noncompatWidthPixels) * ratio) + 0.5f);
            inoutMetrics.noncompatHeightPixels = (int) ((((float) inoutMetrics.noncompatHeightPixels) * ratio) + 0.5f);
            inoutMetrics.noncompatDensity *= ratio;
            inoutMetrics.noncompatDensityDpi = (int) ((((float) inoutMetrics.noncompatDensityDpi) * ratio) + 0.5f);
            inoutMetrics.noncompatScaledDensity *= ratio;
            inoutMetrics.noncompatXdpi *= ratio;
            inoutMetrics.noncompatYdpi *= ratio;
            Log.i(TAG, "APS: ROG: HwApsImpl, adjust PMS display metrics to " + inoutMetrics);
        }
    }

    public void scaleInsetsWhenSdrUpInRog(String pkgName, Rect inoutInsets) {
        if (pkgName != null && inoutInsets != null) {
            float ratio = getResolutionRatioByPkgName(pkgName);
            if (ratio > 1.0f) {
                inoutInsets.scale(1.0f / ratio);
                Log.i(TAG, "APS: ROG: HwApsImp, scale TaskSnapshot insets according to app up scale ratio");
            }
        }
    }

    private int getCurrentDensityDpi() {
        try {
            return WindowManagerGlobal.getWindowManagerService().getBaseDisplayDensity(0);
        } catch (RemoteException e) {
            Log.e(TAG, "HwApsImpl.getCurrentDensityDpi, throws remote exception.");
            return -1;
        }
    }

    public Display.Mode scaleDisplayModeInRog(Display.Mode inMode) {
        if (inMode == null) {
            return null;
        }
        float ratio = getRogRatio();
        if (Float.compare(ratio, 1.0f) == 0) {
            return inMode;
        }
        Display.Mode mode = new Display.Mode(inMode.getModeId(), (int) ((((float) inMode.getPhysicalWidth()) * ratio) + 0.5f), (int) ((((float) inMode.getPhysicalHeight()) * ratio) + 0.5f), inMode.getRefreshRate());
        Log.i(TAG, "scaleDisplayModeInRog, return new display mode = " + mode);
        return mode;
    }

    public Display.Mode[] scaleDisplayModesInRog(Display.Mode[] inModes) {
        if (inModes == null) {
            Log.e(TAG, "scaleDisplayModesInRog, inModes = null");
            return null;
        }
        Application app = ActivityThread.currentApplication();
        if (!(app == null || app.getPackageName() == null || !app.getPackageName().equals("android.display.cts"))) {
            float ratio = getRogRatio();
            if (Float.compare(ratio, 1.0f) != 0) {
                Display.Mode[] newModes = new Display.Mode[inModes.length];
                for (int i = 0; i < newModes.length; i++) {
                    newModes[i] = new Display.Mode(inModes[i].getModeId(), (int) ((((float) inModes[i].getPhysicalWidth()) * ratio) + 0.5f), (int) ((((float) inModes[i].getPhysicalHeight()) * ratio) + 0.5f), inModes[i].getRefreshRate());
                    Log.i(TAG, "scaleDisplayModesInRog, change to new display mode = " + newModes[i]);
                }
                return newModes;
            }
        }
        return (Display.Mode[]) Arrays.copyOf(inModes, inModes.length);
    }

    public boolean isIn1kResolutionof2kScreen() {
        int width = SystemProperties.getInt("sys.rog.width", 0);
        int density = SystemProperties.getInt("sys.rog.density", 0);
        int realWidth = SystemProperties.getInt("persist.sys.rog.width", 0);
        if (width != 0 && width == realWidth && density == getCurrentDensityDpi()) {
            return true;
        }
        return false;
    }

    private boolean isStatusNavigationWindow(int type) {
        return type == 2000 || type == 2014 || type == 2019 || type == 2024;
    }

    public int getLowResolutionMode(String pkgName, WindowManager.LayoutParams attrs, float scaleRatio) {
        if (pkgName == null || attrs == null) {
            return 1;
        }
        if (pkgName.contains("com.huawei.hwid") || pkgName.contains("com.huawei.gameassistant") || pkgName.contains("com.huawei.appmarket") || attrs.type == 2020) {
            return 2;
        }
        boolean statusBarExpanded = false;
        if (isStatusNavigationWindow(attrs.type)) {
            if ((attrs.hwFlags & 4) != 0) {
                statusBarExpanded = true;
            }
            if ((attrs.type == 2000 || attrs.type == 2014) && statusBarExpanded) {
                return 1;
            }
            return 2;
        } else if (isValidSdrRatio(scaleRatio)) {
            return 2;
        } else {
            return 0;
        }
    }

    public int getAppKillModeWhenRogChange(Context context, String processName) {
        boolean isResolutionChanging;
        if (context != null) {
            isResolutionChanging = Settings.Global.getInt(context.getContentResolver(), "aps_display_resolution_changing", 0) == 1;
        } else {
            isResolutionChanging = false;
        }
        if (!isResolutionChanging) {
            return 0;
        }
        for (String name : ROG_NOT_KILL_APPS) {
            if (name.equals(processName)) {
                return 2;
            }
        }
        for (String name2 : ROG_KILL_APPS) {
            if (name2.equals(processName)) {
                return 1;
            }
        }
        return 0;
    }

    public static void notifyActivityIdle(String pkgName, String processName, String activityName) {
        int switchFlag;
        if (pkgName != null && (switchFlag = SystemProperties.getInt("sys.rog.switchflag", -1)) != -1) {
            String lowerCaseName = pkgName.toLowerCase(Locale.ENGLISH);
            if (lowerCaseName.contains(HwSettings.AUTHORITY) || lowerCaseName.contains("systemmanager")) {
                Slog.i(TAG, "APS: ROG, HwApsImpl, activity is ready, name = " + activityName);
                SystemProperties.set("sys.rog.switchflag", Integer.toString(((((switchFlag & 14) >> 1) + 1) << 1) | (switchFlag & 1)));
            }
        }
    }

    public boolean isAllLowResolutionSwitchable() {
        return SystemProperties.getInt("sys.aps.sdr.always_switchable", 1) == 1;
    }

    private boolean isApsFeatureSupport(int featureBit) {
        if (sLibraryLoadedOk) {
            return nIsApsFeatureSupport(featureBit);
        }
        return false;
    }

    private boolean isChipsetSupportRog(int featureBit) {
        if (sLibraryLoadedOk) {
            return nIsChipsetSupportRog();
        }
        return false;
    }

    public void callNativeStart(long nativeObject, int fps) {
        if (sLibraryLoadedOk) {
            nativeStart(nativeObject, fps);
        }
    }

    public void callNativeStartFeedback(long nativeObject, int fpsIncrement) {
        if (sLibraryLoadedOk) {
            nativeStartFeedback(nativeObject, fpsIncrement);
        }
    }

    public void callNativeStop(long nativeObject) {
        if (sLibraryLoadedOk) {
            nativeStop(nativeObject);
        }
    }

    public int callNativeGetCurFps(long nativeObject) {
        if (sLibraryLoadedOk) {
            return nativeGetCurFps(nativeObject);
        }
        return -1;
    }

    public int callNativeGetTargetFps(long nativeObject) {
        if (sLibraryLoadedOk) {
            return nativeGetTargetFps(nativeObject);
        }
        return -1;
    }

    public void callNativeFpsRequestRelease(long nativeObject) {
        if (sLibraryLoadedOk) {
            nativeFpsRequestRelease(nativeObject);
        }
    }

    public long callNativeInitFpsRequest(long type) {
        if (sLibraryLoadedOk) {
            return nativeInitFpsRequest(type);
        }
        return 0;
    }

    public long callNativeInitFpsController() {
        if (sLibraryLoadedOk) {
            return nativeInitFpsController();
        }
        return 0;
    }

    public void callNativePowerCtroll(long nativeObject) {
        if (sLibraryLoadedOk) {
            nativePowerCtroll(nativeObject);
        }
    }

    public void callNativeFpsControllerRelease(long nativeObject) {
        if (sLibraryLoadedOk) {
            nativeFpsControllerRelease(nativeObject);
        }
    }

    public void callNativeSetUiFrameState(boolean isState) {
        if (sLibraryLoadedOk) {
            nativeSetUiFrameState(isState);
        }
    }
}
