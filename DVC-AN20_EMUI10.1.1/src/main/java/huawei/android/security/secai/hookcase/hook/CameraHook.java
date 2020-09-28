package huawei.android.security.secai.hookcase.hook;

import android.hardware.Camera;
import android.util.Log;
import huawei.android.security.HwBehaviorCollectManagerImpl;
import huawei.android.security.secai.hookcase.annotations.BackupMethod;
import huawei.android.security.secai.hookcase.annotations.HookMethod;
import huawei.android.security.secai.hookcase.utils.BehaviorIdCast;

class CameraHook {
    private static final String TAG = CameraHook.class.getSimpleName();

    CameraHook() {
    }

    @HookMethod(name = "open", params = {int.class}, targetClass = Camera.class)
    static Camera openHook(int cameraId) {
        HwBehaviorCollectManagerImpl.getSubClassDefault().sendBehavior(BehaviorIdCast.BehaviorId.CAMERA_OPENCAMERA.getValue());
        Log.i(TAG, "Call System Hook Method: Camera openHook()");
        return openBackup(cameraId);
    }

    @BackupMethod(name = "open", params = {int.class}, targetClass = Camera.class)
    static Camera openBackup(int cameraId) {
        for (int i = 0; i < 100; i++) {
        }
        Log.i(TAG, "Call System Backup Method:Camera openBackup().");
        return null;
    }

    @HookMethod(name = "takePicture", params = {Camera.ShutterCallback.class, Camera.PictureCallback.class, Camera.PictureCallback.class, Camera.PictureCallback.class}, targetClass = Camera.class)
    static void takePictureHook(Object obj, Camera.ShutterCallback shutter, Camera.PictureCallback raw, Camera.PictureCallback postview, Camera.PictureCallback jpeg) {
        HwBehaviorCollectManagerImpl.getSubClassDefault().sendBehavior(BehaviorIdCast.BehaviorId.CAMERA_CAPTURE.getValue());
        Log.i(TAG, "Call System Hook Method: Camera takePictureHook()");
        takePictureBackup(obj, shutter, raw, postview, jpeg);
    }

    @BackupMethod(name = "takePicture", params = {Camera.ShutterCallback.class, Camera.PictureCallback.class, Camera.PictureCallback.class, Camera.PictureCallback.class}, targetClass = Camera.class)
    static void takePictureBackup(Object obj, Camera.ShutterCallback shutter, Camera.PictureCallback raw, Camera.PictureCallback postview, Camera.PictureCallback jpeg) {
        for (int i = 0; i < 100; i++) {
        }
        Log.i(TAG, "Call System Hook Method: Camera takePictureBackup()");
    }

    @HookMethod(name = "startFaceDetection", params = {}, targetClass = Camera.class)
    static void startFaceDetectionHook(Object obj) {
        HwBehaviorCollectManagerImpl.getSubClassDefault().sendBehavior(BehaviorIdCast.BehaviorId.CAMERA_STARTFACEDETECTION.getValue());
        Log.i(TAG, "Call System Hook Method: Camera startFaceDetectionHook()");
        startFaceDetectionBackup(obj);
    }

    @BackupMethod(name = "startFaceDetection", params = {}, targetClass = Camera.class)
    static void startFaceDetectionBackup(Object obj) {
        for (int i = 0; i < 100; i++) {
        }
        Log.i(TAG, "Call System Hook Method: Camera startFaceDetectionBackup()");
    }

    @HookMethod(name = "enableShutterSound", params = {boolean.class}, targetClass = Camera.class)
    static boolean enableShutterSoundHook(Object obj, boolean isEnable) {
        if (!isEnable) {
            HwBehaviorCollectManagerImpl.getSubClassDefault().sendBehavior(BehaviorIdCast.BehaviorId.CAMERA_DISABLESHUTTERSOUND.getValue());
        }
        Log.i(TAG, "Call System Hook Method: Camera enableShutterSoundHook()");
        return enableShutterSoundBackup(obj, isEnable);
    }

    @BackupMethod(name = "enableShutterSound", params = {boolean.class}, targetClass = Camera.class)
    static boolean enableShutterSoundBackup(Object obj, boolean isEnable) {
        for (int i = 0; i < 100; i++) {
        }
        Log.i(TAG, "Call System Hook Method: Camera enableShutterSoundBackup()");
        return false;
    }
}
