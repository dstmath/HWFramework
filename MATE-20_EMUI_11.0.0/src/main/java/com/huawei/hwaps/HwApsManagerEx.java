package com.huawei.hwaps;

import android.aps.ApsAppInfo;
import android.aps.IApsManager;
import android.common.HwFrameworkFactory;
import android.util.Log;

public class HwApsManagerEx {
    private static final String TAG = "HwApsManagerEx";
    private static HwApsManagerEx sInstance = null;
    private IApsManager mApsManager;

    public static class ApsAppInfoEx {
        public ApsAppInfo apsAppInfo;

        public ApsAppInfoEx(String pkgName) {
            this(pkgName, 1.0f, 60, 60, 100, 100, false);
        }

        public ApsAppInfoEx(String pkgName, float resolutionRatio, int minFrameRatio, int maxFrameRatio, int texturePercent, int brightnessPercent, boolean isSwitchable) {
            this.apsAppInfo = new ApsAppInfo(pkgName, resolutionRatio, minFrameRatio, maxFrameRatio, texturePercent, brightnessPercent, isSwitchable);
        }

        public ApsAppInfoEx(ApsAppInfo info) {
            this.apsAppInfo = info;
        }

        public int hashCode() {
            return this.apsAppInfo.hashCode();
        }

        public boolean equals(Object otherInfo) {
            if (otherInfo != null && (otherInfo instanceof ApsAppInfoEx)) {
                return this.apsAppInfo.equals(((ApsAppInfoEx) otherInfo).apsAppInfo);
            }
            return false;
        }

        public String toString() {
            return this.apsAppInfo.toString();
        }

        public void setResolutionRatio(float resolutionRatio, boolean isEnable) {
            this.apsAppInfo.setResolutionRatio(resolutionRatio, isEnable);
        }

        public void setFps(int frameRate) {
            this.apsAppInfo.setFps(frameRate);
        }

        public void setMaxFps(int frameRate) {
            this.apsAppInfo.setMaxFps(frameRate);
        }
    }

    private HwApsManagerEx() {
        this.mApsManager = null;
        this.mApsManager = HwFrameworkFactory.getApsManager();
        if (this.mApsManager == null) {
            Log.e(TAG, "APS: APK: HwApsManagerEx: mApsManager null");
        }
    }

    @Deprecated
    public static HwApsManagerEx getIntance() {
        return getInstance();
    }

    public static synchronized HwApsManagerEx getInstance() {
        HwApsManagerEx hwApsManagerEx;
        synchronized (HwApsManagerEx.class) {
            if (sInstance == null) {
                sInstance = new HwApsManagerEx();
            }
            hwApsManagerEx = sInstance;
        }
        return hwApsManagerEx;
    }

    public int getDynamicFps(String pkgName) {
        IApsManager iApsManager = this.mApsManager;
        if (iApsManager != null) {
            return iApsManager.getDynamicFps(pkgName);
        }
        return -1;
    }

    public int setDynamicFps(String pkgName, int fps) {
        IApsManager iApsManager = this.mApsManager;
        if (iApsManager != null) {
            return iApsManager.setDynamicFps(pkgName, fps);
        }
        return -1;
    }

    public ApsAppInfoEx getPackageApsInfo(String pkgName) {
        ApsAppInfo info;
        IApsManager iApsManager = this.mApsManager;
        if (iApsManager == null || (info = iApsManager.getPackageApsInfo(pkgName)) == null) {
            return null;
        }
        return new ApsAppInfoEx(info);
    }

    public int setPackageApsInfo(String pkgName, ApsAppInfoEx info) {
        IApsManager iApsManager = this.mApsManager;
        if (iApsManager == null || info == null) {
            return -1;
        }
        return iApsManager.setPackageApsInfo(pkgName, info.apsAppInfo);
    }

    public boolean deletePackageApsInfo(String pkgName) {
        IApsManager iApsManager = this.mApsManager;
        if (iApsManager != null) {
            return iApsManager.deletePackageApsInfo(pkgName);
        }
        return false;
    }

    public int getMaxFps(String pkgName) {
        IApsManager iApsManager = this.mApsManager;
        if (iApsManager != null) {
            return iApsManager.getMaxFps(pkgName);
        }
        return -1;
    }

    public int setLowResolutionMode(int lowResolutionMode) {
        IApsManager iApsManager = this.mApsManager;
        if (iApsManager != null) {
            return iApsManager.setLowResolutionMode(lowResolutionMode);
        }
        return -1;
    }

    public float getResolution(String pkgName) {
        IApsManager iApsManager = this.mApsManager;
        if (iApsManager != null) {
            return iApsManager.getResolution(pkgName);
        }
        return -1.0f;
    }
}
