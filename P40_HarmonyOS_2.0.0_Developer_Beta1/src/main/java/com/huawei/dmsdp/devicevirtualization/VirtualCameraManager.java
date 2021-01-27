package com.huawei.dmsdp.devicevirtualization;

import com.huawei.dmsdpsdk2.DMSDPAdapter;
import com.huawei.dmsdpsdk2.DMSDPConfig;
import com.huawei.dmsdpsdk2.DMSDPDevice;
import com.huawei.dmsdpsdk2.DMSDPDeviceService;
import com.huawei.dmsdpsdk2.DMSDPListener;
import com.huawei.dmsdpsdk2.HwLog;
import com.huawei.dmsdpsdk2.util.Util;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class VirtualCameraManager extends VirtualManager {
    private static final int SLEEP_TIME = 100;
    private static final int SLEEP_TIMEOVER = 15;
    private static final String TAG = "VirtualCameraManager";
    private Map<String, DMSDPDeviceService> allRunningCameraServices = new ConcurrentHashMap(0);
    private DMSDPListener dmsdpListener = new DMSDPListener() {
        /* class com.huawei.dmsdp.devicevirtualization.VirtualCameraManager.AnonymousClass1 */

        @Override // com.huawei.dmsdpsdk2.DMSDPListener
        public void onDeviceChange(DMSDPDevice device, int state, Map<String, Object> map) {
            if (device == null) {
                HwLog.e("VirtualCameraManager", "onDeviceChange device is null");
                return;
            }
            HwLog.e("VirtualCameraManager", "onDeviceChange device change state is " + state);
        }

        @Override // com.huawei.dmsdpsdk2.DMSDPListener
        public void onDeviceServiceChange(DMSDPDeviceService dmsdpDeviceService, int state, Map<String, Object> map) {
            if (dmsdpDeviceService == null) {
                HwLog.e("VirtualCameraManager", "deviceService device is null");
                return;
            }
            HwLog.i("VirtualCameraManager", String.format(Locale.ENGLISH, "onDeviceServiceChange service type is %d state is %d", Integer.valueOf(dmsdpDeviceService.getServiceType()), Integer.valueOf(state)));
            if (dmsdpDeviceService.getServiceType() == 1) {
                String key = dmsdpDeviceService.getDeviceId() + DMSDPConfig.SPLIT + dmsdpDeviceService.getServiceId();
                if (state == 204) {
                    VirtualCameraManager.this.allRunningCameraServices.put(key, dmsdpDeviceService);
                    VirtualCameraManager.this.checkCameraIdNum();
                } else if (state == 205) {
                    VirtualCameraManager.this.allRunningCameraServices.remove(key);
                    VirtualCameraManager.this.checkCameraIdNum();
                } else if (state == 206) {
                    VirtualCameraManager.this.allRunningCameraServices.remove(key);
                    VirtualCameraManager.this.checkCameraIdNum();
                } else {
                    HwLog.w("VirtualCameraManager", "state " + state + "no need to handle");
                }
            }
        }
    };
    private DMSDPAdapter mDMSDPAdapter;
    private boolean mIsCompleted = false;

    public static VirtualCameraManager getInstance() {
        return InstanceHolder.INSTANCE;
    }

    private static final class InstanceHolder {
        private static final VirtualCameraManager INSTANCE = new VirtualCameraManager();

        private InstanceHolder() {
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.huawei.dmsdp.devicevirtualization.VirtualManager
    public void onConnect(VirtualService service) {
        HwLog.i("VirtualCameraManager", " onConnect");
        this.mDMSDPAdapter = service.getDMSDPAdapter();
        this.mDMSDPAdapter.registerDMSDPListener(4, this.dmsdpListener);
    }

    /* access modifiers changed from: package-private */
    @Override // com.huawei.dmsdp.devicevirtualization.VirtualManager
    public void onDisConnect() {
        HwLog.i("VirtualCameraManager", " onDisConnect");
        DMSDPAdapter dMSDPAdapter = this.mDMSDPAdapter;
        if (dMSDPAdapter != null) {
            dMSDPAdapter.unRegisterDMSDPListener(4, this.dmsdpListener);
        }
        this.allRunningCameraServices.clear();
        this.mIsCompleted = false;
        this.mDMSDPAdapter = null;
    }

    public List<VirtualCamera> getVirtualCameraList(String deviceId) {
        List<VirtualCamera> virCameraList = new ArrayList<>(1);
        List<DMSDPDeviceService> services = new ArrayList<>(1);
        if (deviceId != null) {
            VirtualDeviceManager.getInstance().getDeviceCapabilityInfo(deviceId, EnumSet.of(Capability.CAMERA), services);
        } else {
            if (!checkCameraIdNum()) {
                int times = 0;
                while (true) {
                    if (this.mIsCompleted) {
                        break;
                    }
                    int times2 = times + 1;
                    if (times >= 15) {
                        HwLog.e("VirtualCameraManager", "waitingForGetCameraServices timeout!!");
                        break;
                    }
                    try {
                        Thread.sleep(100);
                        times = times2;
                    } catch (InterruptedException e) {
                        HwLog.e("VirtualCameraManager", "waitingForGetCameraServices error", e);
                    }
                }
            }
            if (!checkCameraIdNum()) {
                HwLog.e("VirtualCameraManager", "getVirtualCameraList failed");
                return virCameraList;
            }
            for (Map.Entry<String, DMSDPDeviceService> entry : this.allRunningCameraServices.entrySet()) {
                services.add(entry.getValue());
            }
        }
        for (DMSDPDeviceService service : services) {
            VirtualCamera virtualCamera = VirtualCamera.convertFromDMSDPDeviceService(service);
            if (virtualCamera != null) {
                virCameraList.add(virtualCamera);
            }
        }
        HwLog.i("VirtualCameraManager", "getVirtualCameraList:" + virCameraList);
        return virCameraList;
    }

    public int openVirtualCameraStream(String streamId, String cameraId, Map<String, Object> cameraParas, CameraDataCallback callback) {
        if (this.mDMSDPAdapter == null) {
            HwLog.e("VirtualCameraManager", "mDMSDPAdapter is null");
            return -1;
        }
        VirtualCamera camera = getVirtualCamera(cameraId);
        if (camera == null) {
            HwLog.e("VirtualCameraManager", "camera id is not valid");
            return -1;
        }
        HwLog.i("VirtualCameraManager", "openVirtualCameraStream, streamId: " + streamId + " cameraId:" + cameraId + " deviceId:" + Util.anonymizeData(camera.getDeviceId()) + " serviceId:" + Util.anonymizeData(camera.getName()));
        return this.mDMSDPAdapter.openVirtualCameraStream(streamId, camera.getDeviceId(), camera.getName(), cameraParas, callback);
    }

    public int closeVirtualCameraStream(String streamId, String cameraId) {
        if (this.mDMSDPAdapter == null) {
            HwLog.e("VirtualCameraManager", "mDMSDPAdapter is null");
            return -1;
        }
        VirtualCamera camera = getVirtualCamera(cameraId);
        if (camera == null) {
            HwLog.e("VirtualCameraManager", "camera id is not valid");
            return -1;
        }
        HwLog.i("VirtualCameraManager", "closeVirtualCameraStream, streamId: " + streamId + " cameraId:" + cameraId + " deviceId:" + Util.anonymizeData(camera.getDeviceId()) + " serviceId:" + Util.anonymizeData(camera.getName()));
        return this.mDMSDPAdapter.closeVirtualCameraStream(streamId, camera.getDeviceId(), camera.getName());
    }

    private VirtualCamera getVirtualCamera(String cameraId) {
        for (VirtualCamera cam : getVirtualCameraList(null)) {
            if (cam.getCameraId().equals(cameraId)) {
                return cam;
            }
        }
        return null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean checkCameraIdNum() {
        List<String> cameraIdList = new ArrayList<>();
        this.mDMSDPAdapter.getVirtualCameraList(4, cameraIdList);
        boolean result = cameraIdList.size() == this.allRunningCameraServices.size();
        if (!result) {
            HwLog.w("VirtualCameraManager", "virtual camera list size is not equal to camera services, wait for getting services. camera id list size:" + cameraIdList.size() + " running camera services size:" + this.allRunningCameraServices.size());
            this.mIsCompleted = false;
        } else {
            this.mIsCompleted = true;
        }
        return result;
    }
}
