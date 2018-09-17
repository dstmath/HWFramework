package com.android.server.aps;

import android.app.ActivityManagerInternal;
import android.aps.ApsAppInfo;
import android.aps.IApsManager;
import android.aps.IApsManagerServiceCallback;
import android.aps.IHwApsManager.Stub;
import android.content.Context;
import android.os.Binder;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.util.DisplayMetrics;
import android.util.Slog;
import android.view.Display;
import android.view.WindowManager;
import com.android.server.LocalServices;
import com.android.server.ServiceThread;
import com.android.server.display.HwEyeProtectionDividedTimeControl;
import java.util.ArrayList;
import java.util.List;

public class HwApsManagerService extends Stub {
    static final int APS_COMPAT_MODE_MSG_FORCESTOP_APK = 310;
    static final int APS_COMPAT_MODE_MSG_WRITE = 300;
    private static final float APS_MANAGER_SERVICE_VERSION = 1.0f;
    public static final int LOW_RESOLUTION_CLOSE_STATUS = 0;
    public static final int LOW_RESOLUTION_OPEN_STATUS = 1;
    private static final String LOW_RESOLUTION_SWITCH_STATUS = "low_resolution_switch";
    private static final String TAG = "HwApsManagerService";
    private static int apsMask = 15;
    private int longSize = 0;
    private ArrayList<IApsManagerServiceCallback> mCallbacks = new ArrayList();
    private final Context mContext;
    private final ApsMainHandler mHandler;
    private final ServiceThread mHandlerThread;
    private HwApsManagerServiceConfig mHwApsManagerServiceConfig;
    private int shortSize = 0;

    final class ApsMainHandler extends Handler {
        public ApsMainHandler(Looper looper) {
            super(looper, null, true);
        }
    }

    private final class LocalService implements IApsManager {
        /* synthetic */ LocalService(HwApsManagerService this$0, LocalService -this1) {
            this();
        }

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

        public boolean registerCallback(IApsManagerServiceCallback callback) {
            return false;
        }

        public float getSeviceVersion() {
            return 1.0f;
        }

        public boolean stopPackages(List<String> list) {
            return false;
        }

        public int setDynamicResolutionRatio(float ratio) {
            return 0;
        }

        public float getDynamicResolutionRatio() {
            return -1.0f;
        }

        public int setDynamicFPS(int fps) {
            return 0;
        }

        public int getDynamicFPS() {
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
    }

    public void systemReady() {
        this.mHwApsManagerServiceConfig = new HwApsManagerServiceConfig(this, this.mHandler);
        LocalServices.addService(IApsManager.class, new LocalService(this, null));
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
        int state;
        if (SystemProperties.getBoolean("sys.aps.2kenablesdrdefault", false)) {
            state = Global.getInt(this.mContext.getContentResolver(), LOW_RESOLUTION_SWITCH_STATUS, -1);
            if (state == -1) {
                if (this.longSize >= 2000) {
                    state = 1;
                } else {
                    state = 0;
                }
                Global.putInt(this.mContext.getContentResolver(), LOW_RESOLUTION_SWITCH_STATUS, state);
            }
        } else {
            state = Global.getInt(this.mContext.getContentResolver(), LOW_RESOLUTION_SWITCH_STATUS, 0);
        }
        if (state == 0) {
            return 0;
        }
        return 1;
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
        if (!checkCallingPermission("com.huawei.aps.permission.UPDATE_APS_INFO", "setLowResolutionMode")) {
            return -3;
        }
        int lowResolutionModeLocked;
        synchronized (this) {
            lowResolutionModeLocked = this.mHwApsManagerServiceConfig.setLowResolutionModeLocked(lowResolutionMode);
        }
        return lowResolutionModeLocked;
    }

    public int setResolution(String pkgName, float ratio, boolean switchable) {
        if (!checkCallingPermission("com.huawei.aps.permission.UPDATE_APS_INFO", "setResolution")) {
            return -3;
        }
        int result;
        synchronized (this) {
            result = this.mHwApsManagerServiceConfig.setResolutionLocked(pkgName, ratio, switchable);
            if (result == 0) {
                this.mHwApsManagerServiceConfig.notifyApsManagerServiceCallback(this.mCallbacks);
            }
        }
        return result;
    }

