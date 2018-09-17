package com.huawei.android.hwaps;

import android.app.HwApsInterface;
import android.util.Log;

public class FpsController implements IFpsController {
    private static final String TAG = "Hwaps";
    private long mNativeObject;

    public void powerCtroll() {
        HwApsInterface.nativePowerCtroll(this.mNativeObject);
    }

    public FpsController() {
        Log.d(TAG, "FpsController create");
        this.mNativeObject = HwApsInterface.nativeInitFpsController();
    }

    protected void finalize() throws Throwable {
        try {
            HwApsInterface.nativeFpsControllerRelease(this.mNativeObject);
        } finally {
            super.finalize();
        }
    }
}
