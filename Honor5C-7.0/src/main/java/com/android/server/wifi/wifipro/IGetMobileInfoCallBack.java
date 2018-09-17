package com.android.server.wifi.wifipro;

public interface IGetMobileInfoCallBack {
    int getTotalRoMobileData();

    int onGetMobileRATType();

    int onGetMobileSignalLevel();
}
