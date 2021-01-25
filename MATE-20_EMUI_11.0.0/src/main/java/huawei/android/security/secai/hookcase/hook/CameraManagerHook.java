package huawei.android.security.secai.hookcase.hook;

import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.os.Handler;
import android.util.Log;
import huawei.android.security.HwBehaviorCollectManagerImpl;
import huawei.android.security.secai.hookcase.annotations.BackupMethod;
import huawei.android.security.secai.hookcase.annotations.HookMethod;
import huawei.android.security.secai.hookcase.utils.BehaviorIdCast;

class CameraManagerHook {
    private static final String TAG = CameraManagerHook.class.getSimpleName();

    CameraManagerHook() {
    }

    @HookMethod(name = "openCamera", params = {String.class, CameraDevice.StateCallback.class, Handler.class}, targetClass = CameraManager.class)
    static void openCameraHook(Object obj, String cameraId, CameraDevice.StateCallback callback, Handler handler) {
        HwBehaviorCollectManagerImpl.getSubClassDefault().sendBehavior(BehaviorIdCast.BehaviorId.CAMERA_OPENCAMERA.getValue());
        Log.i(TAG, "Call System Hook Method: CameraManager openCameraHook().");
        openCameraBackup(obj, cameraId, callback, handler);
    }

    @BackupMethod(name = "openCamera", params = {String.class, CameraDevice.StateCallback.class, Handler.class}, targetClass = CameraManager.class)
    static void openCameraBackup(Object obj, String cameraId, CameraDevice.StateCallback callback, Handler handler) {
        for (int i = 0; i < 100; i++) {
        }
        Log.i(TAG, "Call System Backup Method: CameraManager openCameraBackup().");
    }

    @HookMethod(name = "setTorchMode", params = {String.class, boolean.class}, targetClass = CameraManager.class)
    static void setTorchModeHook(Object obj, String cameraId, boolean isEnabled) {
        if (!isEnabled) {
            HwBehaviorCollectManagerImpl.getSubClassDefault().sendBehavior(BehaviorIdCast.BehaviorId.CAMERA_SETTORCHMODE.getValue());
        }
        Log.i(TAG, "Call System Hook Method: CameraManager setTorchModeHook().");
        setTorchModeBackup(obj, cameraId, isEnabled);
    }

    @BackupMethod(name = "setTorchMode", params = {String.class, boolean.class}, targetClass = CameraManager.class)
    static void setTorchModeBackup(Object obj, String cameraId, boolean isEnabled) {
        for (int i = 0; i < 100; i++) {
        }
        Log.i(TAG, "Call System Backup Method:CameraManager setTorchModeBackup().");
    }
}
