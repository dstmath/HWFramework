package huawei.android.security.secai.hookcase.hook;

import android.hardware.camera2.CaptureRequest;
import android.util.Log;
import huawei.android.security.HwBehaviorCollectManagerImpl;
import huawei.android.security.secai.hookcase.annotations.BackupMethod;
import huawei.android.security.secai.hookcase.annotations.HookMethod;
import huawei.android.security.secai.hookcase.utils.BehaviorIdCast;
import java.util.List;
import java.util.concurrent.Executor;

class CameraDeviceImplHook {
    private static final String CAMERADEVICEIMPL_CLASSNAME = "android.hardware.camera2.impl.CameraDeviceImpl";
    private static final String CAPTURECALLBACK_CLASSNAME = "android.hardware.camera2.impl.CameraDeviceImpl$CaptureCallback";
    private static final String EXECUTOR_CLASSNAME = "java.util.concurrent.Executor";
    private static final String LIST_CLASSNAME = "java.util.List";
    private static final String TAG = CameraDeviceImplHook.class.getSimpleName();

    CameraDeviceImplHook() {
    }

    @HookMethod(name = "captureBurst", reflectionParams = {LIST_CLASSNAME, CAPTURECALLBACK_CLASSNAME, EXECUTOR_CLASSNAME}, reflectionTargetClass = CAMERADEVICEIMPL_CLASSNAME)
    static int captureBurstHook(Object obj, List<CaptureRequest> requests, Object callback, Executor executor) {
        HwBehaviorCollectManagerImpl.getSubClassDefault().sendBehavior(BehaviorIdCast.BehaviorId.CAMERA_CAPTUREBURST.getValue());
        Log.i(TAG, "Call System Hook Method: CameraDeviceImpl captureBurstHook()");
        return captureBurstBackup(obj, requests, callback, executor);
    }

    @BackupMethod(name = "captureBurst", reflectionParams = {LIST_CLASSNAME, CAPTURECALLBACK_CLASSNAME, EXECUTOR_CLASSNAME}, reflectionTargetClass = CAMERADEVICEIMPL_CLASSNAME)
    static int captureBurstBackup(Object obj, List<CaptureRequest> list, Object callback, Executor executor) {
        for (int i = 0; i < 100; i++) {
        }
        Log.i(TAG, "Call System Hook Method:CameraDeviceImpl captureBurstBackup()");
        return -1;
    }
}
