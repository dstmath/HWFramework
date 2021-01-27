package com.huawei.dmsdp.devicevirtualization;

import com.huawei.android.hwpartdevicevirtualization.BuildConfig;
import com.huawei.dmsdpsdk2.DMSDPDeviceService;
import com.huawei.dmsdpsdk2.DeviceParameterConst;
import com.huawei.dmsdpsdk2.HwLog;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

public class VirtualCamera {
    private static final String CAMERA_DESC_STR = "CAMERA_DESC";
    private static final String CAMERA_EXTEND_INFO_STR = "extendInfos";
    private static final String CAMERA_LOCATION_STR = "CAMERA_LOCATION";
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
        Object obj2 = service.getProperties(DeviceParameterConst.CAMERA_INFO_JSON_STRING);
        if (obj2 == null || !(obj2 instanceof String)) {
            HwLog.w(TAG, "get camera json string failed");
        } else {
            String cameraInfoJsonStr = (String) obj2;
            try {
                JSONObject extendInfos = new JSONObject(cameraInfoJsonStr).getJSONObject(CAMERA_EXTEND_INFO_STR);
                String location = extendInfos.getString(CAMERA_LOCATION_STR);
                if (!location.isEmpty()) {
                    attr.put(Constants.CAMERA_POSITION, location);
                }
                String desc = extendInfos.getString(CAMERA_DESC_STR);
                if (!desc.isEmpty()) {
                    attr.put(Constants.CAMERA_DESC, desc);
                }
                attr.put(Constants.CAMERA_CONFIG, cameraInfoJsonStr);
            } catch (JSONException e) {
                HwLog.w(TAG, "parse camera json string failed. " + e.getLocalizedMessage());
            }
        }
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

    public String getAttribute(String attributeName) {
        if (this.attributes.containsKey(attributeName)) {
            return this.attributes.get(attributeName);
        }
        return BuildConfig.FLAVOR;
    }
}
