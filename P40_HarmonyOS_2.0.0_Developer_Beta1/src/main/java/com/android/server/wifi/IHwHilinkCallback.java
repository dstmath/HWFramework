package com.android.server.wifi;

public interface IHwHilinkCallback {
    void onConnectFailedResult(int i, int i2);

    void onConnectSuccessResult(int i);

    void onConnectionStateChanged(int i);

    void onProxyLostStateChanged();

    void onProxyReadyStateChanged(int i);
}
