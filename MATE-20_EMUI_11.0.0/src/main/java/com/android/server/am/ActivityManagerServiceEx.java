package com.android.server.am;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.graphics.Rect;
import android.os.IBinder;
import android.os.ServiceManager;
import com.android.server.wm.ActivityTaskManagerServiceEx;
import com.android.server.wm.WindowManagerServiceEx;
import java.util.List;

public class ActivityManagerServiceEx {
    private ActivityManagerService mAms;

    public ActivityManagerService getActivityManagerService() {
        return this.mAms;
    }

    public void setActivityManagerService(ActivityManagerService activityManagerService) {
        this.mAms = activityManagerService;
    }

    public HwActivityManagerService switchToHwActivityManagerService() {
        HwActivityManagerService hwActivityManagerService = this.mAms;
        if (hwActivityManagerService == null || !(hwActivityManagerService instanceof HwActivityManagerService)) {
            return null;
        }
        return hwActivityManagerService;
    }

    public void takeTaskSnapshot(IBinder binder, boolean alwaysTake) {
        ActivityManagerService activityManagerService = this.mAms;
        if (activityManagerService != null) {
            activityManagerService.mWindowManager.getWindowManagerServiceEx().takeTaskSnapshot(binder, alwaysTake);
        }
    }

    public ActivityTaskManagerServiceEx getActivityTaskManagerEx() {
        if (this.mAms == null) {
            return null;
        }
        ActivityTaskManagerServiceEx activityTaskManagerServiceEx = new ActivityTaskManagerServiceEx();
        activityTaskManagerServiceEx.setActivityTaskManagerService(this.mAms.mActivityTaskManager);
        return activityTaskManagerServiceEx;
    }

    public WindowManagerServiceEx getWindowManagerServiceEx() {
        if (this.mAms == null) {
            return null;
        }
        WindowManagerServiceEx windowManagerServiceEx = new WindowManagerServiceEx();
        windowManagerServiceEx.setWindowManagerService(this.mAms.mWindowManager);
        return windowManagerServiceEx;
    }

    public void resizeStack(int stackId, Rect destBounds, boolean allowResizeInDockedMode, boolean preserveWindows, boolean animate, int animationDuration) {
        ActivityManagerService activityManagerService = this.mAms;
        if (activityManagerService != null) {
            activityManagerService.resizeStack(stackId, destBounds, allowResizeInDockedMode, preserveWindows, animate, animationDuration);
        }
    }

    public List<ActivityManager.RunningAppProcessInfo> getRunningAppProcesses() {
        return this.mAms.getRunningAppProcesses();
    }

    public void forceStopPackage(String pkgName, int userId) {
        this.mAms.forceStopPackage(pkgName, userId);
    }

    public boolean removeTask(int taskId) {
        return this.mAms.removeTask(taskId);
    }

    public int getCurrentUserId() {
        return this.mAms.getCurrentUser().id;
    }

    public List<ActivityManager.RecentTaskInfo> getRecentTasks(int maxNum, int flags, int userId) {
        ActivityManagerService activityManagerService = this.mAms;
        if (activityManagerService != null) {
            return activityManagerService.getRecentTasks(maxNum, flags, userId).getList();
        }
        return null;
    }

    public static boolean serviceIsRunning(ComponentName cmpName, int user) {
        return ((HwActivityManagerService) ServiceManager.getService("activity")).serviceIsRunning(cmpName, user);
    }
}
