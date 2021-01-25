package com.huawei.hiai.awareness;

public class AwarenessConstants {
    public static final int ACTIVITY_STATUS_CHANGE_ACTION = 2;
    public static final int ALARM_ALERT_ACTION = 32;
    public static final int ANDROID_INTENT_TIME_SET_ACTION = 4096;
    public static final int APP_LIFE_CHANGE_TYPE = 11;
    public static final int APP_SATTUS_CLOSE_STATUS = 2;
    public static final int APP_SATTUS_OPEN_STATUS = 1;
    public static final int APP_STATUS_CHANGE_ACTION = 1;
    public static final int APP_USE_TOTAL_TIME_ACTION = 1;
    public static final String AWARENESS_FENCE_KEY = "fenceKey";
    public static final String AWARENESS_PACKAGE_NAME = "com.huawei.hiai";
    public static final String AWARENESS_SERVICE_ACTION_NAME = "com.huawei.hiai.awareness.IAwarenessService";
    public static final String AWARENESS_SERVICE_CLASS_NAME = "com.huawei.hiai.awareness.service.AwarenessService";
    public static final int BROADCAST_ACTION_MAX = 65535;
    public static final String BROADCAST_FENCE_CALLBACK_INTENT = "BROADCAST_FENCE_CALLBACK_INTENT";
    public static final int CELLID_TRIGGER_ACTION = 4;
    public static final String CITY_NAME = "City_name";
    public static final int COMMON_STATUS = 4;
    public static final int COMMON_SYSTEM_EVENT_TRIGGER_TYPE = 10;
    public static final String CUSTOM_DURATION_DURATION_TIME_PERCENT_AGE = "CUSTOM_DURATION_DURATION_TIME_PERCENT_AGE";
    public static final String CUSTOM_DURATION_FILL_LIGHT_MODE = "CUSTOM_DURATION_FILL_LIGHT_MODE";
    public static final String DATA_ACTION_STRING_TYPE = "action_string";
    public static final String DATA_ACTION_TYPE = "action";
    public static final String DATA_EVENT_TYPE = "eventType";
    public static final String DATA_SENSOR_TIME_STAMP = "dataSensorTimeStamp";
    public static final int DEVICE_STATUS_TYPE = 3;
    public static final int DEVICE_USE_TOTAL_TIME_ACTION = 3;
    public static final int DEVICE_USE_TYPE = 9;
    public static final int ENTER_COMPANY_STATUS = 4;
    public static final int ENTER_HOME_STATUS = 1;
    public static final int ENTER_NOT_RESIDENCE_STATUS = 32;
    public static final int ENTER_OVERSEA_STATUS = 16;
    public static final int ENTER_RESIDENCE_STATUS = 64;
    public static final String ERROR_CANCEL_REGISTRY = "error_cancel_registry";
    public static final int ERROR_CANCEL_REGISTRY_CODE = 200006;
    public static final String ERROR_FUNCTION_NOT_SUPPORTED = "error_function_not_supported";
    public static final int ERROR_FUNCTION_NOT_SUPPORTED_CODE = 200009;
    public static final String ERROR_INVALID_FREQUENCY = "error_invalid_frequency";
    public static final int ERROR_INVALID_FREQUENCY_CODE = 200002;
    public static final String ERROR_LIMITED_REGISTRY = "error_limited_registry";
    public static final int ERROR_LIMITED_REGISTRY_CODE = 200005;
    public static final String ERROR_NO_PERMISSION = "error_no_permission";
    public static final int ERROR_NO_PERMISSION_CODE = 200001;
    public static final String ERROR_PARAMETER = "error_parameter";
    public static final int ERROR_PARAMETER_CODE = 200007;
    public static final String ERROR_REGISTER_SAME_FENCE = "error_register_same_fence";
    public static final int ERROR_REGISTER_SAME_FENCE_CODE = 200008;
    public static final String ERROR_REMOTE_CALLBACK = "error_remote_callback_failure";
    public static final int ERROR_REMOTE_CALLBACK_CODE = 200012;
    public static final String ERROR_SERVICE_NOT_CONNECTED = "error_service_not_connected";
    public static final int ERROR_SERVICE_NOT_CONNECTED_CODE = 200011;
    public static final String ERROR_SWING_FAILURE = "error_swing_failure";
    public static final int ERROR_SWING_FAILURE_CODE = 200013;
    public static final int ERROR_SWING_MULTI_CLIENT_CODE = 200014;
    public static final int ERROR_THIRD_PART_PROCESS_CODE = 200010;
    public static final String ERROR_TIMEOUT = "error_timeout";
    public static final int ERROR_TIMEOUT_CODE = 200003;
    public static final String ERROR_UNKNOWN = "error_unknown";
    public static final int ERROR_UNKNOWN_CODE = 200004;
    public static final int EVENT_ALREADY_TRIGGER_TAG = 1;
    public static final int EVENT_DEFAULT_STATUS_TAG = -1;
    public static final int EVENT_NOT_EXIST_TRIGGER_TAG = 0;
    public static final int EVENT_NOT_TRIGGER_TAG = 2;
    public static final int EVENT_REGISTER_FAILED_TAG = 4;
    public static final int EVENT_REGISTER_SUCCESS_TAG = 3;
    public static final int EVENT_REMOVE_FAILED_TAG = 6;
    public static final int EVENT_REMOVE_SUCCESS_TAG = 5;
    public static final int EVENT_SWING_UNREGISTER_FAILED_TAG = 9;
    public static final int EVENT_SWITCH_OFF_TAG = 7;
    public static final int EVENT_SWITCH_ON_TAG = 8;
    public static final int EXIT_COMPANY_STATUS = 8;
    public static final int EXIT_HOME_STATUS = 2;
    public static final int GPS_TRIGGER_ACTION = 2;
    public static final int HEADSET_PLUG_ACTION = 8;
    public static final int HIACTION_CARDUPDATE_ACTION = 1024;
    public static final int HIACTION_EXPRESS_ACTION = 2048;
    public static final String HW_MSDP_OTHER_PARAMS = "HwMSDPOtherParams";
    public static final int INTERVAL_LOOP_ACTION = 2;
    public static final int INTERVAL_ONE_ACTION = 1;
    public static final int IS_RESIDENT_TYPE = 20;
    public static final String LAUNCH_AWARENESS_PACKAGE_NAME = "LAUNCH_AWARENESS_PACKAGE_NAME";
    public static final int LOCATION_ACTION_MAX = 7;
    public static final int LOCATION_COMPANY = 1;
    public static final String LOCATION_CUSTOM = "LOCATION&CUSTOM";
    public static final String LOCATION_CUSTOM_COMPANY_CITY = "LOCATION_CUSTOM_COMPANY_CITY";
    public static final String LOCATION_CUSTOM_COMPANY_LAT = "LOCATION_CUSTOM_COMPANY_LAT";
    public static final String LOCATION_CUSTOM_COMPANY_LON = "LOCATION_CUSTOM_COMPANY_LON";
    public static final String LOCATION_CUSTOM_COMPANY_RADIUS = "LOCATION_CUSTOM_COMPANY_RADIUS";
    public static final String LOCATION_CUSTOM_ENTER_COMPANY_RADIUS = "LOCATION_CUSTOM_ENTER_COMPANY_RADIUS";
    public static final String LOCATION_CUSTOM_ENTER_HOME_RADIUS = "LOCATION_CUSTOM_ENTER_HOME_RADIUS";
    public static final String LOCATION_CUSTOM_HOME_CITY = "LOCATION_CUSTOM_HOME_CITY";
    public static final String LOCATION_CUSTOM_HOME_LAT = "LOCATION_CUSTOM_HOME_LAT";
    public static final String LOCATION_CUSTOM_HOME_LON = "LOCATION_CUSTOM_HOME_LON";
    public static final String LOCATION_CUSTOM_HOME_RADIUS = "LOCATION_CUSTOM_HOME_RADIUS";
    public static final String LOCATION_CUSTOM_LEAVE_COMPANY_RADIUS = "LOCATION_CUSTOM_LEAVE_COMPANY_RADIUS";
    public static final String LOCATION_CUSTOM_LEAVE_HOME_RADIUS = "LOCATION_CUSTOM_LEAVE_HOME_RADIUS";
    public static final String LOCATION_CUSTOM_RESIDENCE_CITY = "LOCATION_CUSTOM_RESIDENCE_CITY";
    public static final int LOCATION_HOME = 0;
    public static final int LOCATION_NOT_RESIDENCE = 2;
    public static final int LOCATION_OVERSEA = 4;
    public static final int LOCATION_RESIDENCE = 3;
    public static final int LOCATION_SOURCE_CELLID = 4;
    public static final int LOCATION_SOURCE_GPS = 2;
    public static final int LOCATION_SOURCE_MSDP = 8;
    public static final int LOCATION_SOURCE_WIFI = 1;
    public static final int LOCATION_STATUS_MAX = 127;
    public static final int LOCATION_TYPE = 6;
    public static final int LOCATION_UNKNOWN = -1;
    public static final int MOVEMENT_ENTER_STATUS = 1;
    public static final int MOVEMENT_EXIT_STATUS = 2;
    public static final int MOVEMENT_TYPE = 1;
    public static final int MSDP_ENVIRONMENT_TYPE_HOME = 65536;
    public static final int MSDP_ENVIRONMENT_TYPE_OFFICE = 131072;
    public static final int MSDP_ENVIRONMENT_TYPE_WAY_HOME = 262144;
    public static final int MSDP_ENVIRONMENT_TYPE_WAY_OFFICE = 524288;
    public static final int MSDP_MOVEMENT_TYPE_ELEVATOR = 256;
    public static final int MSDP_MOVEMENT_TYPE_FAST_WALKING = 32;
    public static final int MSDP_MOVEMENT_TYPE_HIGH_SPEED_RAIL = 64;
    public static final int MSDP_MOVEMENT_TYPE_IN_VEHICLE = 1;
    public static final int MSDP_MOVEMENT_TYPE_LYING_POSTURE = 2048;
    public static final int MSDP_MOVEMENT_TYPE_ON_BICYCLE = 2;
    public static final int MSDP_MOVEMENT_TYPE_ON_FOOT = 128;
    public static final int MSDP_MOVEMENT_TYPE_RELATIVE_STILL = 512;
    public static final int MSDP_MOVEMENT_TYPE_RUNNING = 8;
    public static final int MSDP_MOVEMENT_TYPE_STILL = 16;
    public static final int MSDP_MOVEMENT_TYPE_WALKING = 4;
    public static final int MSDP_MOVEMENT_TYPE_WALKING_HANDHOLD = 1024;
    public static final long MSDP_REPORT_FREQUECE_NS = 200000000000L;
    public static final long MSDP_REPORT_FREQUECE_ONE_SECOND_NS = 1000000000;
    public static final int NETWORK_STATE_CHANGED_ACTION = 128;
    public static final int ONE_APP_CONTINUOUS_USE_TIME_ACTION = 2;
    public static final String PACKAGE_TOPKEY_SPLITE_TAG = "_";
    public static final int PHONE_STATE_CHANGED_ACTION = 16384;
    public static final int POWER_MODE_CHANGED_ACTION = 8192;
    public static final String REGISTER_APP_LIFE_FENCE_CALLBACK_INTENT = "REGISTER_APP_LIFE_FENCE_CALLBACK__INTENT";
    public static final String REGISTER_APP_LIFE_FENCE_INTENT = "REGISTER_APP_LIFE_FENCE_INTENT";
    public static final String REGISTER_BROADCAST_FENCE_INTENT = "REGISTER_BROADCAST_FENCE_INTENT";
    public static final int REGISTER_SUCCESS_CODE = 200000;
    public static final String REGISTER_SWING_FENCE_CALLBACK_INTENT = "REGISTER_SWING_FENCE_CALLBACK_INTENT";
    public static final String REGISTER_SWING_FENCE_INTENT = "REGISTER_SWING_FENCE_INTENT";
    public static final int RESULT_TYPE_GET_CAPABILITY_FAILED = 4;
    public static final int RESULT_TYPE_GET_CAPABILITY_SUCCESS = 5;
    public static final int RESULT_TYPE_GET_CURRENT_STATUS_FAILED = 3;
    public static final int RESULT_TYPE_GET_CURRENT_STATUS_SUCCESS = 2;
    public static final int RESULT_TYPE_GET_FENCE_TRIGGER_RESULT = 1;
    public static final int RESULT_TYPE_GET_FENCE_TRIGGER_RESULT_FAILED = 9;
    public static final int RESULT_TYPE_GET_FENCE_TRIGGER_RESULT_SUCCESS = 8;
    public static final int RESULT_TYPE_SET_REPORT_PERIOD_FAILURE = 7;
    public static final int RESULT_TYPE_SET_REPORT_PERIOD_SUCCESS = 6;
    public static final int RINGER_MODE_CHANGED_ACTION = 16;
    public static final int SCREEN_OFF_ACTION = 2;
    public static final int SCREEN_ON_ACTION = 4;
    public static final int SCREEN_ON_EVENT_TYPE = 15;
    public static final int SCREEN_UNLOCK_ACTION = 1;
    public static final int SCREEN_UNLOCK_TOTAL_NUMBER_ACTION = 4;
    public static final String SECOND_ACTION_SPLITE_TAG = "~";
    public static final String SENSORHUB_CONTROL_REPORT_PERIOD = "sensorhub_control_report_period";
    public static final int SET_SWING_SUCCESS_CODE = 1;
    public static final int SINGER_FENCE_CONTAIN_FENCE_NUMBER = 1;
    public static final int STATUSBAR_VISIBLE_CHANGE_ACTION = 512;
    public static final int SWING_AGE_ACTION_MAX = 31;
    public static final int SWING_AGE_ALL_ACTION = 16;
    public static final int SWING_AGE_ESTIMATE_STATUS = 4;
    public static final int SWING_AGE_EXCEED_EIGHTEEN_ACTION = 8;
    public static final int SWING_AGE_TEN_TO_THIRTEEN_ACTION = 2;
    public static final int SWING_AGE_THIRTEEN_TO_EIGHTEEN_ACTION = 4;
    public static final int SWING_AGE_UNDER_TEN_ACTION = 1;
    public static final int SWING_AMBIENT_LIGHT_ACTION_MAX = 7;
    public static final int SWING_AMBIENT_LIGHT_HIGH_ACTION = 4;
    public static final int SWING_AMBIENT_LIGHT_LOW_ACTION = 1;
    public static final int SWING_AMBIENT_LIGHT_NORMAL_ACTION = 2;
    public static final int SWING_AMBIENT_LIGHT_STATUS = 10;
    public static final String SWING_CUSTOM_CAMERA_FOV_RATIO = "cameraFOVRatio";
    public static final String SWING_CUSTOM_DARK_LIGHT_ENABLE = "SWING_CUSTOM_DARK_LIGHT_ENABLE ";
    public static final String SWING_CUSTOM_DIRECTION_ANGLE = "SWING_CUSTOM_DIRECTION_ANGLE";
    public static final String SWING_CUSTOM_DURATION_PERIOD_PERCENTAGE = "SWING_CUSTOM_DURATION_PERIOD_PERCENTAGE";
    public static final String SWING_CUSTOM_DURATION_PERIOD_TIME = "SWING_CUSTOM_DURATION_PERIOD_TIME";
    public static final String SWING_CUSTOM_ENTER_EVENT_TIME = "SWING_CUSTOM_ENTER_EVENT_TIME";
    public static final String SWING_CUSTOM_EXIT_EVENT_TIME = "SWING_CUSTOM_EXIT_EVENT_TIME";
    public static final String SWING_CUSTOM_FACEINFO_VAILD_TIME = "SWING_CUSTOM_FACEINFO_VAILD_TIME ";
    public static final String SWING_CUSTOM_HIGH_LIGHT = "SWING_CUSTOM_HIGH_LIGHT";
    public static final String SWING_CUSTOM_INTERVAL_PERIOD_TIME = "SWING_CUSTOM_INTERVAL_PERIOD_TIME";
    public static final String SWING_CUSTOM_LOW_LIGHT = "SWING_CUSTOM_LOW_LIGHT";
    public static final String SWING_CUSTOM_MAXDISTANCE = "SWING_CUSTOM_MAXDISTANCE";
    public static final String SWING_CUSTOM_MAX_PERSON_NUMBER = "SWING_CUSTOM_MAX_PERSON_NUMBER";
    public static final String SWING_CUSTOM_MINDISTANCE = "SWING_CUSTOM_MINDISTANCE";
    public static final String SWING_CUSTOM_PERSON_NUMBER = "SWING_CUSTOM_PERSON_NUMBER";
    public static final String SWING_CUSTOM_REPORT_PERIOD_TIME = "SWING_CUSTOM_REPORT_PERIOD_TIME";
    public static final String SWING_CUSTOM_SAMPLING_PERIOD_TIME = "SWING_CUSTOM_SAMPLING_PERIOD_TIME";
    public static final String SWING_CUSTOM_TRACKING_FACE_INFO_VALID_TIMER = "SWING_CUSTOM_TRACKING_FACE_INFO_VALID_TIMER";
    public static final int SWING_DEVICE_CLOSE_ACTION = 0;
    public static final int SWING_DEVICE_OPEN_ACTION = 1;
    public static final int SWING_DISTANCE_ACTION = 4;
    public static final int SWING_DISTANCE_ACTION_MAX = 7;
    public static final int SWING_DISTANCE_EXCEED_MAXDISTANCE_ACTION = 2;
    public static final int SWING_DISTANCE_STATUS = 6;
    public static final int SWING_DISTANCE_UNDER_MINDISTANCE_ACTION = 1;
    public static final int SWING_ENTER_LYING_ACTION = 1;
    public static final int SWING_ENTER_WALKING_ACTION = 1;
    public static final int SWING_EXIT_LYING_ACTION = 2;
    public static final int SWING_EXIT_WALKING_ACTION = 2;
    public static final int SWING_EYE_GAZE_SCREEN_ON_ACTION = 1;
    public static final int SWING_EYE_GAZE_STATUS = 3;
    public static final int SWING_FACE_DIRECTION_ACTION = 1;
    public static final int SWING_FACE_DIRECTION_STATUS = 5;
    public static final int SWING_FACE_NUM_ACTION = 2;
    public static final int SWING_FACE_NUM_ACTION_MAX = 2;
    public static final int SWING_FACE_NUM_CHANGE_STATUS = 1;
    public static final int SWING_FACE_RECOGNITION_ACTION = 1;
    public static final int SWING_FACE_TRACKING_CHANGE_ACTION = 1;
    public static final int SWING_FACE_TRACKING_STATUS = 2;
    public static final int SWING_FENCE_TYPE = 13;
    public static final int SWING_GESTURE_ACTION_MAX = 511;
    public static final int SWING_GESTURE_FETCH_ACTION = 32;
    public static final int SWING_GESTURE_PUSH_ACTION = 16;
    public static final int SWING_GESTURE_SLIDE_DOWN_ACTION = 2;
    public static final int SWING_GESTURE_SLIDE_INIT_ACTION = 0;
    public static final int SWING_GESTURE_SLIDE_LEFT_ACTION = 4;
    public static final int SWING_GESTURE_SLIDE_RIGHT_ACTION = 8;
    public static final int SWING_GESTURE_SLIDE_UP_ACTION = 1;
    public static final int SWING_GESTURE_START_ACTION = 64;
    public static final int SWING_GESTURE_START_DOWN_ACTION = 256;
    public static final int SWING_GESTURE_START_UP_ACTION = 128;
    public static final int SWING_HAVE_FACE_ACTION = 1;
    public static final int SWING_LYING_ACTION_MAX = 3;
    public static final int SWING_LYING_STATUS = 8;
    public static final int SWING_MOTION_GESTURE_STATUS = 7;
    public static final int SWING_MULTI_EYE_GAZE_STATUS = 11;
    public static final String SWING_PARAMETER_CUSTOM = "SWING_PARAMETER_CUSTOM";
    public static final int SWING_WALKING_ACTION_MAX = 3;
    public static final int SWING_WALKING_STATUS = 9;
    public static final int TELEPHONY_SMS_RECEIVED_ACTION = 256;
    public static final int TIME_TYPE = 8;
    public static final int TRAVEL_HELPER_DATA_CHANGE_ACTION = 32768;
    public static final String TYPE_DEVICE_STATUS_INTENT = "motion";
    public static final int WIFI_STATE_CHANGED_ACTION = 64;
    public static final int WIFI_TRIGGER_ACTION = 1;

