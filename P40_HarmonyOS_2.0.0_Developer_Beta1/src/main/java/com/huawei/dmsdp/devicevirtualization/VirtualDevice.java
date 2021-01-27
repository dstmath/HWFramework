package com.huawei.dmsdp.devicevirtualization;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import com.huawei.android.hwpartdevicevirtualization.BuildConfig;
import com.huawei.dmsdpsdk2.DMSDPConfig;
import com.huawei.dmsdpsdk2.DMSDPDevice;
import com.huawei.dmsdpsdk2.DMSDPDeviceService;
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
    private static final int SECONDS_OF_ONE_MINUTE = 60;
    private static final int SINGLE_SERVICE_AMOUNT = 1;
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
        Object sensorProperties = dmsdpDevice.getProperties(DeviceParameterConst.DEVICE_WEAR_SUPPORT_SENSOR_BOOLEAN);
        if ((sensorProperties instanceof Boolean) && ((Boolean) sensorProperties).booleanValue()) {
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

    public int setData(String configName, Map<String, Object> params) {
        if (params == null || configName == null) {
            HwLog.e(TAG, "setData failed for params or configName is null");
            return -2;
        } else if (configName.equals(Constants.CAMERA_CONFIG)) {
            return dealCameraConfig(params);
        } else {
            if (configName.equals(Constants.TRANSPORT_CONFIG)) {
                return dealTransportConfig(params);
            }
            HwLog.e(TAG, "failed for configName invalid.");
            return -2;
        }
    }

    private int dealTransportConfig(Map<String, Object> params) {
        String value = (String) params.get(Constants.KEEP_CHANNEL_ACTIVE);
        if (value == null) {
            HwLog.e(TAG, "dealTransportConfig failed for no config");
            return -2;
        } else if (value.equals(Constants.KEEP_CHANNEL_ACTIVE_ONE_MINUTE)) {
            return VirtualDeviceManager.getInstance().keepChannelActive(getDeviceId(), SECONDS_OF_ONE_MINUTE);
        } else {
            HwLog.e(TAG, "dealTransportConfig failed for invalid duration time");
            return -2;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:27:0x009c, code lost:
        r3 = -1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x009f, code lost:
        com.huawei.dmsdpsdk2.HwLog.e(com.huawei.dmsdp.devicevirtualization.VirtualDevice.TAG, "Not supported UTF-8 encoding exception, id: " + r13);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x00b4, code lost:
        return -1;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x009e A[ExcHandler: UnsupportedEncodingException (e java.io.UnsupportedEncodingException), Splitter:B:20:0x0073] */
    private int dealCameraConfig(Map<String, Object> params) {
        Context context = VirtualDeviceManager.getInstance().getContext();
        int i = -1;
        if (context == null) {
            HwLog.e(TAG, "setData failed for context is null");
            return -1;
        }
        CameraManager manager = (CameraManager) context.getSystemService("camera");
        if (manager == null) {
            HwLog.e(TAG, "setData failed for manager is null");
            return -1;
        } else if (!params.containsKey(Constants.CAMERA_POSITION)) {
            HwLog.e(TAG, "setData failed for no CAMERA_POSITION.");
            return -1;
        } else {
            List<String> virtualCameraIdList = VirtualDeviceManager.getInstance().getVirtualCameraId();
            DMSDPDeviceService dmsdpDeviceService = VirtualDeviceManager.getInstance().getDmsdpDeviceService();
            Map<String, Object> cameraParams = transformCameraParams(params);
            if (virtualCameraIdList.size() == 1) {
                return VirtualDeviceManager.getInstance().updateDeviceService(dmsdpDeviceService, 207, cameraParams);
            }
            String cameraId = getData((String) params.get(Constants.CAMERA_POSITION));
            if (cameraId.isEmpty()) {
                HwLog.e(TAG, "setData failed for cameraId is empty.");
                return -1;
            }
            try {
                return VirtualDeviceManager.getInstance().updateDeviceService(new DMSDPDeviceService(dmsdpDeviceService.getDeviceId(), null, new String((byte[]) manager.getCameraCharacteristics(cameraId).get(VirtualCameraCharacteristics.HUAWEI_VIRTUAL_CAMERA_SERVICE_ID), UTF8_CHARSET), 1), 207, cameraParams);
            } catch (CameraAccessException e) {
                HwLog.e(TAG, "failed for CameraAccessException.");
                return i;
            } catch (UnsupportedEncodingException e2) {
            }
        }
    }

    private Map<String, Object> transformCameraParams(Map<String, Object> params) {
        Map<String, Object> cameraParams = new HashMap<>(0);
        if (params.containsKey(Constants.AUTO_ORIENTATION)) {
            if (params.get(Constants.AUTO_ORIENTATION).equals(Constants.AUTO_ORIENTATION_ENABLE)) {
                cameraParams.put(DMSDPConfig.UserConfig.USER_CFG_VIRCAM_AUTO_ORIENTATION, true);
                cameraParams.put(DMSDPConfig.UserConfig.USER_CFG_VIRCAM_TYPE, DMSDPConfig.VirCameraType.REGISTER_CAMERA.toString());
            } else if (params.get(Constants.AUTO_ORIENTATION).equals(Constants.AUTO_ORIENTATION_DISABLE)) {
                cameraParams.put(DMSDPConfig.UserConfig.USER_CFG_VIRCAM_AUTO_ORIENTATION, false);
                cameraParams.put(DMSDPConfig.UserConfig.USER_CFG_VIRCAM_TYPE, DMSDPConfig.VirCameraType.REGISTER_CAMERA.toString());
            } else {
                HwLog.e(TAG, "the value of AUTO_ORIENTATION is invalid");
            }
        }
        if (params.containsKey(Constants.DO_MIRROR)) {
            if (params.get(Constants.DO_MIRROR).equals(Constants.DO_MIRROR_ENABLE)) {
                cameraParams.put(DMSDPConfig.UserConfig.USER_CFG_VIRCAM_DO_MIRROR, true);
                cameraParams.put(DMSDPConfig.UserConfig.USER_CFG_VIRCAM_TYPE, DMSDPConfig.VirCameraType.REGISTER_CAMERA.toString());
            } else if (params.get(Constants.DO_MIRROR).equals(Constants.DO_MIRROR_DISABLE)) {
                cameraParams.put(DMSDPConfig.UserConfig.USER_CFG_VIRCAM_DO_MIRROR, false);
                cameraParams.put(DMSDPConfig.UserConfig.USER_CFG_VIRCAM_TYPE, DMSDPConfig.VirCameraType.REGISTER_CAMERA.toString());
            } else {
                HwLog.e(TAG, "the value for DO_MIRROR is invalid");
            }
        }
        return cameraParams;
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
                HwLog.e(TAG, "failed for CameraAccessException.");
            } catch (UnsupportedEncodingException e2) {
                HwLog.e(TAG, "failed for not supported UTF-8 encoding exception.");
            } catch (IllegalArgumentException e3) {
                HwLog.e(TAG, "getCameraCharacteristics failed for IllegalArgumentException.");
            }
        }
        return cameraExtInfoMap;
    }
}
