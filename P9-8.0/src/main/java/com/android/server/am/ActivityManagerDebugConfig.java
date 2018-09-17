package com.android.server.am;

import android.util.Log;

class ActivityManagerDebugConfig {
    static final boolean APPEND_CATEGORY_NAME = false;
    static final boolean DEBUG_ADD_REMOVE;
    static boolean DEBUG_ALL = false;
    static final boolean DEBUG_ALL_ACTIVITIES;
    static final boolean DEBUG_ANR = false;
    static final boolean DEBUG_APP;
    static final boolean DEBUG_BACKGROUND_CHECK;
    static final boolean DEBUG_BACKUP;
    static final boolean DEBUG_BROADCAST;
    static final boolean DEBUG_BROADCAST_BACKGROUND;
    static final boolean DEBUG_BROADCAST_LIGHT;
    static final boolean DEBUG_CLEANUP;
    static final boolean DEBUG_CONFIGURATION;
    static final boolean DEBUG_CONTAINERS;
    static final boolean DEBUG_FOCUS = false;
    static final boolean DEBUG_FOREGROUND_SERVICE;
    static final boolean DEBUG_IDLE;
    static final boolean DEBUG_IMMERSIVE;
    static final boolean DEBUG_LOCKSCREEN;
    static final boolean DEBUG_LOCKTASK;
    static final boolean DEBUG_LRU;
    static final boolean DEBUG_MU;
    static final boolean DEBUG_NETWORK;
    static final boolean DEBUG_OOM_ADJ;
    static final boolean DEBUG_OOM_ADJ_REASON;
    static final boolean DEBUG_PAUSE;
    static final boolean DEBUG_PERMISSIONS_REVIEW;
    static final boolean DEBUG_POWER;
    static final boolean DEBUG_POWER_QUICK;
    static final boolean DEBUG_PROCESSES;
    static final boolean DEBUG_PROCESS_OBSERVERS;
    static final boolean DEBUG_PROVIDER;
    static final boolean DEBUG_PSS;
    static final boolean DEBUG_RECENTS;
    static final boolean DEBUG_RELEASE;
    static final boolean DEBUG_RESULTS;
    static final boolean DEBUG_SAVED_STATE;
    static final boolean DEBUG_SCREENSHOTS;
    static final boolean DEBUG_SERVICE;
    static final boolean DEBUG_SERVICE_EXECUTING;
    static final boolean DEBUG_STACK;
    static final boolean DEBUG_STATES;
    static final boolean DEBUG_SWITCH;
    static final boolean DEBUG_TASKS;
    static final boolean DEBUG_THUMBNAILS;
    static final boolean DEBUG_TRANSITION;
    static final boolean DEBUG_UID_OBSERVERS;
    static final boolean DEBUG_URI_PERMISSION;
    static final boolean DEBUG_USAGE_STATS;
    static final boolean DEBUG_USER_LEAVING;
    static final boolean DEBUG_VISIBILITY;
    static final boolean DEBUG_VISIBLE_BEHIND;
    static final boolean DEBUG_WHITELISTS;
    static boolean HWFLOW = false;
    static final String POSTFIX_ADD_REMOVE = "";
    static final String POSTFIX_APP = "";
    static final String POSTFIX_BACKUP = "";
    static final String POSTFIX_BROADCAST = "";
    static final String POSTFIX_CLEANUP = "";
    static final String POSTFIX_CONFIGURATION = "";
    static final String POSTFIX_CONTAINERS = "";
    static final String POSTFIX_FOCUS = "";
    static final String POSTFIX_IDLE = "";
    static final String POSTFIX_IMMERSIVE = "";
    static final String POSTFIX_LOCKSCREEN = "";
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
    static final String POSTFIX_SCREENSHOTS = "";
    static final String POSTFIX_SERVICE = "";
    static final String POSTFIX_SERVICE_EXECUTING = "";
    static final String POSTFIX_STACK = "";
    static final String POSTFIX_STATES = "";
    static final String POSTFIX_SWITCH = "";
    static final String POSTFIX_TASKS = "";
    static final String POSTFIX_THUMBNAILS = "";
    static final String POSTFIX_TRANSITION = "";
    static final String POSTFIX_UID_OBSERVERS = "";
    static final String POSTFIX_URI_PERMISSION = "";
    static final String POSTFIX_USER_LEAVING = "";
    static final String POSTFIX_VISIBILITY = "";
    static final String POSTFIX_VISIBLE_BEHIND = "";
    static final String TAG_AM = "ActivityManager";
    static final String TAG_FLOW = "ActivityManager_FLOW";
    static final boolean TAG_WITH_CLASS_NAME = false;

