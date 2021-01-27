package ohos.media.camera;

import ohos.app.Context;
import ohos.eventhandler.EventHandler;
import ohos.media.camera.device.CameraAbility;
import ohos.media.camera.device.CameraDeviceCallback;
import ohos.media.camera.device.CameraInfo;
import ohos.media.camera.device.CameraManager;
import ohos.media.camera.device.CameraStateCallback;

public final class CameraKit {
    private static CameraManager cameraManager;
    private static CameraKit instance;

    public void setFlashlight(String str, boolean z) {
    }

    private CameraKit(Context context) {
        cameraManager = CameraManager.getInstance(context);
    }

    public static synchronized CameraKit getInstance(Context context) {
        synchronized (CameraKit.class) {
            if (context == null) {
                return null;
            }
            if (instance == null) {
                instance = new CameraKit(context);
            }
            return instance;
        }
    }

    public String[] getCameraIds() {
        return cameraManager.getCameraIdList();
    }

    public CameraInfo getCameraInfo(String str) {
        return cameraManager.getCameraInfo(str);
    }

    public CameraAbility getCameraAbility(String str) {
        return cameraManager.getCameraAbility(str);
    }

    public void createCamera(String str, CameraStateCallback cameraStateCallback, EventHandler eventHandler) {
        cameraManager.createCamera(str, cameraStateCallback, eventHandler);
    }

    public void registerCameraDeviceCallback(CameraDeviceCallback cameraDeviceCallback, EventHandler eventHandler) {
        cameraManager.registerCameraDeviceCallback(cameraDeviceCallback, eventHandler);
    }

    public void unregisterCameraDeviceCallback(CameraDeviceCallback cameraDeviceCallback) {
        cameraManager.unregisterCameraDeviceCallback(cameraDeviceCallback);
    }
}