    public static final class NotifyServiceCreateConstants {
        public static final String ACTION_AWARENESS_SERVICE_CREATE = "com.huawei.hiai.awareness.action.service.create";
        public static final String ACTION_AWARENESS_SERVICE_CREATE_SUFFIX = ".awareness.action.service.create";
        public static final String AWARENESS_SERVICE_CREATE_TYPE = "awareness_service_create_type";
        public static final String PERMISSION_AWARENESS_SERVICE_CREATE = "com.huawei.hiai.awareness.permission.NOTIFY_RESTART_SERVICE";
        public static final int SERVICE_EXP_RESTART = 2;
        public static final int SYSTEM_BOOT_CREATE = 1;

        private NotifyServiceCreateConstants() {
        }
    }

    public static final class MapInfoFenceConstants {
        public static final String CUSTOM_DESTINATION_LAT = "CUSTOM_DESTINATION_LAT";
        public static final String CUSTOM_DESTINATION_LON = "CUSTOM_DESTINATION_LON";
        public static final String CUSTOM_REPORT_PERIOD = "CUSTOM_REPORT_PERIOD";
        public static final String CUSTOM_ROUTE_SEARCH_MODE = "CUSTOM_ROUTE_SEARCH_MODE";
        public static final int DEFAULT_REPORT_PERIOD = 300;
        public static final int FASTEST_SHORTEST_AVOID_CONGESTION = 10;
        public static final String FENCE_TRIGGER_BUNDLE_DATA = "FENCE_TRIGGER_BUNDLE_DATA";
        public static final String FENCE_TRIGGER_CLIENT_TOP_KEY = "FENCE_TRIGGER_CLIENT_TOP_KEY";
        public static final String FENCE_TRIGGER_IS_VALID_DATA = "FENCE_TRIGGER_IS_VALID_DATA";
        public static final String FENCE_TRIGGER_REMAIN_DISTANCE_DATA = "FENCE_TRIGGER_REMAIN_DISTANCE_DATA";
        public static final String FENCE_TRIGGER_REMAIN_TIME_DATA = "FENCE_TRIGGER_REMAIN_TIME_DATA";
        public static final String FENCE_TRIGGER_REMAIN_TRAFFIC_LIGHTS_DATA = "FENCE_TRIGGER_REMAIN_TRAFFIC_LIGHTS_DATA";
        public static final int FENCE_TYPE = 17;
        public static final int NO_HIGHWAY_SAVE_MONEY_AVOID_CONGESTION = 9;
        public static final String REGISTER_FENCE_INTENT = "REGISTER_FENCE_INTENT";
        public static final int ROUTE_INFO_REPORT_ACTION = 1;

