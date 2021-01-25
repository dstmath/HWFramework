package huawei.android.security.secai.hookcase.hook;

import android.content.BroadcastReceiver;
import android.util.Log;
import huawei.android.security.secai.hookcase.annotations.BackupMethod;
import huawei.android.security.secai.hookcase.annotations.HookMethod;

class BroadcastReceiverHook {
    private static final String TAG = BroadcastReceiverHook.class.getSimpleName();

    BroadcastReceiverHook() {
    }

    @HookMethod(name = "abortBroadcast", params = {}, targetClass = BroadcastReceiver.class)
    static void abortBroadcastHook(Object obj) {
        Log.i(TAG, "Call System Hook Method: BroadcastReceiver abortBroadcastHook().");
        abortBroadcastBackup(obj);
    }

    @BackupMethod(name = "abortBroadcast", params = {}, targetClass = BroadcastReceiver.class)
    static void abortBroadcastBackup(Object obj) {
        for (int i = 0; i < 100; i++) {
        }
        Log.i(TAG, "Call System Backup Method: BroadcastReceiver abortBroadcastBackup().");
    }
}
