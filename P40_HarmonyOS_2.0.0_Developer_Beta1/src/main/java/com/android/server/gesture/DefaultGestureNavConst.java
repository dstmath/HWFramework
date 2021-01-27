package com.android.server.gesture;

import android.content.Context;
import android.os.SystemProperties;
import com.huawei.util.LogEx;

public class DefaultGestureNavConst {
    public static final int ADD_HW_SPLIT_SCREEN = 0;
    public static final int CHECK_AFT_TIMEOUT = 2500;
    public static final boolean DEBUG = ("1".equals(SystemProperties.get("ro.debuggable", "0")) || SystemProperties.getInt("ro.logsystem.usertype", 1) == 3 || SystemProperties.getInt("ro.logsystem.usertype", 1) == 5);
    public static final boolean DEBUG_ALL = (DEBUG && LogEx.getHwLog());
    public static final String DEFAULT_LAUNCHER_PACKAGE = "com.huawei.android.launcher";
    public static final String DEFAULT_QUICKSTEP_CLASS = "com.android.quickstep.RecentsActivity";
    public static final int FOLD_STATE_CHANGED = 2;
    public static final int GESTURE_GO_HOME_MIN_DISTANCE_THRESHOLD = 180;
    public static final int GESTURE_GO_HOME_TIMEOUT = 500;
    public static final int ID_GESTURE_NAV_INVALID = 0;
    public static final int ID_GESTURE_NAV_LEFT = 1;
    public static final int ID_GESTURE_NAV_RIGHT = 2;
    public static final boolean IS_SUPPORT_FULL_BACK = (!SystemProperties.getBoolean("hw_mc.launcher.disable_back_hotzone_fullscreen", false));
    public static final String KEY_SECURE_GESTURE_NAVIGATION = "secure_gesture_navigation";
    public static final int REMOVE_HW_SPLIT_SCREEN = 1;
    public static final boolean SUPPORT_DOCK_TRIGGER = SystemProperties.getBoolean("ro.config.hw_multiwindow_optimization", false);
    public static final String TAG_GESTURE_OPS = "GestureNav_OPS";
    public static final String TAG_GESTURE_QS = "GestureNav_QS";

    public static boolean isGestureNavEnabled(Context context, int userHandle) {
        return false;
    }

    public boolean getGestureNavEnabled(Context context, int userHandle) {
        return false;
    }
}
