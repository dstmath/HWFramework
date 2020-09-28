package com.android.internal.telephony;

import android.os.Handler;

public class HwDsdsController extends Handler {
    private static HwDsdsController mInstance;

    private HwDsdsController() {
    }

    public static HwDsdsController getInstance() {
        if (mInstance == null) {
            mInstance = new HwDsdsController();
        }
        return mInstance;
    }

    public boolean uiccHwdsdsNeedSetActiveMode() {
        throw new RuntimeException("not support any more, please delete it from your code");
    }
}
