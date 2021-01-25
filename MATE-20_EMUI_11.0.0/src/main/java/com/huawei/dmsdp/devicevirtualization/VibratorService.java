package com.huawei.dmsdp.devicevirtualization;

import com.huawei.dmsdpsdk2.DMSDPAdapter;
import com.huawei.dmsdpsdk2.DMSDPDevice;
import com.huawei.dmsdpsdk2.DMSDPDeviceService;
import com.huawei.dmsdpsdk2.DMSDPListener;
import com.huawei.dmsdpsdk2.HwLog;
import com.huawei.dmsdpsdk2.vibrate.VirtualVibrator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VibratorService extends VirtualManager {
    private static final String TAG = "VirtualSensorService";
    private static final Object VIBRATORSER_LOCK = new Object();
    private static DMSDPListener mDmsdpListener = null;
    private DMSDPAdapter mDMSDPAdapter;

    private VibratorService() {
    }

    private static final class InstanceHolder {
        private static final VibratorService INSTANCE = new VibratorService();

        private InstanceHolder() {
        }
    }

    public static VibratorService getInstance() {
        return InstanceHolder.INSTANCE;
    }

    public List<VirtualVibrator> getVibrateList(String deviceId) {
        synchronized (VIBRATORSER_LOCK) {
            List<VirtualVibrator> vibrateList = new ArrayList<>();
            if (this.mDMSDPAdapter == null) {
                HwLog.e("VirtualSensorService", "getVibrateList mDMSDPAdapter is null");
                return vibrateList;
            } else if (deviceId == null) {
                HwLog.e("VirtualSensorService", "getVibrateList deviceId is null");
                return vibrateList;
            } else {
                List<VirtualVibrator> list = new ArrayList<>();
                int ret = this.mDMSDPAdapter.getVibrateList(deviceId, list);
                if (ret != 0) {
                    HwLog.e("VirtualSensorService", "getVibrateList get err is:" + ret);
                    return vibrateList;
                }
                for (VirtualVibrator vibrateTemp : list) {
                    vibrateList.add(new VirtualVibrator(vibrateTemp.getVibrateId(), vibrateTemp.getDeviceId()));
                }
                return vibrateList;
            }
        }
    }

    private static DMSDPListener getDmsdpListener() {
        synchronized (VIBRATORSER_LOCK) {
            if (mDmsdpListener != null) {
                return mDmsdpListener;
            }
            mDmsdpListener = new DMSDPListener() {
                /* class com.huawei.dmsdp.devicevirtualization.VibratorService.AnonymousClass1 */

                @Override // com.huawei.dmsdpsdk2.DMSDPListener
                public void onDeviceChange(DMSDPDevice dmsdpDevice, int event, Map<String, Object> map) {
                }

                @Override // com.huawei.dmsdpsdk2.DMSDPListener
                public void onDeviceServiceChange(DMSDPDeviceService dmsdpDeviceService, int event, Map<String, Object> info) {
                    HwLog.i("VirtualSensorService", "onDeviceServiceChange event:" + event);
                    VibratorService.onServiceChange(dmsdpDeviceService, event, info);
                }
            };
            return mDmsdpListener;
        }
    }

    /* access modifiers changed from: private */
    public static void onServiceChange(DMSDPDeviceService dmsdpDeviceService, int event, Map<String, Object> info) {
        if (dmsdpDeviceService != null) {
            HwLog.i("VirtualSensorService", "ServiceChange type:" + dmsdpDeviceService.getServiceType() + " event:" + Integer.toString(event));
        }
        deviceServiceChangeHandler(dmsdpDeviceService, event, info);
    }

    private static void deviceServiceChangeHandler(DMSDPDeviceService dmsdpDeviceService, int event, Map<String, Object> map) {
        if (dmsdpDeviceService == null) {
            HwLog.e("VirtualSensorService", "dmsdpDeviceService is null when deviceServiceChangeHandler");
        } else {
            if (dmsdpDeviceService.getServiceType() != 4096) {
            }
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.huawei.dmsdp.devicevirtualization.VirtualManager
    public void onConnect(VirtualService dmsdpService) {
        synchronized (VIBRATORSER_LOCK) {
            VirtualVibrator.onConnect(dmsdpService);
            if (dmsdpService != null) {
                this.mDMSDPAdapter = dmsdpService.getDMSDPAdapter();
                if (this.mDMSDPAdapter != null) {
                    this.mDMSDPAdapter.registerDMSDPListener(5, getDmsdpListener());
                } else {
                    HwLog.e("VirtualSensorService", "dmsdpAdapter is null when register dmsdpListener");
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.huawei.dmsdp.devicevirtualization.VirtualManager
    public void onDisConnect() {
        synchronized (VIBRATORSER_LOCK) {
            if (this.mDMSDPAdapter != null) {
                this.mDMSDPAdapter.unRegisterDMSDPListener(5, getDmsdpListener());
            }
            this.mDMSDPAdapter = null;
            VirtualVibrator.onDisConnect();
        }
    }
}
