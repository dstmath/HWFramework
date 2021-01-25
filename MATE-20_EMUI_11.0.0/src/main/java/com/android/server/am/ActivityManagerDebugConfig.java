package com.android.server.am;

import android.net.INetd;
import android.os.SystemProperties;
import android.util.Log;

public class ActivityManagerDebugConfig {
    static final String AMS_LOG_SWITCH = SystemProperties.get("ro.config.hw_ams_log", "");
    static final boolean APPEND_CATEGORY_NAME = false;
    static final boolean DEBUG_ALL = (Log.HWLog || (Log.HWModuleLog && Log.isLoggable("ActivityManager", 3)));
    static final boolean DEBUG_ANR = false;
    static final boolean DEBUG_BACKGROUND_CHECK = (DEBUG_ALL || DEBUG_HW_SERVICE);
    static final boolean DEBUG_BACKUP = (DEBUG_ALL);
    static final boolean DEBUG_BROADCAST = (DEBUG_ALL || DEBUG_HW_BROADCAST);
    static final boolean DEBUG_BROADCAST_BACKGROUND = (DEBUG_BROADCAST || DEBUG_HW_BROADCAST);
    static final boolean DEBUG_BROADCAST_DEFERRAL = (DEBUG_BROADCAST || DEBUG_HW_BROADCAST);
    static final boolean DEBUG_BROADCAST_LIGHT = (DEBUG_BROADCAST || DEBUG_HW_BROADCAST);
    static final boolean DEBUG_COMPACTION = (DEBUG_ALL);
    static final boolean DEBUG_FOREGROUND_SERVICE = (DEBUG_ALL || DEBUG_HW_SERVICE);
    static final boolean DEBUG_HW_BROADCAST = AMS_LOG_SWITCH.contains(INetd.IF_FLAG_BROADCAST);
    static final boolean DEBUG_HW_PROVIDER = AMS_LOG_SWITCH.contains("provider");
    static final boolean DEBUG_HW_SERVICE = AMS_LOG_SWITCH.contains("service");
    static final boolean DEBUG_LRU = (DEBUG_ALL);
    static final boolean DEBUG_MU = (DEBUG_ALL);
    static final boolean DEBUG_NETWORK = (DEBUG_ALL);
    static final boolean DEBUG_OOM_ADJ = (DEBUG_ALL);
    static final boolean DEBUG_OOM_ADJ_REASON = (DEBUG_ALL);
    static final boolean DEBUG_PERMISSIONS_REVIEW = (DEBUG_ALL);
    static final boolean DEBUG_POWER = (DEBUG_ALL);
    static final boolean DEBUG_POWER_QUICK = (DEBUG_POWER);
    static final boolean DEBUG_PROCESSES = (DEBUG_ALL);
    static final boolean DEBUG_PROCESS_OBSERVERS = (DEBUG_ALL);
    static final boolean DEBUG_PROVIDER = (DEBUG_ALL || DEBUG_HW_PROVIDER);
    static final boolean DEBUG_PSS = (DEBUG_ALL);
    static final boolean DEBUG_SERVICE = (DEBUG_ALL || DEBUG_HW_SERVICE);
    static final boolean DEBUG_SERVICE_EXECUTING = (DEBUG_ALL || DEBUG_HW_SERVICE);
    static final boolean DEBUG_UID_OBSERVERS = (DEBUG_ALL);
    static final boolean DEBUG_USAGE_STATS = (DEBUG_ALL);
    static final boolean DEBUG_WHITELISTS;
    static final boolean HWFLOW = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable("ActivityManager", 4)));
    static final String POSTFIX_BACKUP = "";
    static final String POSTFIX_BROADCAST = "";
    static final String POSTFIX_CLEANUP = "";
    static final String POSTFIX_LRU = "";
    static final String POSTFIX_MU = "_MU";
    static final String POSTFIX_NETWORK = "_Network";
    static final String POSTFIX_OOM_ADJ = "";
    static final String POSTFIX_POWER = "";
    static final String POSTFIX_PROCESSES = "";
    static final String POSTFIX_PROCESS_OBSERVERS = "";
    static final String POSTFIX_PROVIDER = "";
    static final String POSTFIX_PSS = "";
    static final String POSTFIX_SERVICE = "";
    static final String POSTFIX_SERVICE_EXECUTING = "";
    static final String POSTFIX_UID_OBSERVERS = "";
    static final String POSTFIX_URI_PERMISSION = "";
    static final String TAG_AM = "ActivityManager";
    static final String TAG_FLOW = "ActivityManager_FLOW";
    static final boolean TAG_WITH_CLASS_NAME = false;

    static {
        boolean z = false;
        if (DEBUG_ALL || DEBUG_HW_SERVICE) {
            z = true;
        }
        DEBUG_WHITELISTS = z;
    }
}
