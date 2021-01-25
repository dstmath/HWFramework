package huawei.android.security.secai.hookcase.hook;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.util.Log;
import huawei.android.security.HwBehaviorCollectManagerImpl;
import huawei.android.security.secai.hookcase.annotations.BackupMethod;
import huawei.android.security.secai.hookcase.annotations.HookMethod;
import huawei.android.security.secai.hookcase.utils.BehaviorIdCast;

class ClipboardManagerHook {
    private static final String TAG = ClipboardManagerHook.class.getSimpleName();

    ClipboardManagerHook() {
    }

    @HookMethod(name = "setPrimaryClip", params = {ClipData.class}, targetClass = ClipboardManager.class)
    static void setPrimaryClipHook(Object obj, ClipData clip) {
        HwBehaviorCollectManagerImpl.getSubClassDefault().sendBehavior(BehaviorIdCast.BehaviorId.CLIPBOARD_SETPRIMARYCLIP.getValue());
        Log.i(TAG, "Call System Hook Method: ClipboardManager setPrimaryClipHook()");
        setPrimaryClipBackup(obj, clip);
    }

    @BackupMethod(name = "setPrimaryClip", params = {ClipData.class}, targetClass = ClipboardManager.class)
    static void setPrimaryClipBackup(Object obj, ClipData clip) {
        for (int i = 0; i < 100; i++) {
        }
        Log.i(TAG, "Call System Backup Method: ClipboardManager setPrimaryClipBackup().");
    }
}
