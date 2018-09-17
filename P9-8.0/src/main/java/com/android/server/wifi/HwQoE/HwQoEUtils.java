package com.android.server.wifi.HwQoE;

import android.os.SystemProperties;
import android.util.Log;

public class HwQoEUtils {
    public static final int APP_NO_BGLIMIT = 1000;
    public static final int APP_WIFI_NO_SLEEP = 0;
    public static final int APP_WIFI_SLEEP_UNKNOWN = -1;
    public static final boolean GAME_ASSISIT_ENABLE = "1".equalsIgnoreCase(SystemProperties.get("ro.config.gameassist", ""));
    public static final boolean MAINLAND_REGION = "CN".equalsIgnoreCase(SystemProperties.get("ro.product.locale.region", ""));
    public static final int QOE_MSG_EVALUATE_OTA_INFO = 106;
    public static final int QOE_MSG_MONITOR_GET_QUALITY_INFO = 105;
    public static final int QOE_MSG_MONITOR_HAVE_INTERNET = 103;
    public static final int QOE_MSG_MONITOR_NO_INTERNET = 104;
    public static final int QOE_MSG_MONITOR_UPDATE_TCP_INFO = 101;
    public static final int QOE_MSG_MONITOR_UPDATE_UDP_INFO = 102;
    public static final int QOE_MSG_WIFI_CHECK_TIMEOUT = 112;
    public static final int QOE_MSG_WIFI_CONNECTED = 109;
    public static final int QOE_MSG_WIFI_DELAY_DISCONNECT = 116;
    public static final int QOE_MSG_WIFI_DISABLE = 108;
    public static final int QOE_MSG_WIFI_DISCONNECT = 110;
    public static final int QOE_MSG_WIFI_ENABLED = 107;
    public static final int QOE_MSG_WIFI_EVALUATE_TIMEOUT = 114;
    public static final int QOE_MSG_WIFI_INTERNET = 111;
    public static final int QOE_MSG_WIFI_ROAMING = 115;
    public static final int QOE_MSG_WIFI_RSSI_CHANGED = 117;
    public static final int QOE_MSG_WIFI_START_EVALUATE = 113;
    public static final int SENSITIVE_APP_SCORE = 2;
    public static final int SENSITIVE_UPD_PACKETS = 10;
    public static final String SEPARATOR = ":";
    public static final String TAG = "HwQoEUtils";
    public static final int VOWIFI_STATE_CALL_BEGIN = 1;
    public static final int VOWIFI_STATE_CALL_END = 2;
    public static final int VOWIFI_STATE_CALL_IDLE = 0;
    public static final int WECHAT_AUDIO_ON = 2;
    public static final int WECHAT_VIDEO_OFF = 0;
    public static final int WECHAT_VIDEO_ON = 1;
    public static final int WHITE_LIST_TYPE_WIFI_SLEEP = 7;
    public static final int WIFI_CHECK_TIMEOUT = 4000;

    public static void logD(String info) {
        Log.d(TAG, info);
    }

    public static void logW(String info) {
        Log.w(TAG, info);
    }

    public static void logE(String info) {
        Log.e(TAG, info);
    }
}
