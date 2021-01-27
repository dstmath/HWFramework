package com.huawei.hwaps;

import android.util.Log;

public class FpsRequest implements IFpsRequest {
    private static final String TAG = "Hwaps";
    private long mNativeObject;

    public enum SceneTypeE {
        DEFAULT,
        TOUCH_IDENTY,
        OPENGL_IDENTY,
        EXACTLY_IDENTIFY,
        EXACTLY_NONPLAY_IDENTIFY,
        TOUCH_EMERGENCY,
        OPENGL_SETTING,
        BLACK_LIST,
        GAME_CENTER,
        THERMO_CONTROL,
        USER_EXPERIENCE
    }

    public FpsRequest() {
        this(SceneTypeE.DEFAULT);
    }

    public FpsRequest(SceneTypeE type) {
        Log.d(TAG, "Fpsrequest create,type:" + type);
        this.mNativeObject = HwApsImpl.getDefault().callNativeInitFpsRequest((long) type.ordinal());
    }

    public void start(int fps) {
        HwApsImpl.getDefault().callNativeStart(this.mNativeObject, fps);
    }

    public void startFeedback(int fpsIncrement) {
        HwApsImpl.getDefault().callNativeStartFeedback(this.mNativeObject, fpsIncrement);
    }

    public void stop() {
        HwApsImpl.getDefault().callNativeStop(this.mNativeObject);
    }

    public int getCurFps() {
        return HwApsImpl.getDefault().callNativeGetCurFps(this.mNativeObject);
    }

    public int getTargetFps() {
        return HwApsImpl.getDefault().callNativeGetTargetFps(this.mNativeObject);
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        try {
            HwApsImpl.getDefault().callNativeFpsRequestRelease(this.mNativeObject);
        } finally {
            super.finalize();
        }
    }
}
