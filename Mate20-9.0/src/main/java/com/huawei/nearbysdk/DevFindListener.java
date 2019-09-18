package com.huawei.nearbysdk;

public interface DevFindListener {
    void onDeviceFound(NearbyDevice nearbyDevice);

    void onDeviceLost(NearbyDevice nearbyDevice);
}
