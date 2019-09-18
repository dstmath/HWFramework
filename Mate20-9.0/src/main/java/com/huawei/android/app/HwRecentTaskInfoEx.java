package com.huawei.android.app;

import android.app.HwRecentTaskInfo;
import android.content.ComponentName;
import android.content.Intent;

public class HwRecentTaskInfoEx {
    private HwRecentTaskInfo mRecentTaskInfo = null;

    public HwRecentTaskInfoEx(HwRecentTaskInfo recentTaskInfo) {
        this.mRecentTaskInfo = recentTaskInfo;
    }

    public boolean isEmpty() {
        return this.mRecentTaskInfo == null;
    }

    public CharSequence getDescription() {
        if (this.mRecentTaskInfo != null) {
            return this.mRecentTaskInfo.description;
        }
        return null;
    }

    public Intent getBaseIntent() {
        if (this.mRecentTaskInfo != null) {
            return this.mRecentTaskInfo.baseIntent;
        }
        return null;
    }

    public int getId() {
        if (this.mRecentTaskInfo != null) {
            return this.mRecentTaskInfo.id;
        }
        return -1;
    }

    public ComponentName getRealActivity() {
        if (this.mRecentTaskInfo != null) {
            return this.mRecentTaskInfo.realActivity;
        }
        return null;
    }

    public ComponentName getTopActivity() {
        if (this.mRecentTaskInfo != null) {
            return this.mRecentTaskInfo.topActivity;
        }
        return null;
    }

    public ComponentName getOrigActivity() {
        if (this.mRecentTaskInfo != null) {
            return this.mRecentTaskInfo.origActivity;
        }
        return null;
    }

    public int getWindowState() {
        if (this.mRecentTaskInfo != null) {
            return this.mRecentTaskInfo.windowState;
        }
        return 0;
    }

    public int getPersistentId() {
        if (this.mRecentTaskInfo != null) {
            return this.mRecentTaskInfo.persistentId;
        }
        return -1;
    }

    public int getStackId() {
        if (this.mRecentTaskInfo != null) {
            return this.mRecentTaskInfo.stackId;
        }
        return -1;
    }

    public int getDisplayId() {
        if (this.mRecentTaskInfo != null) {
            return this.mRecentTaskInfo.displayId;
        }
        return -1;
    }
}
