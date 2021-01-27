package com.huawei.hwaps;

import android.util.Log;

public class FpsController implements IFpsController {
    private static final String TAG = "Hwaps";
    private long mNativeObject = HwApsImpl.getDefault().callNativeInitFpsController();

    public FpsController() {
        Log.d(TAG, "FpsController create");
    }

    public void powerCtroll() {
        HwApsImpl.getDefault().callNativePowerCtroll(this.mNativeObject);
    }

    public void setUiFrameState(boolean isState) {
        HwApsImpl.getDefault().callNativeSetUiFrameState(isState);
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        try {
            HwApsImpl.getDefault().callNativeFpsControllerRelease(this.mNativeObject);
        } finally {
            super.finalize();
        }
    }
}
