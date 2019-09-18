package com.huawei.android.hardware.display;

import android.hardware.display.DisplayManagerGlobal;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;

public class DisplayManagerEx {
    public static final String HW_WFD_ACTION_DLNA_ON_P2P_START = "HWE_DLNA_START";
    public static final String HW_WFD_ACTION_DLNA_ON_P2P_STOP = "HWE_DLNA_STOP";
    private static final String TAG = "DisplayManagerEx";

    private static DisplayManagerGlobal getDisplayManagerGlobal() {
        return DisplayManagerGlobal.getInstance();
    }

    public static void startWifiDisplayScan(int channelId) {
        if (getDisplayManagerGlobal() != null) {
            Log.d(TAG, "startWifiDisplayScan, pid:" + Process.myPid() + ", tid:" + Process.myTid() + ", uid:" + Process.myUid());
            getDisplayManagerGlobal().startWifiDisplayScan(channelId);
        }
    }

    public static void connectWifiDisplay(String deviceAddress, String verificaitonCode) {
        if (getDisplayManagerGlobal() != null) {
            getDisplayManagerGlobal().connectWifiDisplay(deviceAddress, verificaitonCode);
        }
    }

    public static boolean checkVerificationResult(boolean isRight) {
        if (getDisplayManagerGlobal() == null) {
            return false;
        }
        getDisplayManagerGlobal().checkVerificationResult(isRight);
        return true;
    }

    public static boolean sendWifiDisplayAction(String action) {
        if (getDisplayManagerGlobal() == null || TextUtils.isEmpty(action)) {
            return false;
        }
        return getDisplayManagerGlobal().sendWifiDisplayAction(action);
    }
}
