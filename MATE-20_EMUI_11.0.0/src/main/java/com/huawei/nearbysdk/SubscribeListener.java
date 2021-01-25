package com.huawei.nearbysdk;

public interface SubscribeListener {
    public static final int ERROR_BLUETOOTH_OFF = 1001;
    public static final int ERROR_SCREEN_OFF = 1002;

    void onStatusChanged(int i);
}
