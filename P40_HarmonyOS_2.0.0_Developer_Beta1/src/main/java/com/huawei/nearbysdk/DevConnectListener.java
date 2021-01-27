package com.huawei.nearbysdk;

public interface DevConnectListener {
    void onConnectFail(int i);

    void onConnectSuccess(String str);

    void onDevDisconnected(NearbyDevice nearbyDevice);
}
