package ohos.msdp.devicevirtualization;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

class DeviceTypeConverter {
    private static final Map<Integer, String> DEVICE_TYPE_COVERT_MAP;

    DeviceTypeConverter() {
    }

    static {
        HashMap hashMap = new HashMap();
        hashMap.put(3, DeviceType.DEVICE_TYPE_TV);
        hashMap.put(5, DeviceType.DEVICE_TYPE_CAMERA);
        hashMap.put(6, DeviceType.DEVICE_TYPE_VOICEBOX);
        hashMap.put(9, DeviceType.DEVICE_TYPE_HIWEAR);
        DEVICE_TYPE_COVERT_MAP = Collections.unmodifiableMap(hashMap);
    }

    public static String convertDeviceType(int i) {
        return DEVICE_TYPE_COVERT_MAP.containsKey(Integer.valueOf(i)) ? DEVICE_TYPE_COVERT_MAP.get(Integer.valueOf(i)) : DeviceType.DEVICE_TYPE_UNKNOWN;
    }
}
