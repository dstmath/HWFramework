package huawei.android.security.secai.hookcase.hook;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import huawei.android.security.HwBehaviorCollectManagerImpl;
import huawei.android.security.secai.hookcase.annotations.BackupMethod;
import huawei.android.security.secai.hookcase.annotations.HookMethod;
import huawei.android.security.secai.hookcase.utils.BehaviorIdCast;

class ToastHook {
    private static final String TAG = ToastHook.class.getSimpleName();

    ToastHook() {
    }

    @HookMethod(name = "makeText", params = {Context.class, CharSequence.class, int.class}, targetClass = Toast.class)
    static Toast makeTextHook(Context context, CharSequence text, int duration) {
        HwBehaviorCollectManagerImpl.getSubClassDefault().sendBehavior(BehaviorIdCast.BehaviorId.TOAST_MAKETEXT.getValue());
        Log.i(TAG, "Call System Hook Method:Toast makeTextHook()");
        return makeTextBackup(context, text, duration);
    }

    @BackupMethod(name = "makeText", params = {Context.class, CharSequence.class, int.class}, targetClass = Toast.class)
    static Toast makeTextBackup(Context context, CharSequence text, int duration) {
        for (int i = 0; i < 100; i++) {
        }
        Log.i(TAG, "Call System Backup Method:Toast makeTextBackup().");
        return null;
    }
}
