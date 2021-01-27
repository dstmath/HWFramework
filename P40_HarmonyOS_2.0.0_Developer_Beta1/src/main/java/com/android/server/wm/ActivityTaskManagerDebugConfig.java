package com.android.server.wm;

import android.os.SystemProperties;
import android.util.Log;

public class ActivityTaskManagerDebugConfig {
    private static final boolean APPEND_CATEGORY_NAME = false;
    static final boolean DEBUG_ADD_REMOVE = (DEBUG_ALL_ACTIVITIES || DEBUG_HW_ACTIVITY);
    static boolean DEBUG_ALL = (Log.HWLog || (Log.HWModuleLog && Log.isLoggable(TAG_ATM, 3)));
    private static final boolean DEBUG_ALL_ACTIVITIES = (DEBUG_ALL);
    static final boolean DEBUG_APP = (DEBUG_ALL_ACTIVITIES);
    public static final boolean DEBUG_CLEANUP = (DEBUG_ALL);
    public static final boolean DEBUG_CONFIGURATION = (DEBUG_ALL || DEBUG_HW_ACTIVITY);
    static final boolean DEBUG_CONTAINERS = (DEBUG_ALL_ACTIVITIES);
    static final boolean DEBUG_FOCUS = DEBUG_HW_ACTIVITY;
    static final boolean DEBUG_HWFREEFORM;
    static final boolean DEBUG_HW_ACTIVITY = ams_log_switch.contains("activity");
    static final boolean DEBUG_IDLE = (DEBUG_ALL_ACTIVITIES || DEBUG_HW_ACTIVITY);
    static final boolean DEBUG_IMMERSIVE = (DEBUG_ALL);
    static final boolean DEBUG_KEYGUARD = (DEBUG_ALL || DEBUG_HW_ACTIVITY);
    static final boolean DEBUG_LOCKTASK = (DEBUG_ALL);
    public static final boolean DEBUG_METRICS = (DEBUG_ALL);
    static final boolean DEBUG_PAUSE = (DEBUG_ALL || DEBUG_HW_ACTIVITY);
    static final boolean DEBUG_PERMISSIONS_REVIEW = (DEBUG_ALL);
    static final boolean DEBUG_RECENTS = (DEBUG_ALL);
    static final boolean DEBUG_RECENTS_TRIM_TASKS = (DEBUG_RECENTS);
    static final boolean DEBUG_RELEASE = (DEBUG_ALL_ACTIVITIES);
    static final boolean DEBUG_RESULTS = (DEBUG_ALL || DEBUG_HW_ACTIVITY);
    static final boolean DEBUG_SAVED_STATE = (DEBUG_ALL_ACTIVITIES);
    static final boolean DEBUG_STACK = (DEBUG_ALL || DEBUG_HW_ACTIVITY);
    static final boolean DEBUG_STATES = (DEBUG_ALL_ACTIVITIES || DEBUG_HW_ACTIVITY);
    public static final boolean DEBUG_SWITCH = (DEBUG_ALL || DEBUG_HW_ACTIVITY);
    static final boolean DEBUG_TASKS = (DEBUG_ALL || DEBUG_HW_ACTIVITY);
    static final boolean DEBUG_TRANSITION = (DEBUG_ALL);
    static final boolean DEBUG_USER_LEAVING = (DEBUG_ALL);
    static final boolean DEBUG_VISIBILITY = (DEBUG_ALL || DEBUG_HW_ACTIVITY);
    static boolean HWFLOW = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG_ATM, 4)));
    static final String POSTFIX_ADD_REMOVE = "";
    static final String POSTFIX_APP = "";
    static final String POSTFIX_CLEANUP = "";
    public static final String POSTFIX_CONFIGURATION;
    static final String POSTFIX_CONTAINERS = "";
    static final String POSTFIX_FOCUS = "";
    static final String POSTFIX_IDLE = "";
    static final String POSTFIX_IMMERSIVE = "";
    static final String POSTFIX_KEYGUARD = "_keyguard";
    public static final String POSTFIX_LOCKTASK = "";
    static final String POSTFIX_PAUSE = "";
    static final String POSTFIX_RECENTS = "";
    static final String POSTFIX_RELEASE = "";
    static final String POSTFIX_RESULTS = "";
    static final String POSTFIX_SAVED_STATE = "";
    static final String POSTFIX_STACK = "";
    static final String POSTFIX_STATES = "";
    public static final String POSTFIX_SWITCH = "";
    static final String POSTFIX_TASKS = "";
    static final String POSTFIX_TRANSITION = "";
    static final String POSTFIX_USER_LEAVING = "";
    static final String POSTFIX_VISIBILITY;
    static final String TAG_ATM = "ActivityTaskManager";
    static final boolean TAG_WITH_CLASS_NAME = false;
    static final String ams_log_switch;

    static {
        boolean z = false;
        String str = "";
        ams_log_switch = SystemProperties.get("ro.config.hw_ams_log", str);
        if (DEBUG_ALL || DEBUG_HW_ACTIVITY) {
            z = true;
        }
        DEBUG_HWFREEFORM = z;
        POSTFIX_CONFIGURATION = DEBUG_HW_ACTIVITY ? "_Configuration" : str;
        if (DEBUG_HW_ACTIVITY) {
            str = "_Visibility";
        }
        POSTFIX_VISIBILITY = str;
    }
}
