package com.huawei.hwwifiproservice;

import com.huawei.hwwifiproservice.HwNetworkPropertyChecker;

public interface IHwWifiProPart {
    int getAutoOpenCnt();

    String getCurrentPackageName();

    void notifyHttpReachableForWifiPro(boolean z);

    void notifyHttpRedirectedForWifiPro();

    void notifyRenewDhcpTimeoutForWifiPro();

    void notifyRoamingCompletedForWifiPro(String str);

    void setAutoOpenCnt(int i);

    String syncQueryDhcpResultsByBssid(String str);

    void updateDhcpResultsByBssid(String str, String str2);

    void updateStandardPortalTable(HwNetworkPropertyChecker.StarndardPortalInfo starndardPortalInfo);
}
