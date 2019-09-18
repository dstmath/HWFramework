package com.android.server.wm;

import android.util.Log;

public class WindowManagerDebugConfig {
    static final boolean DEBUG = false;
    static final boolean DEBUG_ADD_REMOVE = true;
    static boolean DEBUG_ALL = Log.HWLog;
    static final boolean DEBUG_ANIM = false;
    static boolean DEBUG_APP_ORIENTATION = false;
    static boolean DEBUG_APP_TRANSITIONS = false;
    static final boolean DEBUG_BOOT = (DEBUG_ALL);
    static boolean DEBUG_CONFIGURATION = false;
    static final boolean DEBUG_DIM_LAYER = false;
    static boolean DEBUG_DISPLAY = false;
    static final boolean DEBUG_DRAG = false;
    static boolean DEBUG_FOCUS = false;
    static final boolean DEBUG_FOCUS_LIGHT = (DEBUG_FOCUS);
    static boolean DEBUG_INPUT = false;
    static boolean DEBUG_INPUT_METHOD = false;
    static boolean DEBUG_KEEP_SCREEN_ON = false;
    static final boolean DEBUG_KEYGUARD = false;
    static boolean DEBUG_LAYERS = false;
    static boolean DEBUG_LAYOUT = false;
    static final boolean DEBUG_LAYOUT_REPEATS = false;
    static boolean DEBUG_ORIENTATION = false;
    static final boolean DEBUG_ORIENTATION_FREEZING = true;
    static final boolean DEBUG_POWER = false;
    static final boolean DEBUG_RECENTS_ANIMATIONS = false;
    static final boolean DEBUG_REMOTE_ANIMATIONS;
    static final boolean DEBUG_RESIZE = false;
    static final boolean DEBUG_SCREENSHOT = false;
    static boolean DEBUG_SCREEN_ON = false;
    static final boolean DEBUG_STACK = false;
    static boolean DEBUG_STARTING_WINDOW = (DEBUG_STARTING_WINDOW_VERBOSE);
    static boolean DEBUG_STARTING_WINDOW_VERBOSE = false;
    static final boolean DEBUG_TASK_MOVEMENT = false;
    static final boolean DEBUG_TASK_POSITIONING = false;
    static final boolean DEBUG_TOKEN_MOVEMENT = false;
    static final boolean DEBUG_UNKNOWN_APP_VISIBILITY = false;
    static boolean DEBUG_VISIBILITY = false;
    static boolean DEBUG_WALLPAPER = false;
    static boolean DEBUG_WALLPAPER_LIGHT = DEBUG_WALLPAPER;
    static final boolean DEBUG_WINDOW_CROP = false;
    static final boolean DEBUG_WINDOW_MOVEMENT = false;
    static final boolean DEBUG_WINDOW_TRACE = false;
    static boolean HWFLOW = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG_WM, 4)));
    static final String POSTFIX_VISIBILITY = "_visibility";
    static final boolean SHOW_LIGHT_TRANSACTIONS = false;
    static final boolean SHOW_STACK_CRAWLS = false;
    static final boolean SHOW_SURFACE_ALLOC = false;
    static final boolean SHOW_TRANSACTIONS = false;
    static final boolean SHOW_VERBOSE_TRANSACTIONS = false;
    static final String TAG_KEEP_SCREEN_ON = "DebugKeepScreenOn";
    static final boolean TAG_WITH_CLASS_NAME = false;
    static final String TAG_WM = "WindowManager";

    static {
        boolean z = true;
        if (!DEBUG_APP_TRANSITIONS) {
            z = false;
        }
        DEBUG_REMOTE_ANIMATIONS = z;
    }
}
