package com.android.server.wifi.wifinearfind;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.SystemProperties;
import com.android.server.wm.HwWindowManagerServiceEx;
import java.util.List;
import java.util.Objects;

public class HwWifiNearFindUtils {
    public static final int BOOL_FALSE_TO_INT = 0;
    public static final int BOOL_TRUE_TO_INT = 1;
    private static final int CURRENT_TASK_NUMBER = 0;
    private static final String DEFAULT_CHIP_TYPE = "unknown";
    private static final int DEFAULT_RANGE_LIN = 0;
    public static final String HILINK_APK = "com.huawei.hilink.framework";
    public static final String HILINK_TOAST_ACTIVITY = "com.huawei.hilink.framework.app.activity.SoftApDeviceDiscoveryDialogActivity";
    private static final String HISI_CHIP_1105 = "1105";
    public static final String HOME_APK = "com.huawei.android.launcher";
    public static final int HWSHARE_SWITCH_OFF = -1;
    public static final int HWSHARE_SWITCH_ON = 1;
    public static final String KEY_HWSHARE_INIT_SETTINGS = "instantshare_state";
    public static final int MSG_AIRLINK_BIND_FAILED = 12;
    public static final int MSG_AUDIO_OFF = 6;
    public static final int MSG_AUDIO_ON = 7;
    public static final int MSG_BASE = 0;
    public static final int MSG_CHECK_WIFI_SCAN_STATE = 17;
    public static final int MSG_HICAR_STARTED = 8;
    public static final int MSG_HICAR_STOPPED = 9;
    public static final int MSG_HOME_APP_CHANGED = 2;
    public static final int MSG_HWSHARE_SWITCH_STATE = 15;
    public static final int MSG_INITIALIZE = 1;
    public static final int MSG_LONG_DISTANCE_SCAN = 16;
    public static final int MSG_P2P_CONNECTED = 5;
    public static final int MSG_SCREEN_OFF = 4;
    public static final int MSG_SCREEN_ON = 3;
    public static final int MSG_SCREEN_UNLOCK = 13;
    public static final int MSG_START_PERIOD_WIFI_SCAN = 10;
    public static final int MSG_STOP_WIFI_SCAN = 11;
    public static final int MSG_WIFI_SWITCH_STATE = 14;
    private static final String PROP_CHIP_TYPE = "ro.connectivity.sub_chiptype";
    public static final String PROP_WIFI_NEAR_FIND_LIN = "hw_sc.wifi_near_find_lin";
    private static final int RUNNING_TASK_NUMBER = 1;
    public static final int WIFI_SWITCH_OFF = 0;
    public static final int WIFI_SWITCH_ON = 1;

    private HwWifiNearFindUtils() {
    }

    public static int booleanToInt(boolean value) {
        return value ? 1 : 0;
    }

    public static boolean isInHomeLauncher(Context context) {
        List<ActivityManager.RunningTaskInfo> tasks;
        ActivityManager.RunningTaskInfo runningTask;
        if (context == null || (tasks = ((ActivityManager) context.getSystemService("activity")).getRunningTasks(1)) == null || tasks.isEmpty() || (runningTask = tasks.get(0)) == null || runningTask.topActivity == null || !Objects.equals(runningTask.topActivity.getPackageName(), HOME_APK)) {
            return false;
        }
        return true;
    }

    public static boolean isExistFloatingWindows(HwWindowManagerServiceEx hwWindowManagerServiceEx) {
        List<Bundle> result;
        if (hwWindowManagerServiceEx == null || (result = hwWindowManagerServiceEx.getVisibleWindows(24)) == null || result.size() == 0) {
            return false;
        }
        return true;
    }

    public static boolean isHiLinkInstalled(Context context) {
        PackageManager packageManager;
        if (context == null || (packageManager = context.getPackageManager()) == null) {
            return false;
        }
        try {
            if (packageManager.getPackageInfo(HILINK_APK, 0) != null) {
                return true;
            }
            return false;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static boolean isHi1105Chip() {
        String chipset = SystemProperties.get(PROP_CHIP_TYPE, DEFAULT_CHIP_TYPE);
        if (chipset == null || !chipset.contains(HISI_CHIP_1105)) {
            return false;
        }
        return true;
    }

    public static boolean isWifiNearFindSwitchOn() {
        return true;
    }

    public static int getWifiNearFindLin() {
        return SystemProperties.getInt(PROP_WIFI_NEAR_FIND_LIN, 0);
    }
}
