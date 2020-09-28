package huawei.android.security.secai.hookcase.hook;

import android.util.Log;
import huawei.android.security.secai.hookcase.annotations.BackupMethod;
import huawei.android.security.secai.hookcase.annotations.HookMethod;

class SystemPropertiesHook {
    private static final String SYSTEMPROPERTIES_CLASSNAME = "android.os.SystemProperties";
    private static final String TAG = SystemPropertiesHook.class.getSimpleName();

    SystemPropertiesHook() {
    }

    @HookMethod(name = "get", params = {String.class}, reflectionTargetClass = SYSTEMPROPERTIES_CLASSNAME)
    static Class getWithOneParamHook(String key) {
        Log.i(TAG, "Call System Hook Method: SystemProperties get(String).");
        return getWithOneParamBackup(key);
    }

    @BackupMethod(name = "get", params = {String.class}, reflectionTargetClass = SYSTEMPROPERTIES_CLASSNAME)
    static Class getWithOneParamBackup(String key) {
        for (int i = 0; i < 100; i++) {
        }
        Log.i(TAG, "Call Backup Method: SystemProperties get(String).");
        return null;
    }

    @HookMethod(name = "get", params = {String.class, String.class}, reflectionTargetClass = SYSTEMPROPERTIES_CLASSNAME)
    static Class getWithTwoParamHook(String key, String def) {
        Log.i(TAG, "Call System Hook Method: SystemProperties get(String,def).");
        return getWithTwoParamBackup(key, def);
    }

    @BackupMethod(name = "get", params = {String.class, String.class}, reflectionTargetClass = SYSTEMPROPERTIES_CLASSNAME)
    static Class getWithTwoParamBackup(String key, String def) {
        for (int i = 0; i < 100; i++) {
        }
        Log.i(TAG, "Call Backup Method: SystemProperties get(String,def).");
        return null;
    }
}
