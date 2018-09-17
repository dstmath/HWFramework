package com.android.server.wm;

import android.util.Log;

public class WindowManagerDebugConfig {
    static final boolean DEBUG = false;
    static final boolean DEBUG_ADD_REMOVE = true;
    static boolean DEBUG_ALL = Log.HWLog;
    static final boolean DEBUG_ANIM = false;
    static final boolean DEBUG_APP_ORIENTATION = false;
    static final boolean DEBUG_APP_TRANSITIONS = false;
    static final boolean DEBUG_BOOT;
    static final boolean DEBUG_CONFIGURATION = false;
    static final boolean DEBUG_DIM_LAYER = false;
    static final boolean DEBUG_DISPLAY = false;
    static final boolean DEBUG_DRAG = false;
    static final boolean DEBUG_FOCUS = false;
    static final boolean DEBUG_FOCUS_LIGHT = false;
    static final boolean DEBUG_INPUT = false;
    static final boolean DEBUG_INPUT_METHOD = false;
    static final boolean DEBUG_KEEP_SCREEN_ON = false;
    static final boolean DEBUG_KEYGUARD = false;
    static final boolean DEBUG_LAYERS = false;
    static final boolean DEBUG_LAYOUT = false;
    static final boolean DEBUG_LAYOUT_REPEATS = false;
    static final boolean DEBUG_ORIENTATION = false;
    static final boolean DEBUG_ORIENTATION_FREEZING = true;
    static final boolean DEBUG_POWER = false;
    static final boolean DEBUG_RESIZE = false;
    static final boolean DEBUG_SCREENSHOT = false;
    static final boolean DEBUG_SCREEN_ON = false;
    static final boolean DEBUG_STACK = false;
    static final boolean DEBUG_STARTING_WINDOW = false;
    static final boolean DEBUG_STARTING_WINDOW_VERBOSE = false;
    static final boolean DEBUG_SURFACE_TRACE = false;
    static final boolean DEBUG_TASK_MOVEMENT = false;
    static final boolean DEBUG_TASK_POSITIONING = false;
    static final boolean DEBUG_TOKEN_MOVEMENT = false;
    static final boolean DEBUG_UNKNOWN_APP_VISIBILITY = false;
    static final boolean DEBUG_VISIBILITY = false;
    static final boolean DEBUG_WALLPAPER = false;
    static final boolean DEBUG_WALLPAPER_LIGHT = false;
    static final boolean DEBUG_WINDOW_CROP = false;
    static final boolean DEBUG_WINDOW_MOVEMENT = false;
    static final boolean DEBUG_WINDOW_TRACE = false;
    static final boolean SHOW_LIGHT_TRANSACTIONS = false;
    static final boolean SHOW_STACK_CRAWLS = false;
    static final boolean SHOW_SURFACE_ALLOC = false;
    static final boolean SHOW_TRANSACTIONS = false;
    static final boolean SHOW_VERBOSE_TRANSACTIONS = false;
    static final String TAG_KEEP_SCREEN_ON = "DebugKeepScreenOn";
    static final boolean TAG_WITH_CLASS_NAME = false;
    static final String TAG_WM = "WindowManager";

    static {
        boolean z;
        if (DEBUG_ALL) {
            z = true;
        } else {
            z = false;
        }
        DEBUG_BOOT = z;
    }
}
