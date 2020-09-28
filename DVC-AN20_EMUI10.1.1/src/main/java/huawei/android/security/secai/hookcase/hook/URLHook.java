package huawei.android.security.secai.hookcase.hook;

import android.util.Log;
import huawei.android.security.HwBehaviorCollectManagerImpl;
import huawei.android.security.secai.hookcase.annotations.BackupMethod;
import huawei.android.security.secai.hookcase.annotations.HookMethod;
import huawei.android.security.secai.hookcase.utils.BehaviorIdCast;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;

class URLHook {
    private static final String TAG = URLHook.class.getSimpleName();

    URLHook() {
    }

    @HookMethod(name = "openConnection", params = {Proxy.class}, targetClass = URL.class)
    static URLConnection openConnectionHook(Object obj, Proxy proxy) {
        HwBehaviorCollectManagerImpl.getSubClassDefault().sendBehavior(BehaviorIdCast.BehaviorId.HTTP_OPENCONNECTION.getValue());
        Log.i(TAG, "Call System Hook Method: URL openConnectionHook(Proxy)");
        return openConnectionBackup(obj, proxy);
    }

    @BackupMethod(name = "openConnection", params = {Proxy.class}, targetClass = URL.class)
    static URLConnection openConnectionBackup(Object obj, Proxy proxy) {
        for (int i = 0; i < 100; i++) {
        }
        Log.i(TAG, "Call System Backup Method:URL openConnectionHook(Proxy)");
        return null;
    }

    @HookMethod(name = "openConnection", params = {}, targetClass = URL.class)
    static URLConnection openConnectionHook(Object obj) {
        HwBehaviorCollectManagerImpl.getSubClassDefault().sendBehavior(BehaviorIdCast.BehaviorId.HTTP_OPENCONNECTION.getValue());
        Log.i(TAG, "Call System Hook Method: URL openConnectionHook()");
        return openConnectionBackup(obj);
    }

    @BackupMethod(name = "openConnection", params = {}, targetClass = URL.class)
    static URLConnection openConnectionBackup(Object obj) {
        for (int i = 0; i < 100; i++) {
        }
        Log.i(TAG, "Call System Backup Method:URL openConnectionHook()");
        return null;
    }
}
