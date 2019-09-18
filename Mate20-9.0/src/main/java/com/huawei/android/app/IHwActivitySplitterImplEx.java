package com.huawei.android.app;

import android.app.Activity;
import android.app.HwActivitySplitterImpl;
import android.app.IHwActivitySplitterImpl;
import android.common.HwFrameworkFactory;
import android.content.Intent;

public class IHwActivitySplitterImplEx {
    IHwActivitySplitterImpl mIHwActivitySplitterImpl;

    public IHwActivitySplitterImplEx(Activity currentActivity, boolean isBase) {
        this.mIHwActivitySplitterImpl = HwFrameworkFactory.getHwActivitySplitterImpl(currentActivity, isBase);
    }

    public boolean isSplitMode() {
        if (this.mIHwActivitySplitterImpl == null) {
            return false;
        }
        return this.mIHwActivitySplitterImpl.isSplitMode();
    }

    public static void setAsJumpActivity(Intent intent) {
        HwActivitySplitterImpl.setAsJumpActivity(intent);
    }
}
