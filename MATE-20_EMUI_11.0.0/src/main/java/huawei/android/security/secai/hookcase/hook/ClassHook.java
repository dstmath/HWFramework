package huawei.android.security.secai.hookcase.hook;

import android.util.Log;
import huawei.android.security.secai.hookcase.annotations.BackupMethod;
import huawei.android.security.secai.hookcase.annotations.HookMethod;

class ClassHook {
    private static final String TAG = ClassHook.class.getSimpleName();

    ClassHook() {
    }

    @HookMethod(name = "forName", params = {String.class, boolean.class, ClassLoader.class}, targetClass = Class.class)
    static Class forNameHook(String name, boolean isInit, ClassLoader loader) {
        Log.i(TAG, "Call System Hook Method: Class forName(String,boolean,ClassLoader).");
        return forNameBackup(name, isInit, loader);
    }

    @BackupMethod(name = "forName", params = {String.class, boolean.class, ClassLoader.class}, targetClass = Class.class)
    static Class forNameBackup(String name, boolean isInit, ClassLoader loader) {
        for (int i = 0; i < 100; i++) {
        }
        Log.i(TAG, "Call Backup Method: Class forNameBackup(String,boolean,ClassLoader).");
        return null;
    }
}
