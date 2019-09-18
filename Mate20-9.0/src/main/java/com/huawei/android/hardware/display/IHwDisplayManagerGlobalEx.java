package com.huawei.android.hardware.display;

public interface IHwDisplayManagerGlobalEx {
    void checkVerificationResult(boolean z);

    void connectWifiDisplay(String str, String str2);

    boolean sendWifiDisplayAction(String str);

    void startWifiDisplayScan(int i);
}
