package com.android.server.display;

import android.hardware.display.WifiDisplay;
import com.huawei.android.hardware.display.HwWifiDisplayParameters;

public interface IHwWifiDisplayAdapterEx {
    void checkVerificationResultLocked(boolean z);

    void displayCasting(WifiDisplay wifiDisplay);

    int getConnectionFailReason(boolean z);

    void handleSendCastingBroadcast();

    void handleSendDisplayDataBroadcast(String str);

    void requestConnectLocked(String str, HwWifiDisplayParameters hwWifiDisplayParameters);

    void requestStartScanLocked(int i);

    void sendWifiDisplayActionLocked(String str);

    void setConnectParameters(String str);

    void setConnectionFailedReason(int i);

    void setHwWifiDisplayParameters(HwWifiDisplayParameters hwWifiDisplayParameters);

    void updateDensityForPcMode(DisplayDeviceInfo displayDeviceInfo);
}
