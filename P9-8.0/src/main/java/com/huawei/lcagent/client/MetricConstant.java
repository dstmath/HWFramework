package com.huawei.lcagent.client;

public class MetricConstant {
    public static final String ACTION_POLICY_CONFIGURE_INTENT = "com.huawei.lcagent.POLICY_CONFIGURE";
    public static final String ACTION_POLICY_CONF_RESULT_INTENT = "com.huawei.lcagent.POLICY_CONF_RESULT";
    public static final String ACTION_RESUME_UPLOAD_INTENT = "com.huawei.lcagent.RESUME_UPLOAD";
    public static final String ACTION_SUBMIT_METRIC_INTENT = "com.huawei.lcagent.client.ACTION_SUBMIT_METRIC_INTENT";
    public static final String ACTION_UPLOAD_REQUEST_INTENT = "com.huawei.lcagent.UPLOAD_REQUEST";
    public static final String ACTION_UPLOAD_RESULT_INTENT = "com.huawei.lcagent.UPLOAD_RESULT";
    public static final int APP_METRIC_ID = 3;
    public static final int APP_METRIC_ID_EX = 103;
    public static final int APR_STATISTICS_METRIC_ID = 9;
    public static final int AUDIO_METRIC_ID = 13;
    public static final int AUTO_MODE = 1;
    public static final int AUTO_OFF = 2;
    public static final int AUTO_ON = 3;
    public static final int BATTERY_METRIC_ID = 8;
    public static final int BLUETOOTH_METRIC_ID = 18;
    public static final int BLUETOOTH_METRIC_ID_EX = 107;
    public static final int CALL_METRIC_ID = 7;
    public static final int CAMERA_METRIC_ID_EX = 104;
    public static final int COMMUNICATION_METRIC_ID_EX = 102;
    public static final int DMD_METRIC_ID = 16;
    public static final int EX_METRIC_ID_MAX = 110;
    public static final int EX_METRIC_ID_MIN = 100;
    public static final int GPS_METRIC_ID = 14;
    public static final int GPS_METRIC_ID_EX = 108;
    public static final int INTERNET_METRIC_ID = 5;
    public static final int JANK_METRIC_ID = 11;
    public static final int LEVEL_A = 1;
    public static final int LEVEL_B = 16;
    public static final int LEVEL_C = 256;
    public static final int LEVEL_D = 4096;
    public static final int LOG_TRACK_METRIC_ID = 12;
    public static final int MANUAL_MODE = 0;
    public static final int MANUAL_OFF = 0;
    public static final int MANUAL_ON = 1;
    public static final int METRIC_ID_MAX = 256;
    public static final int METRIC_ID_MIN = 0;
    public static final int METRIC_ID_TEMPERATURE = 10;
    public static final int OTHER_METRIC_ID_EX = 100;
    public static final int POWER_METRIC_ID_EX = 105;
    public static final int RADIO_METRIC_ID = 1;
    public static final int REBOOT_METRIC_ID = 2;
    public static final int REBOOT_METRIC_ID_EX = 101;
    public static final int SCREEN_METRIC_ID_EX = 109;
    public static final int SDCARD_METRIC_ID_EX = 110;
    public static final int SIM_METRIC_ID = 6;
    public static final int SUBSYSTEM_METRIC_ID = 17;
    public static final int SWITCH_OFF = 0;
    public static final int SWITCH_ON = 1;
    public static final int TOUCH_METRIC_ID = 4;
    public static final int WIFI_METRIC_ID = 15;
    public static final int WIFI_METRIC_ID_EX = 106;

    public static boolean isValidMetricId(int id) {
        if (id >= 256 || id <= 0) {
            return false;
        }
        return true;
    }

    public static String getStringID(int metricId) {
        switch (metricId) {
            case 1:
                return "LOG_CHR";
            case 2:
                return "Reboot";
            case 3:
                return "App";
            case 4:
                return "Touch";
            case 5:
                return "Internet";
            case 6:
                return "Sim";
            case 7:
                return "Call";
            case 8:
                return "Battery";
            case 14:
                return "GPS_CHR";
            case 15:
                return "WIFI_CHR";
            case 18:
                return "BT_CHR";
            default:
                return String.valueOf(metricId);
        }
    }
}
