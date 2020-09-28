package com.huawei.hwaps;

import android.aps.IApsManager;
import android.common.HwFrameworkFactory;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.graphics.RenderNode;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.HwLog;
import android.util.Log;
import android.util.Slog;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewRootImpl;
import android.view.WindowManager;
import com.android.server.LocalServices;
import com.huawei.android.hwaps.HwapsWrapper;
import com.huawei.android.hwaps.IEventAnalyzed;
import com.huawei.android.hwaps.IFpsController;
import com.huawei.sidetouch.TpCommandConstant;
import com.huawei.uikit.effect.BuildConfig;
import huawei.android.provider.HwSettings;
import java.util.Locale;

public class HwApsImpl implements IHwApsImpl {
    private static final int MIN_TIME_INTERNAL = 60000;
    private static final String TAG = "HwApsImpl";
    private static final String[] mRogShouldKillApps = {"com.huawei.android.launcher"};
    private static final String[] mRogShouldNotKillApps = {"com.android.deskclock", "com.huawei.aod", "com.huawei.android.thememanager", "com.huawei.security:SERVICE"};
    private static HwApsImpl sInstance = null;
    private boolean mApsInitialized = false;
    public View mBeingInvalidatedChild = null;
    private int mEpsTvCount = 0;
    private long mEpsTvDirty = 0;
    private int mEpsUiCount = 0;
    private long mEpsUiDirty = 0;
    private IEventAnalyzed mEventAnalyzed = null;
    private boolean mIsSupportApsDropEmptyFrame = false;
    private long mLastTime = System.currentTimeMillis();
    private int mWindowHeight = 0;
    private int mWindowWidth = 0;

    private native boolean nIsApsFeatureSupport(int i);

    private native boolean nIsChipsetSupportRog();

    private native boolean nIsDumpPartialUpdateOn();

    private native boolean nIsSupportApsPartialUpdate();

    private native void nSetSfrWorkPid(int i);

    static {
        try {
            System.loadLibrary("hwapsimpl_jni");
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "Load hwapsimpl_jni so failed, not found.");
        }
    }

    private HwApsImpl() {
        if (isSupportAps()) {
            this.mEventAnalyzed = HwapsWrapper.getEventAnalyzed();
            this.mIsSupportApsDropEmptyFrame = nIsApsFeatureSupport(67108864);
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

    public void initAPS(Context context, int screenWidth, int myPid) {
        if (!this.mApsInitialized) {
            IEventAnalyzed iEventAnalyzed = this.mEventAnalyzed;
            if (iEventAnalyzed != null) {
                iEventAnalyzed.initAPS(context, myPid);
                nSetSfrWorkPid(myPid);
                this.mWindowWidth = context.getResources().getDisplayMetrics().widthPixels;
                this.mWindowHeight = context.getResources().getDisplayMetrics().heightPixels;
            }
            this.mApsInitialized = true;
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

    public void setAPSOnPause() {
        if (isSupportFps()) {
            this.mEventAnalyzed.setHasOnPaused(true);
        }
    }

    public void processApsPointerEvent(Context context, String pkgName, int screenWidth, int myPid, MotionEvent event) {
        if (isSupportFps()) {
            initAPS(context, screenWidth, myPid);
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

    public void setLowResolutionMode(Context context, boolean enableLowResolutionMode) {
        Log.i("sdr", "APS: SDR: HwApsImpl.setLowResolutionMod, enableLowResolutionMode = " + enableLowResolutionMode);
        try {
            IApsManager apsManager = HwFrameworkFactory.getApsManager();
            if (apsManager != null) {
                apsManager.setLowResolutionMode(enableLowResolutionMode ? 1 : 0);
            }
        } catch (Exception e) {
            Log.i("sdr", "APS: SDR: HwApsImpl.setLowResolutionMode, exception = " + e.getClass());
        }
    }

    public boolean isLowResolutionSupported() {
        return isApsFeatureSupport(TpCommandConstant.TSA_EVENT_POWER_DOUBLE_CLICK);
    }

    public boolean isSupportApsPartialUpdate() {
        return nIsSupportApsPartialUpdate();
    }

    private boolean isDumpPartialUpdateOn() {
        return nIsDumpPartialUpdateOn();
    }

    private boolean isSupportFps() {
        return nIsApsFeatureSupport(1);
    }

    public void savePartialUpdateDirty(Rect currentDirtyRect, int l, int t, int r, int b, Context context, String name) {
        if (isSupportApsPartialUpdate()) {
            currentDirtyRect.union(l, t, r, b);
            if (isDumpPartialUpdateOn()) {
                Slog.i("partial", "APS: PU, invalidate, pkgname=" + context.getPackageName() + ", union dirty = (" + l + "," + t + "," + r + "," + b + "), now dirty = " + currentDirtyRect + ", classname = " + name);
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
        return 0.0f < resolutionRatio && resolutionRatio < 1.0f;
    }

    public float getResolutionRatioByPkgName(String appPkgName, String owningPkgName) {
        String pkgName = appPkgName;
        if (pkgName == null || pkgName.isEmpty()) {
            pkgName = owningPkgName;
        }
        return getResolutionRatioByPkgName(pkgName);
    }

    public float getResolutionRatioByPkgName(String packageName) {
        try {
            IApsManager apsManager = (IApsManager) LocalServices.getService(IApsManager.class);
            if (apsManager != null) {
                return apsManager.getResolution(packageName);
            }
            return 1.0f;
        } catch (Exception e) {
            Log.e("SDR", "APS: SDR: Apsmanager.getResolution, Exception is thrown!" + e.getClass());
            return 1.0f;
        }
    }

    public boolean checkAndApplyToDMByRatio(float resolutionRatio, DisplayMetrics inoutDm) {
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

    public boolean isIn1kResolutionof2kScreen() {
        int width = SystemProperties.getInt("sys.rog.width", 0);
        int realWidth = SystemProperties.getInt("persist.sys.rog.width", 0);
        if (width == 0 || width != realWidth) {
            return false;
        }
        return true;
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
        } else if (0.0f >= scaleRatio || scaleRatio >= 1.0f) {
            return 0;
        } else {
            return 2;
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
        for (String name : mRogShouldNotKillApps) {
            if (name.equals(processName)) {
                return 2;
            }
        }
        for (String name2 : mRogShouldKillApps) {
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

    public float getRogRatio() {
        try {
            float rogRatio = Float.parseFloat(SystemProperties.get("persist.sys.rog.ratio", BuildConfig.VERSION_NAME));
            if (0.0f >= rogRatio || rogRatio > 1.0f) {
                return 1.0f;
            }
            return rogRatio;
        } catch (NumberFormatException nfe) {
            Slog.e(TAG, "APS: ROG, HwApsImpl, exception is thrown, nfe = " + nfe);
        }
    }

    private boolean isApsFeatureSupport(int featureBit) {
        return nIsApsFeatureSupport(featureBit);
    }

    private boolean isChipsetSupportRog(int featureBit) {
        return nIsChipsetSupportRog();
    }
}
