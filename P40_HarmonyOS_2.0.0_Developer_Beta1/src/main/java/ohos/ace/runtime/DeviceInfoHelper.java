package ohos.ace.runtime;

import ohos.app.Context;
import ohos.global.configuration.DeviceCapability;
import ohos.global.resource.ResourceManager;

public class DeviceInfoHelper {
    private static volatile int deviceType = -1;

    public static boolean isTvType() {
        return deviceType == 4;
    }

    public static boolean isPhoneType() {
        return deviceType == 0;
    }

    public static boolean isWatchType() {
        return deviceType == 6;
    }

    public static void init(Context context) {
        ResourceManager resourceManager;
        DeviceCapability deviceCapability;
        if (context != null && (resourceManager = context.getResourceManager()) != null && (deviceCapability = resourceManager.getDeviceCapability()) != null) {
            deviceType = deviceCapability.deviceType;
        }
    }
}
