package com.huawei.nearbysdk.clone;

public interface WifiStatusListener {
    public static final int WIFI_CONNECTION_CONNECTED = 0;
    public static final int WIFI_CONNECTION_DISCONNECTED = 3;
    public static final int WIFI_CONNECTION_FAILURE = 1;
    public static final int WIFI_CONNECTION_TIMEOUT = 2;
    public static final int WIFI_STATE_CLOSURE = 3;
    public static final int WIFI_STATE_ERROR = -1;
    public static final int WIFI_STATE_FAILURE = 1;
    public static final int WIFI_STATE_SUCCESS = 0;
    public static final int WIFI_STATE_TIMEOUT = 2;

    void onConnectionChange(int i, int i2, String str, int i3);

    void onStateChange(int i);
}
