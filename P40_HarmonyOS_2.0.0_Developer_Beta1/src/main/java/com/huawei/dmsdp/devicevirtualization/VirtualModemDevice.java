package com.huawei.dmsdp.devicevirtualization;

import com.huawei.dmsdpsdk2.DMSDPConfig;
import com.huawei.dmsdpsdk2.DMSDPDeviceService;
import com.huawei.dmsdpsdk2.HwLog;
import java.util.List;

/* access modifiers changed from: package-private */
public class VirtualModemDevice {
    private static final int INVALID_VALUE = -1;
    private static final String TAG = "VirtualModemDevice";

    private VirtualModemDevice() {
    }

    public static void eventHandle(ILocalVirtualDeviceObserver stateChangeCallback, DMSDPDeviceService service, int event) {
        if (stateChangeCallback == null || service == null) {
            HwLog.e(TAG, "stateChangeCallback or service is null");
            return;
        }
        switch (event) {
            case DMSDPConfig.EVENT_DEVICE_SERVICE_INVALID /* 220 */:
                stateChangeCallback.onDeviceStateChange(LocalVirtualDeviceAdapter.convertToVirtualDeviceInfo(VirtualDeviceType.MODEM, service, VirtualDeviceState.INVALID));
                return;
            case DMSDPConfig.EVENT_DEVICE_SERVICE_READY /* 221 */:
                stateChangeCallback.onDeviceStateChange(LocalVirtualDeviceAdapter.convertToVirtualDeviceInfo(VirtualDeviceType.MODEM, service, VirtualDeviceState.READY));
                return;
            case DMSDPConfig.EVENT_DEVICE_SERVICE_RUNNING /* 222 */:
                stateChangeCallback.onDeviceStateChange(LocalVirtualDeviceAdapter.convertToVirtualDeviceInfo(VirtualDeviceType.MODEM, service, VirtualDeviceState.RUNNING));
                return;
            case DMSDPConfig.EVENT_DEVICE_SERVICE_BUSY /* 223 */:
                stateChangeCallback.onDeviceStateChange(LocalVirtualDeviceAdapter.convertToVirtualDeviceInfo(VirtualDeviceType.MODEM, service, VirtualDeviceState.BUSY));
                return;
            default:
                return;
        }
    }

    public static int switchToRemote(String virtualDeviceId) {
        if (virtualDeviceId != null) {
            return LocalVirtualDeviceAdapter.switchToRemoteAdapter(VirtualDeviceType.MODEM, virtualDeviceId);
        }
        HwLog.e(TAG, "switchToRemote virtualDeviceId is null");
        return -1;
    }

    public static int switchToLocal(String virtualDeviceId) {
        if (virtualDeviceId != null) {
            return LocalVirtualDeviceAdapter.switchToLocalAdapter(VirtualDeviceType.MODEM, virtualDeviceId);
        }
        HwLog.e(TAG, "switchToLocal virtualDeviceId is null");
        return -1;
    }

    public static List<VirtualDeviceInfo> getModemStatus() {
        return LocalVirtualDeviceAdapter.getVirtualDeviceInfo(VirtualDeviceType.MODEM);
    }
}
