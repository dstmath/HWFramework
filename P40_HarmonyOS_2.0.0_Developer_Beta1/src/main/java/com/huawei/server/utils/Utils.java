package com.huawei.server.utils;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import com.android.server.wm.ActivityDisplayEx;
import com.android.server.wm.ActivityRecordEx;
import com.android.server.wm.TaskRecordEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.util.SlogEx;
import java.util.Locale;

public class Utils {
    public static final String KEY_LAUNCH_FROM_MAIN = "launchFromMain";
    private static final boolean MAGICWIN_LOG_SWITCH = SystemPropertiesEx.getBoolean("ro.config.hw_magicwindow_log", false);
    public static final int MSG_ADJWIN_TOFULL_WHEN_RESUME = 14;
    public static final int MSG_ALARM_UPDATE_CONFIG = 4;
    public static final int MSG_BACK_TO_FOLD_FULL_DISPLAY = 11;
    public static final int MSG_FORCE_STOP_PACKAGE = 6;
    public static final int MSG_GET_WALLPAPER = 1;
    public static final int MSG_LOCAL_CHANGE = 18;
    public static final int MSG_MOVE_LATEST_TO_TOP = 10;
    public static final int MSG_ONE_HOP_APP_SWITCH_CHANGE = 22;
    public static final int MSG_ONE_HOP_CONFIG_FILES = 21;
    public static final int MSG_ONE_HOP_CONN_STATE = 19;
    public static final int MSG_ONE_HOP_SVC_STATE = 20;
    public static final int MSG_REPORT_USAGE_STATISTICS = 9;
    public static final int MSG_SCHEDULE_IDLE = 24;
    public static final int MSG_SCREEN_OFF = 8;
    public static final int MSG_SERVICE_INIT = 17;
    public static final int MSG_SET_BOUNDS = 0;
    public static final int MSG_SET_CAMERA_FULLSCREEN = 23;
    public static final int MSG_SET_MULTIWIN_CAMERA_PROP = 3;
    public static final int MSG_START_RELATE_ACT = 13;
    public static final int MSG_UPDATE_APP_CONFIG = 7;
    public static final int MSG_UPDATE_BOUND_FOR_DENSITY_CHANGE = 16;
    public static final int MSG_UPDATE_MAGIC_WINDOW_CONFIG = 2;
    public static final int MSG_UPDATE_STACK_VISIBILITY = 12;
    public static final int MSG_USER_SWITCH = 5;
    public static final int MSG_WRITE_SETTING_XML = 15;
    public static final int POLICY_DUMPMGC = 100;
    public static final int POLICY_GET_APP_INFO = 110;
    public static final int REASON_ENTER_FULLSCREEN = 1;
    public static final int REASON_EXIT_FULLSCREEN = 2;
    public static final int REASON_STACK_EMPTY_SWITCH = 3;
    public static final String SETTINGS_DEVICETYPE_FOR_OSD_CAMERA = "devicetype_for_osd_camera";
    public static final String SYS_OSD_CANERA = "sys.osd_for_camera";
    public static final String TAG_SETTING = "HWMW_setting";

    public static void dbg(String tag, String msg) {
        if (MAGICWIN_LOG_SWITCH) {
            SlogEx.d(tag, msg);
        }
    }

    public static String getRealPkgName(ActivityRecordEx ar) {
        if (ar == null || ar.getTaskRecordEx() == null || ar.getTaskRecordEx().getRealActivity() == null) {
            return null;
        }
        return ar.getTaskRecordEx().getRealActivity().getPackageName();
    }

    public static String getClassName(ActivityRecordEx ar) {
        if (ar == null || ar.getIntent() == null || ar.getIntent().getComponent() == null) {
            return null;
        }
        return ar.getIntent().getComponent().getClassName();
    }

    public static String getPackageName(ActivityRecordEx ar) {
        if (ar == null || ar.getIntent() == null || ar.getIntent().getComponent() == null) {
            return null;
        }
        return ar.getIntent().getComponent().getPackageName();
    }

    public static String getPackageName(Intent intent) {
        if (intent == null || intent.getComponent() == null) {
            return null;
        }
        return intent.getComponent().getPackageName();
    }

    public static String getPackageName(ActivityDisplayEx activityDisplay) {
        if (activityDisplay.getTopStackEx() == null || activityDisplay.getTopStackEx().topTask() == null) {
            return null;
        }
        TaskRecordEx taskRecord = activityDisplay.getTopStackEx().topTask();
        if (taskRecord.getRealActivity() == null || taskRecord.getRealActivity().getPackageName() == null) {
            return null;
        }
        return taskRecord.getRealActivity().getPackageName();
    }

    public static boolean isNightMode(Context context) {
        if (context == null || context.getResources() == null || context.getResources().getConfiguration() == null || (context.getResources().getConfiguration().uiMode & 48) != 32) {
            return false;
        }
        return true;
    }

    public static boolean isLayoutRtl() {
        return TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) == 1;
    }

    public static int strToInt(String value, int defaultValue) {
        if (isEmpty(value)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            SlogEx.e(TAG_SETTING, "parse string to int error");
            return defaultValue;
        }
    }

    private static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }
}
