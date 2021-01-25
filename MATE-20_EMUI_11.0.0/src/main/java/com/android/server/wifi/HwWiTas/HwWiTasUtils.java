package com.android.server.wifi.HwWiTas;

import android.os.SystemProperties;

public class HwWiTasUtils {
    private static final boolean IS_WITAS_ENABLE = SystemProperties.getBoolean("ro.config.hw_witas_enable", false);
    public static final String TAG = "HwWiTasUtils";
    protected static final int WITAS_ANT0_INDEX = 0;
    protected static final int WITAS_ANT1_INDEX = 1;
    protected static final int WITAS_ANT_ADJUST_PWOER_ERROR = 6;
    protected static final int WITAS_ANT_CHECK_ERROR = 5;
    protected static final int WITAS_ANT_FORWARD_SAMPLING_COUNT = 12;
    protected static final int WITAS_ANT_MEASURE_ERROR = 1;
    protected static final int WITAS_ANT_SWITCH_ERROR = 4;
    protected static final int WITAS_CHECK_RTT_VALUE_DELAY_TIME = 300000;
    protected static final int WITAS_CHECK_STABILITY_INTERVAL_TIME = 1000;
    protected static final int WITAS_CMD_ADJUST_TXPOWER = 103;
    protected static final int WITAS_CMD_GET_CURRENT_ANTENNA = 105;
    protected static final int WITAS_CMD_MEASURE_RSSI = 102;
    protected static final int WITAS_CMD_SWITCH_ANTENNA = 104;
    protected static final int WITAS_CORE0_DEFAULT_ANT_MODE = 0;
    protected static final int WITAS_CORE0_INDEX = 0;
    protected static final int WITAS_CORE0_WITAS_ANT_MODE = 1;
    protected static final int WITAS_CORE1_DEFAULT_ANT_MODE = 2;
    protected static final int WITAS_CORE1_INDEX = 1;
    protected static final int WITAS_CORE1_WITAS_ANT_MODE = 3;
    protected static final int WITAS_DST_ANT_MEASURE_TIMEOUT = 3;
    protected static final int WITAS_FORWARD_SAMPLING_ONY_DAY = 86400000;
    protected static final int WITAS_GAMELIST_SCENE = 0;
    protected static final int WITAS_GAME_DELAY_THRESHOLD = 200;
    protected static final int WITAS_GAME_START_DELAY_TIME = 5000;
    protected static final int WITAS_HIGH_RSSI_THRESHOLD = 4;
    protected static final int WITAS_LANDSCAPE_SCENE = 1;
    protected static final int WITAS_LONG_FREEZE_TIME = 30000;
    protected static final int WITAS_LOW_RSSI_THRESHOLD = 1;
    protected static final int WITAS_MEASURE_DST_ANTENNA = 2;
    protected static final int WITAS_MEASURE_IDEL_STATE = 0;
    protected static final int WITAS_MEASURE_SRC_ANTENNA = 1;
    protected static final int WITAS_MEASURE_TIMEOUT_THRESHOLD = 3000;
    protected static final int WITAS_MIMO_MODE_RSSI_VALUE = -130;
    private static final int WITAS_MODE = SystemProperties.getInt("ro.config.hw_witas_mode", 0);
    protected static final int WITAS_MSG_ANT_SWITCH_ARBITRA = 16;
    protected static final int WITAS_MSG_CHECK_RTT_VALUE = 26;
    protected static final int WITAS_MSG_CHECK_STABILITY = 23;
    protected static final int WITAS_MSG_CONFIGURATION_CHANGED = 27;
    protected static final int WITAS_MSG_DST_ANT_MEASURE = 13;
    protected static final int WITAS_MSG_DST_ANT_MEASURE_TIMEOUT = 14;
    protected static final int WITAS_MSG_DST_ANT_REPORT_RSSI = 15;
    protected static final int WITAS_MSG_FREEZE_STATE_ENTER = 17;
    protected static final int WITAS_MSG_FREEZE_STATE_EXIT = 18;
    protected static final int WITAS_MSG_GAME_LAG = 9;
    protected static final int WITAS_MSG_GAME_START = 7;
    protected static final int WITAS_MSG_GAME_STOP = 8;
    protected static final int WITAS_MSG_PHONE_CALL_CONNECTED = 5;
    protected static final int WITAS_MSG_PHONE_CALL_DISCONNECTED = 6;
    protected static final int WITAS_MSG_RSSI_MONITOR = 19;
    protected static final int WITAS_MSG_RTT_COLLECT = 24;
    protected static final int WITAS_MSG_SRC_ANT_MEASURE = 10;
    protected static final int WITAS_MSG_SRC_ANT_MEASURE_TIMEOUT = 11;
    protected static final int WITAS_MSG_SRC_ANT_REPORT_RSSI = 12;
    protected static final int WITAS_MSG_STREADY_STATE = 22;
    protected static final int WITAS_MSG_SWITCH_TO_MIMO_MODE = 21;
    protected static final int WITAS_MSG_SWITCH_TO_SISO_MODE = 20;
    protected static final int WITAS_MSG_TAKE_POSITIVE_SAMPLE = 25;
    protected static final int WITAS_MSG_WIFI_CONNECTED = 1;
    protected static final int WITAS_MSG_WIFI_DISABLE = 4;
    protected static final int WITAS_MSG_WIFI_DISCONNECTED = 2;
    protected static final int WITAS_MSG_WIFI_ENABLED = 3;
    protected static final int WITAS_RSSI_MAX_FLUCTUATION = 6;
    protected static final int WITAS_RSSI_MONITOR_INTERVAL_TIME = 3000;
    protected static final int WITAS_RSSI_THRESHOLD = -65;
    protected static final int WITAS_RTT_COLLECT_DELAY_TIME = 10000;
    private static final int WITAS_SCENE = SystemProperties.getInt("hw_mc.wifi.witas_scene", 0);
    protected static final int WITAS_SET_COMMAND_ERROR = -1;
    protected static final int WITAS_SET_COMMAND_SUCCESS = 0;
    protected static final int WITAS_SHORT_FREEZE_TIME = 15000;
    protected static final int WITAS_SISO_MODE_RSSI_VALUE = -128;
    protected static final int WITAS_SRC_ANT_MEASURE_TIMEOUT = 2;
    protected static final byte WITAS_STATE_CHOOSE_DST_ANT = 4;
    protected static final byte WITAS_STATE_CHOOSE_SRC_ANT = 3;
    protected static final byte WITAS_STATE_DST_MEASURE = 2;
    protected static final byte WITAS_STATE_IDEL = 0;
    protected static final byte WITAS_STATE_SISO_MODE = 6;
    protected static final byte WITAS_STATE_SRC_MEASURE = 1;
    protected static final byte WITAS_STATE_STEADY = 5;
    protected static final int WITAS_STREADY_STATE_DELAY_TIME = 4000;
    protected static final int WITAS_TAKE_POSITIVE_SAMPLE_DELAY_TIME = 30000;
    protected static final int WITAS_TAKE_POSITIVE_SAMPLE_EVENT = 7;
    protected static int mCoreAntIndex = 0;
    protected static int mDefaultAntIndex = 0;
    protected static int mWitasAntIndex = 1;
    protected static int sTasScene = 0;

    public static boolean getWiTasEnable() {
        return IS_WITAS_ENABLE;
    }

    public static int getWitasMode() {
        return WITAS_MODE;
    }

    public static int getWitasScene() {
        return WITAS_SCENE;
    }
}
