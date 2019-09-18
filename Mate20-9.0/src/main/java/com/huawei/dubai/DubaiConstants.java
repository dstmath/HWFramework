package com.huawei.dubai;

public final class DubaiConstants {
    public static final int FLAG_APPLICATIONS = 2;
    public static final int FLAG_COMPONENTS = 1;
    public static final int FLAG_DEFAULT = 0;
    public static final int FLAG_WITH_CHARGE = 4;
    public static final int MASK_MODULE_ALL = 32767;
    public static final int MODULE_AUDIO = 1;
    public static final int MODULE_BATTERY = 0;
    public static final int MODULE_BLUETOOTH = 2;
    public static final int MODULE_CAMERA = 3;
    public static final int MODULE_CPU = 4;
    public static final int MODULE_DISPLAY = 5;
    public static final int MODULE_DPM = 6;
    public static final int MODULE_FLASHLIGHT = 7;
    public static final int MODULE_GNSS = 8;
    public static final int MODULE_GPU = 9;
    public static final int MODULE_INVALID = -1;
    public static final int MODULE_MODEM = 10;
    public static final int MODULE_SENSOR = 11;
    public static final int MODULE_SYSTEM_IDLE = 14;
    public static final int MODULE_SYSTEM_SUSPEND = 13;
    public static final int MODULE_WIFI = 12;
    public static final int QUERY_NO_DATA = 0;
    public static final int QUERY_RESPONSE_ERROR = -1;

    private DubaiConstants() {
        throw new RuntimeException("no instance permitted");
    }
}
