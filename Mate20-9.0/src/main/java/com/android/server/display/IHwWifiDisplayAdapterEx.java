package com.android.server.display;

public interface IHwWifiDisplayAdapterEx {
    void LaunchMKForWifiMode();

    void checkVerificationResultLocked(boolean z);

    void requestConnectLocked(String str, String str2);

    void requestStartScanLocked(int i);

    void sendWifiDisplayActionLocked(String str);

    void setConnectParameters(String str);
}