    public int setDescentGradeResolution(String pkgName, int reduceLevel, boolean switchable) {
        if (!checkCallingPermission("com.huawei.aps.permission.UPDATE_APS_INFO", "setDescentGradeResolution")) {
            return -3;
        }
        float ratio;
        if (this.shortSize == 1080 && this.longSize == 1920) {
            if (reduceLevel == 1) {
                ratio = 0.6667f;
            } else if (reduceLevel == 0) {
                ratio = 1.0f;
            } else {
                Slog.e(TAG, "setDescentGradeResolution invalid param =" + reduceLevel);
                return -1;
            }
        } else if (this.shortSize != HwEyeProtectionDividedTimeControl.DAY_IN_MINUTE || this.longSize != 2560) {
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
        if (!checkCallingPermission("com.huawei.aps.permission.UPDATE_APS_INFO", "setFps")) {
            return -3;
        }
        int result;
        synchronized (this) {
            result = this.mHwApsManagerServiceConfig.setFpsLocked(pkgName, fps);
            if (result == 0) {
                this.mHwApsManagerServiceConfig.notifyApsManagerServiceCallback(this.mCallbacks);
            }
        }
        return result;
    }

    public int setBrightness(String pkgName, int ratioPercent) {
        if (!checkCallingPermission("com.huawei.aps.permission.UPDATE_APS_INFO", "setBrightness")) {
            return -3;
        }
        int result;
        synchronized (this) {
            result = this.mHwApsManagerServiceConfig.setBrightnessLocked(pkgName, ratioPercent);
            if (result == 0) {
                this.mHwApsManagerServiceConfig.notifyApsManagerServiceCallback(this.mCallbacks);
            }
        }
        return result;
    }

    public int setTexture(String pkgName, int ratioPercent) {
        if (!checkCallingPermission("com.huawei.aps.permission.UPDATE_APS_INFO", "setTexture")) {
            return -3;
        }
        int result;
        synchronized (this) {
            result = this.mHwApsManagerServiceConfig.setTextureLocked(pkgName, ratioPercent);
            if (result == 0) {
                this.mHwApsManagerServiceConfig.notifyApsManagerServiceCallback(this.mCallbacks);
            }
        }
        return result;
    }

    public int setPackageApsInfo(String pkgName, ApsAppInfo info) {
        if (!checkCallingPermission("com.huawei.aps.permission.UPDATE_APS_INFO", "setPackageApsInfo")) {
            return -3;
        }
        int result;
        synchronized (this) {
            result = this.mHwApsManagerServiceConfig.setPackageApsInfoLocked(pkgName, info);
            if (result == 0) {
                this.mHwApsManagerServiceConfig.notifyApsManagerServiceCallback(this.mCallbacks);
            }
        }
        return result;
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
        if ((apsMask & 1) == 0) {
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
        if (checkCallingPermission("com.huawei.aps.permission.UPDATE_APS_INFO", "deletePackageApsInfo")) {
            return this.mHwApsManagerServiceConfig.deletePackageApsInfoLocked(pkgName);
        }
        return false;
    }

    public int isFeaturesEnabled(int bitmask) {
        if (!checkCallingPermission("com.huawei.aps.permission.UPDATE_APS_INFO", "isFeaturesEnabled")) {
            return -3;
        }
        if (validBitmask(bitmask)) {
            return apsMask & bitmask;
        }
        return -1;
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
        if (checkCallingPermission("com.huawei.aps.permission.UPDATE_APS_INFO", "getAllPackagesApsInfo")) {
            return this.mHwApsManagerServiceConfig.getAllPackagesApsInfoLocked();
        }
        return null;
    }

    public List<String> getAllApsPackages() {
        enforceNotIsolatedCaller("getAllApsPackages");
        if (checkCallingPermission("com.huawei.aps.permission.UPDATE_APS_INFO", "getAllApsPackages")) {
            return this.mHwApsManagerServiceConfig.getAllApsPackagesLocked();
        }
        return null;
    }

    public boolean updateApsInfo(List<ApsAppInfo> infos) {
        enforceNotIsolatedCaller("updateApsInfo");
        if (checkCallingPermission("com.huawei.aps.permission.UPDATE_APS_INFO", "updateApsInfo")) {
            return this.mHwApsManagerServiceConfig.updateApsInfoLocked(infos);
        }
        return false;
    }

    public boolean registerCallback(IApsManagerServiceCallback callback) {
        if (!checkCallingPermission("com.huawei.aps.permission.UPDATE_APS_INFO", "registerCallback")) {
            return false;
        }
        synchronized (this) {
            this.mCallbacks.add(callback);
        }
        return false;
    }

    public float getSeviceVersion() {
        if (checkCallingPermission("com.huawei.aps.permission.UPDATE_APS_INFO", "getSeviceVersion")) {
            return 1.0f;
        }
        return 0.0f;
    }

    public boolean stopPackages(List<String> pkgs) {
        Slog.w(TAG, "stopPackages.");
        if (((ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class)) == null) {
            Slog.e(TAG, "stopPackages: can not get AMS.");
            return false;
        }
        for (String pkgName : pkgs) {
            Slog.i(TAG, "forceStopPackage=" + pkgName);
        }
        return true;
    }
}