        private MapInfoFenceConstants() {
        }
    }

    public static final class DataBaseFenceConstants {
        public static final int DATABASE_CHANGE_ACTION = 1;
        public static final String DATABASE_CUSTOM_URL = "DATABASE_CUSTOM_URL";
        public static final int DATABASE_FENCE_TYPE = 16;
        public static final String REGISTER_DATA_BASE_CHANGE_FENCE_INTENT = "REGISTER_DATA_BASE_CHANGE_FENCE_INTENT";
        public static final String REGISTER_DATA_BASE_FENCE_CALLBACK_URI = "REGISTER_DATA_BASE_FENCE_CALLBACK_URI";

        private DataBaseFenceConstants() {
        }
    }

    public static final class CarBluetoothConstants {
        public static final String BLUETOOTH_COD = "Bluetooth_cod";
        public static final String BLUETOOTH_MAC = "Bluetooth_mac";
        public static final String BLUETOOTH_NAME = "Bluetooth_name";
        public static final int CAR_BLUETOOTH_ACTION_MAX = 3;
        public static final int CAR_BLUETOOTH_STATUS = 2;
        public static final int CAR_BLUETOOTH_TYPE = 19;
        public static final int CONNECT_CAR_BLUETOOTH_ACTION = 1;
        public static final int DISCONNECT_CAR_BLUETOOTH_ACTION = 2;

