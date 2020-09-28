package huawei.android.security.secai.hookcase.hook;

import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CaptureRequest;
import android.os.Handler;
import android.util.Log;
import huawei.android.security.HwBehaviorCollectManagerImpl;
import huawei.android.security.secai.hookcase.annotations.BackupMethod;
import huawei.android.security.secai.hookcase.annotations.HookMethod;
import huawei.android.security.secai.hookcase.utils.BehaviorIdCast;

class CameraCaptureSessionImplHook {
    private static final String CAMERACAPTURESESSIONIMPL_NAME = "android.hardware.camera2.impl.CameraCaptureSessionImpl";
    private static final String TAG = CameraCaptureSessionImplHook.class.getSimpleName();

    CameraCaptureSessionImplHook() {
    }

    @HookMethod(name = "capture", params = {CaptureRequest.class, CameraCaptureSession.CaptureCallback.class, Handler.class}, reflectionTargetClass = CAMERACAPTURESESSIONIMPL_NAME)
    static int captureHook(Object obj, CaptureRequest request, CameraCaptureSession.CaptureCallback callback, Handler handler) {
        HwBehaviorCollectManagerImpl.getSubClassDefault().sendBehavior(BehaviorIdCast.BehaviorId.CAMERA_CAPTURE.getValue());
        Log.i(TAG, "Call System Hook Method: CameraCaptureSessionImpl captureHook()");
        return captureBackup(obj, request, callback, handler);
    }

    @BackupMethod(name = "capture", params = {CaptureRequest.class, CameraCaptureSession.CaptureCallback.class, Handler.class}, reflectionTargetClass = CAMERACAPTURESESSIONIMPL_NAME)
    static int captureBackup(Object obj, CaptureRequest request, CameraCaptureSession.CaptureCallback callback, Handler handler) {
        for (int i = 0; i < 100; i++) {
        }
        Log.i(TAG, "Call System Backup Method: CameraCaptureSessionImpl captureBackup().");
        return -1;
    }
}
