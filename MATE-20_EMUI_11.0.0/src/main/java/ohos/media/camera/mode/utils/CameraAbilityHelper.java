package ohos.media.camera.mode.utils;

import ohos.media.camera.device.impl.CameraAbilityImpl;

public class CameraAbilityHelper {
    private CameraAbilityHelper() {
    }

    public static synchronized CameraAbilityImpl getCameraAbility(String str) {
        CameraAbilityImpl cameraAbility;
        synchronized (CameraAbilityHelper.class) {
            cameraAbility = CameraManagerHelper.getCameraManager().getCameraAbility(str);
        }
        return cameraAbility;
    }
}
