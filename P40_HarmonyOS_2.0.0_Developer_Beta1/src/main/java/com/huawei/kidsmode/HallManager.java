package com.huawei.kidsmode;

import com.android.server.devicepolicy.HwLog;

public class HallManager {
    public static final int CODE_FAILED = -1;
    private static final String TAG = "HallManager";

    private static native int native_getPsExternalIrMode();

    private static native int native_setPsExternalIrMode(int i);

    private HallManager() {
    }

    public static int setPsExternalIrMode(int id) {
        try {
            return native_setPsExternalIrMode(id);
        } catch (UnsatisfiedLinkError e) {
            HwLog.e(TAG, "setPsExternalIrMode failed!");
            return -1;
        }
    }
}
