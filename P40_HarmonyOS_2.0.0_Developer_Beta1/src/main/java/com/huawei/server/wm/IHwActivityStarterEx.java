package com.huawei.server.wm;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import com.android.server.wm.ActivityRecord;
import com.android.server.wm.ActivityStack;
import com.android.server.wm.ActivityStackSupervisor;

public interface IHwActivityStarterEx {
    boolean checkActivityStartForPCMode(ActivityOptions activityOptions, ActivityRecord activityRecord, ActivityStack activityStack);

    Bundle checkActivityStartedOnDisplay(ActivityRecord activityRecord, int i, ActivityOptions activityOptions, ActivityRecord activityRecord2);

    void effectiveIawareToLaunchApp(Intent intent, ActivityInfo activityInfo, String str);

    void handleFreeFormStackIfNeed(ActivityRecord activityRecord);

    boolean isAbleToLaunchInPCCastMode(String str, int i, ActivityRecord activityRecord);

    boolean isAbleToLaunchInVr(Context context, Intent intent, String str, ActivityInfo activityInfo);

    boolean isAbleToLaunchVideoActivity(Context context, Intent intent);

    boolean isAppDisabledByMdmNoComponent(ActivityInfo activityInfo, Intent intent, String str, ActivityStackSupervisor activityStackSupervisor);

    void moveFreeFormToFullScreenStackIfNeed(ActivityRecord activityRecord, boolean z);

    void preloadApplication(ApplicationInfo applicationInfo, String str);
}
