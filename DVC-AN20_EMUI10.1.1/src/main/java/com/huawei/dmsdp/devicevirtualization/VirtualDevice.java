package com.huawei.dmsdp.devicevirtualization;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.util.Log;
import com.huawei.android.hwpartdevicevirtualization.BuildConfig;
import com.huawei.dmsdpsdk2.DMSDPDevice;
import com.huawei.dmsdpsdk2.DeviceParameterConst;
import com.huawei.dmsdpsdk2.HwLog;
import com.huawei.dmsdpsdk2.util.camera.VirtualCameraCharacteristics;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;

public class VirtualDevice {
    private static final String DVSTR = "android.cameraid.";
    private static final String TAG = "VirtualDevice";
    private static final String UTF8_CHARSET = "UTF-8";
    private EnumSet<Capability> capabilities = EnumSet.noneOf(Capability.class);
    private String deviceId;
    private String deviceName;
    private String deviceType;

    public VirtualDevice(DMSDPDevice dmsdpDevice) {
        this.deviceId = dmsdpDevice.getDeviceId();
        this.deviceType = DeviceTypeConverter.convertDeviceType(dmsdpDevice.getDeviceType());
        this.deviceName = dmsdpDevice.getDeviceName();
        if (dmsdpDevice.getProperties(DeviceParameterConst.DEVICE_SUPPORTCAMERA_BOOLEAN) != null && ((Boolean) dmsdpDevice.getProperties(DeviceParameterConst.DEVICE_SUPPORTCAMERA_BOOLEAN)).booleanValue()) {
            this.capabilities.add(Capability.CAMERA);
        }
        if (dmsdpDevice.getProperties(DeviceParameterConst.DEVICE_SUPPORTMIC_BOOLEAN) != null && ((Boolean) dmsdpDevice.getProperties(DeviceParameterConst.DEVICE_SUPPORTMIC_BOOLEAN)).booleanValue()) {
            this.capabilities.add(Capability.MIC);
        }
        if (dmsdpDevice.getProperties(DeviceParameterConst.DEVICE_SUPPORTSPEAKER_BOOLEAN) != null && ((Boolean) dmsdpDevice.getProperties(DeviceParameterConst.DEVICE_SUPPORTSPEAKER_BOOLEAN)).booleanValue()) {
            this.capabilities.add(Capability.SPEAKER);
        }
        if (dmsdpDevice.getProperties(DeviceParameterConst.DEVICE_SUPPORTDISPLAY_BOOLEAN) != null && ((Boolean) dmsdpDevice.getProperties(DeviceParameterConst.DEVICE_SUPPORTDISPLAY_BOOLEAN)).booleanValue()) {
            this.capabilities.add(Capability.DISPLAY);
        }
        if (dmsdpDevice.getProperties(DeviceParameterConst.DEVICE_WEAR_SUPPORT_VIBRATE_BOOLEAN) != null && ((Boolean) dmsdpDevice.getProperties(DeviceParameterConst.DEVICE_WEAR_SUPPORT_VIBRATE_BOOLEAN)).booleanValue()) {
            this.capabilities.add(Capability.VIBRATE);
        }
        if (dmsdpDevice.getProperties(DeviceParameterConst.DEVICE_WEAR_SUPPORT_PPG_BOOLEAN) != null && ((Boolean) dmsdpDevice.getProperties(DeviceParameterConst.DEVICE_WEAR_SUPPORT_PPG_BOOLEAN)).booleanValue()) {
            this.capabilities.add(Capability.SENSOR);
        }
        Object heartRateProperties = dmsdpDevice.getProperties(DeviceParameterConst.DEVICE_WEAR_SUPPORT_HEART_RATE_BOOLEAN);
        if ((heartRateProperties instanceof Boolean) && ((Boolean) heartRateProperties).booleanValue()) {
            this.capabilities.add(Capability.SENSOR);
        }
        Object notificationProperties = dmsdpDevice.getProperties(DeviceParameterConst.DEVICE_WEAR_SUPPORT_NOTIFICATION_BOOLEAN);
        if ((notificationProperties instanceof Boolean) && ((Boolean) notificationProperties).booleanValue()) {
            this.capabilities.add(Capability.NOTIFICATION);
        }
    }

