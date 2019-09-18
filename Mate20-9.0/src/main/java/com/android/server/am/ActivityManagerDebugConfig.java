package com.android.server.am;

import android.os.SystemProperties;
import android.util.Log;

class ActivityManagerDebugConfig {
    static final boolean APPEND_CATEGORY_NAME = false;
    static final boolean DEBUG_ADD_REMOVE = (DEBUG_ALL_ACTIVITIES || DEBUG_HW_ACTIVITY);
    static boolean DEBUG_ALL = (Log.HWLog || (Log.HWModuleLog && Log.isLoggable("ActivityManager", 3)));
    static final boolean DEBUG_ALL_ACTIVITIES = (DEBUG_ALL);
    static final boolean DEBUG_ANR = false;
    static final boolean DEBUG_APP = (DEBUG_ALL_ACTIVITIES);
    static final boolean DEBUG_BACKGROUND_CHECK = (DEBUG_ALL || DEBUG_HW_SERVICE);
    static final boolean DEBUG_BACKUP = (DEBUG_ALL);
    static final boolean DEBUG_BROADCAST = (DEBUG_ALL || DEBUG_HW_BROADCAST);
    static final boolean DEBUG_BROADCAST_BACKGROUND = (DEBUG_BROADCAST || DEBUG_HW_BROADCAST);
    static final boolean DEBUG_BROADCAST_LIGHT = (DEBUG_BROADCAST || DEBUG_HW_BROADCAST);
    static final boolean DEBUG_CLEANUP = (DEBUG_ALL);
    static final boolean DEBUG_CONFIGURATION = (DEBUG_ALL || DEBUG_HW_ACTIVITY);
    static final boolean DEBUG_CONTAINERS = (DEBUG_ALL_ACTIVITIES);
    static final boolean DEBUG_FOCUS = DEBUG_HW_ACTIVITY;
    static final boolean DEBUG_FOREGROUND_SERVICE = (DEBUG_ALL || DEBUG_HW_SERVICE);
    static final boolean DEBUG_HW_ACTIVITY = ams_log_switch.contains("activity");
    static final boolean DEBUG_HW_BROADCAST = ams_log_switch.contains("broadcast");
    static final boolean DEBUG_HW_PROVIDER = ams_log_switch.contains("provider");
    static final boolean DEBUG_HW_SERVICE = ams_log_switch.contains("service");
    static final boolean DEBUG_IDLE = (DEBUG_ALL_ACTIVITIES || DEBUG_HW_ACTIVITY);
    static final boolean DEBUG_IMMERSIVE = (DEBUG_ALL);
    static final boolean DEBUG_KEYGUARD;
    static final boolean DEBUG_LOCKTASK = (DEBUG_ALL);
    static final boolean DEBUG_LRU = (DEBUG_ALL);
    static final boolean DEBUG_METRICS = (DEBUG_ALL);
    static final boolean DEBUG_MU = (DEBUG_ALL);
    static final boolean DEBUG_NETWORK = (DEBUG_ALL);
    static final boolean DEBUG_OOM_ADJ = (DEBUG_ALL);
    static final boolean DEBUG_OOM_ADJ_REASON = (DEBUG_ALL);
    static final boolean DEBUG_PAUSE = (DEBUG_ALL || DEBUG_HW_ACTIVITY);
    static final boolean DEBUG_PERMISSIONS_REVIEW = (DEBUG_ALL);
    static final boolean DEBUG_POWER = (DEBUG_ALL);
    static final boolean DEBUG_POWER_QUICK = (DEBUG_POWER);
    static final boolean DEBUG_PROCESSES = (DEBUG_ALL);
    static final boolean DEBUG_PROCESS_OBSERVERS = (DEBUG_ALL);
    static final boolean DEBUG_PROVIDER = (DEBUG_ALL || DEBUG_HW_PROVIDER);
    static final boolean DEBUG_PSS = (DEBUG_ALL);
    static final boolean DEBUG_RECENTS = (DEBUG_ALL);
    static final boolean DEBUG_RECENTS_TRIM_TASKS = (DEBUG_RECENTS);
    static final boolean DEBUG_RELEASE = (DEBUG_ALL_ACTIVITIES);
    static final boolean DEBUG_RESULTS = (DEBUG_ALL || DEBUG_HW_ACTIVITY);
    static final boolean DEBUG_SAVED_STATE = (DEBUG_ALL_ACTIVITIES);
    static final boolean DEBUG_SERVICE = (DEBUG_ALL || DEBUG_HW_SERVICE);
    static final boolean DEBUG_SERVICE_EXECUTING = (DEBUG_ALL || DEBUG_HW_SERVICE);
    static final boolean DEBUG_STACK = (DEBUG_ALL || DEBUG_HW_ACTIVITY);
    static final boolean DEBUG_STATES = (DEBUG_ALL_ACTIVITIES || DEBUG_HW_ACTIVITY);
    static final boolean DEBUG_SWITCH = (DEBUG_ALL || DEBUG_HW_ACTIVITY);
    static final boolean DEBUG_TASKS = (DEBUG_ALL || DEBUG_HW_ACTIVITY);
    static final boolean DEBUG_TRANSITION = (DEBUG_ALL);
    static final boolean DEBUG_UID_OBSERVERS = (DEBUG_ALL);
    static final boolean DEBUG_URI_PERMISSION = (DEBUG_ALL);
    static final boolean DEBUG_USAGE_STATS = (DEBUG_ALL);
    static final boolean DEBUG_USER_LEAVING = (DEBUG_ALL);
    static final boolean DEBUG_VISIBILITY = (DEBUG_ALL || DEBUG_HW_ACTIVITY);
    static final boolean DEBUG_WHITELISTS = (DEBUG_ALL || DEBUG_HW_SERVICE);
    static boolean HWFLOW = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable("ActivityManager", 4)));
    static final String POSTFIX_ADD_REMOVE = "";
    static final String POSTFIX_APP = "";
    static final String POSTFIX_BACKUP = "";
    static final String POSTFIX_BROADCAST = "";
    static final String POSTFIX_CLEANUP = "";
    static final String POSTFIX_CONFIGURATION = (DEBUG_HW_ACTIVITY ? "_Configuration" : BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
    static final String POSTFIX_CONTAINERS = "";
    static final String POSTFIX_FOCUS = "";
    static final String POSTFIX_IDLE = "";
    static final String POSTFIX_IMMERSIVE = "";
    static final String POSTFIX_KEYGUARD = "_keyguard";
    static final String POSTFIX_LOCKTASK = "";
    static final String POSTFIX_LRU = "";
    static final String POSTFIX_MU = "_MU";
    static final String POSTFIX_NETWORK = "_Network";
    static final String POSTFIX_OOM_ADJ = "";
    static final String POSTFIX_PAUSE = "";
    static final String POSTFIX_POWER = "";
    static final String POSTFIX_PROCESSES = "";
    static final String POSTFIX_PROCESS_OBSERVERS = "";
    static final String POSTFIX_PROVIDER = "";
    static final String POSTFIX_PSS = "";
    static final String POSTFIX_RECENTS = "";
    static final String POSTFIX_RELEASE = "";
    static final String POSTFIX_RESULTS = "";
    static final String POSTFIX_SAVED_STATE = "";
    static final String POSTFIX_SERVICE = "";
    static final String POSTFIX_SERVICE_EXECUTING = "";
    static final String POSTFIX_STACK = "";
    static final String POSTFIX_STATES = "";
    static final String POSTFIX_SWITCH = "";
    static final String POSTFIX_TASKS = "";
    static final String POSTFIX_TRANSITION = "";
    static final String POSTFIX_UID_OBSERVERS = "";
    static final String POSTFIX_URI_PERMISSION = "";
    static final String POSTFIX_USER_LEAVING = "";
    static final String POSTFIX_VISIBILITY = (DEBUG_HW_ACTIVITY ? "_Visibility" : BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
    static final String TAG_AM = "ActivityManager";
    static final String TAG_FLOW = "ActivityManager_FLOW";
    static final boolean TAG_WITH_CLASS_NAME = false;
    static final String ams_log_switch = SystemProperties.get("ro.config.hw_ams_log", BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);

    ActivityManagerDebugConfig() {
    }

    static {
        boolean z = true;
        if (!DEBUG_ALL && !DEBUG_HW_ACTIVITY) {
            z = false;
        }
        DEBUG_KEYGUARD = z;
    }
}
