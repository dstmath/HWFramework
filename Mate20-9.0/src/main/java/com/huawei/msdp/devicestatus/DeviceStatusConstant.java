package com.huawei.msdp.devicestatus;

public class DeviceStatusConstant {
    public static final String[] AR_SERVICE_ABILITY_NAME = {"android.activity_recognition.still", "android.activity_recognition.high_speed_rail", "android.activity_recognition.in_vehicle", "android.activity_recognition.on_bicycle", "android.activity_recognition.running", "android.activity_recognition.walking", "android.activity_recognition.fast_walking", "android.activity_recognition.relative_still"};
    public static final String AR_SERVICE_TYPE_MOVEMENT_OF_FAST_WALKING = "android.activity_recognition.fast_walking";
    public static final String AR_SERVICE_TYPE_MOVEMENT_OF_WALKING = "android.activity_recognition.walking";
    public static final String AR_SERVICE_TYPE_STILL_OF_ABSOLUTE = "android.activity_recognition.still";
    public static final String AR_SERVICE_TYPE_STILL_OF_HIGH_SPEED = "android.activity_recognition.high_speed_rail";
    public static final String AR_SERVICE_TYPE_STILL_OF_IN_VEHICLE = "android.activity_recognition.in_vehicle";
    public static final String AR_SERVICE_TYPE_STILL_OF_ON_BICYCLE = "android.activity_recognition.on_bicycle";
    public static final String AR_SERVICE_TYPE_STILL_OF_RELATIVE_STEP_NUMBER = "android.activity_recognition.relative_still";
    public static final String AR_SERVICE_TYPE_STILL_OF_RUNNING = "android.activity_recognition.running";
    public static final String AR_SERVICE_TYPE_STILL_OF_UNKNOWN = "android.activity_recognition.unknown";
    public static final int EVENT_TYPE_ENTER = 1;
    public static final int EVENT_TYPE_EXIT = 2;
    public static final int EVENT_TYPE_FLUSH_COMPLETE = 0;
    public static final String LOCATION_STATUS_OF_IN_HANDS = "inHands";
    public static final String LOCATION_STATUS_OF_IN_PACKAGE = "inPackage";
    public static final String LOCATION_STATUS_OF_IN_POCKET = "inPocket";
    public static final String LOCATION_STATUS_OF_ON_BOARD = "onBoard";
    public static final String[] MAP_MOTION_NAME = {TYPE_PICKUP_NAME, TYPE_FLIP_NAME, TYPE_PROXIMITY_EAR_NAME, TYPE_SHAKE_NAME, TYPE_TAP_BACK_NAME, TYPE_TILT_LR_NAME, TYPE_ROTATION_NAME, TYPE_POCKET_NAME, TYPE_ACTIVITY_NAME, TYPE_TAKE_OFF_NAME, TYPE_HW_STEP_COUNTER_NAME, TYPE_WAVE_LIGHT_NAME, TYPE_HEAD_DOWN_NAME, TYPE_TRIPLE_FINGER_NAME};
    public static final int[] MAP_MOTION_VALUE = {100, 200, 300, 400, 500, 600, 700, 800, 900, 1000, TYPE_HW_STEP_COUNTER, TYPE_WAVE_LIGHT, TYPE_HEAD_DOWN, 1500};
    public static final long MIN_DELAY_TIME = 1000000000;
    public static final int MSDP_DEVICESTATUS_ID_INVALID = -1;
    public static final int MSDP_DEVICESTATUS_ID_MAX = 8;
    public static final String MSDP_DEVICESTATUS_TYPE_IN_HANDS = "android.activity_recognition.in_hands";
    public static final String MSDP_DEVICESTATUS_TYPE_IN_PACKAGE = "android.activity_recognition.in_package";
    public static final String MSDP_DEVICESTATUS_TYPE_IN_POCKET = "android.activit_recognition.in_pocket";
    public static final String MSDP_DEVICESTATUS_TYPE_ON_BOARD = "android.activity_recognition.on_board";
    public static final String MSDP_DEVICESTATUS_TYPE_STILL_STATUS = "msdp.devicestatus_type_still_status";
    public static final String MSDP_DEVICESTATUS_TYPE_UNKNOWN = "android.msdp.device_status.type.unknown";
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
    public static final int MSDP_DEVICE_STATUS_APPINFO_SERVICE = 5;
    public static final int MSDP_DEVICE_STATUS_AR_SERVICE = 1;
    public static final int MSDP_DEVICE_STATUS_COARSE_SERVICE = 8;
    public static final int MSDP_DEVICE_STATUS_FUSION_SERVICE = 7;
    public static final int MSDP_DEVICE_STATUS_IN_HANDS = 1001;
    public static final int MSDP_DEVICE_STATUS_IN_PACKAGE = 1003;
    public static final int MSDP_DEVICE_STATUS_IN_POCKET = 1002;
    public static final int MSDP_DEVICE_STATUS_MOTION_SERVICE = 6;
    public static final int MSDP_DEVICE_STATUS_MOVEMENT = 2003;
    public static final int MSDP_DEVICE_STATUS_ON_BOARD = 1004;
    public static final int MSDP_DEVICE_STATUS_STILL_OF_ABSOLUTE = 2001;
    public static final int MSDP_DEVICE_STATUS_STILL_OF_RELATIVE = 2002;
    public static final int MSDP_DEVICE_STATUS_UNKNOWN = 0;
    public static final int MSDP_DEVICE_STATUS_VOICE_SERVICE = 4;
    public static final String MSDP_SERVICE_TYPE_STILL_OF_RELATIVE_STEP_NUMBER = "android.msdp.still_of_relative_step_number";
    public static final String STILL_STATUS_OF_ABSOLUTE = "android.activity_recognition.still";
    public static final String STILL_STATUS_OF_MOVEMENT = "movement";
    public static final String STILL_STATUS_OF_RELATIVE = "relative";
    public static final String[] SUPPORTS = {TYPE_PICKUP_NAME, TYPE_FLIP_NAME, TYPE_PROXIMITY_EAR_NAME, TYPE_SHAKE_NAME, TYPE_TAP_BACK_NAME, TYPE_TILT_LR_NAME, TYPE_ROTATION_NAME, TYPE_POCKET_NAME, TYPE_ACTIVITY_NAME, TYPE_TAKE_OFF_NAME, TYPE_HW_STEP_COUNTER_NAME, TYPE_WAVE_LIGHT_NAME, TYPE_HEAD_DOWN_NAME, TYPE_TRIPLE_FINGER_NAME, MSDP_DEVICETSTATUS_TYPE_HIGH_STILL, MSDP_DEVICETSTATUS_TYPE_FINE_STILL, MSDP_DEVICETSTATUS_TYPE_COARSE_STILL, MSDP_DEVICETSTATUS_TYPE_CELL_CHANGED};
    public static final int TYPE_ACTIVITY = 900;
    public static final String TYPE_ACTIVITY_NAME = "msdp.type_activity";
    public static final int TYPE_FLIP = 200;
    public static final String TYPE_FLIP_NAME = "msdp.type_flip";
    public static final int TYPE_HEAD_DOWN = 1300;
    public static final String TYPE_HEAD_DOWN_NAME = "msdp.type_head_down";
    public static final int TYPE_HW_STEP_COUNTER = 1100;
    public static final String TYPE_HW_STEP_COUNTER_NAME = "msdp.type_hw_step_counter";
    public static final int TYPE_PICKUP = 100;
    public static final String TYPE_PICKUP_NAME = "msdp.type_pickup";
    public static final int TYPE_POCKET = 800;
    public static final String TYPE_POCKET_NAME = "msdp.type_pocket";
    public static final int TYPE_PROXIMITY_EAR = 300;
    public static final String TYPE_PROXIMITY_EAR_NAME = "msdp.type_proximity_ear";
    public static final int TYPE_ROTATION = 700;
    public static final String TYPE_ROTATION_NAME = "msdp.type_rotation";
    public static final int TYPE_SHAKE = 400;
    public static final String TYPE_SHAKE_NAME = "msdp.type_shake";
    public static final int TYPE_TAKE_OFF = 1000;
    public static final String TYPE_TAKE_OFF_NAME = "msdp.type_take_off";
    public static final int TYPE_TAP_BACK = 500;
    public static final String TYPE_TAP_BACK_NAME = "msdp.type_tap_back";
    public static final int TYPE_TILT_LR = 600;
    public static final String TYPE_TILT_LR_NAME = "msdp.type_tilt_lr";
    public static final int TYPE_TRIPLE_FINGER = 1500;
    public static final String TYPE_TRIPLE_FINGER_NAME = "msdp.type_triple_finger";
    public static final int TYPE_WAVE_LIGHT = 1200;
    public static final String TYPE_WAVE_LIGHT_NAME = "msdp.type_wave_light";

    public static int motionName2Value(String name) {
        for (int j = 0; j < MAP_MOTION_NAME.length; j++) {
            if (MAP_MOTION_NAME[j].equals(name)) {
                return MAP_MOTION_VALUE[j];
            }
        }
        return -1;
    }

    public static String motionValue2Name(int motionType) {
        for (int j = 0; j < MAP_MOTION_NAME.length; j++) {
            if (MAP_MOTION_VALUE[j] == motionType) {
                return MAP_MOTION_NAME[j];
            }
        }
        return null;
    }
}
