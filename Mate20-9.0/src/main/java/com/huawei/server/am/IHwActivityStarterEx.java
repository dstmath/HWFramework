package com.huawei.server.am;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import com.android.server.am.ActivityRecord;

public interface IHwActivityStarterEx {
    void effectiveIawareToLaunchApp(Intent intent, ActivityInfo activityInfo, String str);

    void handleFreeFormStackIfNeed(ActivityRecord activityRecord);

    boolean isAbleToLaunchInPCCastMode(String str, int i, ActivityRecord activityRecord);

    boolean isAbleToLaunchInVR(Context context, String str);

    boolean isAbleToLaunchVideoActivity(Context context, Intent intent);

    void moveFreeFormToFullScreenStackIfNeed(ActivityRecord activityRecord, boolean z);
}
