package com.huawei.hwwifiproservice;

import android.os.Bundle;

public interface INetworkQosCallBack {
    void onNetworkDetectionResult(int i, int i2);

    void onNetworkQosChange(int i, int i2, boolean z);

    void onNotifyWifiSecurityStatus(Bundle bundle);

    void onWifiBqeDetectionResult(int i);

    void onWifiBqeReturnCurrentRssi(int i);

    void onWifiBqeReturnHistoryScore(WifiProEstimateApInfo wifiProEstimateApInfo);

    void onWifiBqeReturnRssiTH(WifiProEstimateApInfo wifiProEstimateApInfo);
}
