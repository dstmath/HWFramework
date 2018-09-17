package com.huawei.chrfile.client;

public class NcMetricConstant {
    public static final int BLUETOOTH_METRIC_ID = 18;
    public static final int GPS_METRIC_ID = 14;
    public static final int LEVEL_A = 1;
    public static final int LEVEL_B = 16;
    public static final int LEVEL_C = 256;
    public static final int LEVEL_D = 4096;
    public static final int METRIC_ID_MAX = 256;
    public static final int METRIC_ID_MIN = 0;
    public static final int NFC_METRIC_ID = 19;
    public static final int RADIO_METRIC_ID = 1;
    public static final int WIFI_METRIC_ID = 15;

    public static boolean isValidMetricId(int id) {
        if (id >= METRIC_ID_MAX || id <= 0) {
            return false;
        }
        return true;
    }

    public static String getStringID(int metricId) {
        switch (metricId) {
            case GPS_METRIC_ID /*14*/:
                return "GPS_CHR";
            case WIFI_METRIC_ID /*15*/:
                return "WIFI_CHR";
            case BLUETOOTH_METRIC_ID /*18*/:
                return "BT_CHR";
            case NFC_METRIC_ID /*19*/:
                return "NFC_CHR";
            default:
                return String.valueOf(metricId);
        }
    }
}
