package huawei.android.security.secai.hookcase.hook;

import android.util.Log;
import huawei.android.security.secai.hookcase.annotations.BackupMethod;
import huawei.android.security.secai.hookcase.annotations.HookMethod;
import java.net.Socket;
import java.net.SocketAddress;

class SocketHook {
    private static final String TAG = SocketHook.class.getSimpleName();

    SocketHook() {
    }

    @HookMethod(name = "connect", params = {SocketAddress.class, int.class}, targetClass = Socket.class)
    static void connectHook(Object obj, SocketAddress endpoint, int timeout) {
        Log.i(TAG, "Call System Hook Method: Socket.connectHook()");
        connectBackup(obj, endpoint, timeout);
    }

    @BackupMethod(name = "connect", params = {SocketAddress.class, int.class}, targetClass = Socket.class)
    static void connectBackup(Object obj, SocketAddress endpoint, int timeout) {
        for (int i = 0; i < 100; i++) {
        }
        Log.i(TAG, "Call System Backup Method: Socket.connectBackup()");
    }
}
