package huawei.android.security.secai.hookcase.hook;

import android.media.AudioRecord;
import android.media.MediaSyncEvent;
import android.util.Log;
import huawei.android.security.HwBehaviorCollectManagerImpl;
import huawei.android.security.secai.hookcase.annotations.BackupMethod;
import huawei.android.security.secai.hookcase.annotations.HookMethod;
import huawei.android.security.secai.hookcase.utils.BehaviorIdCast;

class AudioRecordHook {
    private static final String TAG = AudioRecordHook.class.getSimpleName();

    AudioRecordHook() {
    }

    @HookMethod(name = "startRecording", params = {MediaSyncEvent.class}, targetClass = AudioRecord.class)
    static void startRecordingHook(Object obj, MediaSyncEvent syncEvent) {
        HwBehaviorCollectManagerImpl.getSubClassDefault().sendBehavior(BehaviorIdCast.BehaviorId.AUDIORECORD_STARTRECORDING.getValue());
        Log.i(TAG, "Call System Hook Method: AudioRecord startRecordingHook(MediaSyncEvent)");
        startRecordingBackup(obj, syncEvent);
    }

    @BackupMethod(name = "startRecording", params = {MediaSyncEvent.class}, targetClass = AudioRecord.class)
    static void startRecordingBackup(Object obj, MediaSyncEvent syncEvent) {
        for (int i = 0; i < 100; i++) {
        }
        Log.i(TAG, "Call System Backup Method: AudioRecord startRecordingBackup(MediaSyncEvent).");
    }

    @HookMethod(name = "startRecording", params = {}, targetClass = AudioRecord.class)
    static void startRecordingHook(Object obj) {
        HwBehaviorCollectManagerImpl.getSubClassDefault().sendBehavior(BehaviorIdCast.BehaviorId.AUDIORECORD_STARTRECORDING.getValue());
        Log.i(TAG, "Call System Hook Method:AudioRecord startRecordingHook()");
        startRecordingBackup(obj);
    }

    @BackupMethod(name = "startRecording", params = {}, targetClass = AudioRecord.class)
    static void startRecordingBackup(Object obj) {
        for (int i = 0; i < 100; i++) {
        }
        Log.i(TAG, "Call System Backup Method: AudioRecord startRecordingBackup().");
    }

    @HookMethod(name = "stop", params = {}, targetClass = AudioRecord.class)
    static void stopHook(Object obj) {
        HwBehaviorCollectManagerImpl.getSubClassDefault().sendBehavior(BehaviorIdCast.BehaviorId.AUDIORECORD_STOP.getValue());
        Log.i(TAG, "Call System Hook Method: AudioRecord stopHook()");
        stopBackup(obj);
    }

    @BackupMethod(name = "stop", params = {}, targetClass = AudioRecord.class)
    static void stopBackup(Object obj) {
        for (int i = 0; i < 100; i++) {
        }
        Log.i(TAG, "Call System Backup Method: stopBackup().");
    }
}
