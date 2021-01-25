package ohos.media.camera;

import ohos.app.Context;
import ohos.eventhandler.EventHandler;
import ohos.media.camera.device.CameraAbility;
import ohos.media.camera.device.CameraDeviceCallback;
import ohos.media.camera.device.CameraInfo;
import ohos.media.camera.device.CameraManager;
import ohos.media.camera.device.CameraStateCallback;
import ohos.media.camera.mode.Mode;
import ohos.media.camera.mode.ModeAbility;
import ohos.media.camera.mode.ModeManager;
import ohos.media.camera.mode.ModeStateCallback;

public final class CameraKit {
    private static CameraManager cameraManager;
    private static CameraKit instance;
    private static ModeManager modeManager;

    public void setFlashlight(String str, boolean z) {
    }

    private CameraKit(Context context) {
        cameraManager = CameraManager.getInstance(context);
        modeManager = ModeManager.getInstance(context);
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

    public int[] getSupportedModes(String str) {
        return modeManager.getSupportedModes(str);
    }

    public ModeAbility getModeAbility(String str, @Mode.Type int i) {
        return modeManager.getModeAbility(str, i);
    }

    public void createMode(String str, @Mode.Type int i, ModeStateCallback modeStateCallback, EventHandler eventHandler) {
        modeManager.createMode(str, i, modeStateCallback, eventHandler);
    }

    public void changeMode(Mode mode, @Mode.Type int i, ModeStateCallback modeStateCallback) {
        modeManager.changeMode(mode, i, modeStateCallback);
    }

    public void registerCameraDeviceCallback(CameraDeviceCallback cameraDeviceCallback, EventHandler eventHandler) {
        cameraManager.registerCameraDeviceCallback(cameraDeviceCallback, eventHandler);
    }

    public void unregisterCameraDeviceCallback(CameraDeviceCallback cameraDeviceCallback) {
        cameraManager.unregisterCameraDeviceCallback(cameraDeviceCallback);
    }
}
