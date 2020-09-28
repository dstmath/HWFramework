package huawei.android.security.secai.hookcase.hook;

import android.app.ActivityManager;
import android.util.Log;
import huawei.android.security.HwBehaviorCollectManagerImpl;
import huawei.android.security.secai.hookcase.annotations.BackupMethod;
import huawei.android.security.secai.hookcase.annotations.HookMethod;
import huawei.android.security.secai.hookcase.utils.BehaviorIdCast;
import java.util.Collections;
import java.util.List;

class IActivityManagerHook {
    private static final String IACTIVITYMANAGER_NAME = "android.app.IActivityManager$Stub$Proxy";
    private static final String TAG = IActivityManagerHook.class.getSimpleName();

    IActivityManagerHook() {
    }

    @HookMethod(name = "getFilteredTasks", params = {int.class, int.class, int.class}, reflectionTargetClass = IACTIVITYMANAGER_NAME)
    static List<ActivityManager.RunningTaskInfo> getFilteredTasksHook(Object obj, int maxNum, int ignoreActivityType, int ignoreWindowingMode) {
        HwBehaviorCollectManagerImpl.getSubClassDefault().sendBehavior(BehaviorIdCast.BehaviorId.ACTIVITYMANAGER_GETFILTEREDTASKS.getValue());
        Log.i(TAG, "Call System Hook Method: IActivityManager getFilteredTasksHook()");
        return getFilteredTasksBackup(obj, maxNum, ignoreActivityType, ignoreWindowingMode);
    }

    @BackupMethod(name = "getFilteredTasks", params = {int.class, int.class, int.class}, reflectionTargetClass = IACTIVITYMANAGER_NAME)
    static List<ActivityManager.RunningTaskInfo> getFilteredTasksBackup(Object obj, int maxNum, int ignoreActivityType, int ignoreWindowingMode) {
        for (int i = 0; i < 100; i++) {
        }
        Log.i(TAG, "Call System Backup Method:IActivityManager getFilteredTasksBackup().");
        return Collections.emptyList();
    }
}
