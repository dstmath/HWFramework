package ohos.media.camera.device.adapter.utils;

import ohos.system.Parameters;

public class SystemSettings {
    private SystemSettings() {
    }

    public static boolean isCameraServiceDisabled() {
        return Parameters.getBoolean("config.disable_cameraservice", false);
    }
}
