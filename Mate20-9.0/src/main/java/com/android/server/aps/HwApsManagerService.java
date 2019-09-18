package com.android.server.aps;

import android.app.ActivityManagerInternal;
import android.app.IUiModeManager;
import android.app.UiModeManager;
import android.aps.ApsAppInfo;
import android.aps.IApsManager;
import android.aps.IApsManagerServiceCallback;
import android.aps.IHwApsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Slog;
import android.view.Display;
import android.view.WindowManager;
import com.android.server.LocalServices;
import com.android.server.ServiceThread;
import com.android.server.gesture.GestureNavConst;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class HwApsManagerService extends IHwApsManager.Stub {
    static final int APS_COMPAT_MODE_MSG_FORCESTOP_APK = 310;
    static final int APS_COMPAT_MODE_MSG_WRITE = 300;
    private static final float APS_MANAGER_SERVICE_VERSION = 1.0f;
    public static final int FIRST_ON_SMART_FHD = 1;
    public static final int LOW_RESOLUTION_CLOSE_STATUS = 0;
    public static final int LOW_RESOLUTION_OPEN_STATUS = 1;
    private static final String LOW_RESOLUTION_SWITCH_STATUS = "low_resolution_switch";
    private static final String TAG = "HwApsManagerService";
    private static int apsMask = 255;
    private int longSize = 0;
    private ArrayList<IApsManagerServiceCallback> mCallbacks = new ArrayList<>();
    private final BroadcastReceiver mCarModeReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                try {
                    if (intent.getAction() != null) {
                        if (intent.getAction().equals(UiModeManager.ACTION_ENTER_CAR_MODE) || intent.getAction().equals(UiModeManager.ACTION_EXIT_CAR_MODE)) {
                            if (IUiModeManager.Stub.asInterface(ServiceManager.getService("uimode")).getCurrentModeType() == 3) {
                                HwApsManagerService.this.mInCarMode = true;
                                HwApsManagerService.this.mHwApsManagerServiceConfig.stopAllAppsInLowResolution();
                            } else {
                                HwApsManagerService.this.mInCarMode = false;
                                HwApsManagerService.this.mHwApsManagerServiceConfig.stopAllAppsInLowResolution();
                            }
                            Slog.i(HwApsManagerService.TAG, "APS: HwApsManagerService.BroadcastReceiver.onRecive, now is in car mode? = " + HwApsManagerService.this.mInCarMode);
                        }
                    }
                } catch (RemoteException e) {
                    Slog.e(HwApsManagerService.TAG, "APS: HwApsManagerService.BroadcastReceiver.onRecive, remote exception is thrown. e = " + e);
                }
            }
        }
    };
    private final Context mContext;
    private final ApsMainHandler mHandler;
    private final ServiceThread mHandlerThread;
    /* access modifiers changed from: private */
    public HwApsManagerServiceConfig mHwApsManagerServiceConfig;
    boolean mInCarMode = false;
    private int shortSize = 0;

    final class ApsMainHandler extends Handler {
        public ApsMainHandler(Looper looper) {
            super(looper, null, true);
        }
    }

    private final class LocalService implements IApsManager {
        private LocalService() {
        }

        public int setResolution(String pkgName, float ratio, boolean switchable) {
            return -1;
        }

        public int setLowResolutionMode(int lowResolutionMode) {
            return -1;
        }

        public int setDescentGradeResolution(String pkgName, int reduceLevel, boolean switchable) {
            return -1;
        }

        public int setFps(String pkgName, int fps) {
            return -1;
        }

        public int setMaxFps(String pkgName, int fps) {
            return -1;
        }

        public int setBrightness(String pkgName, int ratioPercent) {
            return -1;
        }

        public int setTexture(String pkgName, int ratioPercent) {
            return -1;
        }

        public int setPackageApsInfo(String pkgName, ApsAppInfo info) {
            return -1;
        }

        public ApsAppInfo getPackageApsInfo(String pkgName) {
            return HwApsManagerService.this.getPackageApsInfo(pkgName);
        }

        public float getResolution(String pkgName) {
            return HwApsManagerService.this.getResolution(pkgName);
        }

        public int getFps(String pkgName) {
            return -1;
        }

        public int getMaxFps(String pkgName) {
            return -1;
        }

        public int getBrightness(String pkgName) {
            return -1;
        }

        public int getTexture(String pkgName) {
            return -1;
        }

        public boolean deletePackageApsInfo(String pkgName) {
            return false;
        }

        public int isFeaturesEnabled(int bitmask) {
            return 0;
        }

        public boolean disableFeatures(int bitmask) {
            return false;
        }

        public boolean enableFeatures(int bitmak) {
            return false;
        }

        public List<ApsAppInfo> getAllPackagesApsInfo() {
            return null;
        }

        public List<String> getAllApsPackages() {
            return null;
        }

        public boolean updateApsInfo(List<ApsAppInfo> list) {
            return false;
        }

        public boolean registerCallback(String pkgName, IApsManagerServiceCallback callback) {
            return false;
        }

        public float getSeviceVersion() {
            return 1.0f;
        }

        public boolean stopPackages(List<String> list) {
            return false;
        }

        public int setDynamicResolutionRatio(String pkgName, float ratio) {
            return 0;
        }

        public float getDynamicResolutionRatio(String pkgName) {
            return -1.0f;
        }

        public int setDynamicFps(String pkgName, int fps) {
            return 0;
        }

        public int getDynamicFps(String pkgName) {
            return -1;
        }

        public int setFbSkip(String pkgName, boolean onoff) {
            return -1;
        }

        public int setHighpToLowp(String pkgName, boolean onoff) {
            return -1;
        }

        public int setShadowMap(String pkgName, int status) {
            return -1;
        }

        public int setMipMap(String pkgName, int status) {
            return -1;
        }

        public boolean getFbSkip(String pkgName) {
            return false;
        }

        public boolean getHighpToLowp(String pkgName) {
            return false;
        }

        public int getShadowMap(String pkgName) {
            return -1;
        }

        public int getMipMap(String pkgName) {
            return -1;
        }
    }

    public HwApsManagerService(Context context) {
        this.mContext = context;
        this.mHandlerThread = new ServiceThread(TAG, -2, false);
        this.mHandlerThread.start();
        this.mHandler = new ApsMainHandler(this.mHandlerThread.getLooper());
        Display display = ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getRealMetrics(displayMetrics);
        if (displayMetrics.widthPixels < displayMetrics.heightPixels) {
            this.longSize = displayMetrics.heightPixels;
            this.shortSize = displayMetrics.widthPixels;
        } else {
            this.longSize = displayMetrics.widthPixels;
            this.shortSize = displayMetrics.heightPixels;
        }
        Slog.e(TAG, "APS ManagerService.screen longSize" + this.longSize + ",shortSize=" + this.shortSize);
        IntentFilter filter = new IntentFilter();
        filter.addAction(UiModeManager.ACTION_ENTER_CAR_MODE);
        filter.addAction(UiModeManager.ACTION_EXIT_CAR_MODE);
        context.registerReceiver(this.mCarModeReceiver, filter);
    }

    public void systemReady() {
        this.mHwApsManagerServiceConfig = new HwApsManagerServiceConfig(this, this.mHandler);
        LocalServices.addService(IApsManager.class, new LocalService());
        Slog.i(TAG, "APS systemReady");
    }

    private boolean checkCallingPermission(String permission, String func) {
        if (Binder.getCallingPid() == Process.myPid() || this.mContext.checkCallingPermission(permission) == 0) {
            return true;
        }
        Slog.w(TAG, "Permission Denial: " + func + " from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + " requires " + permission);
        return false;
    }

    public int getLowResolutionSwitchState() {
        return Settings.Global.getInt(this.mContext.getContentResolver(), LOW_RESOLUTION_SWITCH_STATUS, 0);
    }

    private void enforceNotIsolatedCaller(String caller) {
        if (UserHandle.isIsolated(Binder.getCallingUid())) {
            throw new SecurityException("Isolated process not allowed to call " + caller);
        }
    }

    private boolean validBitmask(int bitmask) {
        if (bitmask >= 0 && bitmask <= 15) {
            return true;
        }
        Slog.e(TAG, "invalide param bitmask =" + bitmask);
        return false;
    }

    public int setLowResolutionMode(int lowResolutionMode) {
        int lowResolutionModeLocked;
        if (!checkCallingPermission("com.huawei.aps.permission.UPDATE_APS_INFO", "setLowResolutionMode")) {
            return -3;
        }
        synchronized (this) {
            lowResolutionModeLocked = this.mHwApsManagerServiceConfig.setLowResolutionModeLocked(lowResolutionMode);
        }
        return lowResolutionModeLocked;
    }

    public int setResolution(String pkgName, float ratio, boolean switchable) {
        int resolutionLocked;
        if (!checkCallingPermission("com.huawei.aps.permission.UPDATE_APS_INFO", "setResolution")) {
            return -3;
        }
        synchronized (this) {
            resolutionLocked = this.mHwApsManagerServiceConfig.setResolutionLocked(pkgName, ratio, switchable);
        }
        return resolutionLocked;
    }

    public int setDescentGradeResolution(String pkgName, int reduceLevel, boolean switchable) {
        float ratio;
        if (!checkCallingPermission("com.huawei.aps.permission.UPDATE_APS_INFO", "setDescentGradeResolution")) {
            return -3;
        }
        if (this.shortSize == 1080 && this.longSize == 1920) {
            if (reduceLevel == 1) {
                ratio = 0.6667f;
            } else if (reduceLevel == 0) {
                ratio = 1.0f;
            } else {
                Slog.e(TAG, "setDescentGradeResolution invalid param =" + reduceLevel);
                return -1;
            }
        } else if (this.shortSize != 1440 || this.longSize != 2560) {
            Slog.e(TAG, "setDescentGradeResolution invalid param.");
            return -1;
        } else if (reduceLevel == 2) {
            ratio = 0.5f;
        } else if (reduceLevel == 1) {
            ratio = 0.75f;
        } else if (reduceLevel == 0) {
            ratio = 1.0f;
        } else {
            Slog.e(TAG, "setDescentGradeResolution invalid param =" + reduceLevel);
            return -1;
        }
        return setResolution(pkgName, ratio, switchable);
    }

    public int setFps(String pkgName, int fps) {
        int fpsLocked;
        if (!checkCallingPermission("com.huawei.aps.permission.UPDATE_APS_INFO", "setFps")) {
            return -3;
        }
        synchronized (this) {
            fpsLocked = this.mHwApsManagerServiceConfig.setFpsLocked(pkgName, fps);
        }
        return fpsLocked;
    }

    public int setMaxFps(String pkgName, int fps) {
        int maxFpsLocked;
        if (!checkCallingPermission("com.huawei.aps.permission.UPDATE_APS_INFO", "setMaxFps")) {
            return -3;
        }
        synchronized (this) {
            maxFpsLocked = this.mHwApsManagerServiceConfig.setMaxFpsLocked(pkgName, fps);
        }
        return maxFpsLocked;
    }

    public int setBrightness(String pkgName, int ratioPercent) {
        int result;
        if (!checkCallingPermission("com.huawei.aps.permission.UPDATE_APS_INFO", "setBrightness")) {
            return -3;
        }
        synchronized (this) {
            result = this.mHwApsManagerServiceConfig.setBrightnessLocked(pkgName, ratioPercent);
            if (result == 0) {
                this.mHwApsManagerServiceConfig.notifyApsManagerServiceCallback(pkgName, 3, ratioPercent);
            }
        }
        return result;
    }

    public int setTexture(String pkgName, int ratioPercent) {
        int result;
        if (!checkCallingPermission("com.huawei.aps.permission.UPDATE_APS_INFO", "setTexture")) {
            return -3;
        }
        synchronized (this) {
            result = this.mHwApsManagerServiceConfig.setTextureLocked(pkgName, ratioPercent);
            if (result == 0) {
                this.mHwApsManagerServiceConfig.notifyApsManagerServiceCallback(pkgName, 2, ratioPercent);
            }
        }
        return result;
    }

    public int setFbSkip(String pkgName, boolean onoff) {
        int result;
        if (!checkCallingPermission("com.huawei.aps.permission.UPDATE_APS_INFO", "setFbSkip")) {
            return -3;
        }
        int status = onoff;
        synchronized (this) {
            result = this.mHwApsManagerServiceConfig.setFbSkipLocked(pkgName, onoff);
            if (result == 0) {
                this.mHwApsManagerServiceConfig.notifyApsManagerServiceCallback(pkgName, 4, (int) status);
            }
        }
        return result;
    }

    public int setHighpToLowp(String pkgName, boolean onoff) {
        int result;
        if (!checkCallingPermission("com.huawei.aps.permission.UPDATE_APS_INFO", "setHighpToLowp")) {
            return -3;
        }
        int status = onoff;
        synchronized (this) {
            result = this.mHwApsManagerServiceConfig.setHighpToLowpLocked(pkgName, onoff);
            if (result == 0) {
                this.mHwApsManagerServiceConfig.notifyApsManagerServiceCallback(pkgName, 5, (int) status);
            }
        }
        return result;
    }

    public int setShadowMap(String pkgName, int status) {
        int result;
        if (!checkCallingPermission("com.huawei.aps.permission.UPDATE_APS_INFO", "setShadowMap")) {
            return -3;
        }
        synchronized (this) {
            result = this.mHwApsManagerServiceConfig.setShadowMapLocked(pkgName, status);
            if (result == 0) {
                this.mHwApsManagerServiceConfig.notifyApsManagerServiceCallback(pkgName, 6, status);
            }
        }
        return result;
    }

    public int setMipMap(String pkgName, int status) {
        int result;
        if (!checkCallingPermission("com.huawei.aps.permission.UPDATE_APS_INFO", "setMipMap")) {
            return -3;
        }
        synchronized (this) {
            result = this.mHwApsManagerServiceConfig.setMipMapLocked(pkgName, status);
            if (result == 0) {
                this.mHwApsManagerServiceConfig.notifyApsManagerServiceCallback(pkgName, 7, status);
            }
        }
        return result;
    }

    public boolean getFbSkip(String pkgName) {
        if ((apsMask & 16) == 0) {
            return false;
        }
        enforceNotIsolatedCaller("getFbSkip");
        return this.mHwApsManagerServiceConfig.getFbSkipLocked(pkgName);
    }

    public boolean getHighpToLowp(String pkgName) {
        if ((apsMask & 32) == 0) {
            return false;
        }
        enforceNotIsolatedCaller("getHighpToLowp");
        return this.mHwApsManagerServiceConfig.getHighpToLowpLocked(pkgName);
    }

    public int getShadowMap(String pkgName) {
        if ((apsMask & 64) == 0) {
            return -6;
        }
        enforceNotIsolatedCaller("getShadowMap");
        return this.mHwApsManagerServiceConfig.getShadowMapLocked(pkgName);
    }

    public int getMipMap(String pkgName) {
        if ((apsMask & 128) == 0) {
            return -6;
        }
        enforceNotIsolatedCaller("getMipMap");
        return this.mHwApsManagerServiceConfig.getMipMapLocked(pkgName);
    }

    public int setDynamicResolutionRatio(String pkgName, float ratio) {
        int result;
        if (!checkCallingPermission("com.huawei.aps.permission.UPDATE_APS_INFO", "setResolution")) {
            return -3;
        }
        synchronized (this) {
            result = this.mHwApsManagerServiceConfig.setDynamicResolutionRatioLocked(pkgName, ratio);
            if (result == 0) {
                this.mHwApsManagerServiceConfig.notifyApsManagerServiceCallback(pkgName, 1, (int) (100000.0f * ratio));
            }
        }
        return result;
    }

    public float getDynamicResolutionRatio(String pkgName) {
        if ((apsMask & 1) == 0) {
            return -1.0f;
        }
        enforceNotIsolatedCaller("getDynamicResolutionRatio");
        return this.mHwApsManagerServiceConfig.getDynamicResolutionRatioLocked(pkgName);
    }

    public int setDynamicFps(String pkgName, int fps) {
        int result;
        if (!checkCallingPermission("com.huawei.aps.permission.UPDATE_APS_INFO", "setFps")) {
            return -3;
        }
        synchronized (this) {
            result = this.mHwApsManagerServiceConfig.setDynamicFpsLocked(pkgName, fps);
            if (result == 0) {
                this.mHwApsManagerServiceConfig.notifyApsManagerServiceCallback(pkgName, 0, fps);
            }
        }
        return result;
    }

    public int getDynamicFps(String pkgName) {
        if ((apsMask & 2) == 0) {
            return -1;
        }
        enforceNotIsolatedCaller("getDynamicFps");
        return this.mHwApsManagerServiceConfig.getDynamicFpsLocked(pkgName);
    }

    public int setPackageApsInfo(String pkgName, ApsAppInfo info) {
        int packageApsInfoLocked;
        if (!checkCallingPermission("com.huawei.aps.permission.UPDATE_APS_INFO", "setPackageApsInfo")) {
            return -3;
        }
        synchronized (this) {
            packageApsInfoLocked = this.mHwApsManagerServiceConfig.setPackageApsInfoLocked(pkgName, info);
        }
        return packageApsInfoLocked;
    }

    public ApsAppInfo getPackageApsInfo(String pkgName) {
        enforceNotIsolatedCaller("getPackageApsInfo");
        if (!checkCallingPermission("com.huawei.aps.permission.UPDATE_APS_INFO", "getPackageApsInfo")) {
            return null;
        }
        if (pkgName == null) {
            Slog.e(TAG, "getPackageApsInfo pkgName null");
        }
        return this.mHwApsManagerServiceConfig.getPackageApsInfoLocked(pkgName);
    }

    public float getResolution(String pkgName) {
        if (this.mHwApsManagerServiceConfig == null || (1 & apsMask) == 0) {
            return -1.0f;
        }
        enforceNotIsolatedCaller("getResolution");
        return this.mHwApsManagerServiceConfig.getResolutionLocked(pkgName);
    }

    public int getFps(String pkgName) {
        if ((apsMask & 2) == 0) {
            return -1;
        }
        enforceNotIsolatedCaller("getFps");
        return this.mHwApsManagerServiceConfig.getFpsLocked(pkgName);
    }

    public int getMaxFps(String pkgName) {
        if ((apsMask & 2) == 0) {
            return -1;
        }
        enforceNotIsolatedCaller("getMaxFps");
        return this.mHwApsManagerServiceConfig.getMaxFpsLocked(pkgName);
    }

    public int getBrightness(String pkgName) {
        if ((apsMask & 8) == 0) {
            return -1;
        }
        enforceNotIsolatedCaller("getBrightness");
        return this.mHwApsManagerServiceConfig.getBrightnessLocked(pkgName);
    }

    public int getTexture(String pkgName) {
        if ((apsMask & 4) == 0) {
            return -1;
        }
        enforceNotIsolatedCaller("getTexture");
        return this.mHwApsManagerServiceConfig.getTextureLocked(pkgName);
    }

    public boolean deletePackageApsInfo(String pkgName) {
        enforceNotIsolatedCaller("deletePackageApsInfo");
        if (!checkCallingPermission("com.huawei.aps.permission.UPDATE_APS_INFO", "deletePackageApsInfo")) {
            return false;
        }
        return this.mHwApsManagerServiceConfig.deletePackageApsInfoLocked(pkgName);
    }

    public int isFeaturesEnabled(int bitmask) {
        if (!checkCallingPermission("com.huawei.aps.permission.UPDATE_APS_INFO", "isFeaturesEnabled")) {
            return -3;
        }
        if (!validBitmask(bitmask)) {
            return -1;
        }
        return apsMask & bitmask;
    }

    public boolean disableFeatures(int bitmask) {
        if (!validBitmask(bitmask)) {
            return false;
        }
        apsMask &= ~bitmask;
        return true;
    }

    public boolean enableFeatures(int bitmask) {
        if (!validBitmask(bitmask)) {
            return false;
        }
        apsMask |= bitmask;
        return true;
    }

    public List<ApsAppInfo> getAllPackagesApsInfo() {
        enforceNotIsolatedCaller("getAllPackagesApsInfo");
        if (!checkCallingPermission("com.huawei.aps.permission.UPDATE_APS_INFO", "getAllPackagesApsInfo")) {
            return null;
        }
        return this.mHwApsManagerServiceConfig.getAllPackagesApsInfoLocked();
    }

    public List<String> getAllApsPackages() {
        enforceNotIsolatedCaller("getAllApsPackages");
        if (!checkCallingPermission("com.huawei.aps.permission.UPDATE_APS_INFO", "getAllApsPackages")) {
            return null;
        }
        return this.mHwApsManagerServiceConfig.getAllApsPackagesLocked();
    }

    public boolean updateApsInfo(List<ApsAppInfo> infos) {
        enforceNotIsolatedCaller("updateApsInfo");
        if (!checkCallingPermission("com.huawei.aps.permission.UPDATE_APS_INFO", "updateApsInfo")) {
            return false;
        }
        return this.mHwApsManagerServiceConfig.updateApsInfoLocked(infos);
    }

    public boolean registerCallback(String pkgName, IApsManagerServiceCallback callback) {
        boolean registerCallbackLocked;
        synchronized (this) {
            registerCallbackLocked = this.mHwApsManagerServiceConfig.registerCallbackLocked(pkgName, callback);
        }
        return registerCallbackLocked;
    }

    public float getSeviceVersion() {
        if (!checkCallingPermission("com.huawei.aps.permission.UPDATE_APS_INFO", "getSeviceVersion")) {
            return GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        }
        return 1.0f;
    }

    public boolean stopPackages(List<String> pkgs) {
        Slog.w(TAG, "stopPackages.");
        if (((ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class)) == null) {
            Slog.e(TAG, "stopPackages: can not get AMS.");
            return false;
        }
        Iterator<String> it = pkgs.iterator();
        while (it.hasNext()) {
            Slog.i(TAG, "forceStopPackage=" + it.next());
        }
        return true;
    }
}
