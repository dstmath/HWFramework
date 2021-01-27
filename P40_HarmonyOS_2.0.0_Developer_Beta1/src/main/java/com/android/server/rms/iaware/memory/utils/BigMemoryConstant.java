package com.android.server.rms.iaware.memory.utils;

public class BigMemoryConstant {
    public static final long ACTIVITY_MAX_DURING_TIME = 500;
    public static final int ACTIVITY_MAX_NUMBER = 100;
    public static final String ACTIVITY_NAME = "activityName";
    public static final int ACTIVITY_NAME_MAX_LEN = 132;
    public static final String BETA_ACTIVITY_BEGIN = "activity_begin";
    public static final String BETA_ACTIVITY_FINISH = "activity_finish";
    public static final String BIG_MEM_INFO_ITEM_TAG = "activity";
    public static final int CAMERA_MEM_CHANGE_THRESHOLD = 50;
    public static final int CAMERA_MEM_CHANGE_THRESHOLD_SD = 50;
    public static final int CAMERA_MIN_MEM = 100;
    public static final String CAMERA_OPEN_ACTIVITY_NAME = "com.huawei.camera.controller.CameraActivity";
    public static final String CAMERA_SERVER = "cameraserver";
    public static final String DEFAULT_CAMERA_NAME = "com.huawei.camera";
    public static final int LEARNING_MAX_COUNT = 5;
    public static final int MEM_MB_TO_KB = 1024;
    public static final String MEM_POLICY_ACTIVITIES_MAX_COUNT = "activities_max_count";
    public static final String MEM_POLICY_ACTIVITY_MAX_MEM = "activity_max_mem";
    public static final String MEM_POLICY_ACTIVITY_MEM_APP = "ActivityMemApp";
    public static final String MEM_POLICY_BIG_MEM_SWITCH = "switch";
    public static final String MEM_POLICY_CAMERA_ACTIVITY = "CameraActivity";
    public static final String MEM_POLICY_CAMERA_MAX_MEM = "camera_max_mem";
    public static final String MEM_POLICY_CAMERA_MEM_CHANGE_THRESHOLD = "camera_mem_change_threshold";
    public static final String MEM_POLICY_CAMERA_MEM_CHANGE_THRESHOLD_SD = "camera_mem_change_threshold_sd";
    public static final String MEM_POLICY_CAMERA_MIN_MEM = "camera_min_mem";
    public static final String MEM_POLICY_CAMERA_OPEN_DELAY_TIME = "camera_open_delay_time";
    public static final String MEM_POLICY_CAMERA_PROCESS_NAME = "camera_process_name";
    public static final int MSG_ACTIVITY_BEGIN_INFO = 103;
    public static final int MSG_ACTIVITY_FINISH_INFO = 104;
    public static final int MSG_APP_UNINSTALL_INFO = 105;
    public static final int MSG_LEARNING_MEMORY_INFO = 101;
    public static final int MSG_OPEN_CAMERA_INFO = 102;
    public static final String NOTIFY_CAMERA_EVENT_CLOSE_TYPE = "close";
    public static final String NOTIFY_CAMERA_EVENT_OPEN_TYPE = "open";
    public static final int OPEN_CAMERA_DELAY_TIME_MAX = 6;
    public static final int OPEN_CAMERA_DELAY_TIME_MIN = 2;
    public static final String PACKAGE_NAME = "packageName";
    public static final String RECENT_ACTIVITY_NAME = "recentActivityName";
    public static final int TIME_S_TO_MS = 1000;
    public static final String UID = "uid";

    private BigMemoryConstant() {
    }
}
