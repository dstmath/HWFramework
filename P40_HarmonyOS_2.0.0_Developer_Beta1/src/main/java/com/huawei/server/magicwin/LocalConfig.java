package com.huawei.server.magicwin;

import com.huawei.server.utils.Utils;

public class LocalConfig extends BaseAppConfig {
    private static final int LOCAL_ONLY = 1000;
    private static final String TAG = "HWMW_LocalConfig";
    private boolean mIsLocalOnly;

    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0087, code lost:
        if (strToBoolean(r19) != false) goto L_0x008d;
     */
    public LocalConfig(String pkg, String mode, String fullScreenVideo, String leftResume, String cameraPreview, String isScaleEnabled, String needRelaunch, String defaultSetting, String isDragable, String isNotchAdapted, String isDragFs, boolean isFoldableDevice) {
        this.mPackageName = pkg;
        boolean z = true;
        this.mMode = Utils.strToInt(mode, 1);
        if (!isValidMode(this.mMode)) {
            this.mMode = 1;
        }
        this.mIsLocalOnly = (this.mMode >= LOCAL_ONLY && this.mMode <= 1002) || this.mMode == -1;
        if (this.mIsLocalOnly && this.mMode >= LOCAL_ONLY) {
            this.mMode -= LOCAL_ONLY;
        }
        this.mSupportVideoFScreen = strToBoolean(fullScreenVideo);
        this.mSupportLeftResume = strToBoolean(leftResume);
        this.mSupportCameraPreview = strToBoolean(cameraPreview);
        this.mIsDragable = strToBoolean(isDragable);
        this.mNeedRelaunch = strToBoolean(needRelaunch);
        this.mIsDefaultSetting = isFoldableDevice ? "true".equals(defaultSetting) : !"false".equals(defaultSetting);
        if (!this.mIsDragable) {
        }
        z = false;
        this.mIsScaleEnabled = z;
        this.mIsNotchAdapted = strToBoolean(isNotchAdapted);
        this.mIsDragToFullscreen = strToBoolean(isDragFs);
    }

    public boolean isLocalOnly() {
        return this.mIsLocalOnly;
    }

    private boolean isValidMode(int mode) {
        return mode == -2 || mode == -1 || mode == 0 || mode == 1 || mode == 2 || mode == LOCAL_ONLY || mode == 1001 || mode == 1002;
    }

    private boolean strToBoolean(String str) {
        return "true".equals(str);
    }
}
