package com.huawei.dmsdp.devicevirtualization;

import java.util.HashMap;
import java.util.Map;

public class DeviceTypeConverter {
    private static final Map<Integer, String> deviceTypeCovertMap = new HashMap() {
        /* class com.huawei.dmsdp.devicevirtualization.DeviceTypeConverter.AnonymousClass1 */

        {
            put(3, DeviceType.DEVICE_TYPE_TV);
            put(5, DeviceType.DEVICE_TYPE_CAMERA);
            put(6, DeviceType.DEVICE_TYPE_VOICEBOX);
            put(9, DeviceType.DEVICE_TYPE_HIWEAR);
        }
    };

    public static String convertDeviceType(int dmsdpret) {
        if (deviceTypeCovertMap.containsKey(Integer.valueOf(dmsdpret))) {
            return deviceTypeCovertMap.get(Integer.valueOf(dmsdpret));
        }
        return DeviceType.DEVICE_TYPE_UNKNOWN;
    }
}
