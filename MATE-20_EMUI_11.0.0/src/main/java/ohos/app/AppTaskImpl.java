package ohos.app;

import android.app.ActivityManager;
import ohos.aafwk.ability.IApplicationTask;
import ohos.appexecfwk.utils.AppLog;

public class AppTaskImpl implements IApplicationTask {
    ActivityManager.AppTask appTask;

    public AppTaskImpl(ActivityManager.AppTask appTask2) {
        this.appTask = appTask2;
    }

    @Override // ohos.aafwk.ability.IApplicationTask
    public void finishTask() {
        ActivityManager.AppTask appTask2 = this.appTask;
        if (appTask2 == null) {
            AppLog.i("construct AppTaskImpl failed", new Object[0]);
        } else {
            appTask2.finishAndRemoveTask();
        }
    }
}
