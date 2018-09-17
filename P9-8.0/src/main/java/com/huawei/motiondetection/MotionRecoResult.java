package com.huawei.motiondetection;

import android.os.Bundle;

public class MotionRecoResult {
    public static final int MOTION_DIRECTION_DEFAULT = 0;
    public static final int MOTION_DIRECTION_DOWN = 4;
    public static final int MOTION_DIRECTION_LEFT = 1;
    public static final int MOTION_DIRECTION_RIGHT = 2;
    public static final int MOTION_DIRECTION_UP = 3;
    public static final int MOTION_RECOGNITION_RESULT_1 = 1;
    public static final int MOTION_RECOGNITION_RESULT_2 = 2;
    public static final int MOTION_RECOGNITION_RESULT_FAILED = 0;
    public static final int MOTION_RECOGNITION_RESULT_SUCCESSFUL = 1;
    public static final int MOTION_REGISTER_TYPE_BINDSERVICE = 2;
    public static final int MOTION_REGISTER_TYPE_BROADCAST = 1;
    public static final int MOTION_REGISTER_TYPE_SERIAL = 3;
    public int mMotionDirection = 0;
    public Bundle mMotionExtras = null;
    public int mMotionType = 0;
    public int mRecoResult = 0;

    public MotionRecoResult(int motion, int result, int direction, Bundle pExtras) {
        this.mMotionType = motion;
        this.mRecoResult = result;
        this.mMotionDirection = direction;
        this.mMotionExtras = pExtras;
    }
}
