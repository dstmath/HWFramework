package com.android.server.display;

public interface IHwDisplayManagerServiceEx {
    void checkVerificationResult(boolean z);

    void connectWifiDisplay(String str, String str2);

    boolean sendWifiDisplayAction(String str);

    void startWifiDisplayScan(int i);
}
