package huawei.android.security.secai.hookcase.hook;

import android.util.Log;
import huawei.android.security.HwBehaviorCollectManagerImpl;
import huawei.android.security.secai.hookcase.annotations.BackupMethod;
import huawei.android.security.secai.hookcase.annotations.HookMethod;
import huawei.android.security.secai.hookcase.utils.BehaviorIdCast;
import java.util.Map;

class ProcessImplHook {
    private static final String TAG = ProcessImplHook.class.getSimpleName();

    ProcessImplHook() {
    }

    @HookMethod(name = "start", reflectionParams = {"java.lang.String[]", "java.util.Map", "java.lang.String", "java.lang.ProcessBuilder$Redirect[]", "boolean"}, reflectionTargetClass = "java.lang.ProcessImpl")
    static Process startHook(String[] cmdArray, Map<String, String> environment, String dir, Object redirects, boolean isRedirectErrorStream) {
        if (cmdArray == null || cmdArray[0] == null) {
            return startBackup(cmdArray, environment, dir, redirects, isRedirectErrorStream);
        }
        String strArg = cmdArray[0];
        if (strArg.startsWith("su")) {
            HwBehaviorCollectManagerImpl.getSubClassDefault().sendBehavior(BehaviorIdCast.BehaviorId.EXECUTE_START.getValue());
        } else if (strArg.startsWith("/data/data/")) {
            HwBehaviorCollectManagerImpl.getSubClassDefault().sendBehavior(BehaviorIdCast.BehaviorId.EXECUTE_START.getValue() + 1);
        } else {
            HwBehaviorCollectManagerImpl.getSubClassDefault().sendBehavior(BehaviorIdCast.BehaviorId.EXECUTE_START.getValue() + 2);
        }
        Log.i(TAG, "Call System Hook Method: ProcessImpl.startHook()");
        return startBackup(cmdArray, environment, dir, redirects, isRedirectErrorStream);
    }

    @BackupMethod(name = "start", reflectionParams = {"java.lang.String[]", "java.util.Map", "java.lang.String", "java.lang.ProcessBuilder$Redirect[]", "boolean"}, reflectionTargetClass = "java.lang.ProcessImpl")
    static Process startBackup(String[] cmdArray, Map<String, String> map, String dir, Object redirects, boolean isRedirectErrorStream) {
        for (int i = 0; i < 100; i++) {
        }
        Log.i(TAG, "Call System Backup Method: ProcessImpl.startBackup()");
        return null;
    }
}
