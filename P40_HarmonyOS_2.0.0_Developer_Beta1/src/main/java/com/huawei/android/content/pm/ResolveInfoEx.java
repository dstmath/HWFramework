package com.huawei.android.content.pm;

import android.content.pm.ActivityInfo;
import android.content.pm.ActivityInfoEx;
import android.content.pm.ComponentInfo;
import android.content.pm.ResolveInfo;

public class ResolveInfoEx {
    private ResolveInfo mResolveInfo;

    public ResolveInfo getResolveInfo() {
        return this.mResolveInfo;
    }

    public void setResolveInfo(ResolveInfo resolveInfo) {
        this.mResolveInfo = resolveInfo;
    }

    public ActivityInfoEx getActivityInfo() {
        ActivityInfo activityInfo = this.mResolveInfo.activityInfo;
        ActivityInfoEx activityInfoEx = new ActivityInfoEx();
        activityInfoEx.setActivityInfo(activityInfo);
        return activityInfoEx;
    }

    public int getPriority() {
        return this.mResolveInfo.priority;
    }

    public static ComponentInfo getComponentInfo(ResolveInfo receiver) {
        if (receiver == null) {
            return null;
        }
        return receiver.getComponentInfo();
    }

    public static String getComponentName(ResolveInfo receiver) throws IllegalStateException {
        if (receiver == null || receiver.getComponentInfo() == null) {
            return null;
        }
        return receiver.getComponentInfo().getComponentName().getClassName();
    }
}
