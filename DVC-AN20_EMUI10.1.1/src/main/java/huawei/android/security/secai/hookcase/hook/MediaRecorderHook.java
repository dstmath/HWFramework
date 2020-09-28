package huawei.android.security.secai.hookcase.hook;

import android.media.MediaRecorder;
import android.util.Log;
import huawei.android.security.HwBehaviorCollectManagerImpl;
import huawei.android.security.secai.hookcase.annotations.BackupMethod;
import huawei.android.security.secai.hookcase.annotations.HookMethod;
import huawei.android.security.secai.hookcase.utils.BehaviorIdCast;

class MediaRecorderHook {
    private static final String TAG = MediaRecorderHook.class.getSimpleName();

    MediaRecorderHook() {
    }

    @HookMethod(name = "prepare", params = {}, targetClass = MediaRecorder.class)
    static void prepareHook(Object obj) {
        HwBehaviorCollectManagerImpl.getSubClassDefault().sendBehavior(BehaviorIdCast.BehaviorId.CAMERA_MEDIARECORDERPREPARE.getValue());
        Log.i(TAG, "Call System Hook Method: MediaProvider prepareHook()");
        prepareBackup(obj);
    }

    @BackupMethod(name = "prepare", params = {}, targetClass = MediaRecorder.class)
    static void prepareBackup(Object obj) {
        for (int i = 0; i < 100; i++) {
        }
        Log.i(TAG, "Call System Backup Method:MediaProvider prepareBackup().");
    }
}