    public String getDeviceId() {
        return this.deviceId;
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

    public String getData(String propName) {
        if (propName == null) {
            return BuildConfig.FLAVOR;
        }
        List<String> virtualCameraIdList = VirtualDeviceManager.getInstance().getVirtualCameraId();
        if (virtualCameraIdList.isEmpty()) {
            return BuildConfig.FLAVOR;
        }
        return getCameraIds(virtualCameraIdList, propName);
    }

    private String getCameraIds(List<String> cameraId, String propName) {
        Context context = VirtualDeviceManager.getInstance().getContext();
        if (context == null) {
            HwLog.w(TAG, "getData failed for context is null, return the first id");
            return cameraId.get(0);
        }
        if (context.getSystemService("camera") instanceof CameraManager) {
            CameraManager manager = (CameraManager) context.getSystemService("camera");
            if (manager == null) {
                HwLog.w(TAG, "getData failed for manager is null, return the first id");
                return cameraId.get(0);
            }
            HashMap<String, List<String>> cameraExtInfoMap = getCamExtInfos(cameraId, manager);
            if (cameraExtInfoMap.isEmpty()) {
                HwLog.w(TAG, "getData failed for manager cameraExtInfoMap null, return the first id");
                return cameraId.get(0);
            } else if (propName.equals(Constants.ANDROID_CAMERAID_ALL)) {
                return new JSONObject(cameraExtInfoMap).toString();
            } else {
                HashMap<String, List<String>> cameraExtInfo = new HashMap<>();
                for (Map.Entry<String, List<String>> next : cameraExtInfoMap.entrySet()) {
                    if (next.getKey().equals(propName)) {
                        if (next.getValue().size() == 1) {
                            return next.getValue().get(0);
                        }
                        cameraExtInfo.put(next.getKey(), next.getValue());
                        return new JSONObject(cameraExtInfo).toString();
                    }
                }
            }
        }
        return cameraId.get(0);
    }

    private String convertStr(String camLocation) {
        String camLoc = camLocation.toLowerCase();
        return DVSTR + camLoc.replace("_", ".");
    }

    private HashMap<String, List<String>> getCamExtInfos(List<String> cameraId, CameraManager manager) {
        HashMap<String, List<String>> cameraExtInfoMap = new HashMap<>();
        for (String camId : cameraId) {
            try {
                String camLocation = new String((byte[]) manager.getCameraCharacteristics(camId).get(VirtualCameraCharacteristics.HUAWEI_VIRTUAL_CAMERA_LOCATION), UTF8_CHARSET);
                if (camLocation.isEmpty()) {
                    camLocation = Constants.ANDROID_CAMERAID_UNKNOWN;
                }
                List<String> camIds = new ArrayList<>();
                if (!cameraExtInfoMap.containsKey(convertStr(camLocation))) {
                    camIds.add(camId);
                    cameraExtInfoMap.put(convertStr(camLocation), camIds);
                } else if (cameraExtInfoMap.get(convertStr(camLocation)) != null) {
                    cameraExtInfoMap.get(convertStr(camLocation)).add(camId);
                }
            } catch (CameraAccessException e) {
                Log.e(TAG, "failed for CameraAccessException.");
            } catch (UnsupportedEncodingException e2) {
                HwLog.e(TAG, "failed for not supported UTF-8 encoding exception.");
            } catch (IllegalArgumentException e3) {
                Log.e(TAG, "getCameraCharacteristics failed for IllegalArgumentException.");
            }
        }
        return cameraExtInfoMap;
    }
}
