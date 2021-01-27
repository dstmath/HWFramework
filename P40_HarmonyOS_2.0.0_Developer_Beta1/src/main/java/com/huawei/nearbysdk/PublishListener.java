package com.huawei.nearbysdk;

public interface PublishListener {
    public static final int ERROR_BLUETOOTH_OFF = 1001;
    public static final int ERROR_SCREEN_OFF = 1002;

    void onDeviceFound(NearbyDevice nearbyDevice);

    void onDeviceLost(NearbyDevice nearbyDevice);

    void onLocalDeviceChange(int i);

    void onStatusChanged(int i);
}
