package ohos.ace.runtime;

import ohos.app.Context;
import ohos.global.configuration.DeviceCapability;
import ohos.global.resource.ResourceManager;

public class DeviceInfoHelper {
    public static boolean isTvType(Context context) {
        DeviceCapability deviceCapability = getDeviceCapability(context);
        if (deviceCapability != null && deviceCapability.deviceType == 4) {
            return true;
        }
        return false;
    }

    public static boolean isPhoneType(Context context) {
        DeviceCapability deviceCapability = getDeviceCapability(context);
        if (deviceCapability != null && deviceCapability.deviceType == 0) {
            return true;
        }
        return false;
    }

    public static boolean isWatchType(Context context) {
        DeviceCapability deviceCapability = getDeviceCapability(context);
        if (deviceCapability != null && deviceCapability.deviceType == 6) {
            return true;
        }
        return false;
    }

    private static DeviceCapability getDeviceCapability(Context context) {
        ResourceManager resourceManager;
        if (context == null || (resourceManager = context.getResourceManager()) == null) {
            return null;
        }
        return resourceManager.getDeviceCapability();
    }
}
