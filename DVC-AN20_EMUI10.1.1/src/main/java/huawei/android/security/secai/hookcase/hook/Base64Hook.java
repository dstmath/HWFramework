package huawei.android.security.secai.hookcase.hook;

import android.util.Log;
import huawei.android.security.HwBehaviorCollectManagerImpl;
import huawei.android.security.secai.hookcase.annotations.BackupMethod;
import huawei.android.security.secai.hookcase.annotations.HookMethod;
import huawei.android.security.secai.hookcase.utils.BehaviorIdCast;
import java.util.Base64;

class Base64Hook {
    private static final String TAG = Base64Hook.class.getSimpleName();

    Base64Hook() {
    }

    @HookMethod(name = "encode0", params = {byte[].class, int.class, int.class, byte[].class}, targetClass = Base64.Encoder.class)
    static int encode0Hook(Object obj, byte[] src, int off, int end, byte[] dst) {
        HwBehaviorCollectManagerImpl.getSubClassDefault().sendBehavior(BehaviorIdCast.BehaviorId.CIPHER_ENCODE.getValue());
        Log.i(TAG, "Call System Hook Method: Base64 encode0Hook(java.util.Base64)");
        return encode0Backup(obj, src, off, end, dst);
    }

    @BackupMethod(name = "encode0", params = {byte[].class, int.class, int.class, byte[].class}, targetClass = Base64.Encoder.class)
    static int encode0Backup(Object obj, byte[] src, int off, int end, byte[] dst) {
        for (int i = 0; i < 100; i++) {
        }
        Log.i(TAG, "Call System Backup Method:Base64 encode0Backup().");
        return -1;
    }

    @HookMethod(name = "encode", params = {byte[].class, int.class, int.class, int.class}, targetClass = android.util.Base64.class)
    static byte[] encodeHook(byte[] input, int offset, int len, int flags) {
        Log.i(TAG, "Call System Hook Method: Base64 encodeHook(android.util.Base64).");
        return encodeBackup(input, offset, len, flags);
    }

    @BackupMethod(name = "encode", params = {byte[].class, int.class, int.class, int.class}, targetClass = android.util.Base64.class)
    static byte[] encodeBackup(byte[] input, int offset, int len, int flags) {
        for (int i = 0; i < 100; i++) {
        }
        Log.i(TAG, "Call System Backup Method:Base64 encodeBackup().");
        return input;
    }
}
