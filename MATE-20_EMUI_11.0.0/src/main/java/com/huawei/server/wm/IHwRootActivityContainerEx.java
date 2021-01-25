package com.huawei.server.wm;

import android.app.ActivityOptions;
import com.android.server.wm.ActivityRecord;
import com.android.server.wm.ActivityStack;

public interface IHwRootActivityContainerEx {
    void checkStartAppLockActivity();

    boolean checkWindowModeForAppLock(ActivityRecord activityRecord, ActivityRecord activityRecord2);

    boolean isAppInLockList(String str, int i);

    boolean resumeAppLockActivityIfNeeded(ActivityStack activityStack, ActivityOptions activityOptions);
}