    ActivityManagerDebugConfig() {
    }

    static {
        boolean z = true;
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable("ActivityManager", 4) : false : true;
        HWFLOW = isLoggable;
        isLoggable = !Log.HWLog ? Log.HWModuleLog ? Log.isLoggable("ActivityManager", 3) : false : true;
        DEBUG_ALL = isLoggable;
        if (DEBUG_ALL) {
            isLoggable = true;
        } else {
            isLoggable = false;
        }
        DEBUG_ALL_ACTIVITIES = isLoggable;
        if (DEBUG_ALL_ACTIVITIES) {
            isLoggable = true;
        } else {
            isLoggable = false;
        }
        DEBUG_ADD_REMOVE = isLoggable;
        if (DEBUG_ALL_ACTIVITIES) {
            isLoggable = true;
        } else {
            isLoggable = false;
        }
        DEBUG_APP = isLoggable;
        if (DEBUG_ALL) {
            isLoggable = true;
        } else {
            isLoggable = false;
        }
        DEBUG_BACKGROUND_CHECK = isLoggable;
        if (DEBUG_ALL) {
            isLoggable = true;
        } else {
            isLoggable = false;
        }
        DEBUG_BACKUP = isLoggable;
        if (DEBUG_ALL) {
            isLoggable = true;
        } else {
            isLoggable = false;
        }
        DEBUG_BROADCAST = isLoggable;
        if (DEBUG_BROADCAST) {
            isLoggable = true;
        } else {
            isLoggable = false;
        }
        DEBUG_BROADCAST_BACKGROUND = isLoggable;
        if (DEBUG_BROADCAST) {
            isLoggable = true;
        } else {
            isLoggable = false;
        }
        DEBUG_BROADCAST_LIGHT = isLoggable;
        if (DEBUG_ALL) {
            isLoggable = true;
        } else {
            isLoggable = false;
        }
        DEBUG_CLEANUP = isLoggable;
        if (DEBUG_ALL) {
            isLoggable = true;
        } else {
            isLoggable = false;
        }
        DEBUG_CONFIGURATION = isLoggable;
        if (DEBUG_ALL_ACTIVITIES) {
            isLoggable = true;
        } else {
            isLoggable = false;
        }
        DEBUG_CONTAINERS = isLoggable;
        if (DEBUG_ALL_ACTIVITIES) {
            isLoggable = true;
        } else {
            isLoggable = false;
        }
        DEBUG_IDLE = isLoggable;
        if (DEBUG_ALL) {
            isLoggable = true;
        } else {
            isLoggable = false;
        }
        DEBUG_IMMERSIVE = isLoggable;
        if (DEBUG_ALL) {
            isLoggable = true;
        } else {
            isLoggable = false;
        }
        DEBUG_LOCKSCREEN = isLoggable;
        if (DEBUG_ALL) {
            isLoggable = true;
        } else {
            isLoggable = false;
        }
        DEBUG_LOCKTASK = isLoggable;
        if (DEBUG_ALL) {
            isLoggable = true;
        } else {
            isLoggable = false;
        }
        DEBUG_LRU = isLoggable;
        if (DEBUG_ALL) {
            isLoggable = true;
        } else {
            isLoggable = false;
        }
        DEBUG_MU = isLoggable;
        if (DEBUG_ALL) {
            isLoggable = true;
        } else {
            isLoggable = false;
        }
        DEBUG_NETWORK = isLoggable;
        if (DEBUG_ALL) {
            isLoggable = true;
        } else {
            isLoggable = false;
        }
        DEBUG_OOM_ADJ = isLoggable;
        if (DEBUG_ALL) {
            isLoggable = true;
        } else {
            isLoggable = false;
        }
        DEBUG_OOM_ADJ_REASON = isLoggable;
        if (DEBUG_ALL) {
            isLoggable = true;
        } else {
            isLoggable = false;
        }
        DEBUG_PAUSE = isLoggable;
        if (DEBUG_ALL) {
            isLoggable = true;
        } else {
            isLoggable = false;
        }
        DEBUG_POWER = isLoggable;
        if (DEBUG_POWER) {
            isLoggable = true;
        } else {
            isLoggable = false;
        }
        DEBUG_POWER_QUICK = isLoggable;
        if (DEBUG_ALL) {
            isLoggable = true;
        } else {
            isLoggable = false;
        }
        DEBUG_PROCESS_OBSERVERS = isLoggable;
        if (DEBUG_ALL) {
            isLoggable = true;
        } else {
            isLoggable = false;
        }
        DEBUG_PROCESSES = isLoggable;
        if (DEBUG_ALL) {
            isLoggable = true;
        } else {
            isLoggable = false;
        }
        DEBUG_PROVIDER = isLoggable;
        if (DEBUG_ALL) {
            isLoggable = true;
        } else {
            isLoggable = false;
        }
        DEBUG_PSS = isLoggable;
        if (DEBUG_ALL) {
            isLoggable = true;
        } else {
            isLoggable = false;
        }
        DEBUG_RECENTS = isLoggable;
        if (DEBUG_ALL_ACTIVITIES) {
            isLoggable = true;
        } else {
            isLoggable = false;
        }
        DEBUG_RELEASE = isLoggable;
        if (DEBUG_ALL) {
            isLoggable = true;
        } else {
            isLoggable = false;
        }
        DEBUG_RESULTS = isLoggable;
        if (DEBUG_ALL_ACTIVITIES) {
            isLoggable = true;
        } else {
            isLoggable = false;
        }
        DEBUG_SAVED_STATE = isLoggable;
        if (DEBUG_ALL_ACTIVITIES) {
            isLoggable = true;
        } else {
            isLoggable = false;
        }
        DEBUG_SCREENSHOTS = isLoggable;
        if (DEBUG_ALL) {
            isLoggable = true;
        } else {
            isLoggable = false;
        }
        DEBUG_SERVICE = isLoggable;
        if (DEBUG_ALL) {
            isLoggable = true;
        } else {
            isLoggable = false;
        }
        DEBUG_FOREGROUND_SERVICE = isLoggable;
        if (DEBUG_ALL) {
            isLoggable = true;
        } else {
            isLoggable = false;
        }
        DEBUG_SERVICE_EXECUTING = isLoggable;
        if (DEBUG_ALL) {
            isLoggable = true;
        } else {
            isLoggable = false;
        }
        DEBUG_STACK = isLoggable;
        if (DEBUG_ALL_ACTIVITIES) {
            isLoggable = true;
        } else {
            isLoggable = false;
        }
        DEBUG_STATES = isLoggable;
        if (DEBUG_ALL) {
            isLoggable = true;
        } else {
            isLoggable = false;
        }
        DEBUG_SWITCH = isLoggable;
        if (DEBUG_ALL) {
            isLoggable = true;
        } else {
            isLoggable = false;
        }
        DEBUG_TASKS = isLoggable;
        if (DEBUG_ALL) {
            isLoggable = true;
        } else {
            isLoggable = false;
        }
        DEBUG_THUMBNAILS = isLoggable;
        if (DEBUG_ALL) {
            isLoggable = true;
        } else {
            isLoggable = false;
        }
        DEBUG_TRANSITION = isLoggable;
        if (DEBUG_ALL) {
            isLoggable = true;
        } else {
            isLoggable = false;
        }
        DEBUG_UID_OBSERVERS = isLoggable;
        if (DEBUG_ALL) {
            isLoggable = true;
        } else {
            isLoggable = false;
        }
        DEBUG_URI_PERMISSION = isLoggable;
        if (DEBUG_ALL) {
            isLoggable = true;
        } else {
            isLoggable = false;
        }
        DEBUG_USER_LEAVING = isLoggable;
        if (DEBUG_ALL) {
            isLoggable = true;
        } else {
            isLoggable = false;
        }
        DEBUG_VISIBILITY = isLoggable;
        if (DEBUG_ALL_ACTIVITIES) {
            isLoggable = true;
        } else {
            isLoggable = false;
        }
        DEBUG_VISIBLE_BEHIND = isLoggable;
        if (DEBUG_ALL) {
            isLoggable = true;
        } else {
            isLoggable = false;
        }
        DEBUG_USAGE_STATS = isLoggable;
        if (DEBUG_ALL) {
            isLoggable = true;
        } else {
            isLoggable = false;
        }
        DEBUG_PERMISSIONS_REVIEW = isLoggable;
        if (!DEBUG_ALL) {
            z = false;
        }
        DEBUG_WHITELISTS = z;
    }
}
