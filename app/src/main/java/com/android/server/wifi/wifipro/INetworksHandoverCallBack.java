package com.android.server.wifi.wifipro;

public interface INetworksHandoverCallBack {
    void onCheckAvailableWifi(boolean z, int i, String str);

    void onWifiConnected(boolean z, int i);

    void onWifiHandoverChange(int i, boolean z, String str, int i2);
}
