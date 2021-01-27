package com.huawei.android.hardware.display;

import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.hardware.display.DisplayManagerGlobal;
import android.hardware.display.WifiDisplayStatus;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;
import com.huawei.annotation.HwSystemApi;

public class DisplayManagerEx {
    @HwSystemApi
    public static final String ACTION_WIFI_DISPLAY_STATUS_CHANGED = "android.hardware.display.action.WIFI_DISPLAY_STATUS_CHANGED";
    @HwSystemApi
    public static final String EXTRA_WIFI_DISPLAY_STATUS = "android.hardware.display.extra.WIFI_DISPLAY_STATUS";
    public static final String HW_WFD_ACTION_DLNA_ON_P2P_START = "HWE_DLNA_START";
    public static final String HW_WFD_ACTION_DLNA_ON_P2P_STOP = "HWE_DLNA_STOP";
    private static final String TAG = "DisplayManagerEx";

    private static DisplayManagerGlobal getDisplayManagerGlobal() {
        return DisplayManagerGlobal.getInstance();
    }

    public static int getActiveWifiDisplayState() {
        if (getDisplayManagerGlobal() == null) {
            Log.e(TAG, "DisplayManagerGlobal is null");
            return -1;
        }
        WifiDisplayStatus status = getDisplayManagerGlobal().getWifiDisplayStatus();
        if (status != null) {
            return status.getActiveDisplayState();
        }
        Log.e(TAG, "get wifiDisplayStatus is null");
        return -1;
    }

    public static void startWifiDisplayScan(int channelId) {
        if (getDisplayManagerGlobal() != null) {
            Log.d(TAG, "startWifiDisplayScan, pid:" + Process.myPid() + ", tid:" + Process.myTid() + ", uid:" + Process.myUid());
            getDisplayManagerGlobal().startWifiDisplayScan(channelId);
        }
    }

    public static void connectWifiDisplay(String deviceAddress, String verificaitonCode) {
    }

    public static void connectWifiDisplay(String deviceAddress, HwWifiDisplayParameters parameters) {
        if (getDisplayManagerGlobal() != null && parameters != null) {
            getDisplayManagerGlobal().connectWifiDisplay(deviceAddress, parameters);
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

    @HwSystemApi
    public static void disconnectWifiDisplay(DisplayManager displayManager) {
        if (displayManager != null) {
            displayManager.disconnectWifiDisplay();
        }
    }

    @HwSystemApi
    public static Point getStableDisplaySize(DisplayManager displayManager) {
        return displayManager.getStableDisplaySize();
    }
}
