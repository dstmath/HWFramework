package com.android.server.wm;

import android.content.pm.ApplicationInfo;
import android.content.res.Configuration;
import android.util.ArraySet;
import java.util.ArrayList;
import java.util.Iterator;

public class WindowProcessControllerEx {
    private WindowProcessController mWindowProcessController;

    public WindowProcessControllerEx() {
    }

    public WindowProcessControllerEx(WindowProcessController windowProcessController) {
        this.mWindowProcessController = windowProcessController;
    }

    public WindowProcessController getWindowProcessController() {
        return this.mWindowProcessController;
    }

    public void setWindowProcessController(WindowProcessController windowProcessController) {
        this.mWindowProcessController = windowProcessController;
    }

    public int getUserId() {
        WindowProcessController windowProcessController = this.mWindowProcessController;
        if (windowProcessController != null) {
            return windowProcessController.mUserId;
        }
        return 0;
    }

    public ArrayList<ActivityRecordEx> getRunningActivitys() {
        WindowProcessController windowProcessController = this.mWindowProcessController;
        if (windowProcessController == null) {
            return null;
        }
        ArrayList<ActivityRecord> list = windowProcessController.mActivities;
        if (list.size() == 0) {
            return null;
        }
        ArrayList<ActivityRecordEx> activityRecordExes = new ArrayList<>();
        Iterator<ActivityRecord> it = list.iterator();
        while (it.hasNext()) {
            ActivityRecord activityRecord = it.next();
            if (instanceOfHwActivityRecord(activityRecord)) {
                ActivityRecordEx activityRecordEx = new ActivityRecordEx();
                activityRecordEx.setActivityRecord(activityRecord);
                activityRecordExes.add(activityRecordEx);
            }
        }
        return activityRecordExes;
    }

    public boolean instanceOfHwActivityRecord(ActivityRecord activityRecord) {
        return activityRecord != null && (activityRecord instanceof ActivityRecordBridge);
    }

    public ApplicationInfo getInfo() {
        WindowProcessController windowProcessController = this.mWindowProcessController;
        if (windowProcessController != null) {
            return windowProcessController.mInfo;
        }
        return null;
    }

    public void onConfigurationChanged(Configuration newGlobalConfig) {
        WindowProcessController windowProcessController = this.mWindowProcessController;
        if (windowProcessController != null) {
            windowProcessController.onConfigurationChanged(newGlobalConfig);
        }
    }

    public boolean isWindowProcessControllerNull() {
        return this.mWindowProcessController == null;
    }

    public String getName() {
        return this.mWindowProcessController.mName;
    }

    public ArraySet<String> getPkgList() {
        return this.mWindowProcessController.mPkgList;
    }

    public int getPid() {
        return this.mWindowProcessController.getPid();
    }

    public int getUid() {
        return this.mWindowProcessController.mUid;
    }
}
