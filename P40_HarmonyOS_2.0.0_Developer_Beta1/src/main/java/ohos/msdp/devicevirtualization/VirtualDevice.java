package ohos.msdp.devicevirtualization;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ohos.dmsdp.sdk.DMSDPConfig;
import ohos.dmsdp.sdk.DMSDPDevice;
import ohos.dmsdp.sdk.DMSDPDeviceService;
import ohos.dmsdp.sdk.HwLog;
import ohos.dmsdp.sdk.util.camera.VirtualCameraCharacteristics;

public class VirtualDevice {
    private static final int COLLECTION_SIZE = 8;
    private static final String DVSTR = "cameraid.";
    private static final int SECONDS_OF_ONE_MINUTE = 60;
    private static final int SINGLE_SERVICE_AMOUNT = 1;
    private static final String TAG = "VirtualDevice";
    private static final String UTF8_CHARSET = "UTF-8";
    private EnumSet<Capability> capabilities;
    private String deviceId;
    private String deviceName;
    private String deviceType;

    public VirtualDevice() {
    }

    VirtualDevice(DMSDPDevice dMSDPDevice) {
        this.deviceId = dMSDPDevice.getDeviceId();
        this.deviceType = DeviceTypeConverter.convertDeviceType(dMSDPDevice.getDeviceType());
        this.deviceName = dMSDPDevice.getDeviceName();
        this.capabilities = EnumSet.noneOf(Capability.class);
        if ((dMSDPDevice.getProperties(5001) instanceof Boolean) && ((Boolean) dMSDPDevice.getProperties(5001)).booleanValue()) {
            this.capabilities.add(Capability.CAMERA);
        }
        if ((dMSDPDevice.getProperties(5002) instanceof Boolean) && ((Boolean) dMSDPDevice.getProperties(5002)).booleanValue()) {
            this.capabilities.add(Capability.MIC);
        }
        if ((dMSDPDevice.getProperties(5003) instanceof Boolean) && ((Boolean) dMSDPDevice.getProperties(5003)).booleanValue()) {
            this.capabilities.add(Capability.SPEAKER);
        }
        if ((dMSDPDevice.getProperties(5004) instanceof Boolean) && ((Boolean) dMSDPDevice.getProperties(5004)).booleanValue()) {
            this.capabilities.add(Capability.DISPLAY);
        }
    }

    public String getDeviceId() {
        return this.deviceId;
    }

    public void setDeviceId(String str) {
        this.deviceId = str;
    }

    public String getDeviceType() {
        return this.deviceType;
    }

    public String getDeviceName() {
        return this.deviceName;
    }

    public EnumSet<Capability> getDeviceCapability() {
        return this.capabilities;
    }

    public int setData(String str, Map<String, Object> map) {
        if (map == null || str == null) {
            HwLog.e(TAG, "setData failed for params or configName is null");
            return -2;
        } else if (str.equals(Constants.CAMERA_CONFIG)) {
            return dealCameraConfig(map);
        } else {
            HwLog.e(TAG, "failed for configName invalid.");
            return -2;
        }
    }

    private int dealCameraConfig(Map<String, Object> map) {
        Context context = VirtualDeviceManager.getInstance().getContext();
        if (context == null) {
            HwLog.e(TAG, "setData failed for context is null");
            return -1;
        }
        CameraManager cameraManager = (CameraManager) context.getSystemService("camera");
        if (cameraManager == null) {
            HwLog.e(TAG, "setData failed for manager is null");
            return -1;
        } else if (!map.containsKey(Constants.CAMERA_POSITION)) {
            HwLog.e(TAG, "setData failed for no CAMERA_POSITION.");
            return -1;
        } else {
            List<String> virtualCameraId = VirtualDeviceManager.getInstance().getVirtualCameraId();
            DMSDPDeviceService dmsdpDeviceService = VirtualDeviceManager.getInstance().getDmsdpDeviceService();
            Map<String, Object> transformCameraParams = transformCameraParams(map);
            if (virtualCameraId.size() == 1) {
                return VirtualDeviceManager.getInstance().updateDeviceService(dmsdpDeviceService, EventType.EVENT_DEVICE_CAPABILITY_BUSY, transformCameraParams);
            }
            if (!(map.get(Constants.CAMERA_POSITION) instanceof String)) {
                HwLog.e(TAG, "setData failed for position can't cast to string.");
                return -1;
            }
            String data = getData((String) map.get(Constants.CAMERA_POSITION));
            if (data.isEmpty()) {
                HwLog.e(TAG, "setData failed for cameraId is empty.");
                return -1;
            }
            try {
                byte[] bArr = (byte[]) cameraManager.getCameraCharacteristics(data).get(VirtualCameraCharacteristics.HUAWEI_VIRTUAL_CAMERA_SERVICE_ID);
                if (bArr == null) {
                    HwLog.e(TAG, "serId is null.");
                    return -1;
                }
                return VirtualDeviceManager.getInstance().updateDeviceService(new DMSDPDeviceService(dmsdpDeviceService.getDeviceId(), (String) null, new String(bArr, UTF8_CHARSET), 1), EventType.EVENT_DEVICE_CAPABILITY_BUSY, transformCameraParams);
            } catch (CameraAccessException unused) {
                HwLog.e(TAG, "failed for CameraAccessException.");
                return -1;
            } catch (UnsupportedEncodingException unused2) {
                HwLog.e(TAG, "Not supported UTF-8 encoding exception, id: " + data);
                return -1;
            }
        }
    }

