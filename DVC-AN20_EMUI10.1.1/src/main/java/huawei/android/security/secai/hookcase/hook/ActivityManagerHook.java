package huawei.android.security.secai.hookcase.hook;

import android.app.ActivityManager;
import android.app.Application;
import android.util.Log;
import huawei.android.security.HwBehaviorCollectManagerImpl;
import huawei.android.security.secai.hookcase.annotations.BackupMethod;
import huawei.android.security.secai.hookcase.annotations.HookMethod;
import huawei.android.security.secai.hookcase.utils.BehaviorIdCast;

class ActivityManagerHook {
    private static final String TAG = ActivityManagerHook.class.getSimpleName();

    ActivityManagerHook() {
    }

    @HookMethod(name = "forceStopPackageAsUser", params = {String.class, int.class}, targetClass = ActivityManager.class)
    static void forceStopPackageAsUserHook(Object obj, String packageName, int userId) {
        String currentPackageName = Application.getProcessName();
        if (currentPackageName == null) {
            forceStopPackageAsUserBackup(obj, packageName, userId);
            return;
        }
        if (currentPackageName.equals(packageName)) {
            HwBehaviorCollectManagerImpl.getSubClassDefault().sendBehavior(BehaviorIdCast.BehaviorId.ACTIVITYMANAGER_FORCESTOPPACKAGEASUSER.getValue());
        } else {
            HwBehaviorCollectManagerImpl.getSubClassDefault().sendBehavior(BehaviorIdCast.BehaviorId.ACTIVITYMANAGER_FORCESTOPPACKAGEASUSER.getValue() + 1);
        }
        Log.i(TAG, "Call System Hook Method: ActivityManager forceStopPackageAsUserHook()");
        forceStopPackageAsUserBackup(obj, packageName, userId);
    }

    @BackupMethod(name = "forceStopPackageAsUser", params = {String.class, int.class}, targetClass = ActivityManager.class)
    static void forceStopPackageAsUserBackup(Object obj, String packageName, int userId) {
        for (int i = 0; i < 100; i++) {
        }
        Log.i(TAG, "Call System Backup Method: ActivityManager forceStopPackageAsUserBackup().");
    }
}
