package ohos.app;

import android.app.ActivityManager;
import ohos.aafwk.ability.IApplicationMission;
import ohos.appexecfwk.utils.AppLog;

public class AppTaskImpl implements IApplicationMission {
    ActivityManager.AppTask appTask;

    public AppTaskImpl(ActivityManager.AppTask appTask2) {
        this.appTask = appTask2;
    }

    @Override // ohos.aafwk.ability.IApplicationMission
    public void finishMission() {
        ActivityManager.AppTask appTask2 = this.appTask;
        if (appTask2 == null) {
            AppLog.e("appTask is null", new Object[0]);
        } else {
            appTask2.finishAndRemoveTask();
        }
    }
}
