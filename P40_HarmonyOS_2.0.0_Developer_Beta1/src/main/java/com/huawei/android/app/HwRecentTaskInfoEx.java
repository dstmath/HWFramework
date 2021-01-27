package com.huawei.android.app;

import android.app.HwRecentTaskInfo;
import android.content.ComponentName;
import android.content.Intent;

public class HwRecentTaskInfoEx {
    private static final int DEFAULT_INVALID_ID = -1;
    private HwRecentTaskInfo mRecentTaskInfo = null;

    public HwRecentTaskInfoEx(HwRecentTaskInfo recentTaskInfo) {
        this.mRecentTaskInfo = recentTaskInfo;
    }

    public boolean isEmpty() {
        return this.mRecentTaskInfo == null;
    }

    public CharSequence getDescription() {
        HwRecentTaskInfo hwRecentTaskInfo = this.mRecentTaskInfo;
        if (hwRecentTaskInfo != null) {
            return hwRecentTaskInfo.description;
        }
        return null;
    }

    public Intent getBaseIntent() {
        HwRecentTaskInfo hwRecentTaskInfo = this.mRecentTaskInfo;
        if (hwRecentTaskInfo != null) {
            return hwRecentTaskInfo.baseIntent;
        }
        return null;
    }

    public int getId() {
        HwRecentTaskInfo hwRecentTaskInfo = this.mRecentTaskInfo;
        if (hwRecentTaskInfo != null) {
            return hwRecentTaskInfo.id;
        }
        return -1;
    }

    public ComponentName getRealActivity() {
        HwRecentTaskInfo hwRecentTaskInfo = this.mRecentTaskInfo;
        if (hwRecentTaskInfo != null) {
            return hwRecentTaskInfo.realActivity;
        }
        return null;
    }

    public ComponentName getTopActivity() {
        HwRecentTaskInfo hwRecentTaskInfo = this.mRecentTaskInfo;
        if (hwRecentTaskInfo != null) {
            return hwRecentTaskInfo.topActivity;
        }
        return null;
    }

    public ComponentName getOrigActivity() {
        HwRecentTaskInfo hwRecentTaskInfo = this.mRecentTaskInfo;
        if (hwRecentTaskInfo != null) {
            return hwRecentTaskInfo.origActivity;
        }
        return null;
    }

    public int getWindowState() {
        HwRecentTaskInfo hwRecentTaskInfo = this.mRecentTaskInfo;
        if (hwRecentTaskInfo != null) {
            return hwRecentTaskInfo.windowState;
        }
        return 0;
    }

    public int getPersistentId() {
        HwRecentTaskInfo hwRecentTaskInfo = this.mRecentTaskInfo;
        if (hwRecentTaskInfo != null) {
            return hwRecentTaskInfo.persistentId;
        }
        return -1;
    }

    public int getStackId() {
        HwRecentTaskInfo hwRecentTaskInfo = this.mRecentTaskInfo;
        if (hwRecentTaskInfo != null) {
            return hwRecentTaskInfo.stackId;
        }
        return -1;
    }

    public int getDisplayId() {
        HwRecentTaskInfo hwRecentTaskInfo = this.mRecentTaskInfo;
        if (hwRecentTaskInfo != null) {
            return hwRecentTaskInfo.displayId;
        }
        return -1;
    }
}
