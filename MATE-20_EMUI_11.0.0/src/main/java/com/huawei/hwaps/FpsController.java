package com.huawei.hwaps;

import android.util.Log;

public class FpsController implements IFpsController {
    private static final String TAG = "Hwaps";
    private long mNativeObject = HwApsImpl.nativeInitFpsController();

    public FpsController() {
        Log.d(TAG, "FpsController create");
    }

    public void powerCtroll() {
        HwApsImpl.nativePowerCtroll(this.mNativeObject);
    }

    public void setUiFrameState(boolean isState) {
        HwApsImpl.nativeSetUiFrameState(isState);
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        try {
            HwApsImpl.nativeFpsControllerRelease(this.mNativeObject);
        } finally {
            super.finalize();
        }
    }
}
