package com.huawei.dmsdp.devicevirtualization;

import com.huawei.android.hwpartdevicevirtualization.BuildConfig;
import com.huawei.dmsdpsdk2.DMSDPDeviceService;
import com.huawei.dmsdpsdk2.DeviceParameterConst;
import com.huawei.dmsdpsdk2.HwLog;
import java.util.HashMap;
import java.util.Map;

public class VirtualCamera {
    private static final String TAG = "VirtualCamera";
    private Map<String, String> attributes = new HashMap(1);
    private String deviceId;
    private String name;

    public static VirtualCamera convertFromDMSDPDeviceService(DMSDPDeviceService service) {
        if (service.getServiceType() != 1) {
            HwLog.e(TAG, "not camera service");
            return null;
        }
        Map<String, String> attr = new HashMap<>(1);
        Object obj = service.getProperties(DeviceParameterConst.CAMERA_VIRTUAL_ID);
        if (obj == null || !(obj instanceof String)) {
            HwLog.e(TAG, "convertFromDMSDPDeviceService failed, camera id not found");
            return null;
        }
        attr.put(Constants.CAMERA_ID, (String) obj);
        return new VirtualCamera(service.getDeviceId(), service.getServiceId(), attr);
    }

    public VirtualCamera(String deviceId2, String name2, Map<String, String> attr) {
        this.deviceId = deviceId2;
        this.name = name2;
        this.attributes = attr;
    }

    public String getDeviceId() {
        return this.deviceId;
    }

    public String getName() {
        return this.name;
    }

    public String getCameraId() {
        if (this.attributes.containsKey(Constants.CAMERA_ID)) {
            return this.attributes.get(Constants.CAMERA_ID);
        }
        return BuildConfig.FLAVOR;
    }
}
