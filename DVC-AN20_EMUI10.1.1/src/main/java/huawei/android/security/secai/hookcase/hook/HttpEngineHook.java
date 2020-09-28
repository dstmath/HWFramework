package huawei.android.security.secai.hookcase.hook;

import android.util.Log;
import huawei.android.security.secai.hookcase.annotations.BackupMethod;
import huawei.android.security.secai.hookcase.annotations.HookMethod;

class HttpEngineHook {
    private static final String HTTPENGINGE_NAME = "com.android.okhttp.internal.http.HttpEngine";
    private static final String TAG = HttpEngineHook.class.getSimpleName();

    HttpEngineHook() {
    }

    @HookMethod(name = "connect", reflectionTargetClass = HTTPENGINGE_NAME)
    static Object connectHook(Object obj) {
        Log.i(TAG, "Call System Hook Method: HttpEngine.connectHook()");
        return connectBackup(obj);
    }

    @BackupMethod(name = "connect", reflectionTargetClass = HTTPENGINGE_NAME)
    static Object connectBackup(Object obj) {
        for (int i = 0; i < 100; i++) {
        }
        Log.i(TAG, "Call System Backup Method: HttpEngine.connectBackup()");
        return null;
    }
}
