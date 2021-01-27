package huawei.android.security.secai.hookcase.hook;

import android.util.Log;
import huawei.android.security.HwBehaviorCollectManagerImpl;
import huawei.android.security.secai.hookcase.annotations.BackupMethod;
import huawei.android.security.secai.hookcase.annotations.HookMethod;
import huawei.android.security.secai.hookcase.utils.BehaviorIdCast;

class RuntimeHook {
    private static final String TAG = RuntimeHook.class.getSimpleName();

    RuntimeHook() {
    }

    @HookMethod(name = "loadLibrary0", params = {ClassLoader.class, String.class}, targetClass = Runtime.class)
    static void loadLibrary0Hook(Object obj, ClassLoader loader, String libname) {
        HwBehaviorCollectManagerImpl.getSubClassDefault().sendBehavior(BehaviorIdCast.BehaviorId.LOAD_ELF_LOADLIBRARY.getValue());
        Log.i(TAG, "Call System Hook Method:Runtime loadLibrary0Hook().");
        loadLibrary0Backup(obj, loader, libname);
    }

    @BackupMethod(name = "loadLibrary0", params = {ClassLoader.class, String.class}, targetClass = Runtime.class)
    static void loadLibrary0Backup(Object obj, ClassLoader loader, String libname) {
        Log.i(TAG, "Call System Backup Method: Runtime loadLibrary0Backup().");
        loadLibrary0Backup(obj, loader, libname);
    }

    @HookMethod(name = "load0", params = {Class.class, String.class}, targetClass = Runtime.class)
    static void load0Hook(Object obj, Class<?> fromClass, String filename) {
        if (filename == null) {
            load0Backup(obj, fromClass, null);
            return;
        }
        if (filename.contains("/files")) {
            HwBehaviorCollectManagerImpl.getSubClassDefault().sendBehavior(BehaviorIdCast.BehaviorId.LOAD_ELF_LOAD.getValue());
        } else {
            HwBehaviorCollectManagerImpl.getSubClassDefault().sendBehavior(BehaviorIdCast.BehaviorId.LOAD_ELF_LOAD.getValue() + 1);
        }
        String str = TAG;
        Log.i(str, "Call System Hook Method: Runtime load0Hook()." + BehaviorIdCast.BehaviorId.LOAD_ELF_LOAD.getValue());
        load0Backup(obj, fromClass, filename);
    }

    @BackupMethod(name = "load0", params = {Class.class, String.class}, targetClass = Runtime.class)
    static void load0Backup(Object obj, Class<?> fromClass, String filename) {
        Log.i(TAG, "Call System Backup Method: Runtime load0Backup().");
        load0Backup(obj, fromClass, filename);
    }
}
