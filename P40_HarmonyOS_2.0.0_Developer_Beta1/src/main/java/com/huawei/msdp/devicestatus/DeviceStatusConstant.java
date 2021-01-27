package com.huawei.msdp.devicestatus;

public class DeviceStatusConstant {
    private static final String[] DEFAULT_SUPPORTS = {"android.msdp.device_status.high_still", "android.msdp.device_status.fine_still", "android.msdp.device_status.coarse_still", MSDP_DEVICETSTATUS_TYPE_CELL_CHANGED, MSDP_DEVICETSTATUS_TYPE_CAR_BLUETOOTH, TYPE_SCREEN_UP_LEVEL_DETECTION, TYPE_SCREEN_DOWN_LEVEL_DETECTION, TYPE_HEAD_UP_UPRIGHT_DETECTION, TYPE_HEAD_DOWN_UPRIGHT_DETECTION, TYPE_VERTICAL_LANDSCAPE_LEFT_DETECTION, TYPE_VERTICAL_LANDSCAPE_RIGHT_DETECTION};
    public static final int EVENT_TYPE_ENTER = 1;
    public static final int EVENT_TYPE_EXIT = 2;
    public static final String MSDP_DEVICESTATUS_TYPE_STILL_STATUS = "msdp.devicestatus_type_still_status";
    public static final String MSDP_DEVICESTATUS_TYPE_UNKNOWN = "android.msdp.device_status.type.unknown";
    public static final String MSDP_DEVICETSTATUS_TYPE_CAR_BLUETOOTH = "com.huawei.msdp.device_status.car_bluetooth";
    public static final String MSDP_DEVICETSTATUS_TYPE_CELL_CHANGED = "android.msdp.device_status.cell_changed";
    public static final String MSDP_DEVICETSTATUS_TYPE_COARSE_STILL = "android.msdp.device_status.coarse_still";
    public static final String MSDP_DEVICETSTATUS_TYPE_FINE_STILL = "android.msdp.device_status.fine_still";
    public static final String MSDP_DEVICETSTATUS_TYPE_HIGH_STILL = "android.msdp.device_status.high_still";
    public static final String MSDP_DEVICETSTATUS_TYPE_MOVEMENT_OF_FAST_WALKING = "android.msdp.movement_of_fast_walking";
    public static final String MSDP_DEVICETSTATUS_TYPE_MOVEMENT_OF_OTHER = "android.msdp.movement_of_other";
    public static final String MSDP_DEVICETSTATUS_TYPE_MOVEMENT_OF_WALKING = "android.msdp.movement_of_walking";
    public static final String MSDP_DEVICETSTATUS_TYPE_STILL_OF_ABSOLUTE = "android.msdp.still_of_absolute";
    public static final String MSDP_DEVICETSTATUS_TYPE_STILL_OF_ABSOLUTE_LEVEL_ONE = "android.activity_recognition.still_of_absolute_level_one";
    public static final String MSDP_DEVICETSTATUS_TYPE_STILL_OF_RELATIVE_CELL = "android.msdp.still_of_relative_cell";
    public static final String MSDP_DEVICETSTATUS_TYPE_STILL_OF_RELATIVE_LEVEL_ONE = "android.activity_recognition.still_of_relative_level_one";
    public static final String MSDP_DEVICETSTATUS_TYPE_STILL_OF_RELATIVE_LEVEL_TWO = "android.activity_recognition.still_of_relative_level_two";
    public static final String MSDP_DEVICETSTATUS_TYPE_STILL_OF_RELATIVE_WIFI = "android.msdp.still_of_relative_wifi";
    public static final String MSDP_SERVICE_TYPE_STILL_OF_RELATIVE_STEP_NUMBER = "android.msdp.still_of_relative_step_number";
    public static final String STILL_STATUS_OF_ABSOLUTE = "android.activity_recognition.still";
    public static final String STILL_STATUS_OF_MOVEMENT = "movement";
    private static final String[] SUPPORTS = {"android.msdp.device_status.high_still", "android.msdp.device_status.fine_still", "android.msdp.device_status.coarse_still", MSDP_DEVICETSTATUS_TYPE_CELL_CHANGED, MSDP_DEVICETSTATUS_TYPE_CAR_BLUETOOTH, TYPE_SCREEN_UP_LEVEL_DETECTION, TYPE_SCREEN_DOWN_LEVEL_DETECTION, TYPE_HEAD_UP_UPRIGHT_DETECTION, TYPE_HEAD_DOWN_UPRIGHT_DETECTION, TYPE_VERTICAL_LANDSCAPE_LEFT_DETECTION, TYPE_VERTICAL_LANDSCAPE_RIGHT_DETECTION};
    public static final String TYPE_HEAD_DOWN_UPRIGHT_DETECTION = "head_down_upright_detection";
    public static final String TYPE_HEAD_UP_UPRIGHT_DETECTION = "head_up_upright_detection";
    public static final String TYPE_SCREEN_DOWN_LEVEL_DETECTION = "screen_down_level_detection";
    public static final String TYPE_SCREEN_UP_LEVEL_DETECTION = "screen_up_level_detection";
    public static final String TYPE_VERTICAL_LANDSCAPE_LEFT_DETECTION = "vertical_landscape_left_detection";
    public static final String TYPE_VERTICAL_LANDSCAPE_RIGHT_DETECTION = "vertical_landscape_right_detection";

    public static String[] getSUPPORTS() {
        return (String[]) SUPPORTS.clone();
    }

    public static String[] getDefaultSupports() {
        return DEFAULT_SUPPORTS;
    }
}
