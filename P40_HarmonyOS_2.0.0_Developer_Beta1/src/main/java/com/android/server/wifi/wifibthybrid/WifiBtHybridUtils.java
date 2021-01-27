package com.android.server.wifi.wifibthybrid;

public class WifiBtHybridUtils {
    public static final int BOOL_FALSE_TO_INT = 0;
    public static final int BOOL_TRUE_TO_INT = 1;
    public static final int MSG_6SLOT_START = 5;
    public static final int MSG_6SLOT_STOP = 6;
    public static final int MSG_A2DP_START = 3;
    public static final int MSG_A2DP_STOP = 4;
    public static final int MSG_BLUETOOTH_CONNECTED = 8;
    public static final int MSG_BLUETOOTH_DISCONNECTED = 7;
    public static final int MSG_GAME_SCENE = 2;
    public static final int MSG_INITIALIZE = 1;
    public static final int MSG_START_RSSI_CHECK = 11;
    public static final int MSG_WIFI_CONNECTED = 9;
    public static final int MSG_WIFI_DISCONNECTED = 10;

    private WifiBtHybridUtils() {
    }

    public static int booleanToInt(boolean value) {
        return value ? 1 : 0;
    }
}
