package com.huawei.android.app;

import android.app.Activity;
import android.app.HwActivitySplitterImpl;
import android.app.IHwActivitySplitterImpl;
import android.common.HwFrameworkFactory;
import android.content.Context;
import android.content.Intent;

public class IHwActivitySplitterImplEx {
    IHwActivitySplitterImpl mIHwActivitySplitterImpl;

    public IHwActivitySplitterImplEx(Activity currentActivity, boolean isBase) {
        this.mIHwActivitySplitterImpl = HwFrameworkFactory.getHwActivitySplitterImpl(currentActivity, isBase);
    }

    public boolean isSplitMode() {
        IHwActivitySplitterImpl iHwActivitySplitterImpl = this.mIHwActivitySplitterImpl;
        if (iHwActivitySplitterImpl == null) {
            return false;
        }
        return iHwActivitySplitterImpl.isSplitMode();
    }

    public static void setAsJumpActivity(Intent intent) {
        HwActivitySplitterImpl.setAsJumpActivity(intent);
    }

    public boolean isReachedSplitSize(Context context) {
        if (this.mIHwActivitySplitterImpl == null || !PackageManagerEx.hasSystemSignaturePermission(context)) {
            return false;
        }
        return this.mIHwActivitySplitterImpl.reachSplitSize();
    }

    public void finishAllSubActivities(Context context) {
        if (this.mIHwActivitySplitterImpl != null && PackageManagerEx.hasSystemSignaturePermission(context)) {
            this.mIHwActivitySplitterImpl.finishAllSubActivities();
        }
    }

    public void reduceIndexView(Context context) {
        if (this.mIHwActivitySplitterImpl != null && PackageManagerEx.hasSystemSignaturePermission(context)) {
            this.mIHwActivitySplitterImpl.reduceIndexView();
        }
    }

    public void cancelSplit(Intent intent, Context context) {
        if (this.mIHwActivitySplitterImpl != null && PackageManagerEx.hasSystemSignaturePermission(context)) {
            this.mIHwActivitySplitterImpl.cancelSplit(intent);
        }
    }

    public void setTargetIntent(Intent intent, Context context) {
        if (this.mIHwActivitySplitterImpl != null && PackageManagerEx.hasSystemSignaturePermission(context)) {
            this.mIHwActivitySplitterImpl.setTargetIntent(intent);
        }
    }
}
