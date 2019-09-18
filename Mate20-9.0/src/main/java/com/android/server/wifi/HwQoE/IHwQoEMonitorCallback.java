package com.android.server.wifi.HwQoE;

public interface IHwQoEMonitorCallback {
    void onNetworkInfoUpdate(HwQoENetWorkInfo hwQoENetWorkInfo);

    void onUDPInternetAccessStatusChange(boolean z);
}