    private Map<String, Object> transformCameraParams(Map<String, Object> map) {
        HashMap hashMap = new HashMap(8);
        if (map.containsKey(Constants.AUTO_ORIENTATION)) {
            if (map.get(Constants.AUTO_ORIENTATION).equals(Constants.AUTO_ORIENTATION_ENABLE)) {
                hashMap.put("VIRCAM_AUTO_ORIENTATION", true);
                hashMap.put("VIRCAM_TYPE", DMSDPConfig.VirCameraType.REGISTER_CAMERA.toString());
            } else if (map.get(Constants.AUTO_ORIENTATION).equals(Constants.AUTO_ORIENTATION_DISABLE)) {
                hashMap.put("VIRCAM_AUTO_ORIENTATION", false);
                hashMap.put("VIRCAM_TYPE", DMSDPConfig.VirCameraType.REGISTER_CAMERA.toString());
            } else {
                HwLog.e(TAG, "the value of AUTO_ORIENTATION is invalid");
            }
        }
        if (map.containsKey(Constants.DO_MIRROR)) {
            if (map.get(Constants.DO_MIRROR).equals(Constants.DO_MIRROR_ENABLE)) {
                hashMap.put("VIRCAM_DO_MIRROR", true);
                hashMap.put("VIRCAM_TYPE", DMSDPConfig.VirCameraType.REGISTER_CAMERA.toString());
            } else if (map.get(Constants.DO_MIRROR).equals(Constants.DO_MIRROR_DISABLE)) {
                hashMap.put("VIRCAM_DO_MIRROR", false);
                hashMap.put("VIRCAM_TYPE", DMSDPConfig.VirCameraType.REGISTER_CAMERA.toString());
            } else {
                HwLog.e(TAG, "the value for DO_MIRROR is invalid");
            }
        }
        return hashMap;
    }

    public String getData(String str) {
        if (str == null) {
            return "";
        }
        List<String> virtualCameraId = VirtualDeviceManager.getInstance().getVirtualCameraId();
        if (virtualCameraId.isEmpty()) {
            return "";
        }
        return getCameraIds(virtualCameraId, str);
    }

    private String convertMapToString(HashMap<String, List<String>> hashMap) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("{\"");
        for (Map.Entry<String, List<String>> entry : hashMap.entrySet()) {
            stringBuffer.append(entry.getKey());
            stringBuffer.append("\":[");
            for (String str : entry.getValue()) {
                stringBuffer.append("\"");
                stringBuffer.append(str);
                stringBuffer.append("\"");
                stringBuffer.append(",");
            }
            stringBuffer.deleteCharAt(stringBuffer.length() - 1);
            stringBuffer.append("]");
            stringBuffer.append(",\"");
        }
        stringBuffer.deleteCharAt(stringBuffer.length() - 1);
        stringBuffer.deleteCharAt(stringBuffer.length() - 1);
        stringBuffer.append("}");
        return stringBuffer.toString();
    }

    private String getCameraIds(List<String> list, String str) {
        Context context = VirtualDeviceManager.getInstance().getContext();
        if (context == null) {
            HwLog.w(TAG, "getData failed for context is null, return the first id");
            return list.get(0);
        }
        if (context.getSystemService("camera") instanceof CameraManager) {
            CameraManager cameraManager = (CameraManager) context.getSystemService("camera");
            if (cameraManager == null) {
                HwLog.w(TAG, "getData failed for manager is null, return the first id");
                return list.get(0);
            }
            HashMap<String, List<String>> camExtInfos = getCamExtInfos(list, cameraManager);
            if (camExtInfos.isEmpty()) {
                HwLog.w(TAG, "getData failed for manager cameraExtInfoMap null, return the first id");
                return list.get(0);
            } else if (str.equals(Constants.CAMERAID_ALL)) {
                return convertMapToString(camExtInfos);
            } else {
                HashMap<String, List<String>> hashMap = new HashMap<>(8);
                for (Map.Entry<String, List<String>> entry : camExtInfos.entrySet()) {
                    if (entry.getKey().equals(str)) {
                        if (entry.getValue().size() == 1) {
                            return entry.getValue().get(0);
                        }
                        hashMap.put(entry.getKey(), entry.getValue());
                        return convertMapToString(hashMap);
                    }
                }
            }
        }
        return list.get(0);
    }

    private String convertStr(String str) {
        String lowerCase = str.toLowerCase();
        return DVSTR + lowerCase.replace("_", ".");
    }

    private HashMap<String, List<String>> getCamExtInfos(List<String> list, CameraManager cameraManager) {
        HashMap<String, List<String>> hashMap = new HashMap<>();
        for (String str : list) {
            try {
                byte[] bArr = (byte[]) cameraManager.getCameraCharacteristics(str).get(VirtualCameraCharacteristics.HUAWEI_VIRTUAL_CAMERA_LOCATION);
                String str2 = Constants.CAMERAID_UNKNOWN;
                String str3 = bArr != null ? new String(bArr, UTF8_CHARSET) : str2;
                if (!str3.isEmpty()) {
                    str2 = str3;
                }
                ArrayList arrayList = new ArrayList();
                if (!hashMap.containsKey(convertStr(str2))) {
                    arrayList.add(str);
                    hashMap.put(convertStr(str2), arrayList);
                } else if (hashMap.get(convertStr(str2)) != null) {
                    hashMap.get(convertStr(str2)).add(str);
                }
            } catch (CameraAccessException unused) {
                HwLog.e(TAG, "failed for CameraAccessException.");
            } catch (UnsupportedEncodingException unused2) {
                HwLog.e(TAG, "failed for not supported UTF-8 encoding exception.");
            } catch (IllegalArgumentException unused3) {
                HwLog.e(TAG, "getCameraCharacteristics failed for IllegalArgumentException.");
            }
        }
        return hashMap;
    }
}
