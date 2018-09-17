package com.huawei.msdp.devicestatus;

public class DeviceStatusConstant {
    public static final int EVENT_TYPE_ENTER = 1;
    public static final int EVENT_TYPE_EXIT = 2;
    public static final int EVENT_TYPE_FLUSH_COMPLETE = 0;
    public static final String LOCATION_STATUS_OF_IN_HANDS = "inHands";
    public static final String LOCATION_STATUS_OF_IN_PACKAGE = "inPackage";
    public static final String LOCATION_STATUS_OF_IN_POCKET = "inPocket";
    public static final String LOCATION_STATUS_OF_ON_BOARD = "onBoard";
    public static final long MIN_DELAY_TIME = 1000000000;
    public static final int MSDP_DEVICESTATUS_ID_INVALID = -1;
    public static final int MSDP_DEVICESTATUS_ID_MAX = 7;
    public static final String MSDP_DEVICESTATUS_TYPE_IN_HANDS = "android.activity_recognition.in_hands";
    public static final String MSDP_DEVICESTATUS_TYPE_IN_PACKAGE = "android.activity_recognition.in_package";
    public static final String MSDP_DEVICESTATUS_TYPE_IN_POCKET = "android.activit_recognition.in_pocket";
    public static final String MSDP_DEVICESTATUS_TYPE_ON_BOARD = "android.activity_recognition.on_board";
    public static final String MSDP_DEVICESTATUS_TYPE_UNKNOWN = "android.msdp.device_status.type.unknown";
    public static final String MSDP_DEVICETSTATUS_TYPE_MOVEMENT = "android.activity_recognition.movement";
    public static final String MSDP_DEVICETSTATUS_TYPE_STILL_OF_ABSOLUTE_LEVEL_ONE = "android.activity_recognition.still_of_absolute_level_one";
    public static final String MSDP_DEVICETSTATUS_TYPE_STILL_OF_RELATIVE_LEVEL_ONE = "android.activity_recognition.still_of_relative_level_one";
    public static final int MSDP_DEVICE_STATUS_APPINFO_SERVICE = 4;
    public static final int MSDP_DEVICE_STATUS_IN_HANDS = 1001;
    public static final int MSDP_DEVICE_STATUS_IN_PACKAGE = 1003;
    public static final int MSDP_DEVICE_STATUS_IN_POCKET = 1002;
    public static final int MSDP_DEVICE_STATUS_LOCATION_SERVICE = 1;
    public static final int MSDP_DEVICE_STATUS_MOVEMENT = 2003;
    public static final int MSDP_DEVICE_STATUS_MOVEMENT_SERVICE = 2;
    public static final int MSDP_DEVICE_STATUS_ON_BOARD = 1004;
    public static final int MSDP_DEVICE_STATUS_STILL_OF_ABSOLUTE = 2001;
    public static final int MSDP_DEVICE_STATUS_STILL_OF_RELATIVE = 2002;
    public static final int MSDP_DEVICE_STATUS_UNKNOWN = 0;
    public static final int MSDP_DEVICE_STATUS_VOICE_SERVICE = 3;
    public static final String STILL_STATUS_OF_ABSOLUTE = "android.activity_recognition.still";
    public static final String STILL_STATUS_OF_MOVEMENT = "movement";
    public static final String STILL_STATUS_OF_RELATIVE = "relative";
}
