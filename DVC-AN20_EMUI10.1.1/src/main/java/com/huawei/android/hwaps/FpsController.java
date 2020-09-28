package com.huawei.android.hwaps;

import android.util.Log;

public class FpsController implements IFpsController {
    private static final String TAG = "Hwaps";
    private long mNativeObject = HwApsInterface.nativeInitFpsController();

    public void powerCtroll() {
        HwApsInterface.nativePowerCtroll(this.mNativeObject);
    }

    public FpsController() {
        Log.d(TAG, "FpsController create");
    }

    public void setUIFrameState(boolean state) {
        HwApsInterface.nativeSetUIFrameState(state);
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        try {
            HwApsInterface.nativeFpsControllerRelease(this.mNativeObject);
        } finally {
            super.finalize();
        }
    }
}
