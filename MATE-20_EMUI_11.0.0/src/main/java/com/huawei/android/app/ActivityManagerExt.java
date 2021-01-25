package com.huawei.android.app;

import android.app.ActivityManager;
import android.app.IActivityManager;
import android.graphics.Rect;
import android.os.IBinder;
import android.os.RemoteException;
import java.util.List;

public class ActivityManagerExt {
    public static final int PROCESS_STATE_BOUND_FOREGROUND_SERVICE = 6;
    public static final int PROCESS_STATE_HOME = 15;
    public static final int PROCESS_STATE_IMPORTANT_FOREGROUND = 7;
    public static final int PROCESS_STATE_NONEXISTENT = 21;
    public static final int PROCESS_STATE_TOP = 2;
    public static final int PROCESS_STATE_TOP_SLEEPING = 13;
    public static final int PROCESS_STATE_UNKNOWN = -1;

    public static void registerUserSwitchObserver(UserSwitchObserverExt observer, String name) throws RemoteException {
        ActivityManager.getService().registerUserSwitchObserver(observer.getUserSwitchObserver(), name);
    }

    public static List<ActivityManager.RunningTaskInfo> getFilteredTasks(int maxNum, int ignoreActivityType, int ignoreWindowingMode) throws RemoteException {
        return ActivityManager.getService().getFilteredTasks(maxNum, ignoreActivityType, ignoreWindowingMode);
    }

    public static int getWindowMode(ActivityManager.RunningTaskInfo rti) {
        if (rti != null) {
            return rti.windowMode;
        }
        return 0;
    }

    public static Rect getBounds(ActivityManager.RunningTaskInfo rti) {
        if (rti != null) {
            return rti.bounds;
        }
        return null;
    }

    public static int getTaskId(ActivityManager.RunningTaskInfo rti) {
        if (rti != null) {
            return rti.taskId;
        }
        return 0;
    }

    public static boolean getSupportsSplitScreenMultiWindow(ActivityManager.RunningTaskInfo rti) {
        if (rti != null) {
            return rti.supportsSplitScreenMultiWindow;
        }
        return false;
    }

    public static void forceStopPackage(ActivityManager activityManager, String packageName) {
        if (activityManager != null) {
            activityManager.forceStopPackage(packageName);
        }
    }

    public static IBinder getIActivityManagerBinder() {
        IActivityManager am = ActivityManager.getService();
        if (am == null) {
            return null;
        }
        return am.asBinder();
    }
}
