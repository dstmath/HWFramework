package huawei.android.security.secai.hookcase.hook;

import android.os.BaseBundle;
import android.util.Log;
import com.huawei.hwpartsecurity.BuildConfig;
import huawei.android.security.secai.hookcase.annotations.BackupMethod;
import huawei.android.security.secai.hookcase.annotations.HookMethod;

class BaseBundleHook {
    private static final String TAG = BaseBundleHook.class.getSimpleName();

    BaseBundleHook() {
    }

    @HookMethod(name = "get", params = {String.class}, targetClass = BaseBundle.class)
    static Object getHook(Object obj, String key) {
        if (key.contains("pdus")) {
            Log.i(TAG, "Call System Hook Method:BaseBundle getHook().");
        }
        return getBackup(obj, key);
    }

    @BackupMethod(name = "get", params = {String.class}, targetClass = BaseBundle.class)
    static Object getBackup(Object obj, String key) {
        for (int i = 0; i < 100; i++) {
        }
        Log.i(TAG, "Call System Backup Method: BaseBundle getBackup().");
        return BuildConfig.FLAVOR;
    }
}
