package com.android.server.hidata.appqoe;

public interface IHwAppQoeCallback {
    void onAppQualityCallBack(HwAppStateInfo hwAppStateInfo, int i);

    void onAppRttInfoCallBack(HwAppStateInfo hwAppStateInfo);

    void onAppStateCallBack(HwAppStateInfo hwAppStateInfo, int i);
}
