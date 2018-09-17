package com.huawei.android.hwaps;

import android.app.HwApsInterface;
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
        this.mNativeObject = HwApsInterface.nativeInitFpsRequest((long) type.ordinal());
    }

    public void start(int fps) {
        HwApsInterface.nativeStart(this.mNativeObject, fps);
    }

    public void startFeedback(int fps_increment) {
        HwApsInterface.nativeStartFeedback(this.mNativeObject, fps_increment);
    }

    public void stop() {
        HwApsInterface.nativeStop(this.mNativeObject);
    }

    public int getCurFPS() {
        return HwApsInterface.nativeGetCurFPS(this.mNativeObject);
    }

    public int getTargetFPS() {
        return HwApsInterface.nativeGetTargetFPS(this.mNativeObject);
    }

    protected void finalize() throws Throwable {
        try {
            HwApsInterface.nativeFpsRequestRelease(this.mNativeObject);
        } finally {
            super.finalize();
        }
    }
}
