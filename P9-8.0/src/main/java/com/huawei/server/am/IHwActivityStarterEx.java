package com.huawei.server.am;

import android.content.Intent;
import android.content.pm.ActivityInfo;

public interface IHwActivityStarterEx {
    void effectiveIawareToLaunchApp(Intent intent, ActivityInfo activityInfo, String str);
}
