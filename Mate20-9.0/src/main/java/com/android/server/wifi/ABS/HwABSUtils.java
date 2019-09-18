package com.android.server.wifi.ABS;

import android.os.SystemProperties;
import android.util.Log;

public class HwABSUtils {
    public static final int ABS_ASSOCIATE_CONTINUOUS_TIMES = 3;
    public static final int ABS_ASSOCIATE_TIMES_HIGH = 50;
    public static final int ABS_ASSOCIATE_TIMES_HIGH_RATE = 10;
    public static final int ABS_ASSOCIATE_TIMES_LOW = 10;
    public static final int ABS_ASSOCIATE_TIMES_LOW_RATE = 30;
    public static final int ABS_ASSOCIATE_TIMES_VOWIFI_HIGH_RATE = 5;
    public static final int ABS_ASSOCIATE_TIMES_VOWIFI_LOW_RATE = 15;
    public static final int ABS_ASSOCIATE_VOWIFI_CONTINUOUS_TIMES = 2;
    public static final String ABS_PROP = "ro.config.hw_abs_enable";
    public static final String ACTION_WIFI_ANTENNA_PREEMPTED = "com.huawei.action.ACTION_WIFI_ANTENNA_PREEMPTED";
    public static final int AUTO_HANDOVER_TIMER = 20000;
    public static final int CAPBILITY_MIMO = 2;
    public static final int CAPBILITY_SISO = 1;
    public static final int CMD_WIFI_PAUSE_HANDOVER = 103;
    public static final int CMD_WIFI_SWITCH_MIMO = 101;
    public static final int CMD_WIFI_SWITCH_SISO = 102;
    public static final String FLAG = "flag";
    public static final int HANDOVER_CAPBILITY_ATION_FRAME = 1;
    public static final int HANDOVER_CAPBILITY_RECONNECT = 2;
    public static final int HANDOVER_CAPBILITY_UNKONW = 0;
    public static final String HUAWEI_BUSSINESS_PERMISSION = "com.huawei.permission.HUAWEI_BUSSINESS_PERMISSION";
    public static final String MODEM0 = "modem0";
    public static final String MODEM1 = "modem1";
    public static final String MODEM2 = "modem2";
    public static final int MSG_BOOT_COMPLETED = 37;
    public static final int MSG_CALL_STATE_IDLE = 8;
    public static final int MSG_CALL_STATE_OFFHOOK = 10;
    public static final int MSG_CALL_STATE_RINGING = 9;
    public static final int MSG_DELAY_SWITCH = 23;
    public static final int MSG_MODEM_ENTER_LONG_CONNECT_STATE = 11;
    public static final int MSG_MODEM_ENTER_SEARCHING_STATE = 14;
    public static final int MSG_MODEM_ENTER_SHORT_CONNECT_STATE = 12;
    public static final int MSG_MODEM_EXIT_CONNECT_STATE = 13;
    public static final int MSG_MODEM_EXIT_SEARCH = 21;
    public static final int MSG_MODEM_EXIT_SEARCHING_STATE = 15;
    public static final int MSG_MODEM_STATE_IN_SERVICE = 25;
    public static final int MSG_MODEM_STATE_POWER_OFF = 22;
    public static final int MSG_MODEM_TRANSITION_TO_SEARCH = 20;
    public static final int MSG_MODEM_TUNERIC_ACTIVE_RESULT = 33;
    public static final int MSG_MODEM_TUNERIC_IACTIVE_RESULT = 35;
    public static final int MSG_OUTGOING_CALL = 7;
    public static final int MSG_RESEND_TUNERIC_ACTIVE_MSG = 34;
    public static final int MSG_RESEND_TUNERIC_IACTIVE_MSG = 36;
    public static final int MSG_SCREEN_OFF = 6;
    public static final int MSG_SCREEN_ON = 5;
    public static final int MSG_SEL_ENGINE_RESET_COMPLETED = 38;
    public static final int MSG_SUPPLICANT_COMPLETE = 24;
    public static final int MSG_WIFI_ANTENNA_PREEMPTED = 16;
    public static final int MSG_WIFI_CHECK_LINK = 17;
    public static final int MSG_WIFI_CHECK_LINK_FAILED = 19;
    public static final int MSG_WIFI_CHECK_LINK_SUCCESS = 18;
    public static final int MSG_WIFI_CONNECTED = 1;
    public static final int MSG_WIFI_DISABLE = 4;
    public static final int MSG_WIFI_DISCONNECTED = 2;
    public static final int MSG_WIFI_ENABLED = 3;
    public static final String RES = "res";
    public static final int SEARCHING_STATE_INVALID = 4;
    public static final int SEARCHING_STATE_IN_SERVICE = 0;
    public static final int SEARCHING_STATE_LIMITED_SERVICE = 1;
    public static final int SEARCHING_STATE_NO_SERVICE = 2;
    public static final int SEARCHING_STATE_TERMINATE = 3;
    public static final String SUB_ID = "subId";
    public static final int SWITCHING = 3;
    public static final String TAG = "HwABSUtils";
    public static final boolean isABSEnable = SystemProperties.getBoolean(ABS_PROP, false);

    public static void logD(String info) {
        Log.d(TAG, info);
    }

    public static void logW(String info) {
        Log.w(TAG, info);
    }

    public static void logE(String info) {
        Log.e(TAG, info);
    }

    public static boolean getABSEnable() {
        return isABSEnable;
    }
}
