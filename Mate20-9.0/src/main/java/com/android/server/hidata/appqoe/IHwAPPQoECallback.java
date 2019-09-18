package com.android.server.hidata.appqoe;

public interface IHwAPPQoECallback {
    void onAPPQualityCallBack(HwAPPStateInfo hwAPPStateInfo, int i);

    void onAPPRttInfoCallBack(HwAPPStateInfo hwAPPStateInfo);

    void onAPPStateCallBack(HwAPPStateInfo hwAPPStateInfo, int i);

    void onNetworkQualityCallBack(int i, int i2, int i3, boolean z);

    void onWifiLinkQuality(int i, int i2, boolean z);
}
