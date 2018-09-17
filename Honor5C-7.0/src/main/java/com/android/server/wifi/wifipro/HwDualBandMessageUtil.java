package com.android.server.wifi.wifipro;

public class HwDualBandMessageUtil {
    public static final String ACTION_NETWORK_CONDITIONS_MEASURED = "huawei.conn.NETWORK_CONDITIONS_MEASURED";
    public static final int AP_INTERNET_READY = 1;
    public static final int AP_NO_INTERNET_READY = 2;
    public static final int AP_SERVING_BAND_24G = 1;
    public static final int AP_SERVING_BAND_5G = 2;
    public static final int AP_TYPE_MIX = 2;
    public static final int AP_TYPE_SINGLE = 1;
    public static final int AP_TYPE_UNKNOW = 0;
    public static final int CHANGE_REASON_ADDED = 0;
    public static final int CHANGE_REASON_CONFIG_CHANGE = 2;
    public static final int CHANGE_REASON_REMOVED = 1;
    public static final int CMD_ON_START = 100;
    public static final int CMD_ON_STOP = 101;
    public static final int CMD_START_MONITOR = 102;
    public static final int CMD_STOP_MONITOR = 103;
    public static final int CMD_UPDATE_AP_INFO = 104;
    public static final String CONFIGURED_NETWORKS_CHANGED_ACTION = "android.net.wifi.CONFIGURED_NETWORKS_CHANGE";
    public static final String EXTRA_CHANGE_REASON = "changeReason";
    public static final String EXTRA_IS_CAPTIVE_PORTAL = "extra_is_captive_portal";
    public static final String EXTRA_IS_INTERNET_READY = "extra_is_internet_ready";
    public static final String EXTRA_WIFI_CONFIGURATION = "wifiConfiguration";
    public static final int INTERNET_CHECK_RESULT_NO_INTERNET = -1;
    public static final int INTERNET_CHECK_RESULT_OK = 5;
    public static final int INTERNET_CHECK_RESULT_PROTAL = 6;
    public static final int MSG_DUAL_BAND_WIFI_TYPE_MIX = 17;
    public static final int MSG_DUAL_BAND_WIFI_TYPE_SINGLE = 16;
    public static final int MSG_HANDLE_STATE_CHANGE = 10;
    public static final String MSG_KEY_APLIST = "aplist";
    public static final String MSG_KEY_AUTHTYPE = "authtype";
    public static final String MSG_KEY_BSSID = "bssid";
    public static final String MSG_KEY_REASON = "reason";
    public static final String MSG_KEY_RSSI = "rssi";
    public static final String MSG_KEY_SSID = "ssid";
    public static final int MSG_SCREEN_OFF = 15;
    public static final int MSG_SCREEN_ON = 14;
    public static final int MSG_WIFI_CONFIG_CHANGED = 8;
    public static final int MSG_WIFI_CONNECTED = 1;
    public static final int MSG_WIFI_DISABLE = 4;
    public static final int MSG_WIFI_DISABLEING = 6;
    public static final int MSG_WIFI_DISCONNECTED = 2;
    public static final int MSG_WIFI_ENABLED = 3;
    public static final int MSG_WIFI_FIND_TARGET = 5;
    public static final int MSG_WIFI_HANDLE_DISABLE = 9;
    public static final int MSG_WIFI_INTERNET_CONNECTED = 11;
    public static final int MSG_WIFI_INTERNET_DISCONNECTED = 12;
    public static final int MSG_WIFI_IS_PORTAL = 13;
    public static final int MSG_WIFI_RSSI_CHANGE = 18;
    public static final int MSG_WIFI_UPDATE_SCAN_RESULT = 7;
    public static final int MSG_WIFI_VERIFYING_POOR_LINK = 19;
    public static final String TAG = "HwDualBandManager";
}