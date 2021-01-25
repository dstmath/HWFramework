package huawei.android.security.secai.hookcase.hook;

import android.util.Log;
import huawei.android.security.secai.hookcase.annotations.BackupMethod;
import huawei.android.security.secai.hookcase.annotations.HookMethod;

class ClassloaderHook {
    private static final String TAG = ClassloaderHook.class.getSimpleName();

    ClassloaderHook() {
    }

    @HookMethod(name = "loadClass", params = {String.class, boolean.class}, targetClass = ClassLoader.class)
    static Class loadClassHook(Object obj, String name, boolean isResolve) {
        Log.i(TAG, "Call System Hook Method: Classloader loadClass(String,boolean).");
        return loadClassBackup(obj, name, isResolve);
    }

    @BackupMethod(name = "loadClass", params = {String.class, boolean.class}, targetClass = ClassLoader.class)
    static Class loadClassBackup(Object obj, String name, boolean isResolve) {
        for (int i = 0; i < 100; i++) {
        }
        Log.i(TAG, "Call Backup Method: Classloader loadClassBackup(String,boolean).");
        return null;
    }
}
