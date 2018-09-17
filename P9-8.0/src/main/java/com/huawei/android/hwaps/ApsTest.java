package com.huawei.android.hwaps;

import android.os.SystemProperties;

public class ApsTest {
    private static final String TAG = "ApsTest";
    private boolean mApsTestStopped = true;
    private FpsRequest mFpsRequest = null;
    private int mLastTargetFps = 60;
    private int mTargetFps = 60;

    public ApsTest() {
        if (this.mFpsRequest == null) {
            this.mFpsRequest = new FpsRequest();
        }
        this.mTargetFps = apsTestGetFpsProp();
        ApsCommon.logI(TAG, "ApsTest create success");
    }

    public static boolean isSupportAPSTset() {
        return 1024 == (SystemProperties.getInt("sys.aps.support", 0) & 1024);
    }

    public void stop() {
        if (!this.mApsTestStopped) {
            this.mFpsRequest.stop();
            this.mLastTargetFps = 60;
            this.mTargetFps = 60;
            this.mApsTestStopped = true;
            ApsCommon.logI(TAG, "stop aps test: set fps.");
        }
    }

    public void start() {
        if (this.mApsTestStopped) {
            this.mTargetFps = apsTestGetFpsProp();
            if (this.mTargetFps != this.mLastTargetFps) {
                this.mFpsRequest.start(this.mTargetFps);
            }
            this.mLastTargetFps = this.mTargetFps;
            this.mApsTestStopped = false;
            ApsCommon.logI(TAG, "start aps test: set fps.");
        }
    }

    private static int apsTestGetFpsProp() {
        return SystemProperties.getInt("debug.aps.playfps", 0);
    }
}