        private CarBluetoothConstants() {
        }
    }

    public static final class TvConstants {
        public static final int CAMERA_EVENT_FORWARD_REWIND_ACTION = 4;
        public static final int CAMERA_EVENT_MUTE_UNMUTE_ACTION = 1;
        public static final int CAMERA_EVENT_PAUSE_PLAY_ACTION = 2;
        public static final int CAMERA_EVENT_STOP_ACTION = 512;
        public static final int CAMERA_EVENT_UNKNOWN_ACTION = 0;
        public static final int CAMERA_EVENT_VOLUME_ACTION = 8;
        public static final int CAMERA_GESTURE_MAX_ACTION = 1023;
        public static final int CAMERA_HAS_NO_PEOPLE_ACTION = 2;
        public static final int CAMERA_HAS_PEOPLE_ACTION = 1;
        public static final int CAMERA_MOTION_GESTURE_STATUS = 12;
        public static final int CAMERA_PEOPLE_NUM_CHANGE_STATUS = 13;
        public static final int CAMERA_PEOPLE_NUM_MAX_ACTION = 3;
        public static final int CAMERA_START_INVALID_ACTION = 16;
        public static final int CAMERA_START_MUTE_ACTION = 128;
        public static final int CAMERA_START_OPEN_HAND_ACTION = 256;
        public static final int CAMERA_START_PINCH_CLOSE_ACTION = 64;
        public static final int CAMERA_START_PINCH_OPEN_ACTION = 32;
        public static final int CAMERA_TYPE = 20;
        public static final String TV_OFFSET_GESTURE_NAME = "GestureOffset";
        public static final String TV_REPORT_ACTION_NAME = "action";
        public static final String TV_REPORT_STATUS_NAME = "status";
        public static final String TV_REPORT_TYPE_NAME = "type";

        private TvConstants() {
        }
    }

    private AwarenessConstants() {
    }
}
