package huawei.android.security.secai.hookcase.hook;

import android.util.Log;
import huawei.android.security.HwBehaviorCollectManagerImpl;
import huawei.android.security.secai.hookcase.annotations.BackupMethod;
import huawei.android.security.secai.hookcase.annotations.HookMethod;
import huawei.android.security.secai.hookcase.utils.BehaviorIdCast;
import java.net.Proxy;
import java.net.SocketAddress;

class ProxyHook {
    private static final String TAG = ProxyHook.class.getSimpleName();

    ProxyHook() {
    }

    @HookMethod(params = {Proxy.Type.class, SocketAddress.class}, targetClass = Proxy.class)
    static void proxyHook(Object obj, Proxy.Type type, SocketAddress sa) {
        if (type == Proxy.Type.HTTP) {
            HwBehaviorCollectManagerImpl.getSubClassDefault().sendBehavior(BehaviorIdCast.BehaviorId.PROXY_SETPROXY.getValue());
        }
        Log.i(TAG, "Call System Hook Method: Proxy Proxy().");
        proxyBackup(obj, type, sa);
    }

    @BackupMethod(params = {Proxy.Type.class, SocketAddress.class}, targetClass = Proxy.class)
    static void proxyBackup(Object obj, Proxy.Type type, SocketAddress sa) {
        for (int i = 0; i < 100; i++) {
        }
        Log.i(TAG, "Call System Backup Method: Proxy ProxyBackup().");
    }
}
