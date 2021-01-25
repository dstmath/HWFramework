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
        this.mNativeObject = HwApsImpl.nativeInitFpsRequest((long) type.ordinal());
    }

    public void start(int fps) {
        HwApsImpl.nativeStart(this.mNativeObject, fps);
    }

    public void startFeedback(int fpsIncrement) {
        HwApsImpl.nativeStartFeedback(this.mNativeObject, fpsIncrement);
    }

    public void stop() {
        HwApsImpl.nativeStop(this.mNativeObject);
    }

    public int getCurFps() {
        return HwApsImpl.nativeGetCurFps(this.mNativeObject);
    }

    public int getTargetFps() {
        return HwApsImpl.nativeGetTargetFps(this.mNativeObject);
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        try {
            HwApsImpl.nativeFpsRequestRelease(this.mNativeObject);
        } finally {
            super.finalize();
        }
    }
}
